package com.huawei.allinone.uniform.service;

import com.huawei.allinone.uniform.domain.ProductInfo;
import com.huawei.allinone.uniform.exception.ApiException;
import com.huawei.allinone.uniform.response.AppCode;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;

@Service("productInfoService")
public class ProductInfoServiceImpl implements ProductInfoService {
    Random random = new Random();

    @Override
    public ProductInfo getOne(ProductInfo productInfo) {
        int orderId = random.nextInt(10);
        if (orderId < 5) {
            throw new ApiException(AppCode.ORDER_NOT_EXIST, "订单号不存在：" + orderId);
        }

        return ProductInfo.builder().productName("手机").productPrice(BigDecimal.valueOf(10000)).productStatus(1).build();
    }
}
