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
import java.util.UUID;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    @Autowired
    private TransactionJpaRepository transactionJpaRepository;

    @Autowired
    private UserJpaRespository userJpaRespository;


    @GetMapping()
    @ResponseBody
    public String getTransaction(HttpServletRequest request)
    {
        String status = AuthFilter.authorizeUser(request,userJpaRespository);
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
        String status = AuthFilter.authorizeUser(request,userJpaRespository);
        if(status.equals("ok"))
        {
            Users user = new Users();
            user.setUserId(1);
            user.setUsername("xyz");
            user.setPwd("xyz");
            transaction.setUser(user);
            UUID u=UUID.randomUUID();
            transaction.setId(u);
            transactionJpaRepository.save(transaction);
            return "created";

        }
        return status;
    }

    @PutMapping(value="/{id}")
    @ResponseBody
    public String updateTransaction(@RequestBody Transaction transaction, HttpServletRequest request,@PathVariable String id) {
        String status = AuthFilter.authorizeUser(request,userJpaRespository);
        if (status.equals("ok"))
        {
            Transaction transc = transactionJpaRepository.findOne(id);
            if(transaction.getDescription()!=null)
                transc.setDescription(transaction.getDescription());

            if(transaction.getAmount()!=null)
                transc.setAmount(transaction.getAmount());

            if(transaction.getCategory()!=null)
                transc.setCategory(transaction.getCategory());

            if(transaction.getMerchant()!=null)
                transc.setMerchant(transaction.getMerchant());

            transactionJpaRepository.save(transc);
            return "created";
        }
        return status;
    }

    @DeleteMapping(value="/{id}")
    @ResponseBody
    public String deleteTransaction (@PathVariable String id,HttpServletRequest request) {
        String status = AuthFilter.authorizeUser(request,userJpaRespository);
        if(status.equals("ok")){
            transactionJpaRepository.delete(id);
            return "deleted";

        }
        return status;
    }
}
