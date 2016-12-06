package io.klerch.alexa.translator.util;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.options.AudioEncodingOptions;

import java.io.File;
import java.io.IOException;

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
                .addExtraArgs("af volume=10dB")
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
}
