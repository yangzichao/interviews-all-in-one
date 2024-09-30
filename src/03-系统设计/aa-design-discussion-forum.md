# 

Similar question

Design a Reddit/Quora.


Note：这种多用户互动的，先考虑用户自己控制自己内容的 use case，然后考虑用户与用户互动的use case. 每个 use case 都会对应一个或者多个 service.

User 区分 public 和 registered, 考虑 role-based.


1. User can Sign up and Login 
2. User can CRUD its own post
3. USer can post comment under other's post
4. User can upvote/downvote other's post and comment
5. Feed of most popular posts. (Feed generation)

论坛的 
- Key Entities: 
    User, Post, Comments, Feed.
    关键的还有 Comments 的 结构 比如 Reddit 就是 tree structure, flat list 的就是 quora这种。 
    - Post contains Title, tag, Body (text and images)
- Key Operation:
    - Signup / login
    - create new post
    - Comment, comment as flat list
    - Delete post or comment
    - Upvote, downvote
    - Feed generation (ranking mechanism)

Non-func:

- Scalability, 用户多
    - Traffic Spikes to handle.
- Performance, 不需要 超低的low-latency, but high throughput
- Fault Tolerance / High Availability (不需要超high)
- AP over CP. Consistency 问题不大。
- Durability. 不能丢数据。或者说是数据的 reliability?


API:
0. 这是一个典型  web app, 所以我们用 REST API over HTTP.
1. 找Entities: User, Post, Comments, Images(Binary File), Votes
    记住找 Entity 就是找资源。REST API 就是合理归类资源，所以我们一定要注意这个顺序。
2. URI. 把资源放到 URI 下。
    - /users/
        - {user-id} 每个用户的资源
    - posts/
        - {post-id} 每个post的资源
    - post/{post-id}/comments
        - {comment-id} 每个评论的资源
    - posts/{post-id}/image
        - posts/{post-id}/image 每个图片的资源
    - post/{post-id}/vote
    - post/{post-id}/comments/{comment-id}/vote vote的资源

3. Define Resources' Representations
    Web app it is typical Json.
    Post: {
        post_id:
        title: 
        user_id:
        tags:[],
        comments: [{}],
        votes:,
        body: {}
    }
    注意这里 body 可以是 json 但是有 markdown format

4. Assign HTTP method:
- User
    - Sign up: POST /users/create, create new user
    - Login: POST /users/login, login existing user
        Login 可以考虑成一个 create Auth Token 操作 这个token以后可以接着用。
    Note: Login 可以考虑成一个 create Auth Token 操作 不然这种用户需要登陆的部分我们后面要解释很多次如何 Authenticate User.
    - 注意用POST 是因为我们不需要在 url 里明文传输用户名和密码 可以藏在 payload里面



API Pagination: GET /posts?limit=20&offset=0, offset=1
查查这个咋回事儿

前端会用这个实现 Infinite scrolling 

No SQL Collection 学习一个

画图：
我们先实现这个func requirement