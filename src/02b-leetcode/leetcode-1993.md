```java
class LockingTree {
    class Node {
        int val;
        int isLocked;
        Node parent;
        List<Node> children;
        Node (int val) {
            this.val = val;
            this.isLocked = 0;
            children = new ArrayList<>();
        }
    }

    Map<Integer, Node> nodeMap;


    public LockingTree(int[] parent) {
        this.nodeMap = new HashMap<>();
        nodeMap.put(-1, new Node(-1));
        for (int i = 0; i < parent.length; i++) {
            Node node = new Node(i);
            nodeMap.put(i, node);
        }
        for (int i = 0; i < parent.length; i++) {
            Node node = nodeMap.get(i);
            Node parentNode = nodeMap.get(parent[i]);
            node.parent = parentNode;
            parentNode.children.add(node);
        }
    }
    
    public boolean lock(int num, int user) {
        Node node = nodeMap.get(num);
        if (node == null || node.isLocked > 0) return false;
        node.isLocked = user;
        return true;
    }
    
    public boolean unlock(int num, int user) {
        Node node = nodeMap.get(num);
        if (node == null || node.isLocked != user) return false;
        node.isLocked = 0;
        return true;
    }
    
    public boolean upgrade(int num, int user) {
        // check node, exist,
        // check node is unlocked
        // check ancestor no lock
        // check at least a locked child/grand child
        Node node = nodeMap.get(num);
        if (node == null || node.isLocked > 0) return false;
        if (!isAncestorNotLocked(node)) return false;
        List<Node> lockedDescendant = new ArrayList<>();
        dfs(node, lockedDescendant);
        if (lockedDescendant.size() == 0) return false;

        for (Node locked : lockedDescendant) {
            locked.isLocked = 0;
        }

        lock(num, user);
        return true;

    }

    private boolean isAncestorNotLocked(Node node) {
        if (node == null) return true;
        if (node.isLocked > 0) return false;
        return isAncestorNotLocked(node.parent);
    }

    private void dfs(Node node, List<Node> lockedDescendant) {
        if (node == null) return;
        for (Node child : node.children) {
            if (child.isLocked > 0) {
                lockedDescendant.add(child);
            }
            dfs(child, lockedDescendant);
        }
    }
}

/**
 * Your LockingTree object will be instantiated and called as such:
 * LockingTree obj = new LockingTree(parent);
 * boolean param_1 = obj.lock(num,user);
 * boolean param_2 = obj.unlock(num,user);
 * boolean param_3 = obj.upgrade(num,user);
 */
```