
这个题最 straightforward 的思路肯定是比较愚蠢的，解法如下：

```java
class SnapshotArray {
    Map<Integer, int[]> snaps;
    int id;
    int[] arr;
    boolean hasChange;
    public SnapshotArray(int length) {
        this.id = -1;
        this.arr = new int[length];
        this.snaps = new HashMap<>();
        this.hasChange = true;
    }
    
    public void set(int index, int val) {
        arr[index] = val;
        hasChange = true;
    }
    
    public int snap() {
        id++;
        if (hasChange) {
            snaps.put(id, Arrays.copyOf(arr, arr.length));
        } else {
            snaps.put(id, snaps.get(id - 1));
        }
        hasChange = false;
        return id;
    }
    
    public int get(int index, int snap_id) {
        return snaps.get(snap_id)[index];
    }
}

/**
 * Your SnapshotArray object will be instantiated and called as such:
 * SnapshotArray obj = new SnapshotArray(length);
 * obj.set(index,val);
 * int param_2 = obj.snap();
 * int param_3 = obj.get(index,snap_id);
 */
```

应当反其道而行之，不是每次 snap 我们记录全部的数组，
而是每次change我们记录当下是什么 snap_id.
这样找 snap 的时候 我们就用 floorkey 去找对应的值就好了。



```java
class SnapshotArray {
    int snapId;
    TreeMap<Integer, Integer>[] arr;

    public SnapshotArray(int length) {
        this.arr = new TreeMap[length];
        this.snapId = 0;
        for (int i = 0; i < length; i++) {
            arr[i] = new TreeMap<>();
            arr[i].put(0, 0);
        }
    }
    
    public void set(int index, int val) {
        TreeMap<Integer, Integer> curIndexRecord = arr[index];
        curIndexRecord.put(snapId, val);
    }
    
    public int snap() {
        return snapId++;
    }
    
    public int get(int index, int snap_id) {
        TreeMap<Integer, Integer> curIndexRecord = arr[index];
        return curIndexRecord.floorEntry(snap_id).getValue();
    }
}
```