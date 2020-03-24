package com.czf.mq;

import com.alibaba.fastjson.JSON;
import com.czf.dao.ItemStockDOMapper;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * @author czf
 * @Date 2020/3/20 11:52 下午
 */
@Component
public class MqConsumer {

    private DefaultMQPushConsumer consumer;
    @Value("${mq.nameserver.addr}")
    private String nameAddr;

    @Value("${mq.topicname}")
    private String topicName;

    @Autowired
    private ItemStockDOMapper itemStockDOMapper;

    @PostConstruct
    public void init() throws MQClientException {
        consumer = new DefaultMQPushConsumer("stock_consumer_group");
        consumer.setNamesrvAddr(nameAddr);
        // 订阅topicName的所有消息
        // 这里的consumer除了能够订阅topic，还可以根据subExpression表达式来做对消息的二次过滤
        consumer.subscribe(topicName,"*");
        // 当消息推送过来时，该怎么处理这个消息！
        // 在这里就是根据消息中的itemId和amount来实现对数据库中库存的减操作.
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                Message message = msgs.get(0);
                String jsonString = new String(message.getBody());
                Map<String, Object> mp = JSON.parseObject(jsonString, Map.class);
                Integer itemId = (Integer) mp.get("itemId");
                Integer amount = (Integer) mp.get("amount");
                itemStockDOMapper.decreaseStock(itemId, amount);
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS; // 返回success则表示这个消息已被消费掉
            }
        });
        consumer.start();
    }
}
