package com.obsidian.ch.service;

import com.alipay.api.AlipayApiException;

public interface OrderService {

    String orderPay(Long orderAmount,Long goodsId) throws AlipayApiException;

}