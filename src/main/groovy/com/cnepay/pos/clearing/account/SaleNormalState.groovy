package com.cnepay.pos.clearing.account

import com.cnepay.pos.clearing.util.ClearingUtil
import com.cnepay.pos.clearing.util.Constants
import com.cnepay.pos.clearing.util.Dao
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

/**
 * Created with IntelliJ IDEA.
 * User: andongxu
 * Date: 15-9-7
 * Time: 下午11:27
 * To change this template use File | Settings | File Templates.
 */
class SaleNormalState implements IAccountState {

    Logger log = LoggerFactory.getLogger(SaleNormalState)

    @Autowired
    Dao dao

    @Override
    def handle(AccountContext context) {


        //无后续操作设置为空
        context.setAccountState(null)
    }
}
