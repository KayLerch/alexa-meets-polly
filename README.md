# Alexa meets AWS Polly

This project demonstrates an integration of [AWS Polly](https://aws.amazon.com/polly/) into an Alexa skill which translates phrases into different languages.
Polly is Amazon's new text-to-speech cloud service and is a perfect fit for Alexa skills aiming for playing back foreign voice.

This project combines the [Alexa Skills Kit](https://developer.amazon.com/alexa-skills-kit), [AWS Polly](https://aws.amazon.com/polly/) and a Translator API to translate common phrases into
17 different languages.

__Important note__
Polly now provides the dynamic range compression SSML-tag and aligned bitrates of audiostreams. This removes the burden of manually converting Polly-mp3 using ffmpeg in order to
comply with audio setting and volume requirements of Alexa. That being said, step 7 to 9 aren't necessary anymore.

![](docs/solution-architecture.png)

1. User speaks to an Alexa device and asks for e.g. _"What is "Good Morning" in Polish?"_

2. NLU of Alexa triggers the Translate-intent and passes in a language-slot with
value _Polish_ and a term-slot having the value _Good Morning_. An [AWS Lambda](https://aws.amazon.com/lambda) function whose code is contained in this
Repo implements a Speechlet that handles the request and returns the translation.

3. Before this skill uses the translation API and TTS service of Polly, it first looks into its own dictionary where all the
previous translations are stored. If it finds a record for _Good Morning_ in Polish in the database it will skip the
 entire round-trip (step 4 to 9) and uses the S3 audio-file referenced in the Dynamo record (learn how it got there in step 10.)

4. However, if _Good Morning_ in Polish has never been translated before the skill requests _Good Morning_ 
 in Polish from Microsoft Translator API (or interchangeably from Google Translate).

5. The returned translation is then passed to AWS Polly. Polly responds with 
an MP3 bitstream with the spoken translation.

6. The stream is persisted in [AWS S3](https://aws.amazon.com/s3) as an mp3-file. 

7.-9. _No custom conversion of Polly-mp3 necessary anymore as it's now aligned to Alexa requirements._

10. Finally, a record is created for _Good Morning_ in Polish in the Dynamo dictionary. Another record that references the new
dictionary entry is created for the user so Alexa keeps in mind the last translation. This is how a user can
request Alexa to repeat the most recent translation.

11. The skill creates the output-speech text and squeezes in an audio-SSML tag with the mp3-url.

12. Output-speech is returned to the Alexa device. Alexa speaks and plays back the translated text with one of
Polly's voices. A card is returned to the Alexa app providing the written translation.



