package com.csye6225.aws;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;

public class AwsS3Client {
    static AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();


    public static String uploadImg(String BUCKET_NAME, UUID key_uuid, File file)
    {
        try {
                System.out.println("/n");
                System.out.println("in uploading method");
                s3Client.putObject(BUCKET_NAME, key_uuid.toString(), file);
                URL url = s3Client.getUrl(BUCKET_NAME, key_uuid.toString());
                System.out.println("/n");
                System.out.println("URL - "+url.toString());
                file.delete();
                return url.toString();
            }
        catch (Exception ex) {
            System.out.println("Error in AwsS3Client upload method = " + ex.getMessage());
            return null;
        }
    }

    public static void deleteImg(String BUCKET_NAME,UUID key_uuid) {
        try {
            s3Client.deleteObject(BUCKET_NAME, key_uuid.toString());
        }
        catch(Exception ex) {
            System.out.println("Error in AwsS3Client delete method= " +ex.getMessage());
        }
    }
    public static File convertMultiPartToFile(MultipartFile file) throws IOException {
        System.out.println("/n");
        System.out.println("convertMultiPartToFile");
        File convFile = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        System.out.println("/n");
        System.out.println("returning from convertMultiPartToFile");
        return convFile;
    }
}
