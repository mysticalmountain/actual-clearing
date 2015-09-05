package com.cnepay.pos.clearing.check

import com.cnepay.pos.clearing.util.CheckException
import com.cnepay.pos.clearing.util.Constants
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * 记账状态检查
 * User: andongxu
 * Time: 15-9-2 上午9:24 
 */
@Component
class AccountStatusCheck implements ICheck{

    Logger log = LoggerFactory.getLogger(AccountStatusCheck)

    @Override
    def execute(transCurrent) {
        def isContinueClearing = true
        if (!Constants.CLEARING_STATUS_SUCCESS.equals(transCurrent.clearing_status)) {
            String error = "清分未成功，不允许记账，交易ID:${transCurrent.transId},清分状态：${transCurrent.clearing_status}"
            throw CheckException("", error)
        } else if (Constants.ACCOUNT_STATUS_PROCESS.equals(transCurrent.account_status)) {
            String error = "记账处理中，不允许重复记账,交易ID:${transCurrent.transId},记账状态：${transCurrent.account_status}"
            throw CheckException("", error)
        } else if (Constants.ACCOUNT_STATUS_SUCCESS.equals(transCurrent.account_status)) {
            String error = "已记账成功，不允许重复记账,交易ID:${transCurrent.transId},记账状态：${transCurrent.account_status}"
            throw CheckException("", error)
        }
//        else if (!Constants.TRANS_TYPE_SALE.equals(transCurrent.trans_type)
//                && !Constants.TRANS_TYPE_AUTH_COMP.equals(transCurrent.trans_type)) {
//            String error = "不受理此类型交易,交易ID:${transCurrent.transId},交易类型:${transCurrent.trans_type}"
//            throw CheckException("", error)
//        }
        return isContinueClearing
    }
}
