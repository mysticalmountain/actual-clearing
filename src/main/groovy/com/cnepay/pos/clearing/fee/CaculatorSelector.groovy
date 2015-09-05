package com.cnepay.pos.clearing.fee

import com.cnepay.pos.clearing.fee.other.CommonReverseCalculator
import com.cnepay.pos.clearing.fee.refund.CommonRefundCalculator
import com.cnepay.pos.clearing.fee.sale.AcquirerSaleCalculator
import com.cnepay.pos.clearing.fee.sale.MerchantSaleCalculator
import com.cnepay.pos.clearing.fee.sale.SPDBSaleCalculator
import com.cnepay.pos.clearing.fee.sale.UPOPSaleCalculator
import com.cnepay.pos.clearing.util.FeeCalcullateException
import com.cnepay.pos.clearing.util.Constants
import com.cnepay.pos.clearing.util.Dao
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

/**
 * 手续费计算器选择器
 * User: andongxu
 * Time: 15-8-21 下午11:08 
 */
@Component
class CaculatorSelector {

    Logger log = LoggerFactory.getLogger(CaculatorSelector)

    /**
     * 通道清分
     */
    public static final int CLEARING_ACQUIRER = 1
    /**
     * 商户清分
     */
    public static final int CLEARING_MERCHANT = 2

    @Autowired
    Dao dao

    @Autowired
    ApplicationContext context

    /**
     * 选择计算器
     * @param clearingType
     * @param transId
     * @return
     */
    public ICalculator select(int clearingType, String transId) {
        if (transId == null || transId.equals("")) {
            throw new IllegalArgumentException("trans id not allow null:${transId}")
        }
        if (CLEARING_ACQUIRER == clearingType) {            //通道清分计算器
            return newAcquirerCalculator(transId)
        } else if (CLEARING_MERCHANT == clearingType) {    //商户清分计算器
            return newMerchantCalculator(transId)
        } else {
            throw new IllegalArgumentException("not support clearing type:${clearingType}")
        }
    }

    /**
     * 选择反向交易计算器
     * @return
     */
    public ICalculator selectReverse() {
        context.getBean(CommonReverseCalculator.class)
    }

    /**
     * 选择退款计算器
     * @return
     */
    public ICalculator selectRefund() {
        context.getBean(CommonRefundCalculator.class)
    }

    /**
     * 新建通道计算器
     * @param transId
     * @return
     */
    private ICalculator newAcquirerCalculator(String transId) {
        def transCurrent = dao.queryTransCurrent(transId)
        if (!transCurrent) {
            return null
        }
        if (Constants.TRANS_TYPE_SALE.equals(transCurrent.trans_type)                             //消费
                || Constants.TRANS_TYPE_AUTH_COMP.equals(transCurrent.trans_type)) {             //预授权完成
            if (Constants.ACQUIRER_CODE_UPOP.equals(transCurrent.acquirer_code)) {               //银联通道
                return context.getBean(UPOPSaleCalculator.class)
            } else if (Constants.ACQUIRER_CODE_BJSPDB.equals(transCurrent.acquirer_code)) {    //北京浦发银行通道
                return context.getBean(SPDBSaleCalculator.class)
            } else {      //通用通道
                return context.getBean(AcquirerSaleCalculator.class)
            }
        }else{
            def error = "不支持的交易类型;${transCurrent.trans_type}, 交易ID:${transCurrent.id}"
            log.error(error)
            throw FeeCalcullateException(null, error)
        }
    }

    /**
     * 新建商户计算器
     * @param transId
     * @return
     */
    private ICalculator newMerchantCalculator(String transId) {
        context.getBean(MerchantSaleCalculator.class)
    }
}
