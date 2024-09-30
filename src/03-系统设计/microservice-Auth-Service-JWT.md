# Auth Service
This service grants authentication and authorization (Auth for short) to a user to allow her/him access to a resource provided by other systems.


基本流程

1. 用户/Client App 输入账号密码发送login 请求到 Authenticate Service.
2. Service 去 userdb 查询验证
3. 验证通过发送回 Access ID, Access Token, Refresh Token
    - Access Id 是包括但不限于 user_Id 的 identifier. 
    - Access Token 一般来说会在短时间内过期（30分钟）。
    - Access Token 有很多标准。比如最流行的JWT(json web token). JWT格式的 Access Token 就包含了 Access Id.
    - Access Token 过期了就会利用 Refresh Token 生成新的 Access Token. Refresh Token 有效期很长。
4. Access Token 会储存在 cookie or LocalStorage, not session storage. 
    - 一般是在Cookie里，因为每个http request都会带上cookie. 所以我们在 cookie 会比较方便。
5. 用户发送请求到其他的 microservice 的时候，带上 access token, 就可以用来验证用户的合法性。
    - 方法1，shared secret key
        - Symmetric Cryptography: Auth server 储存一个 private key, 其他server 也储存一样的 private key.
    - 方法2，public-private key pair （用这个）
        - Asymmetric Crypotography 不对称加密: Auth server signed JWT with a private key. Public key is distributed to other microservices. public key 可以通过api获取，也可以储存在configuration file里。其他的microservice 可以用 public key 结合 JWT 验证用户的signature是否有效。

简化的流程：
- 用户发request到一个 Auth Microservice，得到一个 Access Token 和 Refresh Token. 然后就可以去别的 micro-service 玩了。

重点：
- Q: JWT 里有什么？：
    - 我们这里只关心两部分，一是用户的数据，二是 Auth Server 通过 private key 和 用户数据算出的 signature. 
- Q: 为什么用JWT而不是session？：
    - 如果储存用户的 session 的话，session 需要储存在服务器，这样服务器就变成了stateful, 不是best-practice. 不太好 scale.
- Q: 为什么可以放心的把用户session相关的数据放在JWT里？：
    - 比如 RSA-SHA256 算法，是需要用一个 key 和 相应的 json 数据计算出一个 signature. 因此json数据被手动改变了，合规的signature就变了。其他的service会发现提供的signature和json数据对不上。
- Q: 为什么 Asymmetric Crypotography （private-public key pair）？：
    - 其他的 micro-service 只能用 public key 结合 json data 验证一个 jwt 的signature 是否有效。而不能重新生成一样的signature. 具体的算法原理我们就不关心了。
    - 如果 Private key 泄露了，整个系统的安全性就会被泄露。因此不要把 private key 发的到处都是的。采用 private-public key pair 是更为安全的做法。
- Q: 为什么用 access token + refresh token?
    - access token 是不能被 disable的，如果被盗就无人能敌。除非连夜更改 private public key pair.
    因此 access token 的有效时间往往很短。应用会重复地利用 refresh token 生成新的 access token.
    - refresh token 是指派给了特定的 user, 并且储存在用户的 cookie 和 Auth Server 的 cache layer 里，如 Redis。因此如果 refresh token 被盗，我们可以随时 disable 这个 refresh token. 稍微安全一点。



# Reference

1. https://veglos.github.io/posts/dotnet-auth-microservice-with-clean-architecture/
2. https://www.ruanyifeng.com/blog/2018/07/json_web_token-tutorial.html  写得一般
3. chatGPT


补充，如果加上 sign-up, reset password 之类的功能
这个 service 仍然可以叫 Auth Service, 也可以叫 Identity-Management Service 或者 User-Management-Servce
