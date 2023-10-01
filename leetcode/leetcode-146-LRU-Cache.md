# 146J. LRU Cache

https://leetcode.com/problems/lru-cache/

<pre>
LRU Cache 是一道很高频的题目，非常重要。
它本身的意义就是为了学习 LinkedHashMap/OrderedDict
这个数据结构。而这个数据结构的存在最典型的应用就是LRU Cache，
所以真的是非常重要。

LRU的意思是Least Recently Used Cache.
相关的题目还有 460 LFU Cache Least Frequently Used Cache
这是介绍各种Cache的Wikipedia。
https://en.wikipedia.org/wiki/Cache_replacement_policies#LRU
可以暂时不太学习 LinkedHashMap在Java中是怎么实现的，但是此题
的解决方案实际上是差不多的。
以下是两个中文讲解LinkedHashMap的文章
https://juejin.im/post/5a4b433b6fb9a0451705916f
https://blog.csdn.net/justloveyou_/article/details/71713781
</pre>

## Method 1 Double LinkedList + HashMap

<pre>
如果不考虑时间复杂度O(1)的要求，仅仅实现LRU，
思路是这样的，先创建一个DoubleLinkedList，因此我们需要
定义DoubleLinkedList，和传统的不同的是，我们需要给每个Node两个值，
一个Key, 一个Val. 定不定义Constructor不重要。

我们看这个双向链表是怎么实现LRU的呢？
我们先建立一个dummyHead和一个dummyTail。由于是双向链表，所以不可能只
创建一个dummyHead。如果新加入了节点，就加在头部之后。那么最靠近尾部的
就是Least Recent Used的Node。
因此我们首先需要写一个函数addNode，用来在dummyHead之后加节点。
然后如果一个已经存在的节点重新被使用了怎么办呢？我们应该找到这个节点
并把它移动到头部之后。因此我们需要写两个空返回类型函数，一个是removeNode,
用来删掉该节点。一个是moveToHead来实现这个过程，其实现方式是，先
用removeHead删掉它，再用addNode把它加到dummyHead之后。
所以针对双向链表我们需要实现三个空返回类型的函数：
addNode, removeNode, moveToHead. 这三个都是对链表做一些修改的函数。

如果我们加入了新的本来不存在的值，导致我们的双向链表现在有了N+1个Node，
这就意味着我们的Cache需要删掉一些东西，即删掉LRU。
那么我们就直接删掉最靠近尾部的Node即可. 需要一个函数popTail.
为什么是PopTail并返回该节点？
因为为了实现O(1)的时间复杂度从链表删去节点的同时，还要照顾我们的HashMap。

为了实现O(1),自然而然的会想到使用HashMap。我们用HashMap的Key直接取到
双向链表的对应的Node，自然可以从Node中读取Val。这就可以实现get的O(1).
当然，get这件事本身就相当于用了一次对应Node，还要用moveToHead
把这个Node给提到最前面去。

那么put如何O(1)呢？其实put本来就是O(1)的，这是链表实现保证的。只需要处理
一下添加了Node之后的一些情况。这部分是最为平庸的。

因此我们的LRU除了双向链表之外，
类变量还需要有size,capacity,链表头尾，和一个HashMap。
</pre>

以下的代码基本上就是上面叙述的部分了。

```java
class LRUCache {
    // Doublely Linked List
    // Define Doubly Linked List
    class DLListNode{
        public int key; // for LRU
        // Standard
        public int val;
        public DLListNode next;
        public DLListNode prev;
    }
    public void addNode(DLListNode node){
        node.prev = head;
        node.next = head.next;
        head.next.prev = node;
        head.next = node;
    }
    public void remove(DLListNode node){
        node.next.prev = node.prev;
        node.prev.next = node.next;
        // 为什么不必要以下两句呢？因为没必要让node被回收，
        // remove只在moveToHead中使用，一会儿还要addnode呢。
        // 当然加上以下两句也不会报错
        // node.next = null;
        // node.prev = null;
    }
    public void moveToHead(DLListNode node){
        remove(node);
        addNode(node);
    }

    //为何popTail要返回节点？因为还要从HashMap里删掉该节点的信息
    public DLListNode popTail(){
        DLListNode poped = tail.prev;
        remove(poped);
        return poped;
    }

    // For LRU Cache
    private int size;
    private int capacity;
    private DLListNode head;
    private DLListNode tail;
    private Map<Integer, DLListNode> cache;

    public LRUCache(int capacity) {
        this.size = 0;
        this.capacity = capacity;
        head = new DLListNode();
        tail = new DLListNode();
        head.next = tail;
        tail.prev = head;
        cache = new HashMap<Integer, DLListNode>();
    }

    public int get(int key) {
        DLListNode node = cache.get(key);
        if(node == null) return -1;
        moveToHead(node);
        return node.val;
    }

    public void put(int key, int value) {
        DLListNode node = cache.get(key);
        if(node!=null){
            node.val = value;
            moveToHead(node);
        } else{
            node = new DLListNode();
            node.key = key;
            node.val = value;
            addNode(node);
            size++;
            cache.put(key,node);
            if(size > capacity){
                DLListNode poped = popTail();
                cache.remove(poped.key);
                size--;
            }
        }
    }
}
```

