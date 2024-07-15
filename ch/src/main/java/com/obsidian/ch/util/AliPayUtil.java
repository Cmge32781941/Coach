package com.obsidian.ch.util;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.obsidian.ch.dto.AliPay;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Configuration
public class AliPayUtil {

    @Value("${alipay.appId}")
    private String appId;
    @Value("${alipay.appPrivateKey}")
    private String PRIVATE_KEY;
    @Value("${alipay.alipayPublicKey}")
    private String PUBLIC_KEY;
    @Value("${alipay.notifyUrl}")
    private String NOTIFY_URL;
    @Value("${alipay.returnUrl}")
    private String RETURN_URL;
    @Value("${alipay.signType}")
    private String SIGN_TYPE;
    @Value("${alipay.charset}")
    private String CHARSET;
    @Value("${alipay.gatewayUrl}")
    private String GATEWAY_URL;
    @Value("${alipay.logPath}")
    private String LOG_PATH;

    public String pay(AliPay aliPay, Long orderId) throws AlipayApiException {
        AlipayClient alipayClient = new DefaultAlipayClient(GATEWAY_URL, appId, PRIVATE_KEY, "json", CHARSET, PUBLIC_KEY, SIGN_TYPE);
        // 2、设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        // 页面跳转同步通知页面路径
        String returnUrl = RETURN_URL + "?orderId=" + orderId;
        alipayRequest.setReturnUrl(returnUrl);
        // 服务器异步通知页面路径
        alipayRequest.setNotifyUrl(NOTIFY_URL);
        // 封装参数
        alipayRequest.setBizContent(JSON.toJSONString(aliPay));
        // 3、请求支付宝进行付款，并获取支付结果
        return alipayClient.pageExecute(alipayRequest).getBody();
    }
}
