package com.example.myapplication;

public class RFIDData {
  private String epc;
  private long timestamp;

  public RFIDData(String epc, long timestamp) {
    this.epc = epc;
    this.timestamp = timestamp;
  }

  public String getEpc() {
    return epc;
  }

  public long getTimestamp() {
    return timestamp;
  }
}

