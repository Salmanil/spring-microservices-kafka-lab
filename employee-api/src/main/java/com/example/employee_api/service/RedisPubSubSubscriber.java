package com.example.employee_api.service;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
public class RedisPubSubSubscriber implements MessageListener {

    private final RedisPubSubStore redisPubSubStore;

    public RedisPubSubSubscriber(RedisPubSubStore redisPubSubStore) {
        this.redisPubSubStore = redisPubSubStore;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel());
        String payload = new String(message.getBody());
        redisPubSubStore.record(channel, payload);
    }
}
