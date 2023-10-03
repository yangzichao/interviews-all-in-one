# Introduction

这是一个中文的repo, 主要自己看，都是瞎写的。

## ramdom

* pages 发布于 https://yangzichao.github.io/interviews-all-in-one/
* main branch 用来发布 gitbook, 别又写成master了
* md-auto-summary.py 
使用了如上的python脚本自动生成 SUMMARY.md 常用的就如下语句：
`
python mdbook-auto-summary.py {absolute path} -o
`     
-o 是overwrite 我觉得还好了 放这里方便复制
```shell
python mdbook-auto-summary.py ~/Desktop/interviews-all-in-one/src -o
```
* rename-lc-filenames.py
在 src/leetcode 下执行该python脚本会把所有的 leetcode-123 文档补成 leetcode-0123 等等。

## 简单 mdbook
