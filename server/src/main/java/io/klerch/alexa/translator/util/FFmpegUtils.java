package io.klerch.alexa.translator.util;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.UUID;

public class FFmpegUtils {

    public static File convertUrlToMp3(final String url) throws IOException {
        final String mp3Filename = UUID.randomUUID().toString() + ".mp3";
        final String escFilename = URLEncoder.encode(url.substring(url.lastIndexOf("/") + 1), "UTF-8");
        final String escUrl = url.substring(0, url.lastIndexOf("/") + 1) + escFilename;
        // build a configuration according to what Alexa expects from an MP3 it supports
        // see: https://developer.amazon.com/public/solutions/alexa/alexa-skills-kit/docs/speech-synthesis-markup-language-ssml-reference#audio
        final FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(escUrl)
                .overrideOutputFiles(true)
                .addOutput(mp3Filename)
                .setAudioCodec("libmp3lame")
                .setAudioChannels(FFmpeg.AUDIO_MONO)
                .setAudioBitRate(FFmpeg.AUDIO_SAMPLE_48000)
                .setAudioSampleRate(FFmpeg.AUDIO_SAMPLE_16000)
                .done();

        // ensure ffmpeg is a valid command on the system that service runs on
        // or at least make sure FFMPEG and FFPROBE environment variables are set to correct path
        new FFmpegExecutor().createJob(builder).run();
        return new File(mp3Filename);
    }

    public static File convertUrlToMp3Cmd(final String url) throws IOException, InterruptedException {
        final String mp3Filename = UUID.randomUUID().toString() + ".mp3";
        final String escFilename = URLEncoder.encode(url.substring(url.lastIndexOf("/") + 1), "UTF-8");
        final String escUrl = url.substring(0, url.lastIndexOf("/") + 1) + escFilename;
        // ffmpeg -i https://s3.amazonaws.com/io.klerch.alexa.translator/en-GB/Marlene/good_morning.mp3 -ac 2 -codec:a libmp3lame -b:a 48k -ar 16000 -af volume=10dB sample.mp3
        final String cmd = "ffmpeg -i " + escUrl + " -ac 2 -codec:a libmp3lame -b:a 48k -ar 16000 -af volume=10dB " + mp3Filename;
        System.out.println(cmd);
        final Process p = Runtime.getRuntime().exec(cmd);
        p.waitFor();

        final BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

        String line = "";
        while ((line = reader.readLine())!= null) {
            System.out.println(line);
        }
        return new File(mp3Filename);
    }
}
