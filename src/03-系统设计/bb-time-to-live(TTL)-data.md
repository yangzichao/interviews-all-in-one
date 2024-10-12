这一类data,
This is commonly used for caching, logging, and session management.

data model 如下：
- sssion_id PK
- created_at timestamp
- expires_at timestasmp 
- id 相关的信息 用于定位
    - user_id
    - order_id
    - tracking_numer
- 其他内容信息
    - security code 
