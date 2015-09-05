package temp

import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.Envelope
import com.rabbitmq.client.MessageProperties
import com.rabbitmq.client.QueueingConsumer

/**
 * User: andongxu
 * Time: 15-8-28 上午11:02 
 */
class ReceiveMQTest {

    static void main(String [] args) {
        def factory = new ConnectionFactory()
        factory.uri = new URI('amqp://acq_v3:acq_v3@192.168.1.52:5672/%2facq_v3')
        def conn = factory.newConnection()
        def channel = conn.createChannel()
        def exchangeName = 'exchange.pos.ts.topic.tmp1'
        def queueName = 'queue.pos.clearing.v1'
        channel.queueDeclare(queueName, true, false, false, null)
        channel.queueBind(queueName, exchangeName, 'v1.trans.clearing')
//        int prefetchCount = 1;
//        channel.basicQos(prefetchCount);
        def consumer = new QueueingConsumer(channel)
        boolean ack = false ; //打开应答机制
        channel.basicConsume(queueName, ack, consumer);
        while (true) {
            def delivery = consumer.nextDelivery()
            println "Receive : " + new String(delivery.body)
            Thread.sleep(1000)
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        }

        channel.close()
        conn.close()
    }
}
