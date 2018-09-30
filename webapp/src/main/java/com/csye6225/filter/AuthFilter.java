package com.csye6225.filter;

import com.csye6225.model.ErrorResponse;
import com.csye6225.model.Users;
import com.csye6225.repository.UserJpaRespository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

@WebFilter(urlPatterns = {"/*"})
public class AuthFilter implements Filter {

    @Autowired
    private UserJpaRespository userJpaRespository;

    private FilterConfig config = null;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.config = config;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

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
                        filterChain.doFilter(request, response);
                    }
                }
            }
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setCode(401);
            errorResponse.setMessage("Authentication Failed");

            byte[] responseToSend = restResponseBytes(errorResponse);
            ((HttpServletResponse) response).setHeader("Content-Type", "application/json");
            ((HttpServletResponse) response).setStatus(401);
            response.getOutputStream().write(responseToSend);
            return;
        }
        else {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setCode(401);
            errorResponse.setMessage("Authentication Failed");

            byte[] responseToSend = restResponseBytes(errorResponse);
            ((HttpServletResponse) response).setHeader("Content-Type", "application/json");
            ((HttpServletResponse) response).setStatus(401);
            response.getOutputStream().write(responseToSend);
            return;
        }

    }

    @Override
    public void destroy() {

    }

    private byte[] restResponseBytes(ErrorResponse eErrorResponse) throws IOException {
        String serialized = new ObjectMapper().writeValueAsString(eErrorResponse);
        return serialized.getBytes();
    }

    /*public static String authorizeUser(HttpServletRequest request,UserJpaRespository userJpaRespository)
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
    }*/
}
