本次的项目实践是使用 SpringBoot+Mybatis 对电商项目中秒杀模块的实现. 目前阶段使用4台云服务器来做分布式集群，提高并发处理性能.

## 后端技术

- SpringBoot

- Mybatis

- Redis

## 使用到的第三方工具

- Mybatis-generator

- joda-time

- lombok

- guava

## 集群拓扑图

部署环境为4台服务器，一台作为nginx反向代理服务器，两台作为WebServer服务器，一台作为数据仓库, 拓扑图如下:

![pic](https://ae01.alicdn.com/kf/Hcb1e9d3507b840dd8efad801f4fe5d67r.png)

## 架构层次图
![pic](https://ae01.alicdn.com/kf/Ha41f99ff143a42099dfc4082cddfc8425.png)


### 接入层
- 接入层模型 ViewObject: 
    - View Object与前端对接的模型, 隐藏内部实现，供展示的聚合类型
- 业务层 Model: 
    - 领域模型，贫血+调用服务来提供输出能力
- DataObject 
    - 为领域模型的具体存储形式，一个领域模型由一个或多个DataObject组成，每一个DataObject对应一张表，以ORM方式操作数据库.

- 自定义异常类属性：
    - errCode: 业务逻辑错误码 ( 如：1开头的错误码为通用错误，2开头的错误码为用户相关错误，3开头的错误码为交易信息相关错误等.. )

    - errMsg: 错误提示

### 服务层

- 用户模块:
    - 注册(使用手机获取验证码短信来注册)
    - 登陆

- 交易模块:
    - 下单操作

- 商品模块:
    - 创建商品
    - 展示商品

- 促销模块
    - 维护促销商品列表

## 优化手段:

- ### 使用多级缓存提高查询性能.
    - Redis集中式缓存:
        - 单机配置 (本次使用)
        - sentinal哨兵模式 （了解学习
        - 集群cluster模式 （了解学习
    
    - 本地热点数据缓存
        - 特点:
            - 使用的是JVM的缓存来对热点数据进行存储；因为当数据库对缓存中的数据进行修改后会造成脏读，所以本地缓存的过期时间要设的尽可能的短. 
        - 第三方库:
            - Guava:
                - 提供了这样一种可控制大小和超时时间的线程安全的Map
                - 可配置lru策略（置换算法配置）
    - nginx配合lua脚本实现第三级缓存
        - nginx lua插载点
            - `init_by_lua`: 系统启动时调用
            - `init_workder_by_lua`: worker进程启动时调用
            - `set_by_lua`: nginx变量用复杂lua return
            - `rewrite_by_lua`: 重写url规则
            - `access_by_lua`: 权限验证阶段
            - `content_by_lua`: 内容输出节点
        - 使用Openresty:
            - 共享内存字典，所有worker进程均可见，LRU淘汰. 
                - 速度快，但受内存限制，且会脏读.
            - 支持Redis
                - 会多一次网络查询开销去访问数据库服务器上的redis，好处是能够保证缓存数据一致性. （往往这里的缓存是只读操作，可以访问redis的从库，从而减小对主库的压力。
            