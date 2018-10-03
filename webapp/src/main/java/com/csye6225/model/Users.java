package com.csye6225.model;

import javax.persistence.*;
import java.util.List;

@Entity
public class Users {

    @Id
    private String username;
    private String pwd;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    @OneToMany(mappedBy = "user")
    private List<Transaction> transactionList;
}
