# 概述

MQ全称 `M`essage `Q`ueue（消息队列），是在消息的传输过程中保存消息的容器。大多用于分布式系统之间进行通信。

队列：一种数据结构，特征为**先进先出**

# MQ优势

## 应用解耦

消费方存活与否不影响生产方

**系统的耦合度越高，容错性就越低，可维护性就越低。**

**提高系统容错性和可维护性**

## 异步提速

生产方发完消息，可以继续下一步业务逻辑。

**异步通知和事件驱动：** MQ 可以用于事件驱动架构，例如，当某个事件发生时，消息队列可以向其他服务或应用发送异步通知。由于事件处理不需要立即同步完成，系统的反应速度大大提升。

**提升用户体验和系统吞吐量**

## 削峰填谷

削峰填谷是 MQ 的一项重要优势，能通过合理的负载调度，优化系统的整体性能和响应能力，提高高峰期的吞吐量，同时避免资源的浪费。这使得系统能够更加高效、稳定地运行![image-20251029174815056](C:\Users\Qingfeng\AppData\Roaming\Typora\typora-user-images\image-20251029174815056.png)

**提高系统稳定性**

# MQ劣势

* `系统可用性降低`：系统引入的外部依赖越多，系统稳定性越差。一旦MQ宕机，就会对业务造成影响。**如何保证MQ的高可用性？**
* `系统复杂度提高`：MQ的加入大大增加了系统的复杂度，以前系统间是同步的远程调用，现在是通过MQ进行异步调用。**如何保证消息不被重复消费？怎么处理消息丢失情况？如何保证消息传递的顺序性？**
* `一致性问题`：A系统处理完业务，通过MQ发给B，C，D三个系统发消息数据，如果B，C处理成功，D处理失败。**如何保证消息数据处理的一致性？**



# 实战

## 简易书写

**生产者**

```java
// 1.谁来发？
DefaultMQProducer producer = new DefaultMQProducer("group1");

// 2.发给谁？
producer.setNamesrvAddr("localhost:9876");
producer.start();

// 3.怎么发？

// 4.发给谁？
String msg = "Lichen is a layer.";
Message message = new Message("topic1", "tag1", msg.getBytes());
SendResult sendResult = producer.send(message);

// 5.发什么？
System.out.println(sendResult);

// 6.打扫战场
producer.shutdown();
```

**消费者**

```java
// 谁来收
DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("group1");

// 从哪里收
consumer.setNamesrvAddr("localhost:9876");

// 监听某个消息队列
consumer.subscribe("topic1","*");

// 处理业务流程，注册监听器
consumer.registerMessageListener(new MessageListenerConcurrently() {
    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
        for (MessageExt msg : list) {
            System.out.println("msg = " + msg);
            byte[] body = msg.getBody();
            System.out.println(new String(body));
        }

        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }
});

consumer.start();

System.out.println("Consumer is successful to start.");

// 消费者长连接，持续接受信息
// consumer.shutdown();
```



## 多消费者模式

### 负载均衡

**基本概念：**在负载均衡模式下，多个消费者（同一个Consumer Group内的消费者）共享同一个Topic下的消息，达到负载均衡的效果。

**代码设置**

```java
// 默认是负载均衡CLUSTERING
consumer.setMessageModel(MessageModel.CLUSTERING);
```

**优点：**

* 每条消息只会被一个消费者消费，避免重复消费。
* 自动负载均衡，当消费者数量变化时，RocketMQ会动态调整消息队列的分配。

### 广播模式

**基本概念**：在广播模式下，每个消费者都会收到 **Topic** 下的所有消息。也就是说，所有消费者都会独立地消费一遍消息，而不是共享消费。每个 **Consumer Group** 内的消费者接收到的消息是 **独立的**，不受其他消费者影响。

**代码设置**

```java
// 设置为广播模式BROADCASTING
consumer.setMessageModel(MessageModel.BROADCASTING);
```

**优点：**

* 如果消费者数量过多，可能会增加系统的负担，因为每个消费者独立消费
* 消息的重复消费可能会导致资源浪费

## 消息类型

### 同步消息

**基本概念**：同步消息是指发送方在发送消息后，需要等待接收方处理完成并返回响应（或结果）之后，才能继续执行其他操作。发送方和接收方之间存在着直接的、阻塞的交互。

**特点**：

- 发送方发送消息后，必须等待接收方的响应才能继续进行后续操作。
- 这种模式通常是阻塞式的，发送方会一直等待直到接收到响应或者超时。
- 如果消息传递的时间较长，发送方会被阻塞，导致效率低下。

### 异步消息

**基本概念**：异步消息是指发送方在发送消息后，不需要等待接收方的响应，发送方`可以立即继续执行后续操作`。接收方会在自己有空的时候处理消息，并可能在处理完成后返回一个响应，发送方在之后可以查询结果。

