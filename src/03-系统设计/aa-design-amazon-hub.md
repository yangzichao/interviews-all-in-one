设计亚马逊快递柜 这个题真的很蛋疼
他强制要求一个 code 即使离线也能使用。这是 func requirement. 
但是 hub 本身是在线的，99% 也行。但是这个 code 必须，不是99.99% 是 100% 能用。

但是送货的人只是扫开一个柜子，扫一个条码，然后走了。
也可以送货的人手动用手机输入，把订单放到了某个柜子里。

这个题的点在于给用户发一个 code 
其实就是一个简单的 code generating service -> email send service. 
然后这个code 有 ttl. 用户拿 code 就能开快递柜的箱子。
其中最重要的细节是设计这个题目的 code.


# API 
non-trivial.
Delivery person post finished info. Providing the
hub_id, the locker number, tracking number.
其中 hub_id 和 tracking number 都不太需要自己输入。

# Data model
The code will be generated.


# Code design

仿照雪花算法
machine_id + locker_id + tracking_info + expire_date
然后 hash 到一个 8 digit code.
tracking_info 用来防止一日之内箱子被重复打开。可以扫二维码知道。
这个信息，离线了 client 也是知道的。而且server那边也是知道的。

如果延期了，这个 expire_date 可以重复生成。

如果这个locker 完全离线使用，那么 tracking_info 变成 num_of_total_used on that locker.

hash 防碰撞，存一个 secret dictionary, an array of keywords, 重复生成 code. 
server 那边也要维护一个 hashcode set, 为每个 machine_id 都判断是不是 collision 了。

这里 trade off 就是 碰撞的概率很低，我们算一下成本要不要碰撞。

