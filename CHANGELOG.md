# Changelog

## Unreleased

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