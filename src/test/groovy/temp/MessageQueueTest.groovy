package temp

import org.junit.Test

import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger

/**
 * User: andongxu
 * Time: 15-8-29 上午10:35 
 */
class MessageQueueTest {

    @Test
    void baseTest() {
        MessageQueue<String> queue = new MessageQueue<String>()
        CountDownLatch latch = new CountDownLatch(3)
        AtomicInteger count = new AtomicInteger(0)
        Thread th1 = new Thread(new Runnable() {
            @Override
            void run() {
                for (int i = 0; i < 300; i++) {
                    Random num = new Random()
                    int val = num.nextInt(500)
                    boolean flag = queue.offer(String.valueOf(val))
                    println Thread.currentThread().getName() + ",add:${val},flag:${flag}"
                    if (flag) {
                        count.getAndIncrement()
                    }
                    Thread.sleep(10)
                }
                latch.countDown()
            }
        })

        Thread th2 = new Thread(new Runnable() {
            @Override
            void run() {
                for (int i = 0; i < 300; i++) {
                    Random num = new Random()
                    int val = num.nextInt(500)
                    boolean flag = queue.offer(String.valueOf(val))
                    println Thread.currentThread().getName() + ",add:${val},flag:${flag}"
                    if (flag) {
                        count.getAndIncrement()
                    }
                    Thread.sleep(10)
                }
                latch.countDown()
            }
        })

        Thread th3 = new Thread(new Runnable() {
            @Override
            void run() {
                for (int i = 0; i < 300; i++) {
                    if (queue.getReadyQueue().size() > 0) {
                        queue.removeFirst()
                    }
                    Thread.sleep(10)
                }
                latch.countDown()
            }
        })

        th1.start()
        th2.start()
        th3.start()

        latch.await()

        println "-----------------------------------size:" + count.intValue()

        println "-----------------------------------size:" + queue.getReadyQueue().size()

        println "-----------------------------------size:" + queue.getProcessingQueue().size()

        for (String str : queue.getReadyQueue().sort()) {
            println str
        }

        println "-----------------------------------"

        for (String str : queue.getProcessingQueue().sort()) {
            println str
        }

    }

}
