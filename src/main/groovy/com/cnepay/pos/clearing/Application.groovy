package com.cnepay.pos.clearing

import com.alibaba.druid.pool.DruidDataSource
import com.cnepay.pos.clearing.conf.RemoteConf
import com.cnepay.pos.clearing.message.MessageQueue
import com.cnepay.pos.clearing.message.MessageReceive
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean

import javax.sql.DataSource
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * User: andongxu
 * Time: 15-8-21 下午10:30 
 */
@SpringBootApplication
class Application {

    @Bean
    @ConfigurationProperties(prefix = 'datasource')
    DataSource dataSource() {
        new DruidDataSource()
    }

    @Bean
    ExecutorService saleExecutorService() {
        Executors.newCachedThreadPool();
    }

    @Bean
    MessageReceive messageReceive() {
        new MessageReceive()
    }

    @Bean
    MessageQueue saleMessageQueue() {
        new MessageQueue()
    }

    @Bean
    MessageQueue reverseMessageQueue() {
        new MessageQueue()
    }

    @Bean
    JsonSlurper jsonSlurper() {
        new JsonSlurper()
    }




    static void main(String[] args) {
        println "init romote config begin..."
        RemoteConf.initRomoteConfig()
        println "init romote config end..."
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args)
        MessageReceive mr = context.getBean(MessageReceive)
        mr.recive()
    }

}
