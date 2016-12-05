package io.klerch.alexa.translator.util;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
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

    public boolean isFileAlreadyExisting(final String fileKey) {
        return s3Client.doesObjectExist(bucket, fileKey);
    }

    String getS3Url(final String fileKey) {
        return bucketUrl + fileKey;
    }

    public String uploadFileToS3(final File file, final String filePath) {
        // upload mp3 to S3 bucket
        final PutObjectRequest s3Put = new PutObjectRequest(bucket, filePath, file).withCannedAcl(CannedAccessControlList.PublicRead);
        s3Client.putObject(s3Put);

        if (!file.delete()) {
            logger.warning("Could not delete mp3 temporary audio file.");
        }

        // return public url of mp3 in bucket
        return bucketUrl + filePath;
    }
}
