package com.csye6225.controller;

import com.csye6225.filter.AuthFilter;
import com.csye6225.model.Attachment;
import com.csye6225.model.Transaction;
import com.csye6225.repository.AttachmentjpaRepository;
import com.csye6225.repository.TransactionJpaRepository;
import com.csye6225.repository.UserJpaRespository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@RestController
@RequestMapping("/transaction/{tid}/attachments")
public class AttachmentController {

    @Autowired
    private AttachmentjpaRepository attachmentjpaRepository;

    @Autowired
    private TransactionJpaRepository transactionJpaRepository;

    @Autowired
    private UserJpaRespository userJpaRespository;

    @GetMapping
    @ResponseBody
    public void getAttachment(HttpServletRequest request, HttpServletResponse response, @PathVariable UUID tid) {
        response.setContentType("application/json");
        String username = AuthFilter.authorizeUser(request, userJpaRespository);
        try {
            if (username != null) {
                if (tid == null || tid.toString().trim().length() == 0) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                }
                else{
                    Transaction transc = transactionJpaRepository.findOne(tid);
                    if (transc.getUser().getUsername().equals(username))
                    {
                        Attachment attachment = transc.getAttachment();
                        ObjectMapper mapper = new ObjectMapper();
                        String jsonString = mapper.writeValueAsString(attachment);
                        response.setStatus(HttpServletResponse.SC_OK);
                        response.getWriter().write(jsonString);
                    }
                    else {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write("UnAuthorized");
                    }

                }
            }
            else{
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("UnAuthorized");
            }
        }
        catch(Exception ex)
        {

        }
    }

    @PostMapping (consumes = { "multipart/form-data" })
    @ResponseBody
    public void addAttachment(HttpServletRequest request, HttpServletResponse response, @PathVariable UUID tid)
    {

    }

    @DeleteMapping(value ="/{aid}")
    @ResponseBody
    public void deleteAttachemnt(HttpServletRequest request, HttpServletResponse response, @PathVariable UUID tid, @PathVariable UUID aid)
    {
        response.setContentType("application/json");
        String username = AuthFilter.authorizeUser(request, userJpaRespository);
        try {
            if (username != null) {
                if (tid == null || tid.toString().trim().length() == 0 || aid==null || aid.toString().trim().length()==0)
                {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("BAD REQUEST");
                }
                else {
                    Transaction transc = transactionJpaRepository.findOne(tid);
                    if (transc.getUser().getUsername().equals(username))
                    {
                        if (transc.getAttachment().getAttachment_id() == aid) {
                            attachmentjpaRepository.delete(aid);
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.getWriter().write("Deleted");
                        }
                        else{
                            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                            response.getWriter().write("No Content");
                        }
                    }
                    else {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write("UnAuthorized");
                    }
                }
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("UnAuthorized");
                }
        }
        catch (Exception ex)
        {

        }

    }

}
