# 297 Serialize-and-Deserialize-Binary-Tree

difficulty: Hard

<style>
        section pre{
          background-color: #eee;
          border: 1px solid #ddd;
          padding:10px;
          border-radius: 5px;
        }
      </style>
<section>
<div><p>Serialization is the process of converting a data structure or object into a sequence of bits so that it can be stored in a file or memory buffer, or transmitted across a network connection link to be reconstructed later in the same or another computer environment.</p>
<p>Design an algorithm to serialize and deserialize a binary tree. There is no restriction on how your serialization/deserialization algorithm should work. You just need to ensure that a binary tree can be serialized to a string and this string can be deserialized to the original tree structure.</p>
<p><strong>Example:&nbsp;</strong></p>
<pre>You may serialize the following tree:
    1
   / \
  2   3
     / \
    4   5
as <code>"[1,2,3,null,null,4,5]"</code>
</pre>
<p><strong>Clarification:</strong> The above format is the same as <a href="/faq/#binary-tree">how LeetCode serializes a binary tree</a>. You do not necessarily need to follow this format, so please be creative and come up with different approaches yourself.</p>
<p><strong>Note:&nbsp;</strong>Do not use class member/global/static variables to store states. Your serialize and deserialize algorithms should be stateless.</p>
</div></section>
 
 ## Method One 

这个题是 preorder 的方法，一定要把这个题给吃透了，太关键了。
还要做一个 leetcode 自己储存的方法
```java
/**
 * Definition for a binary tree node.
 * public class TreeNode {
 *     int val;
 *     TreeNode left;
 *     TreeNode right;
 *     TreeNode(int x) { val = x; }
 * }
 */
public class Codec {
    private String EMPTY = "null";
    private String DEL = ",";
    // Encodes a tree to a single string.
    public String serialize(TreeNode root) {
        StringBuilder sb = new StringBuilder();
        serializeHelper(sb, root);
        return sb.toString();
    }

    private void serializeHelper(StringBuilder sb, TreeNode node) {
        if (node == null) {
            sb.append(EMPTY).append(DEL);
            return;
        }
        sb.append(String.valueOf(node.val)).append(DEL);
        serializeHelper(sb, node.left);
        serializeHelper(sb, node.right);
    }

    // Decodes your encoded data to tree.
    public TreeNode deserialize(String data) {
        String[] strs = data.split(DEL);
        Queue<String> queue = new ArrayDeque<>();
        for (String str : strs) queue.offer(str);
        return deserializeHelepr(queue);
    }
    private TreeNode deserializeHelepr(Queue<String> queue) {
        if (queue.isEmpty()) return null;
        String cur = queue.poll();
        if (cur.equals(EMPTY)) return null;
        TreeNode node = new TreeNode(Integer.valueOf(cur));
        node.left = deserializeHelepr(queue);
        node.right = deserializeHelepr(queue);
        return node;
    }
}

// Your Codec object will be instantiated and called as such:
// Codec ser = new Codec();
// Codec deser = new Codec();
// TreeNode ans = deser.deserialize(ser.serialize(root));
```