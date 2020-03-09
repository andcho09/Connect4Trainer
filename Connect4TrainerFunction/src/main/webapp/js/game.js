var game = new Phaser.Game(600, 600, Phaser.AUTO, 'connect-4-trainer-div', {
	create: create
});

// Dimensions
var SPRITE_WIDTH = 8; // kill these
var SPRITE_HEIGHT = 8;
var NUM_ROWS = 6;
var NUM_COLS = 7;

// Board
var canvas;
var canvasBG;
var canvasGrid;
var canvasSprite;
var canvasZoom = 80;
var dropIndicator;

// Disc
var playerYDiscs;
var playerRDiscs;

// Game state
var GAME_STATE = {
	PLAYER_R_TURN: 0,
	PLAYER_Y_TURN: 1,
	PLAYER_R_WON: 2,
	PLAYER_Y_WON: 3,
	DRAW: 4
};
var gameState = GAME_STATE.PLAYER_Y_TURN;
var playerIsWaiting = true;

// Messages/UI
var text;

// Board
var board;

// Player
var Player = function(symbol, colour) {
	this.symbol = symbol;
	this.colour = colour;
};
var playerY = new Player("y", "#fff700");// Human
var playerR = new Player("r", "#e33333");// AI
var firstPlayer = playerY;

// Timing
var MIN_INPUT_INTERVAL = 200;

function resetBoard(swapPlayers) {
	board = [];
	for (var y = 0; y < NUM_ROWS; y++) {
		var a = [];
		for (var x = 0; x < NUM_COLS; x++) {
			a.push('.');
		}
		board.push(a);
	}

	playerYDiscs.forEach(kill, this)
	playerRDiscs.forEach(kill, this)

	if (text) {
		text.y = -50;
		text.kill();
	}

	if(swapPlayers){
		if (firstPlayer == playerR){
			firstPlayer = playerY;
			gameState = GAME_STATE.PLAYER_Y_TURN;
		} else {
			firstPlayer = playerR;
			gameState = GAME_STATE.PLAYER_R_TURN;
		}
	} else {
		gameState = GAME_STATE.PLAYER_Y_TURN;
	}
	
	if (gameState == GAME_STATE.PLAYER_Y_TURN){
		playerIsWaiting = false;
	} else {
		playerIsWaiting = true;
		playAi(); // AI is going first
	}
}

/**
 * Kills and moves off screen
 * 
 * @param thing
 */
function kill(thing) {
	thing.y = -50;
	thing.kill();
}

function create() {
	// Disable browser stuff
	document.body.oncontextmenu = function() {
		return false;
	};
	Phaser.Canvas.setUserSelect(game.canvas, 'none');
	Phaser.Canvas.setTouchAction(game.canvas, 'none');

	game.time.desiredFps = 24;
	game.stage.backgroundColor = '#6464C8';

	createBoard();
	createDiscs();
	createButtons();
	createEventListeners();

	resetBoard(false);
}

function createBoard() {
	canvas = game.make.bitmapData(NUM_COLS * canvasZoom, NUM_ROWS * canvasZoom);
	canvasBG = game.make.bitmapData(canvas.width + 2, canvas.height + 2);
	canvasBG.rect(0, 0, canvasBG.width, canvasBG.height, '#fff'); //white border
	canvasBG.rect(1, 1, canvasBG.width - 2, canvasBG.height - 2, '#3f5c67'); // board background 

	var x = 19; //TODO these are hardcoded
	var y = 19;

	canvasBG.addToWorld(x, y);
	canvasSprite = canvas.addToWorld(x + 1, y + 1);
	
	var graphics = game.add.graphics(x, y);
	graphics.lineStyle(1, 0x00bff3, 0.8);
	for (var i = 0; i < NUM_COLS; i++){
		graphics.moveTo(i * canvasZoom + 1, 1);
		graphics.lineTo(i * canvasZoom + 1, NUM_ROWS * canvasZoom + 1);
	}
	for (var i = 0; i < NUM_ROWS; i++){
		graphics.moveTo(1, i * canvasZoom + 1);
		graphics.lineTo(NUM_COLS * canvasZoom + 1, i * canvasZoom + 1);
	}
}

