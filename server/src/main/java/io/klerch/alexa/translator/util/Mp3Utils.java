package io.klerch.alexa.translator.util;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.info.Codec;
import net.bramp.ffmpeg.info.Format;
import net.bramp.ffmpeg.options.AudioEncodingOptions;
import net.bramp.ffmpeg.options.EncodingOptions;
import net.bramp.ffmpeg.options.MainEncodingOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Mp3Utils {
    private static String AUDIO_MP3_CODEC = "libmp3lame";

    public static File convertWaveToMp3(final File wavFile, final String mp3Filename) throws IOException {
        return convertUrlToMp3(wavFile.getAbsolutePath(), mp3Filename);
    }

    public static File convertUrlToMp3(final String url, final String mp3Filename) throws IOException {
        // will read out path to executables from environment variables FFMPEG and FFPROBE
        // take care of those variables being set in your system
        final FFmpeg ffmpeg = new FFmpeg();
        final FFprobe ffprobe = new FFprobe();
        // build a configuration according to what Alexa expects from an MP3 it supports
        // see: https://developer.amazon.com/public/solutions/alexa/alexa-skills-kit/docs/speech-synthesis-markup-language-ssml-reference#audio
        final FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(url)
                .overrideOutputFiles(true)
                .addOutput(mp3Filename)
                .addExtraArgs("-af volume=15dB")
                .setAudioCodec(AUDIO_MP3_CODEC)
                .setAudioChannels(FFmpeg.AUDIO_MONO)
                .setAudioBitRate(FFmpeg.AUDIO_SAMPLE_48000)
                .setAudioSampleRate(FFmpeg.AUDIO_SAMPLE_16000)
                .done();

        final FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        // Run a one-pass encode
        executor.createJob(builder).run();
        return new File(mp3Filename);
    }

    public static File convertUrlToMp3Manual(final String url, final String mp3Filename) throws IOException, InterruptedException {
        final Process p = Runtime.getRuntime().exec("ffmpeg -i " + url + " -ac 2 -codec:a libmp3lame -b:a 48k -ar 16000 -af volume=20dB " + mp3Filename);
        p.waitFor();

        final BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

        String line = "";
        while ((line = reader.readLine())!= null) {
            System.out.println(line);
        }
        return new File(mp3Filename);
    }
}
