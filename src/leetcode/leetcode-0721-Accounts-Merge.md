# 721 Accounts-Merge 
 
difficulty: Medium 
 
<style>
        section pre{
          background-color: #eee;
          border: 1px solid #ddd;
          padding:10px;
          border-radius: 5px;
        }
      </style>
<section>
<div><p>Given a list of <code>accounts</code> where each element <code>accounts[i]</code> is a list of strings, where the first element <code>accounts[i][0]</code> is a name, and the rest of the elements are <strong>emails</strong> representing emails of the account.</p>
<p>Now, we would like to merge these accounts. Two accounts definitely belong to the same person if there is some common email to both accounts. Note that even if two accounts have the same name, they may belong to different people as people could have the same name. A person can have any number of accounts initially, but all of their accounts definitely have the same name.</p>
<p>After merging the accounts, return the accounts in the following format: the first element of each account is the name, and the rest of the elements are emails <strong>in sorted order</strong>. The accounts themselves can be returned in <strong>any order</strong>.</p>
<p>&nbsp;</p>
<p><strong class="example">Example 1:</strong></p>
<pre><strong>Input:</strong> accounts = [["John","johnsmith@mail.com","john_newyork@mail.com"],["John","johnsmith@mail.com","john00@mail.com"],["Mary","mary@mail.com"],["John","johnnybravo@mail.com"]]
<strong>Output:</strong> [["John","john00@mail.com","john_newyork@mail.com","johnsmith@mail.com"],["Mary","mary@mail.com"],["John","johnnybravo@mail.com"]]
<strong>Explanation:</strong>
The first and second John's are the same person as they have the common email "johnsmith@mail.com".
The third John and Mary are different people as none of their email addresses are used by other accounts.
We could return these lists in any order, for example the answer [['Mary', 'mary@mail.com'], ['John', 'johnnybravo@mail.com'], 
['John', 'john00@mail.com', 'john_newyork@mail.com', 'johnsmith@mail.com']] would still be accepted.
</pre>
<p><strong class="example">Example 2:</strong></p>
<pre><strong>Input:</strong> accounts = [["Gabe","Gabe0@m.co","Gabe3@m.co","Gabe1@m.co"],["Kevin","Kevin3@m.co","Kevin5@m.co","Kevin0@m.co"],["Ethan","Ethan5@m.co","Ethan4@m.co","Ethan0@m.co"],["Hanzo","Hanzo3@m.co","Hanzo1@m.co","Hanzo0@m.co"],["Fern","Fern5@m.co","Fern1@m.co","Fern0@m.co"]]
<strong>Output:</strong> [["Ethan","Ethan0@m.co","Ethan4@m.co","Ethan5@m.co"],["Gabe","Gabe0@m.co","Gabe1@m.co","Gabe3@m.co"],["Hanzo","Hanzo0@m.co","Hanzo1@m.co","Hanzo3@m.co"],["Kevin","Kevin0@m.co","Kevin3@m.co","Kevin5@m.co"],["Fern","Fern0@m.co","Fern1@m.co","Fern5@m.co"]]
</pre>
<p>&nbsp;</p>
<p><strong>Constraints:</strong></p>
<ul>
	<li><code>1 &lt;= accounts.length &lt;= 1000</code></li>
	<li><code>2 &lt;= accounts[i].length &lt;= 10</code></li>
	<li><code>1 &lt;= accounts[i][j].length &lt;= 30</code></li>
	<li><code>accounts[i][0]</code> consists of English letters.</li>
	<li><code>accounts[i][j] (for j &gt; 0)</code> is a valid email.</li>
</ul>
</div></section>
 
 ## Method One 
 
``` Java
class Solution {
    public List<List<String>> accountsMerge(List<List<String>> accounts) {
        List<List<String>> ans = new ArrayList<>();
        if(accounts.size() == 0) return ans;
        // Build graph
        Map<String, String> names = new HashMap<>();
        Map<String, List<String>> graph = new HashMap<>();
        for(List<String> account : accounts){
            String name = account.get(0);
            for(int i = 1; i < account.size(); i++){
                for(int j = 1; j < account.size(); j++){
                    String emailA = account.get(i);
                    String emailB = account.get(j);
                    if(i == j) {
                        graph.putIfAbsent(emailA, new ArrayList<>());
                        names.put(emailA, name);
                        continue;
                    }
                    graph.putIfAbsent(emailA, new ArrayList<>());
                    graph.putIfAbsent(emailB, new ArrayList<>());
                    names.put(emailA, name);
                    names.put(emailB, name);
                    graph.get(emailA).add(emailB);
                    graph.get(emailB).add(emailA);
                }
            }
        }
        // Init DFS, idea is to store result for each island. 
        Set<String> marked = new HashSet<>();
        for(String key : graph.keySet()){
            if(marked.contains(key)) continue;
            
            // === begin dfs ====
            marked.add(key);
            ArrayDeque<String> stack = new ArrayDeque<>();
            stack.push(key);
            ArrayList<String> curPath = new ArrayList<>();
            while(!stack.isEmpty()){
                String cur = stack.pop();
                curPath.add(cur);
                for(String next : graph.get(cur)){
                    if(marked.contains(next)) continue;
                    marked.add(next);
                    stack.push(next);
                }
            }
            // === end dfs ====
            // and begin to organize ans;
            ArrayList<String> newAccount = new ArrayList<>();
            newAccount.add(names.get(key));
            Collections.sort(curPath);
            newAccount.addAll(curPath);
            ans.add(newAccount);
        }
        return ans;
    }
}
​
```