function createDiscs() {
	var dropIndicatorBitmap = game.make.bitmapData(canvasZoom / 2, canvasZoom / 2);
	dropIndicatorBitmap.circle(canvasZoom / 4, 0, canvasZoom / 4, playerY.colour);
	dropIndicator = dropIndicatorBitmap.addToWorld(-50, 0); // draw off

	playerYDiscs = game.add.group();
	playerRDiscs = game.add.group();

	var disc1Bmd = game.make.bitmapData(canvasZoom - 4, canvasZoom - 4, "playerYCircle", true);
	disc1Bmd.circle((canvasZoom - 4) / 2, (canvasZoom - 4) / 2, (canvasZoom - 4) / 2, playerY.colour);
	var disc2Bmd = game.make.bitmapData(canvasZoom - 4, canvasZoom - 4, "playerRCircle", true);
	disc2Bmd.circle((canvasZoom - 4) / 2, (canvasZoom - 4) / 2, (canvasZoom - 4) / 2, playerR.colour);
}

function createButtons() {
	var text = game.add.text(canvasZoom, ((NUM_COLS - 1) * canvasZoom) + 2 * canvasSprite.y, "New Game", {
		font: "24px Arial",
		fill: "#ffff00",
		align: "center"
	});
	text.inputEnabled = true;
	text.events.onInputDown.add(function() {
		resetBoard(true);
	}, this);
}

function createEventListeners() {
	game.input.mouse.capture = true;
	game.input.mouse.stopOnGameOut = true;
	game.input.onUp.add(onMouseUp, this);
	game.input.addMoveCallback(onMouseMove, this);
}

function onMouseUp(pointer) {
	var y = game.math.snapToFloor(pointer.y - canvasSprite.y, canvasZoom) / canvasZoom;
	if (y < 0 || y >= NUM_ROWS) { return; }

	var x = game.math.snapToFloor(pointer.x - canvasSprite.x, canvasZoom) / canvasZoom;
	if (x < 0 || x >= NUM_COLS) { return; }
	if (!playerIsWaiting && gameState == GAME_STATE.PLAYER_Y_TURN && pointer.msSinceLastClick > MIN_INPUT_INTERVAL) {
		play(x);
	} else if (gameState == GAME_STATE.PLAYER_R_WON || gameState == GAME_STATE.PLAYER_Y_WON || gameState == GAME_STATE.DRAW) {
		resetBoard(true);
	}
}

function onMouseMove(pointer) {
	if (playerIsWaiting) { return; }
	var col = getCol(pointer);
	if (col < 0) {
		col = 0;
	} else if (col >= NUM_COLS) {
		col = NUM_COLS - 1;
	}
	var xPos = col * canvasZoom + canvasZoom / 2;
	dropIndicator.x = xPos;
}

/**
 * Handle a human player's play into a specific column.
 * @param col the column the player is playing into, could be an illegal move
 */
function play(col) {
	var data = {
		"action": "next",
		"currentPlayer": playerY.symbol,
		"board": {
			"numCols": NUM_COLS,
			"numRows": NUM_ROWS,
			"rows": board
		},
		"column": col
	};

	playerIsWaiting = true;
	var request = $.ajax({
		url: "/game/play",
		method: "POST",
		data: JSON.stringify(data),
		dataType: "json"
	});

	request.done(function(msg) {
		if(msg.exception != undefined){
			// Error
			if(msg.exception.code == "COLUMN_FULL" || msg.exception.code == "OUT_OF_BOUNDS"){
				playerIsWaiting = false;
			}else{
				alert("Could not play col '" + col + "' because: " + msg.exception);
			}
			return;
		}
		board = msg.playerBoard.rows;
		gameState = msg.gameState;
		// animate play to our row
		var playerDiscTween = drawDisc(playerY, col, msg.playerRow);
		// if we won, animate we won and end game
		if (gameState == GAME_STATE.PLAYER_Y_WON) {
			showText("You won!");
			playerIsWaiting = false;
		} else {
			var aiCol = msg.aiCol;
			var aiRow = msg.aiRow;
			// if ai null AND is draw, then draw and end game
			if (aiCol == null && gameState == GAME_STATE.DRAW) {
				showText("It's a draw!");
				playerIsWaiting = false;
			} else {
				board = msg.aiBoard.rows;
				var opponentTween = drawDisc(playerR, aiCol, aiRow, playerDiscTween);
				if (gameState == GAME_STATE.PLAYER_R_WON) {
					// if opponent won, animate opponent won and end game
					showText("The bot won!");
					playerIsWaiting = false;
				} else if (gameState == GAME_STATE.DRAW) {
					showText("It's a draw!");
					playerIsWaiting = false;
				} else {
					opponentTween.onComplete.add(function(){ playerIsWaiting = false; }, this);
				}
			}
		}
	});

	request.fail(function(jqXhr, textStatus) {
		alert("Request failed: " + jqXhr.responseText);
		playerIsWaiting = false;
	});
}

