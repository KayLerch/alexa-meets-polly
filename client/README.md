# Lambda Test Client

This code is an implementation of a Lambda function whose duty it is
to periodically invoke the skill Lambda function. It is scheduled
by a CloudWatch rule every for five minutes. There are two reasons for
this:

1. This Lambda function validates the response of the Translator skill to
ensure it is working properly. The test requests a translation and expects
to see a specific value string in the skill's response.

2. The skill is implemented in Java. Lambda functions in Java have a bad
cold start performance which impacts the skill user experience. By invoking the skill Lambda function every five
minutes it never idles and won't have cold starts anymore. 