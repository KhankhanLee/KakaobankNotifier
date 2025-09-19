package com.kakaobank.sheetsync.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 카카오뱅크 거래 내역을 나타내는 모델 클래스
 */
public class Transaction {
    
    @JsonProperty("date")
    private String date;
    
    @JsonProperty("time")
    private String time;
    
    @JsonProperty("type")
    private String type; // 입금, 출금, 이체 등
    
    @JsonProperty("amount")
    private String amount; // 금액
    
    @JsonProperty("balance")
    private String balance; // 잔액
    
    @JsonProperty("description")
    private String description; // 거래 내용
    
    // 기본 생성자
    public Transaction() {}
    
    // 전체 생성자
    public Transaction(String date, String time, String type, String amount, String balance, String description) {
        this.date = date;
        this.time = time;
        this.type = type;
        this.amount = amount;
        this.balance = balance;
        this.description = description;
    }
    
    // Getter와 Setter
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
    public String getTime() {
        return time;
    }
    
    public void setTime(String time) {
        this.time = time;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getAmount() {
        return amount;
    }
    
    public void setAmount(String amount) {
        this.amount = amount;
    }
    
    public String getBalance() {
        return balance;
    }
    
    public void setBalance(String balance) {
        this.balance = balance;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public String toString() {
        return "Transaction{" +
                "date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", type='" + type + '\'' +
                ", amount='" + amount + '\'' +
                ", balance='" + balance + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
