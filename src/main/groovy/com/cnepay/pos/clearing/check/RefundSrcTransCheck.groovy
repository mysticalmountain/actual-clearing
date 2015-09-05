package com.cnepay.pos.clearing.check

import com.cnepay.pos.clearing.util.Constants
import com.cnepay.pos.clearing.util.CheckException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * 退款原交易检查
 * User: andongxu
 * Time: 15-9-2 上午9:25 
 */
@Component
class RefundSrcTransCheck implements ICheck {

    Logger log = LoggerFactory.getLogger(RefundSrcTransCheck)

    @Override
    def execute(transCurrent) {
        def isContinueClearing = true
        if (!transCurrent) {
            def error = "原交易不存在"
            log.info(error)
            throw new CheckException(null, error)
        } else if (!Constants.TRANS_STATUS_REFUND.equals(transCurrent.trans_status as String)) {    //原交易状态非已退款
            def error = "原交易状态非已退款,原交易ID:${transCurrent.id},原交易类型：${transCurrent.trans_type},原交易状态:${transCurrent.trans_status}"
            log.info(error)
            throw new CheckException(null, error)
        }
        return isContinueClearing
    }
}
