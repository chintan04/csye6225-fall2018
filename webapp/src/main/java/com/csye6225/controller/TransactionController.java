package com.csye6225.controller;

import com.csye6225.filter.AuthFilter;
import com.csye6225.model.Transaction;
import com.csye6225.model.Users;
import com.csye6225.repository.TransactionJpaRepository;
import com.csye6225.repository.UserJpaRespository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONWrappedObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;
import sun.net.www.protocol.http.AuthCacheImpl;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    @Autowired
    private TransactionJpaRepository transactionJpaRepository;

    @Autowired
    private UserJpaRespository userJpaRespository;


    @GetMapping()
    @ResponseBody
    public String getTransaction(HttpServletRequest request) {
        String status = AuthFilter.authorizeUser(request, userJpaRespository);
        if (status.equals("ok")) {
            List<Transaction> transactionListtemp = new ArrayList<Transaction>(transactionJpaRepository.findAll());
            List<Transaction> transactionListfinal = new ArrayList<>();
            final String authorization = request.getHeader("Authorization");
            String base64Credentials = authorization.substring("Basic".length()).trim();
            byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(credDecoded, StandardCharsets.UTF_8);
            // credentials = username:password
            final String[] values = credentials.split(":", 2);
            String username = values[0];
            for (Transaction t : transactionListtemp) {
                if (t.getUser().getUsername().equals(username)){
                    transactionListfinal.add(t);
                }
            }
            ObjectMapper mapper = new ObjectMapper();
            try {
                String jsonString = mapper.writeValueAsString(transactionListfinal);
                System.out.println(jsonString);
                transactionListfinal=null;
                return jsonString;
            } catch (Exception ex) {
            }
            /*Gson gson = new Gson();
            jsonString = gson.toJson(transactionList);*/


        }
        return status;
    }

    @PostMapping()
    @ResponseBody
    public String createTransaction(@RequestBody Transaction transaction, HttpServletRequest request) {
        String status = AuthFilter.authorizeUser(request, userJpaRespository);
        if (status.equals("ok")) {
            final String authorization = request.getHeader("Authorization");
            if (authorization != null && authorization.toLowerCase().startsWith("basic")) {
                // Authorization: Basic base64credentials
                String base64Credentials = authorization.substring("Basic".length()).trim();
                byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
                String credentials = new String(credDecoded, StandardCharsets.UTF_8);
                // credentials = username:password
                final String[] values = credentials.split(":", 2);
                String username = values[0];
                String pwd = values[1];
                List<Users> userlist = userJpaRespository.findAll();
                for (Users u : userlist) {
                    if (u.getUsername().equals(username)) {
                        if (BCrypt.checkpw(pwd, u.getPwd())) {
                            transaction.setUser(u);
                            break;
                        }
                    }
                }
            }
            UUID u1 = UUID.randomUUID();
            transaction.setId(u1);
            transactionJpaRepository.save(transaction);
            return "created";
        }
        return status;
    }

    @PutMapping(value = "/{id}")
    @ResponseBody
    public String updateTransaction(@RequestBody Transaction transaction, HttpServletRequest request, @PathVariable String id) {
        String status = AuthFilter.authorizeUser(request, userJpaRespository);
        if (status.equals("ok")) {
            Transaction transc = transactionJpaRepository.findOne(id);
            if (transaction.getDescription() != null)
                transc.setDescription(transaction.getDescription());

            if (transaction.getAmount() != null)
                transc.setAmount(transaction.getAmount());

            if (transaction.getCategory() != null)
                transc.setCategory(transaction.getCategory());

            if (transaction.getMerchant() != null)
                transc.setMerchant(transaction.getMerchant());

            transactionJpaRepository.save(transc);
            return "created";
        }
        return status;
    }

    @DeleteMapping(value = "/{id}")
    @ResponseBody
    public String deleteTransaction(@PathVariable String id, HttpServletRequest request) {
        String status = AuthFilter.authorizeUser(request, userJpaRespository);
        if (status.equals("ok")) {
            transactionJpaRepository.delete(id);
            return "deleted";

        }
        return status;
    }
}
