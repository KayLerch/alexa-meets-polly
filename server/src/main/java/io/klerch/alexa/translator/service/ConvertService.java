package io.klerch.alexa.translator.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.amazonaws.util.StringUtils;
import io.klerch.alexa.translator.util.FFmpegUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

@Component
@Path("/convert")
public class ConvertService {
    private static Logger logger = Logger.getLogger(ConvertService.class.getName());

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String convertMp3(@QueryParam("bucket") String bucket, @QueryParam("path") String filePath, @QueryParam("roleArn") String roleArn, @QueryParam("region") String region) {
        Validate.notBlank(bucket, "Bucket must not be null or empty.");
        Validate.notBlank(filePath, "File path must not be null or empty.");
        Validate.isTrue(filePath.toLowerCase().endsWith(".mp3"), "Path does not end with '.mp3'.");

        try {
            final String cleanedPath = filePath.trim().startsWith("/") ? filePath.trim().substring(1) : filePath;
            final String absoluteUrl = String.format("https://s3.amazonaws.com/%1$s/%2$s", bucket, filePath);
            final File mp3File = FFmpegUtils.convertUrlToMp3Cmd(absoluteUrl);
            uploadFileToS3(mp3File, bucket, cleanedPath, region, roleArn);
            // return absolute url to converted file
            return absoluteUrl;
        } catch (final IOException | InterruptedException e) {
            logger.severe(e.getMessage());
        }
        return "";
    }

    public void uploadFileToS3(final File file, final String bucket, final String path, final String region, final String roleArn)  {
        // upload mp3 to S3 bucket
        final PutObjectRequest s3Put = new PutObjectRequest(bucket, path, file).withCannedAcl(CannedAccessControlList.PublicRead);
        getS3Client(region, roleArn).putObject(s3Put);

        if (!file.delete()) {
            logger.warning("Could not delete mp3 temporary audio file.");
        }
    }

    public static AmazonS3 getS3Client(final String region, final String roleArn) {
        final Regions awsRegion = StringUtils.isNullOrEmpty(region) ? Regions.US_EAST_1 : Regions.fromName(region);

        if (StringUtils.isNullOrEmpty(roleArn)) {
            return AmazonS3ClientBuilder.standard().withRegion(awsRegion).build();
        } else {
            final AssumeRoleRequest assumeRole = new AssumeRoleRequest().withRoleArn(roleArn).withRoleSessionName("io-klerch-mp3-converter");

            final AWSSecurityTokenService sts = AWSSecurityTokenServiceClientBuilder.standard().withRegion(awsRegion).build();
            final Credentials credentials = sts.assumeRole(assumeRole).getCredentials();

            final BasicSessionCredentials sessionCredentials = new BasicSessionCredentials(
                    credentials.getAccessKeyId(),
                    credentials.getSecretAccessKey(),
                    credentials.getSessionToken());

            return AmazonS3ClientBuilder.standard().withRegion(awsRegion).withCredentials(new AWSStaticCredentialsProvider(sessionCredentials)).build();
        }
    }
}
