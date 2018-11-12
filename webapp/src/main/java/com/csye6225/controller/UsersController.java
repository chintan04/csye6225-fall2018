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

@RestController
@RequestMapping("/user")
public class UsersController {

    @Autowired
    private UserJpaRespository userJpaRespository;

    private String response = null;

    @PostMapping(value = "/register",produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void register(@RequestBody Users user, HttpServletResponse response) {
        try {
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

    @PostMapping(value = "/reset")
    @ResponseBody
    public void resetPassword(HttpServletRequest httpRequest, HttpServletResponse response, @RequestBody String email) {
        try {
            response.setContentType("application/json");
            if(!email.matches("^(.+)@(.+)\\.(.+)$")){
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                this.response = Response.jsonString("Email / Username is not valid");
                response.getWriter().write(this.response);
                return;
            }
            Users user = userJpaRespository.findOne(email);
            if(user!=null)
            {
                String arn = System.getProperty("snsTopicARN");
                AmazonSNS amazonSNS = AmazonSNSClientBuilder.defaultClient();
                PublishRequest publishRequest = new PublishRequest(arn, email);
                PublishResult publishResult = amazonSNS.publish(publishRequest);
            }
            else
            {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                this.response = Response.jsonString("Username/email not present");
                response.getWriter().write(this.response);
                return;

            }

        }
        catch(Exception ex) {

        }

    }

}
