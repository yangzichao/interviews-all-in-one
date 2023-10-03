这个题最有意思的地方在于初始化 head 和 tail 的位置。

```java
class MyCircularQueue {
    private int capacity;
    private int size;
    private int head;
    private int tail;
    private int[] buffer;

    public MyCircularQueue(int k) {
        this.capacity = k;
        this.size = 0;
        this.head = 0;
        this.tail = -1;
        this.buffer = new int[capacity];
    }

    public boolean enQueue(int value) {
        if (isFull()) return false;
        size++;
        tail = (tail + 1) % capacity;
        buffer[tail] = value;
        return true;
    }

    public boolean deQueue() {
        if (isEmpty()) return false;
        head = (head + 1) % capacity;
        size -= 1;
        return true;
    }

    public int Front() {
        if (isEmpty()) return -1;
        return buffer[head];
    }

    public int Rear() {
        if (isEmpty()) return -1;
        return buffer[tail];
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean isFull() {
        return size == capacity;
    }
}

/**
 * Your MyCircularQueue object will be instantiated and called as such:
 * MyCircularQueue obj = new MyCircularQueue(k);
 * boolean param_1 = obj.enQueue(value);
 * boolean param_2 = obj.deQueue();
 * int param_3 = obj.Front();
 * int param_4 = obj.Rear();
 * boolean param_5 = obj.isEmpty();
 * boolean param_6 = obj.isFull();
 */

```
