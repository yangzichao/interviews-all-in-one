

这个题肯定是写过的，但是不知道为什么没有留下这个笔记


## 2023 年写的
比较直观 当然一些情况可以合并一下
```java
class MyCalendar {
    TreeMap<Integer, Integer> calendar;
    public MyCalendar() {
        this.calendar = new TreeMap<>();
    }
    
    public boolean book(int start, int end) {
        if (calendar.size() == 0) {
            calendar.put(start, end);
            return true;
        }
        Integer floorKey = calendar.floorKey(start);
        Integer ceilingKey = calendar.ceilingKey(start);
        if (floorKey == null) {
            if (end <= ceilingKey) {
                calendar.put(start, end);
                return true;
            }
            return false;
        }
        if (ceilingKey == null) {
            if (start >= calendar.get(floorKey)) {
                calendar.put(start, end);
                return true;
            }
            return false;
        }
        if (start >= calendar.get(floorKey) && end <= ceilingKey) {
            calendar.put(start, end);
            return true;
        }
        return false;
    }
}

/**
 * Your MyCalendar object will be instantiated and called as such:
 * MyCalendar obj = new MyCalendar();
 * boolean param_1 = obj.book(start,end);
 */
```