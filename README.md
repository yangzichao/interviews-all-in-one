# Introduction

这是一个中文的repo, 主要自己看，都是瞎写的。

## ramdom

* pages 发布于 https://yangzichao.github.io/interviews-all-in-one/
* main branch 用来发布 gitbook, 别又写成master了
* gitbook-auto-summary.py 
使用了如上的包自动生成 SUMMARY, 虽然已经很多年了，但是还是好用的，文档链接[在此](https://github.com/mofhu/GitBook-auto-summary). 常用的就如下语句：
`
python gitbook-auto-summary.py {absolute path} -o
`     
-o 是overwrite 我觉得还好了 放这里方便复制
```shell
python gitbook-auto-summary.py ~/Desktop/interviews-all-in-one -o
```

## 简单 gitbook

安装gitbook, Mac ver
```
$ sudo npm install -g gitbook-cli
```
创建gitbook folder

```
$ mkdir sample-book
$ cd sample-book/
$ gitbook init
$ gitbook build
```

别的和 github 普通操作一样，先在github上创建好 repo

```
$ git init
$ cp -R _book/* .
$ git clean -fx _book
$ git add .
$ git commit -m "first commit"
$ git branch -M main
$ git remote add origin https://github.com/your-user-name/sample-gitbook.git
$ git push -u origin main
```