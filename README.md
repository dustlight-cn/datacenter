# DataCenter
[使用](#使用) | [部署](#部署) | [Schema 说明](SCHEMA.md)

Data service based on JSON Schema

## 使用

查看 [ API 文档](http://datacenter.dustlight.cn/doc.html)



## 部署
> 此服务依赖 Elasticsearch、MongoDB 以及 RabbitMQ。
### Helm 部署
选择此部署方式必须先安装 [Helm](https://helm.sh)。  
请查看 Helm 的 [文档](https://helm.sh/docs) 获取更多信息。

当 Helm 安装完毕后，使用下面命令添加仓库：

    helm repo add datacenter https://dustlight-cn.github.io/datacenter

若您已经添加仓库，执行命令 `helm repo update` 获取最新的包。
您可以通过命令 `helm search repo datacenter` 来查看他们的 charts。

创建配置文件 values.yaml：
```yaml
ingress:
  className: "nginx" # Ingress Class Name
  host: "datacenter.dustlight.cn" # 服务域名
  tls:
    enabled: false # 是否开启 HTTPS
    crt: "" 
    key: ""

config:
  elasticsearch:
    endpoints:
      - "elasticsearch-master:9200" # Elasticsearch 地址
  mongodb:
    uri: "mongodb://username:password@example-mongodb-0:27017,example-mongodb-1,example-mongodb-2:27017/datacenter?authSource=admin&appname=datacenter&replicaSet=example-mongodb" # MongoDB 连接地址
    database: "my-datacenter" # 数据库名，同步到 Elasticsearch 时使用相同名称。 
  rabbitmq:
    host: "rabbitmq-svc" # RabbitMQ 服务地址
    port: 5672 # RabbitMQ 服务端口
    username: "" # RabbitMQ 用户名
    password: "" # RabbitMQ 密码
  auth:
    clientId: "test" #替换为自己的 ClientID
    clientSecret: "e6423085f5165a58f8949e763c6691ffe44e2f86" #替换为自己的 ClientSecret
```

安装：

    helm install -f values.yaml my-dc datacenter/datacenter-service

卸载：

    helm delete my-dc

### 配置 RabbitMQ
创建一个 topic 类型的 Exchange，命名为 datacenter-{name}，如：datacenter-my-dc。

### 配置 MongoDB
创建对于配置名称的数据库，如：my-datacenter。

同时创建三个集合：
* form
* form_meta
* form_record