package com.csye6225.controller;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.csye6225.filter.AuthFilter;
import com.csye6225.model.Attachment;
import com.csye6225.model.Response;
import com.csye6225.model.Transaction;
import com.csye6225.repository.AttachmentjpaRepository;
import com.csye6225.repository.TransactionJpaRepository;
import com.csye6225.repository.UserJpaRespository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.*;
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

    @Autowired
    private Environment env;


    private String response = null;
    AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).build();

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
        response.setContentType("application/json");
        String username = AuthFilter.authorizeUser(request, userJpaRespository);
        System.out.println(multipartFile.getContentType());
        try {
            System.out.println("in try");
            String url = null;
            String BUCKET_NAME = env.getProperty("bucketName");
            if (username != null) {
                System.out.println("first if");
                if (tid != null || tid.toString().trim().length() != 0) {
                    System.out.println("second if");
                    Transaction transc = transactionJpaRepository.findOne(tid);
                    if (transc != null && transc.getUser().getUsername().equals(username)) {
                        System.out.println("third if");
                        if (transc.getAttachment() == null) {
                            System.out.println("fourth if");
                            String fileExtension = getFileExtension(multipartFile);
                            if (fileExtension.equalsIgnoreCase("jpeg") || fileExtension.equalsIgnoreCase("jpg") || fileExtension.equalsIgnoreCase("png")) {
                                System.out.println("fifth if");
                                UUID key_uuid = UUID.randomUUID();
                                System.out.println(key_uuid);
                                Attachment attachment = new Attachment();
                                System.out.println("below new attachment");
                                attachment.setAttachment_id(key_uuid);
                                System.out.println("below set attachment");
                                //File file = convertMultiPartToFile(multipartFile);
                                System.out.println("inside convertMultiPartToFile");


                                File file = new File(System.getProperty("java.io.tmpdir")+"/"+multipartFile.getOriginalFilename());
                                System.out.println("inside convertMultiPartToFile1");
                                FileOutputStream fos = new FileOutputStream(file);
                                System.out.println("inside convertMultiPartToFile2");
                                fos.write(multipartFile.getBytes());
                                System.out.println("inside convertMultiPartToFile3");
                                fos.close();
                                System.out.println("inside convertMultiPartToFile2");
                                //multipartFile.transferTo(file);
                                System.out.println("inside convertMultiPartToFile4");


                                System.out.println("ENV - "+env.getProperty("profile"));
                                
                                if (env.getProperty("profile").equals("dev")) {
                                    System.out.println("sixth if");
                                    url = uploadImg(BUCKET_NAME, key_uuid, file);
                                } else {
                                    System.out.println("inside else");
                                    url = file.getAbsolutePath();
                                }
                                if (url != null) {
                                    attachment.setUrl(url);
                                    attachmentjpaRepository.save(attachment);
                                    transc.setAttachment(attachment);
                                    transactionJpaRepository.save(transc);
                                    response.setStatus(HttpServletResponse.SC_OK);
                                    this.response = Response.jsonString("Attachment uploaded");
                                    response.getWriter().write(this.response);
                                } else {
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
            try {
                ex.printStackTrace();
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                ex.printStackTrace(pw);
                String sStackTrace = sw.toString(); // stack trace as a string
                System.out.println();
                System.out.println("StackTrace " + sStackTrace);
                System.out.println();
                this.response = Response.jsonString(sStackTrace);
                response.getWriter().write(this.response);
                System.out.println("bucket - " + env.getProperty("bucketName"));
                System.out.println("ENV - " + env.getProperty("profile"));
                System.out.println("Exception" + ex.getMessage());
            }
            catch(Exception ex1) {}


        }

    }

    @DeleteMapping(value = "/{aid}")
    @ResponseBody
    public void deleteAttachemnt(HttpServletRequest request, HttpServletResponse response, @PathVariable UUID tid, @PathVariable UUID aid) {
        response.setContentType("application/json");
        String username = AuthFilter.authorizeUser(request, userJpaRespository);
        try {
            String BUCKET_NAME = env.getProperty("bucketName");
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
                                    deleteImg(BUCKET_NAME, aid);
                                } else {
                                    url.replace("/", "//");
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
        response.setContentType("application/json");
        String username = AuthFilter.authorizeUser(request, userJpaRespository);
        try {
            String BUCKET_NAME = env.getProperty("bucketName");
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
                                if (transc.getAttachment() != null) {
                                    if (transc.getAttachment().getAttachment_id().equals(aid)) {
                                        File file = convertMultiPartToFile(multipartFile);
                                        if (env.getProperty("profile").equals("dev")) {
                                            url = uploadImg(BUCKET_NAME, attachment.getAttachment_id(), file);
                                        } else {
                                            url = attachment.getUrl();
                                            url.replace("/", "//");
                                            File file1 = new File(url);
                                            file1.delete();
                                            url = file.getAbsolutePath();
                                        }
                                        if (url != null) {
                                            attachment.setUrl(url.toString());
                                            transc.setAttachment(attachment);
                                            attachmentjpaRepository.save(attachment);
                                            response.setStatus(HttpServletResponse.SC_OK);
                                            this.response = Response.jsonString("Attachment updated");
                                            response.getWriter().write(this.response);
                                        } else {
                                            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                                            this.response = Response.jsonString("Issue with AWS CLient");
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
    public String uploadImg(String BUCKET_NAME, UUID key_uuid, File file)
    {
        try {
            System.out.println("1");
            s3Client.putObject(new PutObjectRequest(BUCKET_NAME, key_uuid.toString(), file).withCannedAcl(CannedAccessControlList.PublicRead));
            System.out.println("2");
            //s3Client.putObject(BUCKET_NAME, key_uuid.toString(), file);
            URL url = s3Client.getUrl(BUCKET_NAME, key_uuid.toString());
            System.out.println("3");
            file.delete();
            System.out.println("4");
            return url.toString();
        }
        catch (Exception ex) {
            System.out.println("Error in AwsS3Client upload method = " + ex.getMessage());
            return null;
        }
    }

    public void deleteImg(String BUCKET_NAME,UUID key_uuid) {
        try {
            s3Client.deleteObject(BUCKET_NAME, key_uuid.toString());
        }
        catch(Exception ex) {
            System.out.println("Error in AwsS3Client delete method= " +ex.getMessage());
        }
    }
    public File convertMultiPartToFile(MultipartFile file) throws IOException {
        System.out.println("inside convertMultiPartToFile");
        File convFile = new File(file.getOriginalFilename());
        convFile.createNewFile();
        System.out.println("inside convertMultiPartToFile1");
        FileOutputStream fos = new FileOutputStream(convFile);
        System.out.println("inside convertMultiPartToFile2");
        fos.write(file.getBytes());
        System.out.println("inside convertMultiPartToFile3");
        fos.close();
        System.out.println("inside convertMultiPartToFile2");
        //file.transferTo(convFile);
        System.out.println("inside convertMultiPartToFile4");
        return convFile;
    }


}
