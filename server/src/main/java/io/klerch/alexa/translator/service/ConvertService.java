package io.klerch.alexa.translator.service;

import io.klerch.alexa.translator.util.Mp3Utils;
import io.klerch.alexa.translator.util.S3Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;

@Component
@Path("/convert")
public class ConvertService {
    @Autowired
    private S3Utils s3Utils;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String convertMp3(@QueryParam("mp3") String mp3Url) {
        try {
            final File mp3File = Mp3Utils.convertUrlToMp3Cmd(mp3Url);
            s3Utils.uploadFileToS3(mp3File, mp3Url);
            // return same url as input to indicate success to caller
            return mp3Url;
        } catch (final IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "";
    }
}
