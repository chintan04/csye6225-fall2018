package com.csye6225.filter;

import com.csye6225.model.Users;
import com.csye6225.repository.UserJpaRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

public class AuthFilter {

    @Autowired
    static UserJpaRespository userJpaRespository;

    public static String authorizeUser(HttpServletRequest request)
    {
        final String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.toLowerCase().startsWith("basic")) {
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
                    if(BCrypt.checkpw(pwd, u.getPwd()))
                    {
                        return "ok";
                    }
                }
            }
            return "Authentication Failed";
        }
        else {
            return "You are not authorized user";
        }
    }
}
