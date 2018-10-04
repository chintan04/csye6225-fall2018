package com.csye6225.controller;

import com.csye6225.filter.AuthFilter;
import com.csye6225.model.Transaction;
import com.csye6225.model.Users;
import com.csye6225.repository.TransactionJpaRepository;
import com.csye6225.repository.UserJpaRespository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONWrappedObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    @Autowired
    private TransactionJpaRepository transactionJpaRepository;

    @Autowired
    private UserJpaRespository userJpaRespository;

    @GetMapping
    @ResponseBody
    public void getTransaction(HttpServletRequest request, HttpServletResponse response) {
        try {
            String username = AuthFilter.authorizeUser(request, userJpaRespository);
            List<Transaction> transactionListtemp = new ArrayList<Transaction>(transactionJpaRepository.findAll());
            List<Transaction> transactionListfinal = new ArrayList<>();
            if (username != null) {
                for (Transaction t : transactionListtemp) {
                    if (t.getUser().getUsername().equals(username)) {
                        transactionListfinal.add(t);
                    }
                }
                ObjectMapper mapper = new ObjectMapper();
                String jsonString = mapper.writeValueAsString(transactionListfinal);
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json");
                response.getWriter().write(jsonString);
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @PostMapping
    @ResponseBody
    public void createTransaction(@RequestBody Transaction transaction, HttpServletRequest request, HttpServletResponse response) {
        try {
            String username = AuthFilter.authorizeUser(request, userJpaRespository);
            List<Users> userlist = userJpaRespository.findAll();
            if (username != null) {
                if (transaction.getAmount().equals("") || transaction.getCategory().equals("") ||
                        transaction.getDescription().equals("") || transaction.getMerchant().equals("")) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                    UUID u1 = UUID.randomUUID();
                    transaction.setId(u1);
                    transaction.setDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
                    for (Users u : userlist) {
                        if (u.getUsername().equals(username)) {
                            transaction.setUser(u);
                            break;
                        }
                    }
                    transactionJpaRepository.save(transaction);
                    response.setStatus(HttpServletResponse.SC_CREATED);
                }
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @PutMapping(value = "/{id}")
    @ResponseBody
    public void updateTransaction(@RequestBody Transaction transaction, HttpServletRequest request, @PathVariable UUID id, HttpServletResponse response) {
        try {
            String username = AuthFilter.authorizeUser(request, userJpaRespository);
            if (username != null) {
                if (id == null || id.toString().trim().length() == 0) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                    Transaction transc = transactionJpaRepository.findOne(id);
                    if (transc != null) {
                        if (transc.getUser().getUsername().equals(username)) {
                            if (transaction.getDescription() != null)
                                transc.setDescription(transaction.getDescription());

                            if (transaction.getAmount() != null)
                                transc.setAmount(transaction.getAmount());

                            if (transaction.getCategory() != null)
                                transc.setCategory(transaction.getCategory());

                            if (transaction.getMerchant() != null)
                                transc.setMerchant(transaction.getMerchant());

                            transactionJpaRepository.save(transc);
                            response.setStatus(HttpServletResponse.SC_CREATED);
                        } else {
                            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                        }
                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    }
                }
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @DeleteMapping(value = "/{id}")
    @ResponseBody
    public void deleteTransaction(@PathVariable UUID id, HttpServletRequest request, HttpServletResponse response) {
        try {
            String username = AuthFilter.authorizeUser(request, userJpaRespository);
            if (username != null) {
                if (id == null || id.toString().trim().length() == 0) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                    Transaction transc = transactionJpaRepository.findOne(id);
                    if (transc != null) {
                        if (transc.getUser().getUsername().equals(username)) {
                            transactionJpaRepository.delete(id);
                            response.setStatus(HttpServletResponse.SC_OK);
                        } else {
                            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                        }
                    } else {
                        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    }
                }
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
