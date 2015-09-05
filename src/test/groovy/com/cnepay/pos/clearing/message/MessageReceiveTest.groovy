package com.cnepay.pos.clearing.message

import com.cnepay.pos.clearing.ApplicationTest
import com.cnepay.pos.clearing.util.Dao
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.MessageProperties
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value

/**
 * User: andongxu
 * Time: 15-8-26 下午6:00 
 */
class MessageReceiveTest {

    def mqUrl = "amqp://acq_v3:acq_v3@192.168.1.52:5672/%2facq_v3"
    def exchangeName = "exchange.pos.ts.topic.tmp1"


    /**
     * 消费状态-成功
     */
    @Test
    void saleStatusSuccessTest() {
        def factory = new ConnectionFactory()
        factory.uri = new URI(mqUrl)
        def conn = factory.newConnection()
        def channel = conn.createChannel()
        channel.exchangeDeclare(exchangeName, 'topic', true)
        def message = """
            {
                "data": {
                    "acqMerchantNo": "105290054510838",
                    "acqReferenceNo": "594172749859",
                    "acqTerminalBatchNo": "000001",
                    "acqTerminalNo": "02286143",
                    "acqTerminalTraceNo": "000029",
                    "amount": 1,
                    "batchNo": "000001",
                    "cardNo": "6221881000039838750",
                    "cardbinId": 790,
                    "merchantNo": "102002856992115",
                    "pospNo": "01",
                    "referenceNo": "100081547466",
                    "respCode": "00",
                    "terminalNo": "30696662",
                    "traceNo": "000037",
                    "transDatetime": "2015-04-28 13:31:36",
                    "transId": 79019845
                },
                "transStatus": 1,
                "transType": "sale",
                "version": "1.0.0"
            }
            """
        channel.basicPublish(exchangeName, 'v1.trans.clearing', MessageProperties.PERSISTENT_TEXT_PLAIN, message.bytes)
        println "send:" + message

        channel.close()
        conn.close()
    }

    /**
     * 消费撤销-成功
     */
    @Test
    void reversalSaleStatusSuccessTest () {
        def factory = new ConnectionFactory()
        factory.uri = new URI(mqUrl)
        def conn = factory.newConnection()
        def channel = conn.createChannel()
        channel.exchangeDeclare(exchangeName, 'topic', true)
        def message = """
            {
                "data": {
                    "acqMerchantNo": "105290054510838",
                    "acqReferenceNo": "594172749859",
                    "acqTerminalBatchNo": "000001",
                    "acqTerminalNo": "02286143",
                    "acqTerminalTraceNo": "000029",
                    "amount": 1,
                    "batchNo": "000001",
                    "cardNo": "6221881000039838750",
                    "cardbinId": 790,
                    "merchantNo": "102002856992115",
                    "pospNo": "01",
                    "referenceNo": "100081547466",
                    "respCode": "00",
                    "terminalNo": "30696662",
                    "traceNo": "000037",
                    "transDatetime": "2015-04-28 13:31:36",
                    "transId": 79020121
                },
                "transStatus": 1,
                "transType": "reversal_sale",
                "version": "1.0.0"
            }
            """
        channel.basicPublish(exchangeName, 'v1.trans.clearing', MessageProperties.PERSISTENT_TEXT_PLAIN, message.bytes)
        println "send:" + message

        channel.close()
        conn.close()
    }

    /**
     * 消费撤销冲正-成功
     */
    @Test
    void reversal2SaleStatusSuccessTest() {
        def factory = new ConnectionFactory()
        factory.uri = new URI(mqUrl)
        def conn = factory.newConnection()
        def channel = conn.createChannel()
        channel.exchangeDeclare(exchangeName, 'topic', true)
        def message = """
            {
                "data": {
                    "acqMerchantNo": "105290054510838",
                    "acqReferenceNo": "594172749859",
                    "acqTerminalBatchNo": "000001",
                    "acqTerminalNo": "02286143",
                    "acqTerminalTraceNo": "000029",
                    "amount": 1,
                    "batchNo": "000001",
                    "cardNo": "6221881000039838750",
                    "cardbinId": 790,
                    "merchantNo": "102002856992115",
                    "pospNo": "01",
                    "referenceNo": "100081547466",
                    "respCode": "00",
                    "terminalNo": "30696662",
                    "traceNo": "000037",
                    "transDatetime": "2015-04-28 13:31:36",
                    "transId": 79020121
                },
                "transStatus": 1,
                "transType": "reversal_sale_void",
                "version": "1.0.0"
            }
            """
        channel.basicPublish(exchangeName, 'v1.trans.clearing', MessageProperties.PERSISTENT_TEXT_PLAIN, message.bytes)
        println "send:" + message

        channel.close()
        conn.close()
    }

    @Test
    void UPOPSaleStatusSuccessTest() {
        def factory = new ConnectionFactory()
        factory.uri = new URI(mqUrl)
        def conn = factory.newConnection()
        def channel = conn.createChannel()
        channel.exchangeDeclare(exchangeName, 'topic', true)
        def message = """
            {
                "data": {
                    "acqMerchantNo": "105290054510838",
                    "acqReferenceNo": "594172749859",
                    "acqTerminalBatchNo": "000001",
                    "acqTerminalNo": "02286143",
                    "acqTerminalTraceNo": "000029",
                    "amount": 1,
                    "batchNo": "000001",
                    "cardNo": "6221881000039838750",
                    "cardbinId": 790,
                    "merchantNo": "102002856992115",
                    "pospNo": "01",
                    "referenceNo": "100081547466",
                    "respCode": "00",
                    "terminalNo": "30696662",
                    "traceNo": "000037",
                    "transDatetime": "2015-04-28 13:31:36",
                    "transId": 79019950
                },
                "transStatus": 1,
                "transType": "sale",
                "version": "1.0.0"
            }
            """
        channel.basicPublish(exchangeName, 'v1.trans.clearing', MessageProperties.PERSISTENT_TEXT_PLAIN, message.bytes)
        println "send:" + message

        channel.close()
        conn.close()
    }

    @Test
    void refundSuccessTest() {
        def factory = new ConnectionFactory()
        factory.uri = new URI(mqUrl)
        def conn = factory.newConnection()
        def channel = conn.createChannel()
        channel.exchangeDeclare(exchangeName, 'topic', true)
        def message = """
            {
                "data": {
                    "acqMerchantNo": "105290054510838",
                    "acqReferenceNo": "594172749859",
                    "acqTerminalBatchNo": "000001",
                    "acqTerminalNo": "02286143",
                    "acqTerminalTraceNo": "000029",
                    "amount": 1,
                    "batchNo": "000001",
                    "cardNo": "6221881000039838750",
                    "cardbinId": 790,
                    "merchantNo": "102002856992115",
                    "pospNo": "01",
                    "referenceNo": "100081547466",
                    "respCode": "00",
                    "terminalNo": "30696662",
                    "traceNo": "000037",
                    "transDatetime": "2015-04-28 13:31:36",
                    "transId": 79019941
                },
                "transStatus": 1,
                "transType": "refund",
                "version": "1.0.0"
            }
            """
        channel.basicPublish(exchangeName, 'v1.trans.clearing', MessageProperties.PERSISTENT_TEXT_PLAIN, message.bytes)
        println "send:" + message

        channel.close()
        conn.close()
    }

}
