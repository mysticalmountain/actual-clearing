package com.cnepay.pos.clearing.message

import com.cnepay.account.api.TransApi
import com.cnepay.account.api.vo.MerchantFundUnfreezeReq
import com.cnepay.account.api.vo.MerchantFundUnfreezeRsp
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
 * 退款消息处理
 * User: andongxu
 * Time: 15-8-26 下午5:37 
 */
@Component
@Scope("prototype")
class RefundMessageHandler implements IMessageHandler, Runnable {

    Logger log = LoggerFactory.getLogger(RefundMessageHandler)

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
    @Qualifier("refundSrcTransCheck")
    ICheck refundSrcTransCheck

    @Override
    def execute(msg) {
        log.info("清分开始,${msg}")
        try {
            //手续费计算
            feeCalculate(msg)
            //记账
            accounting(msg)
        }finally {
            log.info("清分完成,${msg}")
        }
    }

    /**
     * 手续费计算
     * @param msg
     */
    def feeCalculate(def msg) {
        log.info("手续费计算开始，交易ID:${msg.transId}")
        def _beginTime = System.currentTimeMillis()
        def isProcesStatus = false                      //标记清分状态是否已经更新为处理中
        try {
            //查询原交易
            def srcTrans = dao.querySrcTransCurrent(msg.transId)
            if (!srcTrans) {
                srcTrans = dao.querySrcTransHistory(msg.transId)
            }
            //原交易检查
            log.info("原交易检查")
            if (!refundSrcTransCheck.execute(srcTrans)) {
                //不做后续清分处理
                return
            }
            //查询当前交易
            def transCurrent = dao.queryTransCurrent(msg.transId)
            //清分状态检查
            log.info("清分状态检查,交易ID:${transCurrent.transId}")
            if (!clearingStatusCheck.execute(transCurrent)) {
                //不做后续清分处理
                return
            }
            if (transCurrent.clearing_status == null || "".equals(transCurrent.clearing_status)) {
                //插入清分状态处理中
                dao.insertClearingStatus(msg.transId as String)
                isProcesStatus = true
            } else {
                //更新清分状态处理中
                dao.updateClearingStatus(transCurrent.clearingId as String, Constants.CLEAR_STATUS_PROCESS)
                isProcesStatus = true
            }
            log.info("计算通道和商户手续费开始：交易ID:${msg.transId}")
            def beginTime = System.currentTimeMillis()
            ICalculator calculator = selector.selectRefund()
            def fee = calculator.calculate(transCurrent)
            def costTime = System.currentTimeMillis() - beginTime
            log.info("计算通道和商户手续费完成：交易ID:${msg.transId},手续费：${fee},耗时：${costTime}ms")
            //更新清分状态和手续费
            updateClearingSuccess(msg, fee)
            costTime = System.currentTimeMillis() - _beginTime
            log.info("手续费计算完成，交易ID:${msg.transId}，耗时：${costTime}ms")
        } catch (Exception e) {
            def costTime = System.currentTimeMillis() - _beginTime
            if (e instanceof CheckException) {
                log.info("交易检查失败：交易ID:${msg.transId},耗时：${costTime},异常信息:" + e.getMessage())
            } else if (e instanceof FeeCalcullateException) {
                log.info("手续费计算失败：交易ID:${msg.transId},耗时：${costTime},异常信息:" + e.getMessage())
            } else {
                log.error("手续费计算发生异常：交易ID:${msg.transId},耗时：${costTime},异常信息:${e.getMessage()}")
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
     * @return
     */
    def accounting(def msg) {
        log.info("记账开始,交易ID:${msg.transId}")
        def _beginTime = System.currentTimeMillis()
        def isProcesStatus = false                      //标记记账状态是否已经更新为处理中
        try {
            //查询当前交易
            def transCurrent = dao.queryTransCurrent(msg.transId)
            //检查交易状态
            log.info("记账状态检查,交易ID:${msg.transId}")
            if(!accountStatusCheck.execute(transCurrent)) {
                //检查失败不继续记账
                return
            }
            //更新记账状态-处理中
            dao.updateAccountStatus(transCurrent.clearingId as String, Constants.ACCOUNT_STATUS_PROCESS)
            isProcesStatus = true
            //解冻
            if (Constants.UNFREEZE_STATUS_TIMEOUT.equals(transCurrent.unfreeze_status)
                    || transCurrent.unfreeze_status == null) {                           //解冻超时或未解冻
                //准备解冻报文
                MerchantFundUnfreezeReq unfreezeReq = prepareUnfreeze(transCurrent)
                def _reqMsg = ClearingUtil.obj2string(unfreezeReq)
                log.info("发送商户资金解冻请求报文：${_reqMsg}")
                MerchantFundUnfreezeRsp unfreezeRsp
                //解冻
                for (int i = 0; i < retryTimes; i++) {
                    unfreezeRsp = transApi.merchantFundUnfreeze(unfreezeReq)
                    if (unfreezeRsp) {
                        break
                    }
                }
                def _resMsg = ClearingUtil.obj2string(unfreezeRsp)
                log.info("接收商户资金解冻返回报文：${_resMsg}")
                if (!unfreezeRsp) {                                     //解冻超时，记录状态；超时不允许做后续记账操作
                    def error = "商户资金解冻超时，解冻请求报文：${_reqMsg}"
                    //记录解冻状态，解冻流水号
                    dao.updateUnfreezeStatus(transCurrent.clearingId as String, Constants.UNFREEZE_STATUS_TIMEOUT, unfreezeReq.jrnNo)
                    throw AccountException(null, error)
                } else if (!unfreezeRsp.isSuccess) {
                    log.error("商户资金解冻失败,解冻请求报文：" + _reqMsg + "，响应报文：" + _resMsg)
                }
                dao.updateUnfreezeStatus(transCurrent.clearingId as String, Constants.UNFREEZE_STATUS_SUCCESS, unfreezeReq.jrnNo)
            }
            //准备记账
            POSTransReq req = preparedAccount(transCurrent)
            def _reqMsg = ClearingUtil.obj2string(req)
            log.info("发送记账代理请求报文：${_reqMsg}")
            POSTransRsp res
            //记账
            for (int i = 0; i < retryTimes; i++) {
                res = transApi.posTransTx(req)
                if (res) {
                    break
                }
            }
            def _resMsg = ClearingUtil.obj2string(res)
            log.info("接收记账代理返回报文：${_resMsg}")
            if (!res) {         //未收到结果，认为超时
                dao.updateAccountStatus(transCurrent.clearingId as String, Constants.ACCOUNT_STATUS_TIMEOUT)
            }
            if (!res.isSuccess) {              //记账失败，再记应收
                log.info("记账失败 记应收，交易ID:${transCurrent.id}")
                req.mustSuccess = true        //设置本次记账必须成功为true
                req.txnTyp = 13                //反交易失败记应收
                log.info("发送记账代理请求报文：${_reqMsg}")
                //记账
                for (int i = 0; i < retryTimes; i++) {
                    res = transApi.posTransTx(req)
                    if (res) {
                        break
                    }
                }
                log.info("接收记账代理返回报文：${_resMsg}")
                if (!res) {     //未收到结果，认为超时
                    dao.updateAccountStatus(transCurrent.clearingId as String, Constants.ACCOUNT_STATUS_TIMEOUT)
                } else {       //收到结果认为失败
                    dao.updateAccountStatus(transCurrent.clearingId as String, Constants.ACCOUNT_STATUS_SUCCESS)
                }
            } else {
                dao.updateAccountStatus(transCurrent.clearingId as String, Constants.ACCOUNT_STATUS_SUCCESS)
            }
        } catch (Exception e) {
            def costTime = System.currentTimeMillis() - _beginTime
            if (e instanceof AccountException) {
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
     * @param acquirerFee
     * @param merchantFee
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
        if (transCurrent.account_trace_no == null || "".equals(transCurrent.account_trace_no)) {
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
        req.adtFee = transCurrent.merchant_additional_fee ? transCurrent.merchant_additional_fee : 0     //附加手续费
        req.capTyp = 1  //资金种类-现金
        req.ccy = 1     //币种-人民币
        req.corgFee = transCurrent.acq_merchant_trans_fee ? transCurrent.acq_merchant_trans_fee : 0   //通道手续费
        req.corgNo = transCurrent.acquirer_code    //银行编号
        req.jrnNo = jrnNo   //记账流水号
        req.logNo = transCurrent.id     //交易流水号
        req.mercFee = transCurrent.merchant_trans_fee   //商户手续费
        req.mercId = transCurrent.merchant_no   //商户号
        req.txAmt = transCurrent.amount     //交易金额
        req.txnTyp = 9
        return req
    }

    /**
     * 准备解冻
     * @param transCurrent
     * @return
     */
    MerchantFundUnfreezeReq prepareUnfreeze(def transCurrent) {
        MerchantFundUnfreezeReq req = new MerchantFundUnfreezeReq()
        def jrnNo
        if (transCurrent.unfreeze_trace_no) {
            jrnNo = transCurrent.unfreeze_trace_no
        } else {
            jrnNo = transApi.getJrnNo()
        }
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss")
        String now = df.format(new Date())
        req.busCnl = Constants.CHANNEL_POS            // 渠道
        req.txDt = now.substring(0, 8)                  //日期
        req.txTm = now.substring(8, 14)                 //时间
        req.acDt = transCurrent.account_date            //会计日
        req.capTyp = 1                                  //资金种类-现金
        req.ccy = 1                                     //币种-人民币
        req.jrnNo = jrnNo                               //记账流水号
        req.logNo = transCurrent.id                     //交易流水号
        req.mercId = transCurrent.merchant_no           //商户号
        req.txAmt = transCurrent.amount                          //交易金额
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
