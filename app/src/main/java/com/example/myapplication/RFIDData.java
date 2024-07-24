package com.example.myapplication;

public class RFIDData {
  private String ID;
  private String unit;
  private String department;
  private String person;
  private String spec;
  private String productName;

  public RFIDData(String ID, String unit, String department, String person, String spec, String productName) {
    this.ID = ID;
    this.unit = unit;
    this.department = department;
    this.person = person;
    this.spec = spec;
    this.productName = productName;
  }
  public String getID(){
    return ID;
  }

  public String getUnit() {
    return unit;
  }

  public String getDepartment() {
    return department;
  }

  public String getPerson() {
    return person;
  }

  public String getSpec() {
    return spec;
  }

  public String getProductName() {
    return productName;
  }
}

