package io.klerch.alexa.translator.util;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.logging.Logger;

@Component("s3Utils")
public class S3Utils {
    private static Logger logger = Logger.getLogger(S3Utils.class.getName());

    private final AmazonS3Client s3Client;

    @Value("${my.bucket}")
    private String bucket;

    @Value("${my.bucketUrl}")
    private String bucketUrl;

    public S3Utils() {
        s3Client = new AmazonS3Client();
    }

    public void uploadFileToS3(final File file, final String mp3Url) throws UnsupportedEncodingException {
        // extract relative path to file from absolute url
        final String filePath = mp3Url.substring(bucketUrl.length());
        System.out.println(filePath);
        System.out.println(file.getAbsoluteFile());
        // upload mp3 to S3 bucket
        final PutObjectRequest s3Put = new PutObjectRequest(bucket, filePath, file).withCannedAcl(CannedAccessControlList.PublicRead);
        s3Client.putObject(s3Put);

        if (!file.delete()) {
            logger.warning("Could not delete mp3 temporary audio file.");
        }
    }
}
