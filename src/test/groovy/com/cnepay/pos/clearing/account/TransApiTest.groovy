package com.cnepay.pos.clearing.account

import com.cnepay.account.api.TransApi
import com.cnepay.account.api.vo.BasicReq
import com.cnepay.pos.clearing.ApplicationTest
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

/**
 * User: andongxu
 * Time: 15-8-31 下午2:43 
 */
class TransApiTest extends ApplicationTest {

    @Autowired
    @Qualifier("transApi")
    TransApi transApi

    @Test
    void getAccountDateTest() {
        BasicReq baseReq = new BasicReq()
        baseReq.busCnl = 'POSP'
        baseReq.txDt = '20150831'
        baseReq.txTm = '121010'
        def accountDate = transApi.getAccountDate(baseReq)

        println "===========================" + accountDate

    }

    @Test
    void getJrnNoTest() {
        println "===========================" + transApi.getJrnNo()
    }
}
