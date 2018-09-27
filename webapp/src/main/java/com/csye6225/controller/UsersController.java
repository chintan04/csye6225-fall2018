package com.csye6225.controller;

import com.csye6225.model.Users;
import com.csye6225.repository.UserJpaRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UsersController {

    @Autowired
    private UserJpaRespository userJpaRespository;

    @PostMapping(value = "/register")
    @ResponseBody
    public String register(@RequestBody Users user) {
        try {
            List<Users> loginusers = userJpaRespository.findAll();
            for (Users l: loginusers) {
                if (l.getUsername().equals(user.getUsername()))
                {
                    return "User already present";

                }
            }
            String pw_hash = BCrypt.hashpw(user.getPwd(), BCrypt.gensalt());
            user.setPwd(pw_hash);
            userJpaRespository.save(user);
            return "You are successfully logged in";
        }
        catch (Exception ex)
        {
            System.out.println("Exception caught while register user : " +ex.getMessage());
            return "Exception occurred..Pls try after sometime";
        }

    }

}
