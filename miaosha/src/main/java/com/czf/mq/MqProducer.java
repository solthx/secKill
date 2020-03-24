package com.czf.mq;

import com.alibaba.fastjson.JSON;
import com.czf.dao.StockLogDOMapper;
import com.czf.dataobject.StockLogDO;
import com.czf.error.BusinessException;
import com.czf.service.ItemService;
import com.czf.service.OrderService;
import org.apache.ibatis.transaction.Transaction;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * @author czf
 * @Date 2020/3/20 11:52 下午
 */
@Component
public class MqProducer {

    // 消息生产者
    private DefaultMQProducer producer;

    private TransactionMQProducer transactionMQProducer;

    @Value("${mq.nameserver.addr}")
    private String nameAddr;

    @Value("${mq.topicname}")
    private String topicName;

    @Autowired
    private OrderService orderService;

    @Autowired
    private StockLogDOMapper stockLogDOMapper;

    // 在bean的初始化完成后，会调用@PostConstruct修饰的方法
    @PostConstruct
    public void init() throws MQClientException {
        // 初始化mq producer
        producer = new DefaultMQProducer("producer_group");
        producer.setNamesrvAddr(nameAddr);
        producer.start();

        transactionMQProducer = new TransactionMQProducer("transaction_producer_group");
        transactionMQProducer.setNamesrvAddr(nameAddr);
        transactionMQProducer.start();

        transactionMQProducer.setTransactionListener(new TransactionListener() {
            @Override
            public LocalTransactionState executeLocalTransaction(Message message, Object arg) {
                Integer itemId = (Integer)((Map)arg).get("itemId");
                Integer userId = (Integer)((Map)arg).get("userId");
                Integer promoId = (Integer)((Map)arg).get("promoId");
                Integer amount = (Integer)((Map)arg).get("amount");
                String stockLogId = (String)((Map)arg).get("stockLogId");
                try {
                    orderService.createOrder(userId, promoId, itemId, amount,stockLogId);
                } catch (BusinessException e) {
                    e.printStackTrace();
                    // 设置对应的stockLog为回滚状态
                    StockLogDO stockLogDO = stockLogDOMapper.selectByPrimaryKey(stockLogId);
                    stockLogDO.setStatus(3);
                    stockLogDOMapper.updateByPrimaryKeySelective(stockLogDO);

                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }
                return LocalTransactionState.COMMIT_MESSAGE;
            }

            // 对队列内的消息，定期调用此方法，用来处理Unknown状态 （要么执行一半挂了，要么执行了太久
            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt msg) {
                // 根据是否扣减库存成功，来判断要返回commit还是rollback，还是unknown
                String jsonString = new String(msg.getBody());
                Map<String, Object> mp = JSON.parseObject(jsonString, Map.class);
                Integer itemId = (Integer) mp.get("itemId");
                Integer amount = (Integer) mp.get("amount");
                String stockLogId = (String) mp.get("stockLog");
                StockLogDO stockLogDO = stockLogDOMapper.selectByPrimaryKey(stockLogId);
                if (stockLogDO == null)
                    return LocalTransactionState.UNKNOW;
                if (stockLogDO.getStatus()==2)
                    return LocalTransactionState.COMMIT_MESSAGE;
                else if (stockLogDO.getStatus()==1)
                    return LocalTransactionState.UNKNOW;
                return LocalTransactionState.ROLLBACK_MESSAGE;
            }
        });

    }

    // 事务< 数据库, 消息发送 >
    public boolean transactionAsyncReduceStock(Integer itemId, Integer amount, Integer promoId, Integer userId, String stockLogId){
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("itemId", itemId);
        bodyMap.put("amount", amount);
        bodyMap.put("stockLogId", stockLogId);

        Map<String, Object> argsMap = new HashMap<>();
        argsMap.put("itemId", itemId);
        argsMap.put("amount", amount);
        argsMap.put("userId", userId);
        argsMap.put("promoId", promoId);
        argsMap.put("stockLogId", stockLogId);

        Message message = new Message(topicName,"increase", JSON.toJSON(bodyMap).toString().getBytes(Charset.forName("UTF-8")));

        TransactionSendResult sendResult=null;
        try {
            sendResult = transactionMQProducer.sendMessageInTransaction(message, argsMap);
        } catch (MQClientException e) {
            e.printStackTrace();
            return false;
        }
        if (sendResult.getLocalTransactionState() == LocalTransactionState.COMMIT_MESSAGE)
            return true;
        return false;
    }

    // 同步库存扣减消息
    public boolean asyncReduceStock(Integer itemId, Integer amount){
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("itemId", itemId);
        bodyMap.put("amount", amount);
        Message message = new Message(topicName,"increase", JSON.toJSON(bodyMap).toString().getBytes(Charset.forName("UTF-8")));
        try {
            producer.send(message);
        } catch (MQClientException e) {
            e.printStackTrace();
            return false;
        } catch (RemotingException e) {
            e.printStackTrace();
            return false;
        } catch (MQBrokerException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
