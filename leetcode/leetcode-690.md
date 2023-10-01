
这是一个没啥难度的题哦。
```java
class Solution {
    public int getImportance(List<Employee> employees, int id) {
        Map<Integer, Employee> idToEmployeeMap = new HashMap<>();
        for(Employee employee : employees){
            idToEmployeeMap.put(employee.id, employee);
        }
        int totalImportance = dfs(id, idToEmployeeMap);
        return totalImportance;
    }
    private int dfs(int id, Map<Integer, Employee> idToEmployeeMap){
        Employee curEmployee = idToEmployeeMap.get(id);
        int curSum = curEmployee.importance;
        for(int subId : curEmployee.subordinates){
            curSum += dfs(subId, idToEmployeeMap);
        }
        return curSum;
    }
}
```