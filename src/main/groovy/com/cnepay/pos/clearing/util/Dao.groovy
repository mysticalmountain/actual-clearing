package com.cnepay.pos.clearing.util

import groovy.sql.Sql
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.sql.DataSource
import java.sql.Timestamp

/**
 * 数据访问对象
 * User: andongxu
 * Time: 15-8-21 下午11:09 
 */
@Component
class Dao {

    private static final Logger log = LoggerFactory.getLogger(Dao)
    @Autowired
    DataSource datasource

    /**
     * 查询卡BIN
     * @param id
     * @return
     */
    def queryCardBin(String id) {
        def sql = """
            select * from DICT_CARDBIN  where id =${id}
       """
        getDb().firstRow(sql)
    }

    /**
     * 查询通道商户费率设置
     * @param acquirerId
     * @param cardbin
     * @return
     */
    def queryAcquirerFeeSettings(String acquirerId, String cardBin) {
        def sql = """
        select * from acq_merchant am
        left join acq_merchant_fee_rate amfr on am.id = amfr.acq_merchant_id
        left join fee_rate_setting frs on amfr.fee_rate_setting_id = frs.id
        where am.id = ${acquirerId}
        and amfr.cardbin_type = ${cardBin}
        """
        getDb().firstRow(sql)
    }

    def queryUPOPFeeSettings() {

    }
    /**
     * 查询商户费率设置
     * @param merchantId
     * @param cardBin
     * @return
     */
    def queryMerchantFeeSettings(String merchantId, String cardBin) {
        def sql = """
            select * from cm_merchant cm
                left join merchant_fee_rate mfr on cm.id = mfr.merchant_id
                left join fee_rate_setting  frs on mfr.fee_rate_setting_id = frs.id
               where mfr.cardbin_type = ${cardBin}
                    and cm.merchant_no = ${merchantId}
        """
        getDb().rows(sql)
    }

    /**
     * 查询银联通道手续费设置
     * @param acquirerId
     * @param specialBillingType
     * @param mccType
     * @param foreigncardType
     * @param cardType
     * @return
     */
    def queryAcquirerCost(String acquirerId, String specialBillingType, String mccType, String foreigncardType, String cardType) {
        def sql = """
            select * from acquirer_cost
             where ACQUIRER_ID = ${acquirerId}
               and SPECIAL_BILLING_TYPE = ${specialBillingType}
               and MCC_TYPE = ${mccType}
               and FOREIGNCARD_TYPE = ${foreigncardType}
               and CARD_TYPE = ${cardType}
        """
        getDb().firstRow(sql)
    }

    /**
     * 查询当前交易
     * @param id 交易ID
     * @return
     */
    def queryTransCurrent(String id) {
        def sql = """
            select tc.id transId, tcs.id clearingId, tc.*, tcs.*, a.*, am.special_billing_type from trans_current tc
                left join trans_clearing_status tcs on tc.id = trans_id
                left join acquirer a on tc.acquirer_id = a.id
                left join acq_merchant am on am.merchant_no = tc.acq_merchant_no
                    where tc.id = ${id}
        """
        getDb().firstRow(sql)
    }

    /**
     * 查询当前交易，使用原交易ID
     * @param srcId
     */
    def queryTransCurrentBySrcId(String srcId) {
        def sql = """
            select tc.*, tcs.*, a.* from trans_current tc
                left join trans_clearing_status tcs on tc.id = trans_id
                left join acquirer a on tc.acquirer_id = a.id
                    where tc.src_id = ${srcId}
        """
        getDb().firstRow(sql)
    }

    /**
     * 查询当前交易对应原交易
     * @param id
     * @return
     */
    def querySrcTransCurrent(String id) {
        def sql = """
            select * from trans_current tc where tc.id = (select src_id from trans_current where id = ${id})
        """
        getDb().firstRow(sql)
    }

    /**
     * 通过原交易查询反向交易
     * @param srcId
     */
    def queryTransCurrentReverse(String srcId) {
        def sql = """
            select *
              from trans_current tc
              left join trans_clearing_status tcs on tc.id = tcs.trans_id
             where tcs.clearing_status in ('1', '2')
               and tc.trans_type in ('reversal_sale', 'sale_void', 'reversal_auth_comp', 'auth_comp_cancel')
               and tc.src_id = ${srcId}
        """
        getDb().firstRow(sql)
    }

    /**
     * 查询交易历史，
     * @param srcId
     * @return
     */
    def querySrcTransHistory(String id) {
        def sql = """
            select * from trans_history tc where tc.id = (select src_id from trans_current where id = ${id})
        """
        getDb().firstRow(sql)
    }

    /**
     * 查询交易清分状态
     * @param transId
     * @return
     */
    def queryClearingStatus(String transId) {
        def sql = """
            select tr.id, tr.trans_type, tr.trans_status, tcs.* from trans_current tr
             left join trans_clearing_status tcs on tr.id = tcs.trans_id
              where tr.id = ${transId}
        """
        getDb().firstRow(sql)
    }
    /**
     * 查询交易清分状态
     * @param transId
     * @param accountDate
     * @return
     */
    def queryClearingStatus(String transId, String accountDate) {
        def sql = """
            select tr.trans_type, tr.trans_status, tcs.* from trans_current tr
                left join trans_clearing_status tcs on tr.id = tcs.trans_id
                where tcs.trans_id = ${transId} and tcs.account_date = ${accountDate}
        """
        getDb().firstRow(sql)
    }

