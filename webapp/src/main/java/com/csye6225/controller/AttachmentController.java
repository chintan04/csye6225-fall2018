package com.csye6225.controller;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.csye6225.aws.AwsS3Client;
import com.csye6225.filter.AuthFilter;
import com.csye6225.model.Attachment;
import com.csye6225.model.Transaction;
import com.csye6225.repository.AttachmentjpaRepository;
import com.csye6225.repository.TransactionJpaRepository;
import com.csye6225.repository.UserJpaRespository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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

    private final String BUCKET_NAME = "csye6225-fall2018-nigama.me.csye6225.com";

    @GetMapping
    @ResponseBody
    public void getAttachment(HttpServletRequest request, HttpServletResponse response, @PathVariable UUID tid) {
        response.setContentType("application/json");
        String username = AuthFilter.authorizeUser(request, userJpaRespository);
        try {
            if (username != null) {
                if (tid == null || tid.toString().trim().length() == 0) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                    Transaction transc = transactionJpaRepository.findOne(tid);
                    if (transc.getUser().getUsername().equals(username)) {
                        Attachment attachment = transc.getAttachment();
                        ObjectMapper mapper = new ObjectMapper();
                        String jsonString = mapper.writeValueAsString(attachment);
                        response.setStatus(HttpServletResponse.SC_OK);
                        response.getWriter().write(jsonString);
                    } else {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write("UnAuthorized");
                    }

                }
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("UnAuthorized");
            }
        } catch (Exception ex) {

        }
    }

    @PostMapping(consumes = {"multipart/form-data"})
    @ResponseBody
    public void addAttachment(HttpServletRequest request, HttpServletResponse response, @PathVariable UUID tid, @RequestPart("file") MultipartFile multipartFile) {
        response.setContentType("application/json");
        String username = AuthFilter.authorizeUser(request, userJpaRespository);
        System.out.println(multipartFile.getContentType());
        try {
            if (username != null) {
                if (tid != null || tid.toString().trim().length() != 0) {
                    Transaction transc = transactionJpaRepository.findOne(tid);
                    if (transc != null && transc.getUser().getUsername().equals(username)) {

                        if (transc.getAttachment() == null) {
                            String fileExtension = getFileExtension(multipartFile);
                            if (fileExtension.equalsIgnoreCase("jpeg") || fileExtension.equalsIgnoreCase("jpg") || fileExtension.equalsIgnoreCase("png")) {
                                UUID key_uuid = UUID.randomUUID();
                                Attachment attachment = new Attachment();
                                attachment.setAttachment_id(key_uuid);
                                String url = AwsS3Client.uploadImg(key_uuid, multipartFile);
                                attachment.setUrl(url.toString());
                                attachmentjpaRepository.save(attachment);
                                transc.setAttachment(attachment);
                                transactionJpaRepository.save(transc);
                            } else {
                                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                                response.getWriter().write("File type not supported");
                            }

                        } else {
                            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            response.getWriter().write("Attachment already exist, please UPDATE transaction");
                        }
                    } else {
                        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                        response.getWriter().write("No Content");
                    }

                } else {
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    response.getWriter().write("No Content");
                }

            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("UnAuthorized");
            }

        } catch (Exception ex) {
            System.out.println("Exception" + ex.getMessage());

        }

    }

    @DeleteMapping(value = "/{aid}")
    @ResponseBody
    public void deleteAttachemnt(HttpServletRequest request, HttpServletResponse response, @PathVariable UUID tid, @PathVariable UUID aid) {
        response.setContentType("application/json");
        String username = AuthFilter.authorizeUser(request, userJpaRespository);
        try {
            if (username != null) {
                if (tid == null || tid.toString().trim().length() == 0 || aid == null || aid.toString().trim().length() == 0) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("BAD REQUEST");
                } else {
                    Transaction transc = transactionJpaRepository.findOne(tid);
                    if (transc.getUser().getUsername().equals(username)) {
                        if (transc.getAttachment() != null) {
                            if (transc.getAttachment().getAttachment_id().equals(aid)) {
                                transc.setAttachment(null);
                                attachmentjpaRepository.delete(aid);
                                AwsS3Client.deleteImg(aid);
                                response.setStatus(HttpServletResponse.SC_OK);
                                response.getWriter().write("Delete successful");
                            } else {
                                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                                response.getWriter().write("No Content");
                            }
                        } else {
                            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                            response.getWriter().write("No Content");
                        }
                    } else {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write("UnAuthorized");
                    }
                }
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("UnAuthorized");
            }
        } catch (Exception ex) {
            System.out.println("Exception is" + ex.getMessage());
        }

    }

    @PutMapping(value = "/{aid}")
    @ResponseBody
    public void updateAttachment(HttpServletRequest request, HttpServletResponse response, @PathVariable UUID tid, @PathVariable UUID aid, @RequestPart("file") MultipartFile multipartFile) {
        response.setContentType("application/json");
        String username = AuthFilter.authorizeUser(request, userJpaRespository);
        try {
            if (username != null) {
                if (tid == null || tid.toString().trim().length() == 0 || aid == null || aid.toString().trim().length() == 0) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("BAD REQUEST");
                } else {
                    if (tid != null || tid.toString().trim().length() != 0) {
                        Transaction transc = transactionJpaRepository.findOne(tid);
                        if (transc != null && transc.getUser().getUsername().equals(username)) {
                            String fileExtension = getFileExtension(multipartFile);
                            if (fileExtension.equalsIgnoreCase("jpeg") || fileExtension.equalsIgnoreCase("jpg") || fileExtension.equalsIgnoreCase("png")) {
                                Attachment attachment = transc.getAttachment();
                                String url = AwsS3Client.uploadImg(attachment.getAttachment_id(), multipartFile);
                                attachment.setUrl(url.toString());
                                transc.setAttachment(attachment);
                                attachmentjpaRepository.save(attachment);
                            } else {
                                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                                response.getWriter().write("File type not supported");
                            }
                        } else {
                            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                            response.getWriter().write("No Content");
                        }

                    } else {
                        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                        response.getWriter().write("No Content");
                    }
                }
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("UnAuthorized");
            }
        } catch (Exception ex) {
            System.out.println("Exception is" + ex.getMessage());
        }

    }

    private static String getFileExtension(MultipartFile file) {
        String extension = "";

        try {
            if (file != null) {
                String name = file.getOriginalFilename();
                extension = name.substring(name.lastIndexOf(".") + 1);
            }
        } catch (Exception e) {
            extension = "";
        }

        return extension;

    }


}
