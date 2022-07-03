package com.huawei.allinone.uniform.domain;

import com.sun.istack.internal.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

/**
 * @Data自动生成一个默认构造函数，但跟@Builder一起使用就是有全部参数的构造函数了，框架创建对象会异常，
 * 需要添加@NoArgsConstructor和@RequiredArgsConstructor，但不能再添加@RequiredArgsConstructor
 */
@Data
@Builder
@NoArgsConstructor
//@RequiredArgsConstructor
@AllArgsConstructor
public class ProductInfo {
    private String productName;

    private BigDecimal productPrice;

    private Integer productStatus;
}