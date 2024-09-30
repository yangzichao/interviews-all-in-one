
常用的 Design Pattern

这个参考资料非常好 建议重点看这个
https://design-patterns.readthedocs.io/zh-cn/latest/index.html

## 基础的 Design Pattern
怎么写之前 先搞明白 有什么用 为什么用

Builder, Singleton, Factory


# Creational Pattern 创建型模式 

## Builder
What problem Builder solves?

1. Product instances are immutable
2. Objects that need other objects to construct them

注意 immutable 的object setter都是 private的。

Builder 对复杂的 construct an object 很有帮助 

Builder 可能有三个部分，一个是接口，一个是实现，还有一个是 director 帮助使用的人正确使用 builder 
比如我们可能写一个接口，然后有几个不同的Builder实现返回不同类型的 instance