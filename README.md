# Alexa meets AWS Polly

This project demonstrates an integration of [AWS Polly](https://aws.amazon.com/polly/) into an Alexa skill which translates words into different languages.
Polly is Amazon's new text-to-speech cloud service and is a perfect fit for Alexa skills aiming for playing back foreign voice.

This project combines the [Alexa Skills Kit](https://developer.amazon.com/alexa-skills-kit), [AWS Polly](https://aws.amazon.com/polly/) and a Translator API to translate common phrases into
17 foreign languages. The code will improve and is experimental in the current state.

![](docs/solution-architecture.png)

1. User speaks to an Alexa device and asks for e.g. _"What does "Good Morning" mean in Polish?"_

2. The Alexa services maps the utterance to the Translate-intent and passes in a language-slot with
value _Polish_ and a term-slot having the value _Good Morning_. An [AWS Lambda](https://aws.amazon.com/lambda) function whose code is contained in this
Repo catches the Speechlet.

3. Before going in the process of translating the text this skill first looks into its dictionary where all the
previous translations are stored. If it finds a record for _Good Morning_ in Polish in the database it skips the
 whole round-trip (step 4 to 9) and instantly uses the S3 audio-file refered to in the Dynamo record.

4. However, if _Good Morning_ wasn't requested in Polish before the skill implementation lets the term _Good Morning_ 
translate to Polish by leveraging Microsoft Translator API (or interchangeably with Google Translate).

5. The resulting translation is then passed to AWS Polly giving it the desired VoiceId. Polly returns
an MP3 stream with the spoken translated term.

6. The stream is persisted in [AWS S3](https://aws.amazon.com/s3) as an MP3 file. Unfortunately it is not ready yet for being returned and played back in Alexa
due to differing audio settings required by SSML audio in Alexa.

7. Therefore, the MP3-Url is given to a custom service-endpoint hosted in an [AWS EC2](https://aws.amazon.com/ec2) server instance. This service converts the bit-rate 
to 48Khz as required by Alexa with help of FFMPEG. Polly's voices are not as loud as Alexa's voice even if raising the volume with their prosody-setting to max. That's why
this conversion also increases volume by 10dB. 

 ```bash
ffmpeg -i https://s3.amazonaws.com/path-to-source.mp3 -ac 2 -codec:a libmp3lame -b:a 48k -ar 16000 -af volume=10dB output.mp3
 ```

 This is the actual [FFmpeg](https://ffmpeg.org/) command that converts the file in S3. The output file is saved to local disc in order to
 immediately upload and replace it with the source file in S3.

8. The resulting MP3 overwrites its origin in S3.

9. The Url of that file - which did not change at all - is given back to the calling speechlet handler in Lambda.

10. Finally, a record is created for _Good Morning_ in Polish in the Dynamo dictionary. A separate record which refers to that
dictionary entry is created for the user so Alexa keeps in mind the last translation made for a user. This is how a user can
request Alexa for repeating the most recent translation.

11. The skill creates the output speech text and squeezes in an audio-SSML tag with the MP3-url.

12. Output speech is returned to the device. Alexa replies and plays back the translated term with one of
Polly's voices. Moreover, a card is returned to the Alexa app providing the written translation which might be very
useful to users.



