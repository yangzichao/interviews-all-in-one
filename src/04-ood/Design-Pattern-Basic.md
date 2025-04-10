
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


Builder 类 现在已经被类似于 lombok 之类的包给代替了，所以别写builder，会显得很不专业。


## Adapter Pattern 适配器模式

Adapter 模式看起来是比较普通的操作，因为在很多地方这个叫做 wrapper.
Wrapper 的思想是比较通用的，我们可以在java 之外的很多地方也用到这个 idea. 
我们经常不自觉的就用到 adapter 类似的结构了，比如

```java
String[] exist = new String[] {"Good", "morning", "Bob", "and", "Alice"};
Set<String> set = new HashSet<>(Arrays.asList(exist));
```

Adapter 的原理经常体现了面向抽象编程这一原则的威力：持有高层接口不但代码更灵活，而且把各种接口组合起来也更容易。
比如你传入参数的时候，使用更高层的接口，比如List, 而不是 ArrayList 或者  LinkedList 这样你就可以传入很多List的实现。

## Bridge Pattern 桥接模式
桥接模式就是为了避免直接继承带来的子类爆炸。

比如说我们车分 大中小 3个尺寸划分，然后另一个分类就是品牌 比如50个品牌，然后分油车电车混动 3种。这样直接就有了450种组合产生的爆炸数量的子类。
桥接模式就是为了避免子类爆炸。

桥接模式实现比较复杂，实际应用也非常少，但它提供的设计思想值得借鉴，即不要过度使用继承，而是优先拆分某些部件，使用组合的方式来扩展功能。



## Proxy Pattern 代理模式
Proxy 是一个比

