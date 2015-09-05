package com.cnepay.pos.clearing.message

import org.springframework.stereotype.Component

/**
 * 消息队列
 * readyQueue：数据待处理的队列
 * processingQueue：数据正在处理中的队列
 * User: andongxu
 * Time: 15-8-29 上午9:55 
 */
class MessageQueue<E> {

    /**
     * 准备处理的队列
     */
    LinkedList<E> readyQueue = new LinkedList<E>()
    /**
     * 处理中的队列
     */
    LinkedList<E> processingQueue = new LinkedList<E>()

    /**
     * 添加指定元素到队列尾部
     * @param e
     * @return
     */
    boolean offer(E e) {
        synchronized (this) {
            if (readyQueue.contains(e) || processingQueue.contains(e)) {
                return false
            }
            return readyQueue.add(e)
        }
    }

    /**
     * 添加指定元素到队列顶部
     * @param e
     * @return
     */
    boolean offerFirst(E e) {
        synchronized (this) {
            if (readyQueue.contains(e) || processingQueue.contains(e)) {
                return false
            }
            return readyQueue.addFirst(e)
        }
    }

    /**
     * 添加指定元素到队列尾部
     * @param e
     * @return
     */
    boolean offerLast(E e) {
        synchronized (this) {
            if (readyQueue.contains(e) || processingQueue.contains(e)) {
                return false
            }
            return readyQueue.addLast(e);
        }
    }

    /**
     * 移除队列顶部元素
     * 将待处理队列的数据移除，移除后将数据存储于数据处理中队列，数据处理完成后需要主动从处理中队列删除
     * @return
     */
    E removeFirst() {
        E e
        synchronized (this) {
            e = readyQueue.removeFirst()
            processingQueue.offer(e)
        }
        return e
    }

    /**
     * 移除队列底部元素
     * 将待处理队列的数据移除，移除后将数据存储于数据处理中队列，数据处理完成后需要主动从处理中队列删除
     * @return
     */
    E removeLast() {
        E e
        synchronized (this) {
            e = readyQueue.removeLast()
            processingQueue.offer(e)
        }
        return e
    }

    /**
     * 是否空
     * @return
     */
    def isEmpty() {
        readyQueue.isEmpty()
    }

    /**
     * 彻底删除队列数据
     * @param e
     * @return
     */
    boolean delete(E e) {
        synchronized (this) {
            processingQueue.remove(e)
        }
    }

    public LinkedList<E> getReadyQueue() {
         return readyQueue
    }

    public LinkedList<E> getProcessingQueue() {
        return processingQueue
    }
}
