# Changelog

## Unreleased

## 0.9 (Mar 2020)

* New
    * Added [AWS X-Ray](https://aws.amazon.com/xray/) tracing which can be enabled during the Gradle build with the ``-Pxray=Active`` option
    * Added "warm" action to instruct services to initialise expensive objects

## 0.8 (Feb 2020)
* New
    * Updated to Java 11
    * Updated Gradle 6 replacing incompatible JavaScript minify plugin

## 0.7 (Dec 2019)
* New
    * Added microservice for storing interesting boards in a DynamoDB table which is fronted by the [Connect4StoreFunction] Lambda function
    * Gradle now handles CloudFormation packaging, uploading the template to S3, and creating the change set

## 0.6 (Nov 2019)
* New
    * Standardised the deployment by using a [SAM](https://github.com/awslabs/serverless-application-model) CloudFormation template to standardise the deployment
    * Reduced latency for Australia and New Zealand by using Lambda@Edge to redirect them to the Connect4Trainer deployed in the Sydney region

## 0.5.1 (Sep 2019)
* Bug fixes
    * Upgraded dependencies which addressed some security holes

## 0.3 (Aug 2019)

* New
    * Expanded Main class to allow user to pick between playing a game or analysing a board from a file
    * Allows board files to have new lines and comments with the '#' character
* Bug fixes
    * Fixed a bug where a column was incorrectly flagged as setting up a 3-in-a-row when infact it blocks it
    * We now check that we don't enable an opponent's win earlier in the analysis

## 0.2 (Aug 2019)
* New
    * AI picks bottom center column if it's free
    * Upgraded to Gradle 5.5 and new CommonTools with YUI Compressor
* Performance fixes
    * Minified JavaScript files with YUI Compressor saving >3KB

## 0.1 (Apr 2019)

* Lots of stuff