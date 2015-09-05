package com.cnepay.pos.clearing.message

import com.cnepay.pos.clearing.util.Constants
import groovy.json.JsonSlurper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Component

import java.util.concurrent.ExecutorService

/**
 * 处理内存队列中消息
 * User: andongxu
 * Time: 15-8-29 下午12:48 
 */
@Component
class MessageProcess implements ApplicationListener<ContextRefreshedEvent> {

    Logger log = LoggerFactory.getLogger(MessageProcess)

    @Autowired
    ExecutorService saleExecutorService
    @Autowired
    ExecutorService reverseExecutorService
    @Autowired
    ApplicationContext context
    @Autowired
    MessageQueue<JsonSlurper> saleMessageQueue
    @Autowired
    MessageQueue<JsonSlurper> reverseMessageQueue

    @Value('${thread.sleep.time}')
    def threadSleepTime


    @Override
    void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("启动处理队列消息开始......")
        saleMessageProcess()
        reverseMessageProcess()
        log.info("启动处理队列消息完成......")
    }

    /**
     * 消费消息处理
     */
    void saleMessageProcess() {
        Thread saleThread = new Thread(new Runnable() {
            @Override
            void run() {
                while (true) {
                    try {
                        if (!saleMessageQueue.isEmpty()) {
                            def obj = saleMessageQueue.removeFirst()
                            try {
                                handlerMessage(obj)
                            } catch (Exception e1) {
                                log.error("处理消息发生异常：" + e1.getMessage())
                            } finally {
                                saleMessageQueue.delete(obj)
                            }
                        } else {
                            Thread.sleep(Integer.valueOf(threadSleepTime))
                        }
                    } catch (Exception) {
                        log.error("处理消息发生异常：" + e1.getMessage())
                    }
                }
            }
        })
        saleThread.start()
    }

    /**
     * 反向交易消息处理
     */
    void reverseMessageProcess() {
        Thread reverseThread = new Thread(new Runnable() {
            @Override
            void run() {
                while (true) {
                    try {
                        if (!reverseMessageQueue.isEmpty()) {
                            def msg = reverseMessageQueue.removeFirst()
                            try {
                                handlerMessage(msg)
                            } catch (Exception e1) {
                                log.error("处理消息发生异常：" + e1.getMessage())
                            } finally {
                                reverseMessageQueue.delete(msg)
                            }
                        } else {
                            Thread.sleep(Integer.valueOf(threadSleepTime))
                        }
                    } catch (Exception e) {
                        log.error("处理消息发生异常：" + e.getMessage())
                    }
                }
            }
        })
        reverseThread.start()
    }

    /**
     * 处理消息
     * @param msg
     */
    void handlerMessage(def msg) {
        if (!Constants.TRANS_STATUS_SUCCESS.equals(msg.transStatus as String)) {           //交易状态非成功不做后续处理
            log.info("交易状态非成功状态，交易ID:${msg.transId},交易状态:${msg.transStatus},交易类型:${msg.transType},通道商户号:${msg.acqMerchantNo},商户号:${msg.merchantNo}")
            return
        }
        if (Constants.TRANS_TYPE_SALE.equals(msg.transType)                           //消费
                || Constants.TRANS_TYPE_AUTH_COMP.equals(msg.transType)) {           //预授权完成
            def saleHandler = context.getBean(SaleMessageHandler)
            saleHandler.setMsg(msg)
            saleExecutorService.submit(saleHandler)
        } else if (Constants.TRANS_TYPE_SALE_VOID.equals(msg.transType)             //消费撤销
                || Constants.TRANS_TYPE_REVERSAL_SALE.equals(msg.transType)) {      //消费冲正
            def reverseHandler = context.getBean(ReverseMessageHandler)
            reverseHandler.execute(msg)
        } else if (Constants.TRANS_TYPE_REVERSAL_SALE_VOID.equals(msg.transType)              //消费撤销冲正
                || Constants.TRANS_TYPE_REVERSAL_AUTH_COMP_CANCEL.equals(msg.transType)) {    //预授权完成撤销冲正
            def reverse2Handler = context.getBean(Reverse2MessageHandler)
            reverse2Handler.execute(msg)
        } else if (Constants.TRANS_TYPE_REFUND.equals(msg.transType)) {             //退货
            def refundHandler = context.getBean(RefundMessageHandler)
            refundHandler.execute(msg)
        } else {
            log.error("不支持的交易类型,交易ID:${msg.transId},交易类型:${msg.transType},通道商户号:${msg.acqMerchantNo},商户号:${msg.merchantNo}")
            return
        }
    }
}
