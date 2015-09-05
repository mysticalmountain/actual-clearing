package com.cnepay.pos.clearing.util

import com.cnepay.pos.clearing.ApplicationTest
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

/**
 * User: andongxu
 * Time: 15-8-25 下午5:37 
 */
class DaoTest extends ApplicationTest {

    @Autowired
    Dao dao

    @Test
    void queryTransCurrentTest() {
        println dao.temp("53699807")
    }


    @Test
    void temp2Test() {
        def res = dao.temp2()

        println "---------" +  res.getClass()

        println "---------" +  res.size()

        res.each { it ->
            println("---------" + it)
        }

        for (def r : res) {
            println r
        }
    }

    @Test
    void temp3Test() {
        def res = dao.temp3()
        println("===================" + res.getClass())
        println("===================" + res)
    }


}
