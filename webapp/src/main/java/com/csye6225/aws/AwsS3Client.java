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
    static AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
            .withRegion(Regions.US_EAST_1).build();

    private static final String BUCKET_NAME ="csye6225-fall2018-nigama.me.csye6225.com";

    public static String uploadImg(UUID key_uuid, MultipartFile multipartFile)
    {
        try {
            File file= convertMultiPartToFile(multipartFile);
            s3Client.putObject(new PutObjectRequest(BUCKET_NAME, key_uuid.toString(), file).withCannedAcl(CannedAccessControlList.PublicRead));
            URL url = s3Client.getUrl(BUCKET_NAME, key_uuid.toString());
            return url.toString();
        }
        catch (Exception ex) {
            System.out.println("Error in AwsS3Client upload method = " + ex.getMessage());
            return null;
        }
    }

    public static void deleteImg(UUID key_uuid) {
        try {
            s3Client.deleteObject(BUCKET_NAME, key_uuid.toString());
        }
        catch(Exception ex) {
            System.out.println("Error in AwsS3Client delete method= " +ex.getMessage());
        }
    }
    private static File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }
}
