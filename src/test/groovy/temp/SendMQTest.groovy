package temp

import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.MessageProperties

/**
 * User: andongxu
 * Time: 15-8-28 上午10:46 
 */
class SendMQTest {

    static void main(String [] args) {
        def factory = new ConnectionFactory()
        factory.uri = new URI('amqp://acq_v3:acq_v3@192.168.1.52:5672/%2facq_v3')
        def conn = factory.newConnection()
        def channel = conn.createChannel()
        def exchangeName = 'exchange.pos.ts.topic.tmp1'
        channel.exchangeDeclare(exchangeName, 'topic', true)

        for (int i = 0; i < 5000; i++) {
            Random ran = new Random()
            int id = ran.nextInt(999999)
            def message = """
                {
                    "data": {
                        "acqMerchantNo": "868515058130001",
                        "acqReferenceNo": "594172749859",
                        "acqTerminalBatchNo": "000001",
                        "acqTerminalNo": "02286143",
                        "acqTerminalTraceNo": "000029",
                        "amount": 1000,
                        "batchNo": "000001",
                        "cardNo": "6221881000039838750",
                        "cardbinId": 6,
                        "merchantNo": "Z08000000616234",
                        "pospNo": "01",
                        "referenceNo": "100081547466",
                        "respCode": "00",
                        "terminalNo": "30696662",
                        "traceNo": "000037",
                        "transDatetime": "2015-04-28 13:31:36",
                        "transId": ${id}
                    },
                    "transStatus": 1,
                    "transType": "sale",
                    "version": "1.0.0"
                }
                """
            channel.basicPublish(exchangeName, 'v1.trans.clearing', MessageProperties.PERSISTENT_TEXT_PLAIN, message.bytes)
            println "send:" + message
            Thread.sleep(20)
        }

        channel.close()
        conn.close()
    }
}
