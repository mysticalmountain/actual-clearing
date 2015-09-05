package com.cnepay.pos.clearing.conf

import com.cnepay.account.api.TransApi
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.ImportResource

/**
 * dubbo 配置
 * User: andongxu
 * Time: 15-8-31 下午1:00 
 */
@Configuration
@ImportResource("classpath:resources-dubbo.xml")
class DubboConf {

    @Autowired
    TransApi transApi

}
