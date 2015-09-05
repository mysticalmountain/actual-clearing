package com.cnepay.pos.clearing.check

import com.cnepay.pos.clearing.util.Constants
import com.cnepay.pos.clearing.util.Dao
import com.cnepay.pos.clearing.util.FeeCalcullateException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * 反向交易的反向交易对应的原交易检查
 * User: andongxu
 * Time: 15-9-2 上午9:26 
 */
@Component
class Reverse2SrcTransCheck implements ICheck {

    Logger log = LoggerFactory.getLogger(Reverse2SrcTransCheck)

    @Autowired
    Dao dao

    @Override
    def execute(transCurrent) {
        def isContinueClearing = true
        if (!Constants.TRANS_STATUS_REVOKE.equals(transCurrent.trans_status)               //已撤销
                || !Constants.TRANS_STATUS_REVERSE.equals(transCurrent.trans_status)) {    //已冲正
            def error = "原交易状态非已撤销或已冲正,原交易ID:${transCurrent.id},原交易类型：${transCurrent.trans_type},商户号:${transCurrent.merchant_no}, 通道ID:${transCurrent.acquirer_id}"
            log.info(error)
            throw new FeeCalcullateException(null, error)
        }
        def reverseTrans = dao.queryTransCurrentReverse(transCurrent.transId)
        if (reverseTrans) {      //每个交易只会清分一条撤销或退款
            def error = "原交易已有反向交易（非退货），不再做后续清分;原交易ID:${transCurrent.transId},原交易类型：${transCurrent.trans_type},商户号:${transCurrent.merchant_no}, 通道ID:${transCurrent.acquirer_id}"
            log.info(error)
            throw new FeeCalcullateException(null, error)
        }
        return isContinueClearing
    }
}
