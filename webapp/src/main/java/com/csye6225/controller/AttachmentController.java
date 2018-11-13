package com.csye6225.controller;

import com.csye6225.aws.AwsS3Client;
import com.csye6225.filter.AuthFilter;
import com.csye6225.model.Attachment;
import com.csye6225.model.Response;
import com.csye6225.model.Transaction;
import com.csye6225.repository.AttachmentjpaRepository;
import com.csye6225.repository.TransactionJpaRepository;
import com.csye6225.repository.UserJpaRespository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.timgroup.statsd.StatsDClient;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
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

    @Autowired
    private Environment env;

    @Autowired
    private StatsDClient statsd;



    private String response = null;

    @GetMapping
    @ResponseBody
    public void getAttachment(HttpServletRequest request, HttpServletResponse response, @PathVariable UUID tid) {
        statsd.incrementCounter("endpoint.attachment.http.get");
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
                        this.response = Response.jsonString("UnAuthorized");
                        response.getWriter().write(this.response);
                    }

                }
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                this.response = Response.jsonString("UnAuthorized");
                response.getWriter().write(this.response);
            }
        } catch (Exception ex) {

        }
    }

    @PostMapping(consumes = {"multipart/form-data"})
    @ResponseBody
    public void addAttachment(HttpServletRequest request, HttpServletResponse response, @PathVariable UUID tid, @RequestPart("file") MultipartFile multipartFile) {
        statsd.incrementCounter("endpoint.attachment.http.post");
        response.setContentType("application/json");
        String username = AuthFilter.authorizeUser(request, userJpaRespository);
        System.out.println(multipartFile.getContentType());
        try {
            String url = null;
            String BUCKET_NAME =env.getProperty("bucketName");
            if (username != null) {
                if (tid != null || tid.toString().trim().length() != 0) {
                    Transaction transc = transactionJpaRepository.findOne(tid);
                    if (transc != null && transc.getUser().getUsername().equals(username)) {

                        if (transc.getAttachment() == null) {
                            String fileExtension = getFileExtension(multipartFile);
                            if (fileExtension.equalsIgnoreCase("jpeg") || fileExtension.equalsIgnoreCase("jpg") || fileExtension.equalsIgnoreCase("png")) {
                                UUID key_uuid = UUID.randomUUID();
                                String profile = env.getProperty("profile");
                                Attachment attachment = new Attachment();
                                attachment.setAttachment_id(key_uuid);
                                File file =  AwsS3Client.convertMultiPartToFile(multipartFile,profile);
                                if (profile.equals("dev")) {
                                     url = AwsS3Client.uploadImg(BUCKET_NAME,key_uuid, file);
                                }
                                else
                                {
                                    url = file.getAbsolutePath();
                                }
                                if (url != null){
                                attachment.setUrl(url);
                                attachmentjpaRepository.save(attachment);
                                transc.setAttachment(attachment);
                                transactionJpaRepository.save(transc);
                                    response.setStatus(HttpServletResponse.SC_OK);
                                    this.response = Response.jsonString("Attachment uploaded");
                                    response.getWriter().write(this.response);
                                }
                                else {
                                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                                    this.response = Response.jsonString("Issue with AWS CLient");
                                    response.getWriter().write(this.response);

                                }

                            } else {
                                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                                this.response = Response.jsonString("File type not supported");
                                response.getWriter().write(this.response);
                            }

                        } else {
                            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            this.response = Response.jsonString("Attachment already exist, please UPDATE transaction");
                            response.getWriter().write(this.response);
                        }
                    } else {
                        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                        this.response = Response.jsonString("No Content");
                        response.getWriter().write(this.response);
                    }

                } else {
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    this.response = Response.jsonString("No Content");
                    response.getWriter().write(this.response);
                }

            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                this.response = Response.jsonString("UnAuthorized");
                response.getWriter().write(this.response);
            }

        } catch (Exception ex) {
            System.out.println("Exception" + ex.getMessage());

        }

    }

    @DeleteMapping(value = "/{aid}")
    @ResponseBody
    public void deleteAttachemnt(HttpServletRequest request, HttpServletResponse response, @PathVariable UUID tid, @PathVariable UUID aid) {
        statsd.incrementCounter("endpoint.attachment.http.delete");
        response.setContentType("application/json");
        String username = AuthFilter.authorizeUser(request, userJpaRespository);
        try {
            String BUCKET_NAME =env.getProperty("bucketName");
            if (username != null) {
                if (tid == null || tid.toString().trim().length() == 0 || aid == null || aid.toString().trim().length() == 0) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    this.response = Response.jsonString("Bad Request");
                    response.getWriter().write(this.response);
                } else {
                    Transaction transc = transactionJpaRepository.findOne(tid);
                    if (transc.getUser().getUsername().equals(username)) {
                        if (transc.getAttachment() != null) {
                            if (transc.getAttachment().getAttachment_id().equals(aid)) {
                                String url = transc.getAttachment().getUrl();
                                transc.setAttachment(null);
                                attachmentjpaRepository.delete(aid);
                                if (env.getProperty("profile").equals("dev")) {
                                    AwsS3Client.deleteImg(BUCKET_NAME, aid);
                                }
                                else {
                                    url.replace("/","//");
                                    File file = new File(url);
                                    file.delete();
                                }
                                response.setStatus(HttpServletResponse.SC_OK);
                                this.response = Response.jsonString("Attachment Deleted");
                                response.getWriter().write(this.response);
                            } else {
                                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                                this.response = Response.jsonString("No Content");
                                response.getWriter().write(this.response);
                            }
                        } else {
                            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                            this.response = Response.jsonString("No Content");
                            response.getWriter().write(this.response);
                        }
                    } else {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        this.response = Response.jsonString("UnAuthorized");
                        response.getWriter().write(this.response);
                    }
                }
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                this.response = Response.jsonString("UnAuthorized");
                response.getWriter().write(this.response);
            }
        } catch (Exception ex) {
            System.out.println("Exception is" + ex.getMessage());
        }

    }

    @PutMapping(value = "/{aid}")
    @ResponseBody
    public void updateAttachment(HttpServletRequest request, HttpServletResponse response, @PathVariable UUID tid, @PathVariable UUID aid, @RequestPart("file") MultipartFile multipartFile) {
        statsd.incrementCounter("endpoint.attachment.http.put");
        response.setContentType("application/json");
        String username = AuthFilter.authorizeUser(request, userJpaRespository);
        try {
            String BUCKET_NAME =env.getProperty("bucketName");
            String url;
            if (username != null) {
                if (tid == null || tid.toString().trim().length() == 0 || aid == null || aid.toString().trim().length() == 0) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    this.response = Response.jsonString("Bad Request");
                    response.getWriter().write(this.response);
                } else {
                    if (tid != null || tid.toString().trim().length() != 0) {
                        Transaction transc = transactionJpaRepository.findOne(tid);
                        if (transc != null && transc.getUser().getUsername().equals(username)) {
                            String fileExtension = getFileExtension(multipartFile);
                            if (fileExtension.equalsIgnoreCase("jpeg") || fileExtension.equalsIgnoreCase("jpg") || fileExtension.equalsIgnoreCase("png")) {
                                Attachment attachment = transc.getAttachment();
                                String profile = env.getProperty("profile");
                                File file =  AwsS3Client.convertMultiPartToFile(multipartFile,profile);
                                if(profile.equals("dev")) {
                                     url = AwsS3Client.uploadImg(BUCKET_NAME,attachment.getAttachment_id(), file);
                                }
                                else {
                                    url = file.getPath();
                                }
                                if(url != null) {
                                    attachment.setUrl(url.toString());
                                    transc.setAttachment(attachment);
                                    attachmentjpaRepository.save(attachment);
                                    response.setStatus(HttpServletResponse.SC_OK);
                                    this.response = Response.jsonString("Attachment updated");
                                    response.getWriter().write(this.response);
                                }
                                else{
                                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                                        this.response = Response.jsonString("Issue with AWS CLient");
                                        response.getWriter().write(this.response);
                                }
                            } else {
                                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                                this.response = Response.jsonString("File type not supported");
                                response.getWriter().write(this.response);
                            }
                        } else {
                            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                            this.response = Response.jsonString("No Content");
                            response.getWriter().write(this.response);
                        }

                    } else {
                        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                        this.response = Response.jsonString("No Content");
                        response.getWriter().write(this.response);
                    }
                }
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                this.response = Response.jsonString("UnAuthorized");
                response.getWriter().write(this.response);
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
