# Introduction

这是一个主要是中文的repo, 目的就是自己看，东西都是瞎写的。

## Main

* https://yangzichao.github.io/interviews-all-in-one/
* main branch 用来发布 mdbook, 别又push到master了
* mdbook-auto-summary.py  
使用了如上的python脚本自动生成 mdbook 所需的 SUMMARY.md 

常用的就如下语句：       

> python mdbook-auto-summary.py {absolute path} -o      

其中 -o 是overwrite

方便复制
> python mdbook-auto-summary.py ~/Desktop/interviews-all-in-one/src -o

* rename-lc-filenames.py
在 src/leetcode 下执行该python脚本会把所有的 leetcode-123 文档补成 leetcode-0123 等等。

## 简单 mdbook

gitbook 已经良心大大的坏了，旧的gitbook也和新版的node 大大的不兼容，是时候投奔mdbook了。

mdbook 非常的舒服，比gitbook 这个垃圾好多了。
只需要 
1. 安装rust 和 cargo 
2. 安装 mdbook 
3. 发布

参见[官方文档](https://rust-lang.github.io/mdBook/guide/installation.html)

现在唯一的问题是，没有我喜欢的自动生成SUMMARY的包，所以就用上面的python脚本对付一下。

#### 创建一个 mdbook

只需使用

> mdbook init my-first-book

就这么简单
然后使用如下两个之一就可以 build / serve 你的 mdbook 啦
> mdbook build   
> mdbook serve

除此之外记得 

> .gitignore  里加上 book 文件夹

#### 配置 book.toml
我喜欢加上这两个，search 是 enable 全文档搜索; fold 是支持导航折叠。
```toml
[output.html.search]
limit-results = 20

[output.html.fold]
enable = true
```

其实这个还可以自动执行一些脚本的。但是我觉得没啥必要。
先不研究rust了。
#### mdbook CI/CD with Github Actions
其实github自己就会跳出来让你选，非常的机智，不用你管。
参见[这个](https://github.com/rust-lang/mdBook/wiki/Automated-Deployment%3A-GitHub-Actions)