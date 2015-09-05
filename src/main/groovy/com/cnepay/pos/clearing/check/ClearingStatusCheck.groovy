package com.cnepay.pos.clearing.check

import com.cnepay.pos.clearing.util.Constants
import com.cnepay.pos.clearing.util.CheckException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * 清分状态检查
 * User: andongxu
 * Time: 15-9-2 上午9:22 
 */
@Component
class ClearingStatusCheck implements ICheck {

    Logger log = LoggerFactory.getLogger(ClearingStatusCheck)

    @Override
    def execute(transCurrent) {
        boolean isContinueClearing = true
        if (!transCurrent) {
            String error = "未找到交易，不做清分处理。"
            log.info(error)
            throw new CheckException("", error as String)
        } else if (!Constants.TRANS_STATUS_SUCCESS.equals(transCurrent.trans_status as String)) {
            String error = "该交易状态为未成功，不做清分处理。交易ID:${transCurrent.transId}"
            log.info(error)
            throw new CheckException("", error as String)
        } else if (Constants.CLEAR_STATUS_PROCESS.equals(transCurrent.clearing_status)) {
            String error = "该交易正在清分处理中，无确认结果前不再做清分处理。交易ID:${transCurrent.transId}"
            log.info(error)
            throw new CheckException("", error as String)
        } else if (Constants.CLEARING_STATUS_SUCCESS.equals(transCurrent.clearing_status)) {
            String error = "该交易已经清分成功，不会再做清分处理。交易ID:${transCurrent.transId}"
            log.info(error)
            isContinueClearing = false
        } else if (Constants.TRANS_STATUS_NOTSUCCESS.equals(transCurrent.trans_status)) {
            String error = "该交易状态为未成功，不做清分处理。交易ID:${transCurrent.transId}"
            log.info(error)
            throw new CheckException("", error as String)
        }
        return isContinueClearing
    }
}
