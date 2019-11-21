# Connect4Trainer
This is a training tool for the game [Connect 4](https://en.wikipedia.org/wiki/Connect_Four). While you could play Connect-4 with this, the goal of the project is to train players for the end game by showing a board and checking whether they've made the "correct" move.


# Usage

1. In Eclipse run the ``Main RestServer`` launch configuration
2. Then in a browser, navigate to [http://localhost:4567/game.htm](http://localhost:4567/game.htm)

# Design Bits

## Algorithm
	given Board b  and Player p
	for each column c
		//analyse what if p played in b at c
		for each analyser a
			run analysis on p playing b at c
			if unplayable or we win in 1
				break (no point in analysing this column)
			end if
		end for

		//perform 'forced' analysis
		for each forced analyser a
			run forced analyser
		end for

		// scoring phase
		score each column to figure out the best column to play
		if tie
			randomly select a column
		end if
	end for

## Analysers 
Analysers detect facts about the board

**Implemented**
- win now (in one move)
- block losing in one move
- enable opponent win (by opponent playing on top of our move)
- execute a trap (our next move has more than one column to win)
- block a trap (block the opponent's next move having more than one column to win)
- set up 3-in-a-row which the opponent can't block in their next move since there's a gap below the row in column that would complete the 4. This can set up a win later on, or at least shutdown the column in our favour (if the opponent plays in the gap, we'll win by playing on top). Also block the opponent from doing this
- set up a double 3-in-a-row which is two 3-in-a-row setups, on top of the other. This effectively creates a forced win
- set up 3-in-a-row with a dangling end

## Forced moves
Forced moves are moves we expect the opponent to play because if they don't they'll lose. Because players have different skill levels, some players may not see the more advanced/obscure moves.

**Easy**
- block losing in one

**Intermediate**
- block trap
- block generic win on top of forced move

**Algorithm:**

	given freshly simple analysed (but not scored) board b and player p at depth d = 0
	
	// check end condition, i.e. we won (or can at least execute a trap), or we're forced to play a column
	if p has won
		return result: p wins at d
	end if
	if p forced 
		return (empty response implies we did not win)
	end if
	
	// begin forced analysis
	set forced analysis result
	for each column c
		if c unplayable
			continue
		end if
		
		play p into column c to create b'
		
		analyse board b' as p'
		if p' forced into c'
			play p' into c' to create b''
			simply analyse b''
			recursively perform forced analysis passing board b'', player p, and depth d + 1
			for each forced analysis result f
				push column c
				push p' move
				push win forced analysis result
			end for
		end if
	end for

	return forced analysis results

# Developing

## Prerequisites

* Java 8 (not going to higher versions because Lambda [doesn't support](https://docs.aws.amazon.com/lambda/latest/dg/lambda-runtimes.html) anything higher currently)
* [Gradle](https://gradle.org/releases/)
* [Eclipse](https://www.eclipse.org/downloads/packages/) with Gradle support (2019-12 which comes with Buildship is good)

## Setup
1. Clone source
	```
	$ git clone git@bitbucket.org:andcho09/connect4trainer.git Connect4Trainer
	```
1. Initialise Gradle wrapper
	```
	$ cd Connect4Trainer/Connect4TrainerFunction
	$ gradle wrapper --gradle-version 5.6.4
	```
1. Import the Eclipse project from the ``Connect4TrainerFunction`` directory
1. Right-click the project > ``Gradle`` > ``Refresh Gradle Project``
1. Run unit tests. The ``Connect4TrainerFunction/All Unit Tests.launch`` is an Eclipse launch file to run all of the unit tests


# AWS Notes

This is deployed onto Amazon Web Services.

## Architecture

CloudFront distribution caches the following:

* static web content from a S3 bucket
* dynamic game content (i.e. ``/game/play`` HTTP POST requests) with a Lambda@Edge [function](LambdaEdgeFunction) deciding which region to send API Gateway which in turn fronts a Lambda function

For now, using [SAM](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/what-is-sam.html)'s simplified CloudFormation template (which makes creatingn API Gateway and CodeDeploy resources significantly easier) but not using ``sam build`` because:

* SAM doesn't handle static web content
* SAM takes away flexibility of Gradle and only packages specific artifacts (i.e. minifying static web content)

## Deployment

### Prerequisites

I.e. first time things set up manually:

1. S3 buckets:
	* one bucket for hosting static web content
	* one bucket in each region for holding CodeBuild artifacts since CloudFormation requires in-region buckets. CodeBuild can put artifacts into one bucket and S3 cross-region replication will keep them in sync
1. CloudFront distribution containing:
	* an origin for S3 static web content
	* one origin for each API Gateway in the deployed regions
	* Lambda@Edge function to switch origins based on ``CloudFront-Viewer-Country`` header
1. CloudFormation templates in a StackSet for deployment to the regions
	* [connect4-application-template.yaml](CloudFormation/connect4-application-template.yaml) - takes build artifacts from S3 to deploy API Gateway and Lambda function within region. This doesn't touch the static web content because that S3 bucket is used cross-region.
1. IAM role for Lambda to use

### Deployment instructions

1. Run Gradle build:
	```
	$ cd Connect4TrainerFunction
	$ gradle build
	```
1. Package the CloudFormation template for deploy (note: this is a **Unix** command):
	```
	$ aws cloudformation package --template-file template.yaml --output-template-file packaged-template.yaml --s3-prefix connect4trainer-releases/$(date +%Y-%m-%d) --force-upload --s3-bucket cloudformation-s3-bucket-<region>
	```
1. Create/update CloudFormation stacks:
	* Notes: 
		* this can be done either through the command line or web console
		* CloudFormation is regional so the ``create-stack``/``update-stack`` commands need to be run in each region
		* S3 references within  the packaged template generated by ``aws cloudformation package``) must be regionally local. Since S3 is setup for cross-region replication, you can _find-replace_ the S3 references in the packaged template instead of running the ``package`` command again for the other region
	* Example console use:
		```
		aws cloudformation update-stack --stack-name Connect4TrainerStack --template-body file://packaged-template.yaml --capabilities CAPABILITY_IAM CAPABILITY_NAMED_IAM CAPABILITY_AUTO_EXPAND --tags Key=Project,Value=Connect4Trainer --region us-east-1
		```
1. Upload new static web content update to S3:
	```
	$ aws cp Connect4TrainerFunction/build/distributions-s3/* s3://<web-s3-bucket>/
	$ gradle build
	```
	* Note since CloudFront caches, content might need to be [invalidated manually](https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/Invalidation.html)

### Testing the deploy

Assert you get some JSON describing the new board when hitting the deployed endpoint using curl:

```
$ curl -d "@Connect4TrainerFunction/src/test/resources/Rest_Play_Req_1.json" -k --verbose &lt;endpoint>
```

where ``<endpoint>`` is either the friendly domain, CloudFront, or API Gateway, e.g.:

* CloudFront:

	``curl -d "@Connect4TrainerFunction/src/test/resources/Rest_Play_Req_1.json" -k --verbose https://d3sz8ye83k6g2v.cloudfront.net.amazonaws.com/BETA/game/play``
* API Gateway

	``curl -d "@Connect4TrainerFunction/src/test/resources/Rest_Play_Req_1.json" -k --verbose https://hvxhcxilw4.execute-api.us-east-1.amazonaws.com/BETA/game/play``
