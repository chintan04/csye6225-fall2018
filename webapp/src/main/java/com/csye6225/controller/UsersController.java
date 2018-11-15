package com.csye6225.controller;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.csye6225.model.Response;
import com.csye6225.model.Users;
import com.csye6225.repository.UserJpaRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.timgroup.statsd.StatsDClient;
import com.timgroup.statsd.NonBlockingStatsDClient;

@RestController
@RequestMapping("/user")
public class UsersController {

    @Autowired
    private UserJpaRespository userJpaRespository;

    private String response = null;

    @Autowired
    private StatsDClient statsd;

    private static int counter=0;

    @PostMapping(value = "/register",produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void register(@RequestBody Users user, HttpServletResponse response) {
        try {
            counter ++;
            statsd.incrementCounter("endpoint.register.http.post");
            System.out.println(counter);
            response.setContentType("application/json");
            if(!user.getUsername().matches("^(.+)@(.+)\\.(.+)$")){
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                this.response = Response.jsonString("Email / Username is not valid");
                response.getWriter().write(this.response);
                return;
            }
            List<Users> loginusers = userJpaRespository.findAll();
            for (Users l: loginusers) {
                if (l.getUsername().equals(user.getUsername()))
                {
                    this.response = Response.jsonString("User already present");
                    response.getWriter().write(this.response);
                    return;

                }
            }
            String pw_hash = BCrypt.hashpw(user.getPwd(), BCrypt.gensalt());
            user.setPwd(pw_hash);
            userJpaRespository.save(user);
            response.setStatus(HttpServletResponse.SC_CREATED);
            this.response = Response.jsonString("User created");
            response.getWriter().write(this.response);
        }
        catch (Exception ex)
        {
            System.out.println("Exception caught while register user : " +ex.getMessage());
        }

    }
    @GetMapping(value = {"/","/time"},produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void gettime(HttpServletRequest httpRequest, HttpServletResponse response) {
        try {
            counter++;
            statsd.incrementCounter("endpoint.time.http.get");
            System.out.println(counter);
            response.setContentType("application/json");
            final String authorization = httpRequest.getHeader("Authorization");
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
                            this.response = Response.jsonString(LocalDateTime.now().toString());
                            response.getWriter().write(this.response);
                            return;
                        }
                    }
                }
                this.response = Response.jsonString("Pls Login");
                response.getWriter().write(this.response);
            } else {
                this.response = Response.jsonString("Pls Login");
                response.getWriter().write(this.response);
            }
        }
        catch(Exception ex) {

        }
    }

    @PostMapping(value = "/reset",produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void resetPassword(HttpServletRequest httpRequest, HttpServletResponse response, @RequestBody  Users user) {
        try {
            statsd.incrementCounter("endpoint.reset.http.post");
            response.setContentType("application/json");
           Users userobj = userJpaRespository.findOne(user.getUsername());
            if(userobj!=null)
            {
                System.out.println("try - " + user.getUsername());
                AmazonSNS amazonSNS = AmazonSNSClientBuilder.defaultClient();
                System.out.println("Getting ARN........");
                 String arn =  amazonSNS.createTopic("password_reset").getTopicArn();
                System.out.println("ARN - "+arn);
                PublishRequest publishRequest = new PublishRequest(arn, user.getUsername());
                System.out.println("Publish Request created.......");
                PublishResult publishResult = amazonSNS.publish(publishRequest);
                 System.out.println("Result published - " + publishResult.getMessageId());
                this.response = Response.jsonString("arn - " + arn + "message ID -" + publishResult.getMessageId()+" test "+user.getUsername());
                response.getWriter().write(this.response);
            }
            else
            {
                System.out.println("inside else");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                this.response = Response.jsonString("Username/email not present " +user.getUsername());
                response.getWriter().write(this.response);
                return;

            }

        }
        catch(Exception ex) {
            System.out.println("Exception = " + ex.getMessage());
        }

    }

}
