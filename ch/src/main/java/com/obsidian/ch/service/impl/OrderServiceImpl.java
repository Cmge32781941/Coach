package com.obsidian.ch.service.impl;


import com.alipay.api.AlipayApiException;
import com.obsidian.ch.dto.AliPay;
import com.obsidian.ch.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.obsidian.ch.util.AliPayUtil;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    AliPayUtil aliPayUtil;
    /**
     * 下单
     *
     * @param orderAmount 订单金额
     * @return 返回支付结果页面内容
     * @throws AlipayApiException
     */
    public String orderPay(Long orderAmount, Long orderId) throws AlipayApiException {
        //1. 调用支付宝
        AliPay aliPay = new AliPay();
        aliPay.setOut_trade_no(String.valueOf(orderId));
        aliPay.setSubject("充值:" + orderAmount);
        aliPay.setTotal_amount(orderAmount.toString());
        String pay = aliPayUtil.pay(aliPay,orderId);
        return pay;
    }
}
