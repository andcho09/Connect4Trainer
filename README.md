# Connect4Trainer
This is a training tool for the game [Connect 4](https://en.wikipedia.org/wiki/Connect_Four). While you could play Connect-4 with this, the goal of the project is to train players for the end game by showing a board and checking whether they've made the "correct" move.

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

**TODO**
- generic win on top of forced move (see below)
- enable a column of win (3 on top of each other, 3 diagonal)

## Forced moves
Forced moves are moves we expect the opponent to play because if they don't they'll lose. Because players have different skill levels, some players may not see the more advanced/obscure moves.

**Easy**
- block losing in one

**Intermediate**
- block trap
- block generic win on top of forced move

**Algorithm:**

	given freshly analysed (but not scored) board b and player p
	
	// check end condition, i.e. we won (or can at least execute a trap), or we're forced to play a column
	for each column c
		if c has win conditions
			return p wins at c // end
		else if p forced to play c
			return p forced to play c 
		end if
	end for
	
	
	// begin forced analysis
	for each column c
		if c unplayable
			continue
		end if
		
		play p into column c to create b'
		
		analyse board b' as p'
		if p' forced into c'
			play p' into c' to create b''
			analyse board b'' as p
				recursive 
		end if
	end for
	
	
The other version...

	want to go down each column
	then
	return the best column(s) or nothing if no recommendations
		where best is: has a win condition (win now, failing that a trap)
		
	