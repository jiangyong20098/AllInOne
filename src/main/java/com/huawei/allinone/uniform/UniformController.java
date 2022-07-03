package com.huawei.allinone.uniform;

import com.huawei.allinone.uniform.advice.NotControllerResponseAdvice;
import com.huawei.allinone.uniform.domain.ProductInfo;
import com.huawei.allinone.uniform.domain.ProductInfoVo;
import com.huawei.allinone.uniform.domain.UserDto;
import com.huawei.allinone.uniform.service.ProductInfoService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;


/**
 * 控制器
 * 如果不引入validation-api、hibernate-validator，@Validated可能不生效
 */
@RestController
@RequestMapping("/uniform")
public class UniformController {
    @Autowired
    ProductInfoService productInfoService;

    @PostMapping("/findByVo")
    public ProductInfo findByVo(@Validated @RequestBody ProductInfoVo vo) {
        ProductInfo productInfo = new ProductInfo();
        BeanUtils.copyProperties(vo, productInfo);
        return productInfoService.getOne(productInfo);
    }

    @GetMapping("/health")
    @NotControllerResponseAdvice
    public String health() {
        return "success";
    }

    @PostMapping("/insert")
    public String validatedDemo1(@Validated @RequestBody UserDto use1Dto){
        System.out.println(use1Dto);
        return "success";
    }
}
