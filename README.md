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

**TODO**
- none

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

# AWS Notes

## Architecture

CloudFront distribution caches the following:

* S3 bucket hosts the web content with [CORS enabled on the bucket](https://docs.aws.amazon.com/AmazonS3/latest/dev/cors.html)
* API Gateway fronts ``/game/play`` POST resource which in turn invokes
    * Lambda function

## Deployment

1. Clone a clean repository
    ``> git clone git@bitbucket.org:andcho09/connect4trainer.git connect4trainer_clean``
1. Run ``gradle build`` to build the .zip file which will be saved to ``build/distributions``
1. Upload the .zip file to AWS Lambda
1. Upload any new web content from ``build/aws-s3-dist`` to the AWS S3 bucket
    * Note CloudFront caches web content so it might need to be [invalidated manually](https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/Invalidation.html)
1. Tag the git commit