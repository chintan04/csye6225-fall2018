package com.csye6225.controller;

import com.csye6225.filter.AuthFilter;
import com.csye6225.model.Transaction;
import com.csye6225.model.Users;
import com.csye6225.repository.TransactionJpaRepository;
import com.csye6225.repository.UserJpaRespository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONWrappedObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sun.net.www.protocol.http.AuthCacheImpl;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/transaction")
public class TranscationController {

    @Autowired
    private TransactionJpaRepository transactionJpaRepository;


    @GetMapping()
    @ResponseBody
    public String getTransaction(HttpServletRequest request)
    {
        String status = AuthFilter.authorizeUser(request);
        if(status.equals("ok"))
        {
            List<Transaction> transactionList = new ArrayList<Transaction>(transactionJpaRepository.findAll());
            ObjectMapper mapper = new ObjectMapper();
            try {
                String jsonString = mapper.writeValueAsString(transactionList);
                System.out.println(jsonString);
                return jsonString;
            }
            catch (Exception ex)
            {}
            /*Gson gson = new Gson();
            jsonString = gson.toJson(transactionList);*/


        }
        return status;
    }

    @PostMapping()
    @ResponseBody
    public String createTransaction(@RequestBody Transaction transaction, HttpServletRequest request)
    {
        String status = AuthFilter.authorizeUser(request);
        if(status.equals("ok"))
        {
            Users user = new Users();
            user.setUserId(1);
            user.setUsername("xyz");
            user.setPwd("xyz");
            transaction.setUser(user);
            transactionJpaRepository.save(transaction);
            return "created";

        }
        return status;
    }

    @PutMapping()
    @ResponseBody
    public String updateTransaction(@RequestBody Transaction transaction, HttpServletRequest request) {
        String status = AuthFilter.authorizeUser(request);
        if (status.equals("ok"))
        {
            return "created";
        }
        return status;
    }

    @DeleteMapping
    @ResponseBody
    public String deleteTransaction (HttpServletRequest request) {
        String status = AuthFilter.authorizeUser(request);
        if(status.equals("ok")){
            return "deleted";

        }
        return status;
    }
}
