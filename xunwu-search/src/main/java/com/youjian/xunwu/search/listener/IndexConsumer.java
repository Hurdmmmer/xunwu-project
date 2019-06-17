package com.youjian.xunwu.search.listener;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.youjian.xunwu.search.entity.HouseIndexMessage;
import com.youjian.xunwu.search.service.ISearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class IndexConsumer {

    @Autowired
    private ISearchService searchService;
    private ThreadLocal<Integer> retry = new ThreadLocal<>();

    @RabbitListener(bindings = @QueueBinding(   // 绑定 queue 到交换机上
            // 声明一个队列, 并需要持久化保存
            value = @Queue(value = "xunwu-queue", durable = "true"),
            // 绑定的 交换机
            exchange = @Exchange(
                    value = "xunwu-exchange",   // 交换机的名称
                    ignoreDeclarationExceptions = "true", // 忽略声明的一些问题, 如果已经存在咋补继续声明, 防止重复声明
                    type = ExchangeTypes.TOPIC  // 交换机类型, fanout, topic, direct
            ),
            // 绑定的 routing key 的路由规则, 可以是一个数组, 绑定多个 key
            key = {"createOrUpdateIndex"}
    ))
    public void consumer(Message message, Channel channel) throws IOException {

        try {
            String json = new String(message.getBody(), StandardCharsets.UTF_8);
            HouseIndexMessage indexMessage = JSONObject.parseObject(json, HouseIndexMessage.class);

            switch (indexMessage.getOperation()) {
                case HouseIndexMessage.INDEX:
                    createIndex(indexMessage);
                    break;
                case HouseIndexMessage.REMOVE:
//                    removeIndex(indexMessage);
                    break;
                default:
                    log.warn("does not support operate: {}", indexMessage.getOperation());
            }
            // 手动确认ACK, 告诉 mq 可以删除该条消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            Integer maxCount = retry.get();
            if (maxCount == null) {
                retry.set(0);
            } else {
                if (++maxCount > 3) {
                    log.info("重试超过 3 次, 检查详细信息");
                    retry.remove();
                    channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                    return;
                }
                retry.set(maxCount);
            }
            // 重新投递
            channel.basicRecover( true);
        }
    }

    private void removeIndex(HouseIndexMessage indexMessage) {
        searchService.remove(indexMessage.getHouseId());
    }

    private void createIndex(HouseIndexMessage indexMessage) {
        searchService.createOrUpdateIndex(indexMessage.getHouseId());
    }
}