以下是答案

```Java
public class LRUCache {

  class DLinkedNode {
    int key;
    int value;
    DLinkedNode prev;
    DLinkedNode next;
  }

  private void addNode(DLinkedNode node) {
    /**
     * Always add the new node right after head.
     */
    node.prev = head;
    node.next = head.next;

    head.next.prev = node;
    head.next = node;
  }

  private void removeNode(DLinkedNode node){
    /**
     * Remove an existing node from the linked list.
     */
    DLinkedNode prev = node.prev;
    DLinkedNode next = node.next;

    prev.next = next;
    next.prev = prev;
  }

  private void moveToHead(DLinkedNode node){
    /**
     * Move certain node in between to the head.
     */
    removeNode(node);
    addNode(node);
  }

  private DLinkedNode popTail() {
    /**
     * Pop the current tail.
     */
    DLinkedNode res = tail.prev;
    removeNode(res);
    return res;
  }

  private Map<Integer, DLinkedNode> cache = new HashMap<>();
  private int size;
  private int capacity;
  private DLinkedNode head, tail;

  public LRUCache(int capacity) {
    this.size = 0;
    this.capacity = capacity;

    head = new DLinkedNode();
    // head.prev = null;

    tail = new DLinkedNode();
    // tail.next = null;

    head.next = tail;
    tail.prev = head;
  }

  public int get(int key) {
    DLinkedNode node = cache.get(key);
    if (node == null) return -1;

    // move the accessed node to the head;
    moveToHead(node);

    return node.value;
  }

  public void put(int key, int value) {
    DLinkedNode node = cache.get(key);

    if(node == null) {
      DLinkedNode newNode = new DLinkedNode();
      newNode.key = key;
      newNode.value = value;

      cache.put(key, newNode);
      addNode(newNode);

      ++size;

      if(size > capacity) {
        // pop the tail
        DLinkedNode tail = popTail();
        cache.remove(tail.key);
        --size;
      }
    } else {
      // update the value.
      node.value = value;
      moveToHead(node);
    }
  }
}
```

这是我最新写的，感觉不错的。比上面的标准答案好多了。
对于 DLList 我们只实际上实现 addToHead 和 remove 两个方法就可以。

```java
class LRUCache {
    class Node {
        int key;
        int val;
        Node prev;
        Node next;
        public Node() {};
        public Node(int key, int val) {
            this.key = key;
            this.val = val;
        }
    }

    class DLList{
        Node head;
        Node tail;
        int size;
        public DLList(){
            this.head = new Node(-1, -1);
            this.tail = new Node(-1, -1);
            this.head.next = this.tail;
            this.tail.prev = this.head;
            this.size = 0;
        };

        public Node remove(Node node) {
            Node prev = node.prev;
            Node next = node.next;
            prev.next = next;
            next.prev = prev;
            this.size -= 1;
            return node;
        }

        public void add(Node node) {
            Node next = head.next;
            node.prev = head;
            node.next = next;
            head.next = node;
            next.prev = node;
            this.size += 1;
        }

        public Node getLast() {
            return this.tail.prev;
        }
    }

    private int capacity;
    private Map<Integer, Node> map;
    private DLList list;

    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.map = new HashMap<>();
        this.list = new DLList();
    }

    public int get(int key) {
        if( !map.containsKey(key) ) {
            return -1;
        }
        Node node = map.get(key);
        update(node);
        return node.val;
    }

    public void put(int key, int value) {
        if( map.containsKey(key) ) {
            map.get(key).val = value;
            update(map.get(key));
            return;
        }
        Node node = new Node(key, value);
        map.put(key, node);
        list.add(node);
        while(map.size() > capacity) {
            Node last = list.remove( list.getLast() );
            map.remove( last.key );
        }
    }

    public void update(Node node) {
        list.add( list.remove(node) );
    }
}

/**
 * Your LRUCache object will be instantiated and called as such:
 * LRUCache obj = new LRUCache(capacity);
 * int param_1 = obj.get(key);
 * obj.put(key,value);
 */
```
