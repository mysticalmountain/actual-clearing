package com.cnepay.pos.clearing.account

/**
 * Created with IntelliJ IDEA.
 * User: andongxu
 * Date: 15-9-7
 * Time: 下午11:21
 * To change this template use File | Settings | File Templates.
 */
class AccountContext {

    IAccountState accountState

    def transCurrent

    IAccountState getAccountState() {
        return accountState
    }

    void setAccountState(IAccountState accountState) {
        this.accountState = accountState
    }

    def getTransCurrent() {
        return transCurrent
    }

    void setTransCurrent(transCurrent) {
        this.transCurrent = transCurrent
    }

    public static void main(String[] args) throws IOException {
        AccountContext machine = new AccountContext();
        IAccountState start = new SaleNormalState();
        machine.setAccountState(start);
        while(machine.getAccountState() != null){
            machine.getAccountState().handle(machine);
        }

        System.out.println("press any key to exit:");
        System.in.read();
    }

}
