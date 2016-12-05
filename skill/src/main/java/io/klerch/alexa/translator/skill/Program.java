package io.klerch.alexa.translator.skill;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import io.klerch.alexa.translator.skill.util.GoogleTranslation;
import io.klerch.alexa.translator.skill.util.Mp3Converter;
import io.klerch.alexa.translator.skill.util.TTSPolly;
import org.apache.commons.io.IOUtils;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

public class Program {
    public static void main(String[] args) {
            /*final String locale = "de-DE";
            final String term = "Wasser";
            final String language = "niederländisch";

            final String translated = new GoogleTranslation(locale).translate(term, language);

            System.out.println(translated);

            final InputStream tts = new TTSPolly(locale).textToSpeech(translated, language);
            final AmazonS3Client s3Client = new AmazonS3Client();

            final String filePath = term + "_" + translated + ".mp3";
            final PutObjectRequest s3Put = new PutObjectRequest(SkillConfig.getS3BucketName(), filePath, tts, new ObjectMetadata()).withCannedAcl(CannedAccessControlList.PublicRead);
            s3Client.putObject(s3Put);

            final String mp3Url = SkillConfig.getS3BucketUrl() + filePath;

            try {
                    System.out.println(Mp3Converter.convertMp3(mp3Url));
            } catch (URISyntaxException e) {
                    e.printStackTrace();
            } catch (IOException e) {
                    e.printStackTrace();
            }*/
    }
}
