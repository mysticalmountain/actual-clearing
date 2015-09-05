package com.cnepay.pos.clearing.account

import com.cnepay.account.api.AccountApi
import com.cnepay.account.api.TransApi
import com.cnepay.account.api.vo.BasicReq
import com.cnepay.pos.clearing.ApplicationTest

import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.ApplicationContext

/**
 * User: andongxu
 * Time: 15-8-31 下午12:29 
 */
class AccountApiTest extends ApplicationTest {

    @Autowired
    @Qualifier("accountApi")
    AccountApi accountApi

    @Test
    void test() {

        BasicReq baseReq = new BasicReq()
        baseReq.busCnl = 'POSP'
        baseReq.txDt = '20150831'
        baseReq.txTm = '121010'
        def accountDate = transApi.getAccountDate(baseReq)

        println "---------------------------------------------" + accountDate

    }
}
