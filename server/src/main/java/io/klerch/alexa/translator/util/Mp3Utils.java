package io.klerch.alexa.translator.util;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Mp3Utils {
    private static String AUDIO_MP3_CODEC = "libmp3lame";

    public static File convertUrlToMp3(final String url) throws IOException {
        final String mp3Filename = StringEscapeUtils.escapeHtml4(url);
        // build a configuration according to what Alexa expects from an MP3 it supports
        // see: https://developer.amazon.com/public/solutions/alexa/alexa-skills-kit/docs/speech-synthesis-markup-language-ssml-reference#audio
        final FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(url)
                .overrideOutputFiles(true)
                .addOutput(mp3Filename)
                .setAudioCodec(AUDIO_MP3_CODEC)
                .setAudioChannels(FFmpeg.AUDIO_MONO)
                .setAudioBitRate(FFmpeg.AUDIO_SAMPLE_48000)
                .setAudioSampleRate(FFmpeg.AUDIO_SAMPLE_16000)
                .done();

        // ensure ffmpeg is a valid command on the system that service runs on
        // or at least make sure FFMPEG and FFPROBE environment variables are set to correct path
        new FFmpegExecutor().createJob(builder).run();
        return new File(mp3Filename);
    }

    public static File convertUrlToMp3Cmd(final String url, final String mp3Filename) throws IOException, InterruptedException {
        final Process p = Runtime.getRuntime().exec("ffmpeg -i " + url + " -ac 2 -codec:a libmp3lame -b:a 48k -ar 16000 " + mp3Filename);
        p.waitFor();

        final BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

        String line = "";
        while ((line = reader.readLine())!= null) {
            System.out.println(line);
        }
        return new File(mp3Filename);
    }
}
