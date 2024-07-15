package com.obsidian.ch.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.obsidian.ch.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("alipay")
public class AliPayController {

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
    @Autowired
    private OrderService orderService;

    private static final Logger LOGGER = LoggerFactory.getLogger(AliPayController.class);

    //问题：在Controller中return 直接返回了字符串,并没有跳转到html网页
    //解决：RestController改成@Controller
    /**
     * 跳转到下单页面
     *
     * @return
     */
    @GetMapping("/goPay")
    public String goPay() {
        return "pay";
    }

    /**
     * 下单，并调用支付宝
     *
     * @param orderAmount
     * @return
     * @throws AlipayApiException
     */
    @RequestMapping(value = "/pay", method = RequestMethod.GET)
    @ResponseBody
    public void pay(@RequestParam(required = false) Long orderAmount, int goodId, HttpServletResponse httpResponse) throws Exception {
        Long orderId = System.currentTimeMillis();
        LOGGER.info("goodId" + String.valueOf(goodId));
        String payResult = orderService.orderPay(orderAmount, orderId);
        httpResponse.setContentType("text/html;charset=" + CHARSET);
        httpResponse.getWriter().write(payResult);
        httpResponse.getWriter().flush();
        httpResponse.getWriter().close();
    }

    @RequestMapping(value = "/goPaySuccPage", method = RequestMethod.GET)
    public String goPaySuccPage(HttpServletRequest request, Long orderId,Model model) {
        //同步回调‌：‌指的是在支付接口请求时设置的return_url参数，‌主要用于网站支付接口支付成功后的同步页面跳转。‌这是一种即时响应机制，‌当支付宝处理完请求后，‌当前页面会自动跳转到商户网站里指定的页面。‌同步回调只有一次页面跳转通知，‌通过GET方式获取，‌同步通知参数携带在同步地址后面
        //AliPayUtil在return_url追加orderId
        LOGGER.info("支付宝同步回调获取订单ID" + String.valueOf(orderId));
        //return "redirect:http://localhost:8080/GoodsDetails?activeParam=2";
        model.addAttribute("orderId",orderId);
        return "paySuccPage";
    }

    @ResponseBody
    @RequestMapping(value = "/notifyPayResult", method = RequestMethod.POST)
    public String notifyPayResult(HttpServletRequest request) {
        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<进入支付宝异步回调->>>>>>>>>>>>>>>>>>>>>>>>>");
        //异步回调‌：‌指的是支付宝服务器主动通知商户服务器里指定的页面http/https路径。‌这种机制可以收到多次通知，‌如支付成功后的异步通知、‌退款后的异步通知等。‌异步通知通过POST方式获取，‌用于判断交易状态，‌如交易是否成功。‌异步通知的应用场景包括但不限于支付场景，‌以防止同步通知时出现意外，‌确保支付结果的准确性。‌异步通知的触发条件包括但不限于交易状态发生改变，‌如支付成功后、‌退款期限到期后等
        Map<String, String[]> requestParams = request.getParameterMap();
        Map<String, String> params = new HashMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }
        //2.封装必须参数
        // 商户订单号
        int orderId = 1;
        //交易状态
        String tradeStatus = params.get("trade_status");
        //3.签名验证(对支付宝返回的数据验证，确定是支付宝返回的)
        boolean signVerified = false;
        try {
            //3.1调用SDK验证签名
            signVerified = AlipaySignature.rsaCheckV1(params, PUBLIC_KEY, CHARSET, SIGN_TYPE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("--------------->验签结果:" + signVerified);
        //4.对验签进行处理
        if (signVerified) {
            //验签通过
            //只处理支付成功的订单: 修改交易表状态,支付成功
            if ("TRADE_FINISHED".equals(tradeStatus) || "TRADE_SUCCESS".equals(tradeStatus)) {
                //根据订单号查找订单,防止多次回调的问题

                return "success";
            } else {
                return "failure";
            }
        } else {
            //验签不通过
            System.err.println("-------------------->验签失败");
            return "failure";
        }
    }
}