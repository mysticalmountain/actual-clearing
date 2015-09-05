package com.cnepay.pos.clearing.message

import com.cnepay.account.api.TransApi
import com.cnepay.account.api.vo.POSTransReq
import com.cnepay.account.api.vo.POSTransRsp
import com.cnepay.pos.clearing.check.ICheck
import com.cnepay.pos.clearing.fee.CaculatorSelector
import com.cnepay.pos.clearing.fee.ICalculator
import com.cnepay.pos.clearing.util.AccountException
import com.cnepay.pos.clearing.util.CheckException
import com.cnepay.pos.clearing.util.ClearingUtil
import com.cnepay.pos.clearing.util.Constants
import com.cnepay.pos.clearing.util.Dao
import com.cnepay.pos.clearing.util.FeeCalcullateException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

import java.text.SimpleDateFormat

/**
 * 反向交易的反向交易  预授权完成撤销冲正 、消费撤销冲正
 * User: andongxu
 * Time: 15-9-1 下午8:32 
 */
@Component
@Scope("prototype")
class Reverse2MessageHandler implements IMessageHandler, Runnable{

    Logger log = LoggerFactory.getLogger(Reverse2MessageHandler)

    def msg

    @Autowired
    CaculatorSelector selector
    @Autowired
    Dao dao
    @Autowired
    TransApi transApi
    @Value('${account.timeout.retry.times}')
    Integer retryTimes
    @Autowired
    @Qualifier("clearingStatusCheck")
    ICheck clearingStatusCheck
    @Autowired
    @Qualifier("accountStatusCheck")
    ICheck accountStatusCheck
    @Autowired
    @Qualifier("reverse2SrcTransCheck")
    ICheck reverse2SrcTransCheck

    @Override
    def execute(msg) {
        log.info("清分开始,${msg}")
        try {
            //手续费计算
            feeCalculate(msg)
            //记账
            accounting(msg)
        } finally {
            log.info("清分完成,${msg}")
        }
    }

    /**
     * 手续费计算
     * @param msg
     */
    def feeCalculate(def msg) {
        log.info("手续费计算开始：交易ID:${msg.transId}")
        def _beginTime = System.currentTimeMillis()
        def isProcesStatus = false                      //标记清分状态是否已经更新为处理中
        try {
            def srcTrans = dao.querySrcTransCurrent(msg.transId)
            if (!srcTrans) {
                srcTrans = dao.querySrcTransHistory(msg.transId)
            }
            //原交易检查
            log.info("原交易检查")
            if (!reverse2SrcTransCheck.execute(srcTrans)) {
                //不做后续清分处理
                return
            }
            //查询当前交易
            def transCurrent = dao.queryTransCurrent(msg.transId)
            //清分状态检查
            if (!clearingStatusCheck.execute(transCurrent)) {
                //不做后续清分处理
                return
            }
            log.info("计算通道和商户手续费开始：交易ID:${msg.transId}")
            def beginTime = System.currentTimeMillis()
            ICalculator calculator = selector.selectReverse()
            def fee = calculator.calculate(transCurrent)
            def costTime = System.currentTimeMillis() - beginTime
            log.info("计算通道和商户手续费完成：交易ID:${msg.transId},手续费：${fee},耗时：${costTime}ms")
            //更新清分状态和手续费
            updateClearingSuccess(msg, fee)
            log.info("手续费计算完成交易ID:${msg.transId}，耗时：${costTime}ms")
        } catch (Exception e) {
            def costTime = System.currentTimeMillis() - _beginTime
            if (e instanceof CheckException) {
                log.info("交易检查失败：交易ID:${msg.transId},耗时：${costTime},异常信息:" + e.getMessage())
            } else if (e instanceof FeeCalcullateException) {
                log.info("手续费计算失败：交易ID:${msg.transId},耗时：${costTime}ms,异常信息:" + e.getMessage())
            } else {
                log.error("手续费计算发生异常：交易ID:${msg.transId},耗时：${costTime}ms,异常信息:${e.getMessage()}")
            }
            if (isProcesStatus) {
                def cs = dao.queryClearingStatus(msg.transId)
                //更新清分状态失败
                dao.updateClearingStatus(cs.id as String, Constants.CLEARING_STATUS_FAIL)
            }
            throw e
        }
    }