    /**
     * 查询单机构代码
     * @param acqMerchantNo
     * @return
     */
    def queryAcqIssuerNo(String acqMerchantNo) {
        def sql = """
            select acq.issuer_no,
                acq.acquirer_code,
                acq.id,
                am.special_billing_type,
                dm.c1_name As mcc_type1,
                dm.c2_name As mcc_type2
           from ACQ_MERCHANT am
          inner join ACQUIRER acq on acq.id = am.acquirer_id
           left join dict_mcc dm on dm.id = am.mcc_id
          where am.merchant_no = ${acqMerchantNo}
        """
        getDb().firstRow(sql)
    }
    /**
     * 插入清分状态
     * @param transId
     * @param accountDate
     * @return
     */
    def insertClearingStatus(String transId) {
        def id = queryDeqId()
        Timestamp date = new Timestamp(System.currentTimeMillis())
        def sql = """
            insert into trans_clearing_status (id, clearing_status, trans_id, last_update_time)
                values (${id}, '1', ${transId}, ${date})
        """
        getDb().execute(sql)
    }

    /**
     * 查询序列ID
     * @return
     */
    def queryDeqId() {
        def sql = "select SEQ_TRANSCLEARINGSTATUS.nextval from dual"
        def sid = getDb().firstRow(sql)
        return sid.NEXTVAL
    }
    /**
     * 更新清分状态
     * @param id
     * @param status
     * @return
     */
    def updateClearingStatus(String id, String status) {
        Timestamp date = new Timestamp(System.currentTimeMillis())
        def sql = """
            update trans_clearing_status set clearing_status = ${status}, last_update_time = ${date} where id = ${id}
        """
        getDb().execute(sql)
    }

    /**
     * 更新记账状态
     * @param id
     * @param status
     * @return
     */
    def updateAccountStatus(String id, String status) {
        Timestamp date = new Timestamp(System.currentTimeMillis())
        def sql = """
            update trans_clearing_status set account_status = ${status}, last_update_time = ${date} where id = ${id}
        """
        getDb().execute(sql)
    }


    /**
     * 更新交易手续费信息
     * @param transId
     * @param acqMerchantTransFee
     * @param acqTransFee
     * @param brandServiceFee
     * @param merchantTransFee
     * @param merchantAdditionalFee
     * @param transRevenue
     * @param transStatus
     * @return
     */
    def updateTransFee(String transId,
                       BigDecimal acqMerchantTransFee,
                       BigDecimal acqTransFee,
                       BigDecimal brandServiceFee,
                       BigDecimal merchantTransFee,
                       BigDecimal merchantAdditionalFee,
                       BigDecimal transRevenue,
                       String acqFeeRate,
                       String brandServiceFeeRate,
                       String merchantFeeRate,
                       String addtionalFeeRate) {
        def sql = """
            update trans_current set ACQ_MERCHANT_TRANS_FEE=:acqMerchantTransFee,
                ACQ_TRANS_FEE=:acqTransFee,
                BRAND_SERVICE_FEE=:brandServiceFee,
                MERCHANT_TRANS_FEE=:merchantTransFee,
                MERCHANT_ADDITIONAL_FEE=:merchantAdditionalFee,
                TRANS_REVENUE=:transRevenue,
                ACQ_FEE_RATE=:acqFeeRate,
                BRAND_SERVICE_FEE_RATE=:brandServiceFeeRate,
                MERCHANT_FEE_RATE=:merchantFeeRate,
                ADDITIONAL_FEE_RATE=:addtionalFeeRate
                where id=:transId
        """
        getDb().execute(sql, [acqMerchantTransFee:acqMerchantTransFee,
                acqTransFee:acqTransFee,
                brandServiceFee:brandServiceFee,
                merchantTransFee:merchantTransFee,
                merchantAdditionalFee:merchantAdditionalFee,
                transRevenue:transRevenue,
                acqFeeRate:acqFeeRate,
                brandServiceFeeRate:brandServiceFeeRate,
                merchantFeeRate:merchantFeeRate,
                addtionalFeeRate:addtionalFeeRate,
                transId:transId])
    }

    /**
     * 更新解冻状态
     * @param id
     * @param status
     * @return
     */
    def updateUnfreezeStatus(String id, String status) {
        Timestamp date = new Timestamp(System.currentTimeMillis())
        def sql = """
            update trans_clearing_status set unfreeze_status = ${status}, last_update_time = ${date} where id = ${id}
        """
        getDb().execute(sql)
    }

    /**
     * 更新解冻状态
     * @param id
     * @param unfreezeStatus
     * @param unfreezeTraceNo
     * @return
     */
    def updateUnfreezeStatus(String id, String unfreezeStatus, String unfreezeTraceNo) {
        Timestamp date = new Timestamp(System.currentTimeMillis())
        def sql = """
            update TRANS_CLEARING_STATUS set unfreeze_status=${unfreezeStatus}, unfreeze_trace_no=${unfreezeTraceNo}, last_update_time=${date}  where id=${id}
        """
        getDb().execute(sql)
    }

    ThreadLocal<Sql> tl = new ThreadLocal();

    def getDb() {
        def db = tl.get()
        if (!db) {
            println datasource
            tl.set(new Sql(datasource))
            db = tl.get()
        }
        return db
    }
}
