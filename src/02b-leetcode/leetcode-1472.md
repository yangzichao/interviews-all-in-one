


```java
class BrowserHistory {
    ArrayList<String> history;
    int curIndex;
    int maxIndex;
    public BrowserHistory(String homepage) {
        this.history = new ArrayList<>();
        history.add(homepage);
        curIndex = 0;
        maxIndex = 0;
    }
    
    public void visit(String url) {
        curIndex += 1;
        if (curIndex == history.size()) {
            history.add(url);
            maxIndex = curIndex;
            return;
        }
        maxIndex = curIndex;
        history.set(curIndex, url);
    }
    
    public String back(int steps) {
        curIndex -= steps;
        if (curIndex < 0) curIndex = 0;
        return history.get(curIndex);
    }
    
    public String forward(int steps) {
        curIndex += steps;
        if (curIndex >= maxIndex) curIndex = maxIndex;
        return history.get(curIndex);
    }
}
```
