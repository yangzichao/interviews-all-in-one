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

gitbook 已经良心大大的坏了，旧的gitbook也和新版的node 大大的不兼容，是时候投奔mdbook了。

mdbook 非常的舒服，比gitbook 这个垃圾好多了。
只需要 1. 安装rust 和 cargo 2. 安装 mdbook 3.发布
参见[官方文档](https://rust-lang.github.io/mdBook/guide/installation.html)

现在唯一的问题是，没有我喜欢的自动生成SUMMARY的包，所以就用上面的python脚本对付一下。

#### mdbook CI/CD with Github Actions
其实github自己就会跳出来让你选，非常的机智，不用你管。
参见[这个](https://github.com/rust-lang/mdBook/wiki/Automated-Deployment%3A-GitHub-Actions)