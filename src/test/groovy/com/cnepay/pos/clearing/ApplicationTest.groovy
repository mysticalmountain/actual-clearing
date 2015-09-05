package com.cnepay.pos.clearing

import com.alibaba.druid.pool.DruidDataSource
import com.cnepay.pos.clearing.conf.RemoteConf
import com.cnepay.pos.clearing.util.Dao
import org.junit.runner.RunWith
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.test.IntegrationTest
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.web.WebAppConfiguration

import javax.sql.DataSource

/**
 * User: andongxu
 * Time: 15-8-21 下午10:30 
 */
//@SpringBootApplication


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
class ApplicationTest {

}