/**
 * Handles AI making a play (e.g. in a game where the AI goes first).
 */
function playAi(){
	var data = {
		"action": "recommend",
		"currentPlayer": playerR.symbol,
		"board": {
			"numCols": NUM_COLS,
			"numRows": NUM_ROWS,
			"rows": board
		}
	};

	playerIsWaiting = true;
	var request = $.ajax({
		url: "/game/play",
		method: "POST",
		data: JSON.stringify(data),
		dataType: "json"
	});
	
	request.done(function(msg) {
		if(msg.exception != undefined){
			// Error
			if(msg.exception.code == "COLUMN_FULL" || msg.exception.code == "OUT_OF_BOUNDS"){
				playerIsWaiting = false;
			}else{
				alert("Could not play col '" + col + "' because: " + msg.exception);
			}
			return;
		}
		board = msg.board.rows;
		gameState = msg.gameState;
		// animate AI's play to row
		var aiDiscTween = drawDisc(playerR, msg.recommendColumn, msg.recommendRow);
		// if AI won, animate opponent won and end game
		if (gameState == GAME_STATE.PLAYER_R_WON) {
			showText("The bot won!");
		} else if (gameState == GAME_STATE.DRAW){
			showText("It's a draw!");
		}
		aiDiscTween.onComplete.add(function(){ playerIsWaiting = false; }, this);
	});
	
	request.fail(function(jqXhr, textStatus) {
		alert("Request failed: " + jqXhr.responseText);
		playerIsWaiting = false;
	});
}

function drawDisc(player, col, row, parentTween) {
	var key = "playerYCircle";
	var group = playerYDiscs;
	if (player == playerR) {
		key = "playerRCircle";
		group = playerRDiscs;
	}
	var x = canvasSprite.x + 2 + col * canvasZoom;
	var y = canvasSprite.y + 2 + (NUM_ROWS - row - 1) * canvasZoom;
	var disc = group.getFirstDead(false, x, canvasSprite.y - canvasZoom);
	if (!disc) {//TODO is this needed if true is used above?
		disc = game.cache.getBitmapData(key).addToWorld(x, canvasSprite.y - canvasZoom);
		group.add(disc);
	}
	
	var duration = (NUM_COLS - row - 1) * 25;
	var tween = game.add.tween(disc);
	var autoStart = true;
	var delay = 0;
	if(parentTween != undefined){
		parentTween.chain(tween);
		autoStart = false;
	}
	tween.to({y:y}, duration, Phaser.Easing.Linear.None, autoStart);
	return tween;
	// TODO grid has extra pixel on left and bottom. 79x79 boxes
}

function showText(textString) {
	text = game.add.text(0, 0, textString, {
		font: "74px Arial Black",
		fill: "#c51b7d",
		boundsAlignH: "center",
		boundsAlignV: "middle"
	});
	text.stroke = "#de77ae";
	text.strokeThickness = 16;
	text.setShadow(2, 2, "#333333", 2, true, true);
	text.setTextBounds(0, canvasSprite.y / 3, game.width, game.height);
}

/**
 * Returns the column position the mouse is on the board. Could be off the
 * board.
 * 
 * @param pointer
 *            the mouse pointer
 * @returns {Number} the column position (0-based). Could be off the board
 */
function getCol(pointer) {
	return game.math.snapToFloor(pointer.x - canvasSprite.x, canvasZoom) / canvasZoom;
}

/**
 * Returns the row position the mouse is on the board. Could be off the board.
 * 
 * @param pointer
 *            the mouse pointer
 * @returns {Number} the row position (0-based). Could be off the board
 */
function getRow(pointer) {
	return game.math.snapToFloor(pointer.y - canvasSprite.y, canvasZoom) / canvasZoom;
}

$(document).ready(function(){
	// Tell server side to warm up to avoid cold-starts.
	var data = {
		"action": "warm"
	};
	$.ajax({
		url: "/game/play",
		method: "POST",
		data: JSON.stringify(data),
		dataType: "json"
	});
});