**特点**：

- 发送方发送消息后不需要等待响应，消息传递是非阻塞的。
- 接收方处理消息时通常是异步的，即处理过程不需要等待发送方的任何反馈。

### 单向消息

**基本概念**：单向消息是指发送方仅仅发送消息到接收方，不期望接收方的任何响应或确认。发送方发送消息后，不关心接收方的处理结果，也不要求接收方返回任何反馈。

**特点**：

- 发送方发送消息后，不需要等待任何响应。
- 消息是单向的，通常用于通知、日志记录等不需要返回结果的场景。
- 这种方式常用于低延迟和高吞吐量的场景，因为它不需要任何确认或等待响应。

### 延时消息

**基本概念**：延时消息是指消息在发送后不会立即被消费，而是要等到一定的时间延迟后才会被接收和处理。也就是说，消息的消费时间被人为推迟，直到指定的延时时间到达。

**特点**：

- 消息在生产者发送后不会立即被消费者接收，而是等待一段时间后才被消费。

```java
// 设置延时级别（这里设置为第 3 级，延时 10 秒）
message.setDelayTimeLevel(3);  
// 3代表延迟10秒（具体延迟时间在RocketMQ配置中定义）
```



### 批量消息

**基本概念**：批量消息是指将多个消息一起打包发送，以减少消息发送和接收的次数，提高系统的吞吐量。批量消息的发送和接收可以在一个操作中处理多个消息，避免每个消息都单独发送的开销。

## tag过滤

Producer发送消息

参数列表

`String topic,@Nullable String tags,byte[] body`

```java
Message message = new Message("topic1", "vip", msg.getBytes());
```



Consumer消费消息

参数列表

`String topic,String subExpression`

```java
// 监听某个消息队列
consumer.subscribe("topic1","tag1 || vip");
```



通过`tags`标签进行消息的过滤

## sql过滤

```java
// Producer
// 消息追加属性
message.putUserProperty("name","layer");
message.putUserProperty("age","18");

// Consumer
// 消费者 sql过滤
consumer.subscribe("topic1", MessageSelector.bySql("age > 16 and name = 'layer'"));
```



## SpringBoot整合

**生产者**

```java
@Autowired
private RocketMQTemplate rocketMQTemplate;

@GetMapping("/send")
public String send(){
    // 发送消息
    String msg = "Lc is a big layer,so I hate her.";
    rocketMQTemplate.convertAndSend("topic1",msg);
    //rocketMQTemplate.convertAndSend("topic1",new User("lc",23));

    return "success";
}
```



**消费者**

```java
@Service
@RocketMQMessageListener(topic = "topic1",consumerGroup = "group1")
public class DemoConsumer implements RocketMQListener<User> {

    @Override
    public void onMessage(User user) {
        System.out.println(user);
    }

    public void onMessage(String msg) {
        System.out.println(msg);
    }

}
```



## 顺序消息

**队列内有序，队列外无序**

**队列内有序**：RocketMQ 确保一个队列内的消息顺序性，即同一个队列中的消息会按照生产者发送的顺序进行消费。如果消费者是单线程消费该队列中的消息，那么消息的消费顺序与发送顺序是相同的。

**队列间无序**：如果你有多个队列（例如同一个 Topic 下有 3 个队列），消息在这些队列之间的顺序是无法保证的。不同队列中的消息是并行消费的，因此无法保证队列之间的顺序。

举个例子：

- 假设有一个 Topic `topicA`，它有 3 个队列（`queue-0`, `queue-1`, `queue-2`），如果你向 `topicA` 发送 9 条消息，RocketMQ 会将它们分配到这 3 个队列中。消息在每个队列内是有顺序的（比如，`queue-0` 中的消息会按照发送顺序消费），但不同队列之间的消费顺序无法保证（即 `queue-0` 和 `queue-1` 中的消息消费顺序是不确定的）。

## 事务消息

**流程**

1. 生产者向Broker发送half消息
2. Broker向生产者返回消息（已得知生产者要发消息）
3. 生产者在本地执行事务
4. 若没有问题，向Broker提交或回滚事务
   * 提交成功，Broker把消息放入队列，等待发送
   * 提交失败，告诉Broker删除消息，并回滚
5. 若一直未提交或回滚事务，Broker对生产者做确认
   * 生产者检测本地事务状态
   * 根据事务状态提交或回滚
   * ......



**状态**

* 提交状态：允许进入队列，此消息与非事务消息无区别
* 回滚状态：不允许进入队列，此消息等同于未发送过
* 中间状态：完成了half消息的发送，未对MQ进行二次状态确认
* 注意：事务消息仅与生产者有关，与消费者无关

![image-20251030181436256](C:\Users\Qingfeng\AppData\Roaming\Typora\typora-user-images\image-20251030181436256.png)