    /**
     * 记账
     * @param msg
     */
    def accounting(def msg) {
        log.info("记账开始,交易ID:${msg.transId}")
        def _beginTime = System.currentTimeMillis()
        def isProcesStatus = false                      //标记记账状态是否已经更新为处理中
        try {
            def transCurrent = dao.queryTransCurrent(msg.transId)
            log.info("检查记账状态,交易ID:${msg.transId}")
            if (!accountStatusCheck.execute(transCurrent)) {
                //检查失败不继续记账
                return
            }
            //更新记账状态-处理中
            dao.updateAccountStatus(transCurrent.clearingId as String, Constants.ACCOUNT_STATUS_PROCESS)
            isProcesStatus = true
            POSTransReq req = preparedAccount(transCurrent)
            log.info("发送记账代理请求报文：" + ClearingUtil.obj2string(req))
            POSTransRsp res
            //记账
            for (int i = 0; i < retryTimes; i++) {
                res = transApi.posTransTx(req)
                if (res) {
                    break
                }
            }
            log.info("接收记账代理返回报文:" + ClearingUtil.obj2string(res))
            if (!res) {     //超时
                log.info("更新记账状态超时，交易ID:${transCurrent.transId}")
                dao.updateAccountStatus(transCurrent.clearingId as String, Constants.ACCOUNT_STATUS_TIMEOUT)
            } else {        //认为成功，因为已经送必mustSuccess=true
                log.info("更新记账状态成功，交易ID:${transCurrent.transId}")
                dao.updateAccountStatus(transCurrent.clearingId as String, Constants.ACCOUNT_STATUS_SUCCESS)
            }
            def costTime = System.currentTimeMillis() - _beginTime
            log.info("记账结束,交易ID:${msg.transId}，耗时：${costTime}ms")
        } catch (Exception e) {
            def costTime = System.currentTimeMillis() - _beginTime
            if (e instanceof CheckException) {
                log.info("交易检查失败：交易ID:${msg.transId},耗时：${costTime},异常信息:" + e.getMessage())
            } else if (e instanceof AccountException) {
                log.info("记账失败：交易ID:${msg.transId},耗时：${costTime},异常信息:" + e.getMessage())
            } else {
                log.error("记账发生异常：交易ID:${msg.transId},耗时：${costTime},异常信息:${e.getMessage()}")
            }
            if (isProcesStatus) {
                def cs = dao.queryClearingStatus(msg.transId)
                //更新清分状态失败
                dao.updateAccountStatus(cs.id as String, Constants.ACCOUNT_STATUS_FAIL)
            }
            throw e
        }
    }

    /**
     * 更新手续费和清分状态成功
     * @param msg
     * @param fee
     */
    void updateClearingSuccess(def msg, def fee) {
        //不计算该交易收入
        def transRevenue = null
        def cs = dao.queryClearingStatus(msg.transId as String)
        //同一事务
        dao.getDb().withTransaction {
            //更新清分状态
            dao.updateClearingStatus(cs.id as String, Constants.CLEARING_STATUS_SUCCESS)
            //更新手续费
            dao.updateTransFee(msg.transId as String,
                    fee.acqMerchantTransFee,
                    fee.acqTransFee,
                    fee.brandServiceFee,
                    fee.merchantTransFee,
                    fee.merchantAdditionalFee,
                    transRevenue,
                    fee.acqTransFeeRate,
                    fee.brandServiceFeeRate,
                    fee.merchantTransFeeRate,
                    fee.merchantAddtionalFeeRate
            )
        }
    }

    /**
     * 准备记账
     * @param transCurrent
     * @return
     */
    POSTransReq preparedAccount(def transCurrent) {
        //获取记账流水号
        def jrnNo
        if (transCurrent.account_trace_no == null || "null".equals(transCurrent.account_trace_no)) {
            jrnNo = transApi.getJrnNo()
        } else {
            jrnNo = transCurrent.account_trace_no
        }
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss")
        String now = df.format(new Date())
        POSTransReq req = new POSTransReq()
        req.busCnl = Constants.CHANNEL_POS    //渠道
        req.txDt = now.substring(0, 8)          //日期
        req.txTm = now.substring(8, 14)         //时间
        req.acDt = transCurrent.account_date    //会计日
        req.adtFee = transCurrent.merchant_additional_fee    //附加手续费
        req.capTyp = 1  //资金种类-现金
        req.ccy = 1     //币种-人民币
        req.corgFee = transCurrent.acq_merchant_trans_fee   //通道手续费
//        req.corgNo = transCurrent.acquirer_code    //银行编号
        req.corgNo = "BOC"
        req.jrnNo = jrnNo   //记账流水号
        req.logNo = transCurrent.trans_id     //交易流水号
        req.mercFee = transCurrent.merchant_trans_fee   //商户手续费
        req.mercId = transCurrent.merchant_no   //商户号
        req.txAmt = transCurrent.amount     //交易金额
        def txnTyp
        if (Constants.TRANS_TYPE_SALE.equals(transCurrent.trans_type)) {
            txnTyp = 1
        } else if (Constants.TRANS_TYPE_AUTH_COMP.equals(transCurrent.trans_type)) {
            txnTyp = 5
        }
        req.mustSuccess = true          //必须成功
        req.txnTyp = txnTyp
        return req
    }

    @Override
    void run() {
        execute(msg)
    }

    public void setMsg(def msg) {
        this.msg = msg
    }
}
