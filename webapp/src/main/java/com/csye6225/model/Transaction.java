package com.csye6225.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Transaction {
    @Id
    private String id;
    private String description;
    private String merchant;
    private String amount;
    private String date;
    private String category;
    @ManyToOne
    @JoinColumn(name = "userId",nullable = false)
    private Users user;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMerchant() {
        return merchant;
    }

    public void setMerchant(String merchant) {
        this.merchant = merchant;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }


    public Transaction(){}

    public Transaction(String id, String description, String merchant, String amount, String date, String category)
    {
        this.id = id;
        this.description = description;
        this.merchant=merchant;
        this.amount=amount;
        this.date = date;
        this.category = category;
    }
}
