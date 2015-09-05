package temp

import com.cnepay.pos.clearing.util.AccountException

import java.text.SimpleDateFormat

/**
 * User: andongxu
 * Time: 15-8-25 上午10:00 
 */
class Test {

    public static final def ERROR_MESSAGE = [
            101: '未找到交易对应的通道手续费设置',
            abc: '测试'
    ]

    public static void main(String [] args){


//        println  "update ${acquirerFee != null ? ACQ_MERCHANT_TRANS_FEE = acquirerFee}"



        println ERROR_MESSAGE[101]


//        throw new FeeCalcullateException("101", Constants.ERROR_MESSAGE[101])

        println ERROR_MESSAGE['abc']

        def acquirerFee = [:]
        acquirerFee.acqMerchantTransFee =  'trans_fee'
        acquirerFee.acqTransFee = 'trans_fee'
        acquirerFee.brandServiceFee = null
        println acquirerFee


        def a = BigDecimal.valueOf(1)

        def b = BigDecimal.valueOf(1)

        println a.compareTo(b)

        double temp = 1.2345

        println BigDecimal.valueOf(1.2345)
        println BigDecimal.valueOf(12345).setScale(2, BigDecimal.ROUND_HALF_UP).intValue()

        def abc = ""

        println abc ? abc : "aaa"

//        println BigDecimal.valueOf(null)


//        def msg = [ID:53699807, ACQ_AUTH_NO:924549, ACQ_MERCHANT_NO:868394059980425, ACQ_MERCHANT_TRANS_FEE:0, ACQ_REFERENCE_NO:200033931193, ACQ_TERMINAL_BATCH_NO:000001, ACQ_TERMINAL_NO:68001369, ACQ_TERMINAL_TRACE_NO:019700, ACQUIRER_ID:88, AMOUNT:100, BATCH_NO:000182, CARD_NO:6222120004000027, CARD_NO_ENCRYPT:NjIyMjEyMDAwNDAwMDAyNw, CARD_NO_WIPE:6222120027, CARDBIN_ID:195, CASHIER_NO:01, COMP_STATUS:2, CS_MERCHANT_ID:null, DATE_CREATED: null, LAST_UPDATED: null, MERCHANT_NO:Z07000000141863, MERCHANT_SETTLE_SHEET_ID:null, MERCHANT_TRANS_FEE:0, POSP_NO:01, REFERENCE_NO:100056116068, RESP_CODE:00, SRC_ID:53699715, STORE_ID:null, TERMINAL_NO:30164187, TRACE_NO:001428, TRANS_DATE_TIME:null, TRANS_REVENUE:0, TRANS_STATUS:3, TRANS_TYPE:auth_comp, ACQ_RESP_CODE:00, MERCHANT_ADDITIONAL_FEE:null, AGENCY_ID:2285, INTEG_TYPE:null, FEE_TYPE:null, AGENCY_FEE:null, ACCOUNT_MANAGER_ID:null, MCC:null, MERCHANT_FEE_RATE:null]


        def msg = [ID:53699807, ACQ_AUTH_NO:924549, ACQ_MERCHANT_NO:868394059980425]


        def temp1

        println temp1 ? temp1 : "aaaaaaaaaaaa"

        def acqMerchantTransFee = "dd"

        println acqMerchantTransFee ? " set ACQ_MERCHANT_TRANS_FEE=${acqMerchantTransFee}" : ""


        def error =    "不受理此类型交易,交易ID:27,交易类型:sale"

        AccountException ae = new AccountException(null, error)

        Calendar now = Calendar.getInstance();

        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss")

        println df.format(new Date())

        println df.format(new Date()).substring(0, 8)
        println df.format(new Date()).substring(8, 14)

        def tt

        println tt ?: "aaa"

    }
}