# 085J. Maximal Rectangle

## Method 1 : Use 84

这个方法直接利用了[84题](leetCode-084-Largest-Rectangle-in-Histogram.md)的函数
除此之外就只是把每一行都看成直方图，建立m个直方图。
第一个就是
```java
class Solution {
    public int maximalRectangle(char[][] matrix) {
        if(matrix.length < 1) return 0;
        int m = matrix.length;
        int n = matrix[0].length;
        int ans = 0;
        for(int i = 0; i < m; i++){
            int[] heights = new int[n];
            for(int j = 0; j < n; j++){
                heights[j] = 0;
                int count = i;
                while(count >= 0){
                    if(matrix[count][j] == '1'){
                        heights[j] += 1;
                        count--;
                    }else{
                        break;
                    }
                }
            }
            ans = Math.max(ans,largestRectangleArea(heights));
        }
        return ans; 
    }
    
    public int largestRectangleArea(int[] heights) {
        Stack<Integer> stack = new Stack<>();
        int ans = 0;
        int p = 0; // pointer from 0 to end;
        while(p < heights.length){
            if(stack.isEmpty()){
                stack.push(p);
                p++;
            }else{
                int curTop = heights[stack.peek()];
                if(curTop <= heights[p]){
                    stack.push(p);
                    p++;
                }else{
                    int posCurr = stack.pop();
                    int posLeft = stack.isEmpty() ? -1 :stack.peek();
                    int posRight = p;
                    int height = heights[posCurr];
                    int width = posRight - posLeft - 1;
                    int area = height * width;
                    ans = Math.max(area,ans);
                }
            }
        }
        while(!stack.isEmpty()){
            int posCurr = stack.pop();
            int posLeft = stack.isEmpty() ? -1 :stack.peek();
            int posRight = p;
            int height = heights[posCurr];
            int width = posRight - posLeft - 1;
            int area = height * width;
            ans = Math.max(area,ans);         
        }
        return ans;
        
    }
}
```