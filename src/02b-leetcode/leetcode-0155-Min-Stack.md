# 155J. Min Stack

## Method 
比较简单，为什么呢？因为不涉及删除最小的节点。
只是需要的得到最小节点的话，我们改变一下node的定义，加一个min的值，
一直记录就好了。

```java
class MinStack {
    private Node first;
    private int size;
    class Node{
        int val;
        int min;
        Node next;
        public Node(){}
        public Node(int x, int min){
            this.val = x;
            this.min = min;
            next = null;
        }
    }
    /** initialize your data structure here. */
    public MinStack() {
        first = null;
        size = 0;
    }
    
    public void push(int x) {
        if(first == null){
            first = new Node(x,x);
        }else{
            Node oldFirst = first;
            first = new Node(x,Math.min(first.min,x));
            first.next = oldFirst;
        }
    }
    
    public void pop() {
        if(first!=null) first = first.next;
    }
    
    public int top() {
        if(first!=null){
            return first.val;
        }
        return -1;
    }
    
    public int getMin() {
        return first.min;
    }
}
```

2022 I prefer this solution now.

```java
class MinStack {
    // use an int[], first val, second min;
    private ArrayDeque<int[]> minStack;
    
    public MinStack() {
        this.minStack = new ArrayDeque<>();
    }
    
    public void push(int val) {
        if(minStack.size() == 0 || val <= getMin() ){
            minStack.addLast(new int[]{val, val});
        }else{
            minStack.addLast(new int[]{val, getMin()});
        }
    }
    
    public void pop() {
        minStack.removeLast();
    }
    
    public int top() {
        return minStack.peekLast()[0];
    }
    
    public int getMin() {
        return minStack.peekLast()[1];
    }
}

```