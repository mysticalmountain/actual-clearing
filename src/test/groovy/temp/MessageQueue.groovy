package temp

/**
 * User: andongxu
 * Time: 15-8-29 上午9:55 
 */
class MessageQueue<E> {


    static LinkedList<E> readyQueue = new LinkedList<E>()

    static LinkedList<E> processingQueue = new LinkedList<E>()

    /**
     * Adds the specified element as the tail (last element) of this list.
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

    boolean offerFirst(E e) {
        synchronized (this) {
            if (readyQueue.contains(e) || processingQueue.contains(e)) {
                return false
            }
            return readyQueue.addFirst(e)
        }
    }

    boolean offerLast(E e) {
        synchronized (this) {
            if (readyQueue.contains(e) || processingQueue.contains(e)) {
                return false
            }
            return readyQueue.addLast(e);
        }
    }

//    boolean remove(E e) {
//        synchronized (this) {
//            processingQueue.remove(e)
//        }
//    }

    E removeFirst() {
        E e
        synchronized (this) {
            e = readyQueue.removeFirst()
            processingQueue.offer(e)
        }
        return e
    }

    E removeLast() {
        E e
        synchronized (this) {
            e = readyQueue.removeLast()
            processingQueue.offer(e)
        }
        return e
    }

    public LinkedList<E> getReadyQueue() {
         return readyQueue
    }

    public LinkedList<E> getProcessingQueue() {
        return processingQueue
    }
}
