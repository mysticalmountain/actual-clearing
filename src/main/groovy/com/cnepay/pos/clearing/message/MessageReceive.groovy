package com.cnepay.pos.clearing.message

import com.cnepay.pos.clearing.util.Constants
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.QueueingConsumer
import com.rabbitmq.client.ShutdownSignalException
import groovy.json.JsonSlurper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Component

/**
 * 接收MQ消息
 * User: andongxu
 * Time: 15-8-21 下午11:19 
 */
@Component
class MessageReceive {

    Logger log = LoggerFactory.getLogger(MessageReceive)

    @Autowired
    JsonSlurper jsonSlurper
    @Autowired
    MessageQueue<JsonSlurper> saleMessageQueue
    @Autowired
    MessageQueue<JsonSlurper> reverseMessageQueue

    @Value('${mq.url}')
    def mqUrl
    @Value('${mq.exchangeName}')
    def exchangeName
    @Value('${mq.queueName}')
    def queueName
    @Value('${mq.queueDurable}')
    def queueDurable
    @Value('${mq.routeKey}')
    def routeKey
    @Value('${mq.receiveSleepTime}')
    def receiveSleepTime
    @Value('${mq.autoAck}')
    def autoAck

    @Value('${sale.memoryQqueue.maxSize}')
    def saleMemoryQueueMaxSize
    @Value('${sale.memoryQqueue.minSize}')
    def saleMemoryQueueMinSize

    /**
     * 接收MQ消息
     */
    public void recive() {
        log.info("启动接收MQ消息开始......")
        log.info("MQ URL;${mqUrl}")
        log.info("MQ exchangeName:${exchangeName}")
        log.info("MQ queueName:${queueName}")
        log.info("MQ queueDurable:${queueDurable}")
        log.info("MQ routeKey:${routeKey}")
        def channel = newChannel()
        def consumer = new QueueingConsumer(channel)
        channel.basicConsume(queueName, Boolean.valueOf(autoAck), consumer);
        def receiveMessageThread = new Thread(new Runnable() {
            @Override
            void run() {
                while (true) {
                    try {
                        if (saleMessageQueue.getReadyQueue().size() < (saleMemoryQueueMaxSize as int)) {
                            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                            String message = new String(delivery.getBody());
                            log.info("接收消息:${message}")
                            def targetMsg = messageConvert(message)
                            saveMessage(targetMsg)
                            log.info("保存消息至队列:${targetMsg.transId}")
                            if (Boolean.valueOf(autoAck)) {
                                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                                log.info("应答MQ:${targetMsg.transId}")
                            }
                        }
                        if ((saleMemoryQueueMaxSize as int) == saleMessageQueue.getReadyQueue().size()) {
                            Thread.sleep(Integer.valueOf(receiveSleepTime))
                        }
                    } catch (Exception e) {
                        if (e instanceof ShutdownSignalException) {                 //MQ链接中断
                            try {
                                log.error("MQ链接中断......")
                                log.info("自动新建MQ链接开始......")
                                channel = newChannel()
                                consumer = new QueueingConsumer(channel)
                                channel.basicConsume(queueName, Boolean.valueOf(autoAck), consumer);
                                log.info("自动新建MQ链接完成......")
                            } catch (Exception e2) {}
                        }else{
                            log.error("接收消息发生发生异常：" + e.getMessage())
                        }
                    }
                }
                channel.close()
            }
        })
        receiveMessageThread.start()
        log.info("启动接收MQ消息完成......")
    }

    /**
     * 保存消息至队列
     * @param msg
     */
    def saveMessage(msg) {
        if (!Constants.TRANS_STATUS_SUCCESS.equals(msg.transStatus as String)) {           //交易状态非成功不做后续处理
            log.info("交易状态非成功状态，交易ID:${msg.transId},交易状态:${msg.transStatus},交易类型:${msg.transType},通道商户号:${msg.acqMerchantNo},商户号:${msg.merchantNo}")
            return
        }
        if (Constants.TRANS_TYPE_SALE.equals(msg.transType)                                        //消费
                || Constants.TRANS_TYPE_AUTH_COMP.equals(msg.transType)) {                        //预授权完成
            saleMessageQueue.offer(msg)
        } else if (Constants.TRANS_TYPE_SALE_VOID.equals(msg.transType)                          //消费撤销
                || Constants.TRANS_TYPE_REVERSAL_SALE.equals(msg.transType)                      //消费冲正
                || Constants.TRANS_TYPE_REVERSAL_SALE_VOID.equals(msg.transType)                //消费撤销冲正
                || Constants.TRANS_TYPE_REVERSAL_AUTH_COMP.equals(msg.transType)                //预授权完成冲正
                || Constants.TRANS_TYPE_AUTH_COMP_CANCEL.equals(msg.transType)                  //预授权完成撤销
                || Constants.TRANS_TYPE_REVERSAL_AUTH_COMP_CANCEL.equals(msg.transType)) {    //预授权完成撤销冲正
            reverseMessageQueue.offer(msg)
        } else if (Constants.TRANS_TYPE_REFUND.equals(msg.transType)) {                         //退货
            reverseMessageQueue.offer(msg)
        } else {
            log.error("不支持的交易类型,交易ID:${msg.transId},交易类型:${msg.transType},通道商户号:${msg.acqMerchantNo},商户号:${msg.merchantNo}")
            return
        }
    }

    def messageConvert(def message) {
        def msgObj = jsonSlurper.parseText(message)
        def targetMsg = [:]
        targetMsg.transId = msgObj.data.transId == null ? "" : msgObj.data.transId
        targetMsg.transType =  msgObj.transType == null ? "" : msgObj.transType  as String
        targetMsg.transStatus = msgObj.transStatus == null ? "" : msgObj.transStatus  as String
        targetMsg.amount = msgObj.data.amount == null ? "" : msgObj.data.amount  as String
        targetMsg.transDatetime = msgObj.data.transDatetime == null ? "" : msgObj.data.transDatetime as String
        targetMsg.acqMerchantNo = msgObj.data.acqMerchantNo == null ? "" : msgObj.data.acqMerchantNo as String
        targetMsg.merchantNo = msgObj.data.merchantNo == null ? "" : msgObj.data.merchantNo as String
        return targetMsg
    }


    def newChannel() {
        def factory = new ConnectionFactory()
        factory.uri = new URI(mqUrl)
        def conn = factory.newConnection()
        def channel = conn.createChannel()
        channel.queueDeclare(queueName, queueDurable as boolean, false, false, null)
        channel.queueBind(queueName, exchangeName, routeKey)
        def consumer = new QueueingConsumer(channel)
        channel.basicConsume(queueName, Boolean.valueOf(autoAck), consumer);
        return channel
    }
}
