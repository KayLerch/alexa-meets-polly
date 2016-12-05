# Alexa meets AWS Polly
This project demonstrates an integration of AWS Polly into an Alexa skill which translates words into different languages.
Polly is Amazon's new text-to-speech cloud service and is a perfect fit for Alexa skills aiming for playing back foreign voice.

This project combines the Alexa Skills Kit, AWS Polly and Google Translate API to translate common phrases into
17 foreign languages. The code will improve and is experimental in the current state.

![](docs/solution-architecture.png)

1. User speaks to an Alexa device and asks for e.g. _"What does Hello mean in Polish?"_

2. The Alexa services maps the utterance to the Translate-intent and passes in a language-slot with
value _Polish_ and a term-slot having the value _Hello_. A Lambda function whose code is contained in this
Repo catches the Speechlet.

3. Firstly, the skill implementation lets the term _Hello_ translate to polish by leveraging
Google Translate API.

4. The resulting translation is then passed to AWS Polly giving it the desired VoiceId. Polly returns
an MP3 stream with the spoken translated term.

5. The MP3 is stored in an S3 bucket but unfortunately it is not ready yet for playback in Alexa.

6. Therefore, the MP3-Url is given to a custom service-endpoint hosted in EC2. This service
converts the MP3 into a format supported by Alexa with help of FFMPEG.

7. The resulting MP3 overwrites its origin in S3.

8. The Url of that file - which did not change at all - is given back to the calling skill code in Lambda.

9. The skill creates the output speech text and squeezes in an audio-SSML tag with the MP3-url.

10. Output speech is returned to the device. Alexa replies and plays back the translated term with one of
Polly's voices.

