package com.dicedev.dispatch.service;

import org.springframework.stereotype.Service;

import com.dicedev.dispatch.message.OrderCreated;

@Service
public class DispatchService {
    public void process(OrderCreated payload) {}
}