### 事务回滚

```java
// 设置事务监听
producer.setTransactionListener(new TransactionListener() {
    @Override
    public LocalTransactionState executeLocalTransaction(Message message, Object o) {
        System.out.println("执行了正常的事务过程。");
        return LocalTransactionState.ROLLBACK_MESSAGE;
    }

    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt messageExt) {
        System.out.println("执行事务补偿过程。");

        return LocalTransactionState.UNKNOW;
    }
});
```



### 事务补偿

`return LocalTransactionState.UNKNOW`



## 集群

### 工作流程

1. NameServer启动，开启监听，等待broker，producer与consumer连接
2. broker启动，根据配置信息，连接所有的NameServer，并保持长连接
   * 如果broker中有现存数据，NameServer将保存topic与broker联系
3. producer发消息，链接某个NameServer，并建立长连接
4. producer发消息
   * 如果topic存在，由NameServer直接分配
   * 如果topic不存在，由NameServer创建topic与broker的关系，并分配
5. producer在broker的topic选择一个消息队列
6. producer与broker建立长连接，用于发送消息
7. producer发送消息

# 高级特性

## 消息的存储

1. 消息生成者发送消息到MQ
2. MQ收到消息后，将消息进行持久化，存储该消息
3. MQ返回ACK给生产者
4. MQ push消息给对应的消费者
5. 消息消费者返回ACK给MQ
6. MQ删除该消息

**注意：**

* 第5步MQ在指定时间内接到消息消费者返回ACK，MQ认定消息消费成功，执行6
* 第5步MQ在指定时间未接到消息消费者返回ACK，MQ认定消费失败，重新执行456



## 高效的消息存储和读写方式

* 顺序写
* 零拷贝



## 消息存储结构

MQ数据存储区域包含内容如下：

* 消息数据存储区域
  * topit
  * queueId
  * message
* 消费逻辑队列
  * minOffset
  * maxOffset
  * consumerOffset
* 索引
  * key索引
  * 创建时间索引

![image-20251031123555671](C:\Users\Qingfeng\AppData\Roaming\Typora\typora-user-images\image-20251031123555671.png)

## 刷盘机制

**同步刷盘**：安全性高，效率低，速度慢

1. 生产者发送消息到MQ，MQ接到消息数据
2. MQ挂起生产者发送消息的线程
3. MQ将消息数据写入内存
4. 内存数据写入硬盘
5. 硬盘存储后返回SUCCESS
6. MQ恢复挂起的生产者线程
7. 发送ACK到生产者

**异步刷盘**：安全性低，效率高，速度快

1. 生产者发送消息到MQ，MQ接到消息数据
2. MQ将消息数据写入内存
3. 发送ACK到生产者

## 高可用性

* nameserver
  * 无状态 + 全服务器注册
* 消息服务器
  * 主从架构
* 消息生产
  * 生产者将相同的topic绑定到多个group组，保障master挂掉后，其他master仍可正常进行消息接收
* 消息消费
  * RocketMQ自身会根据master的压力确认是否由maste承担消息读取的功能，当master繁忙的时候，自动切换由slave承担数据读取的工作

## 主从数据复制

* 同步复制
* 异步复制

## 负载均衡

* Producer负载均衡
  * 内部实现了不同broker集群中对同一topic对应消息队列的负载均衡
* Consumer负载均衡
  * 平均分配
  * 循环平均分配（解决宕机问题）
* 广播模式

## 消息重试

### 顺序消息重试

当消费者消费消息失败后，RocketMQ会自动进行消息重试（每次间隔时间为1s）

注意：应用会出现消息消费被阻塞的情况，因此，要对顺序消息的消费情况进行监控，避免阻塞现象的发生。

### 无序消息重试



## 死信队列

当消息消费重试到达了指定次数（默认**16**次）后，MQ将无法被正常消费的消息称为**死信消息**。死信消息不会被直接抛弃，而是保存到了一个全新的队列中，该队列被称为死信队列。

### 特征

队列特征

* 归属于某一个组，而不归属Topic，也不归属消费者
* 一个死信队列中可以包含同一个组下的多个topic中的死信消息
* 死信队列不会进行默认初始化，当第一个死信出现后，此队列首次初始化

消息特征

* 不会被再次重复消费
* 死信队列中的消息有效期为3天，达到时限后将被清除



## 消息重复消费

原因：

* 生产者发送了重复的消息
  * 网络闪断
  * 生产者宕机
* 消息服务器投递了重复的消息
  * 网络闪断
* 动态的负载均衡过程



## 消息幂等

对同一条消息，无论消费多少次，结果保持一致，称为消息幂等性

解决方案：

* 使用业务id作为消息的key
* 在消费消息时，客户端对key作判定，未使用过放行，使用过抛弃
