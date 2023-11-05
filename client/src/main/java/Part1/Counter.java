package Part1;


public class Counter {
    private int totalReq;
    private int failedReq;

    public Counter() {
        this.totalReq = 0;
        this.failedReq = 0;
    }

    public synchronized void incrTotoalReq(int times) {
        this.totalReq += times;
    }

    public synchronized void incFailedReq(int times) {
        this.failedReq += times;
    }

    public int getTotalReq() {
        return this.totalReq;
    }

    public int getFailedReq() {
        return this.failedReq;
    }

    public int getSuccessReq() {
        return this.totalReq - this.failedReq;
    }
}