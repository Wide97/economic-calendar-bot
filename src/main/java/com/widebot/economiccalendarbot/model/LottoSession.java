package com.widebot.economiccalendarbot.model;

public class LottoSession {
    private String pair;
    private Double capitale;
    private Double rischio;
    private Double stopLoss;

    public String getPair() { return pair; }
    public void setPair(String pair) { this.pair = pair; }

    public Double getCapitale() { return capitale; }
    public void setCapitale(Double capitale) { this.capitale = capitale; }

    public Double getRischio() { return rischio; }
    public void setRischio(Double rischio) { this.rischio = rischio; }

    public Double getStopLoss() { return stopLoss; }
    public void setStopLoss(Double stopLoss) { this.stopLoss = stopLoss; }

    public boolean isComplete() {
        return pair != null && capitale != null && rischio != null && stopLoss != null;
    }
}
