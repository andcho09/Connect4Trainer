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
var player1Discs;
var player2Discs;

// Game state
var GAME_STATE = {
	PLAYER_1_TURN: 0,
	PLAYER_2_TURN: 1,
	PLAYER_1_WON: 2,
	PLAYER_2_WON: 3,
	DRAW: 4
};
var gameState = GAME_STATE.PLAYER_1_TURN;
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
var player1 = new Player("y", "#fff700");// Human
var player2 = new Player("r", "#e33333");// AI

// Timing
var MIN_INPUT_INTERVAL = 200;

function resetBoard() {
	board = [];
	for (var y = 0; y < NUM_ROWS; y++) {
		var a = [];
		for (var x = 0; x < NUM_COLS; x++) {
			a.push('.');
		}
		board.push(a);
	}

	player1Discs.forEach(kill, this)
	player2Discs.forEach(kill, this)

	if (text) {
		text.y = -50;
		text.kill();
	}

	gameState = GAME_STATE.PLAYER_1_TURN;
	playerIsWaiting = false;
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

	resetBoard();
}

function createBoard() {
	game.create.grid('board', NUM_COLS * canvasZoom, NUM_ROWS * canvasZoom, canvasZoom, canvasZoom, 'rgba(0,191,243,0.8)');

	canvas = game.make.bitmapData(NUM_COLS * canvasZoom, NUM_ROWS * canvasZoom);
	canvasBG = game.make.bitmapData(canvas.width + 2, canvas.height + 2);

	canvasBG.rect(0, 0, canvasBG.width, canvasBG.height, '#fff');
	canvasBG.rect(1, 1, canvasBG.width - 2, canvasBG.height - 2, '#3f5c67');

	var x = 19; //TODO these are hardcoded
	var y = 19;

	canvasBG.addToWorld(x, y);
	canvasSprite = canvas.addToWorld(x + 1, y + 1);
	canvasGrid = game.add.sprite(x + 1, y + 1, 'board');
	canvasGrid.crop(new Phaser.Rectangle(0, 0, SPRITE_WIDTH * canvasZoom, SPRITE_HEIGHT * canvasZoom));
}

function createDiscs() {
	var dropIndicatorBitmap = game.make.bitmapData(canvasZoom / 2, canvasZoom / 2);
	dropIndicatorBitmap.circle(canvasZoom / 4, 0, canvasZoom / 4, player1.colour);
	dropIndicator = dropIndicatorBitmap.addToWorld(-50, 0); // draw off

	player1Discs = game.add.group();
	player2Discs = game.add.group();

	var disc1Bmd = game.make.bitmapData(canvasZoom - 4, canvasZoom - 4, "player1Circle", true);
	disc1Bmd.circle((canvasZoom - 4) / 2, (canvasZoom - 4) / 2, (canvasZoom - 4) / 2, player1.colour);
	var disc2Bmd = game.make.bitmapData(canvasZoom - 4, canvasZoom - 4, "player2Circle", true);
	disc2Bmd.circle((canvasZoom - 4) / 2, (canvasZoom - 4) / 2, (canvasZoom - 4) / 2, player2.colour);
}

function createButtons() {
	var text = game.add.text(canvasZoom, ((NUM_COLS - 1) * canvasZoom) + 2 * canvasSprite.y, "New Game", {
		font: "24px Arial",
		fill: "#ffff00",
		align: "center"
	});
	text.inputEnabled = true;
	text.events.onInputDown.add(function() {
		resetBoard();
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
	if (!playerIsWaiting && gameState == GAME_STATE.PLAYER_1_TURN && pointer.msSinceLastClick > MIN_INPUT_INTERVAL) {
		play(x);
	} else if (gameState == GAME_STATE.PLAYER_1_WON || gameState == GAME_STATE.PLAYER_2_WON || gameState == GAME_STATE.DRAW) {
		resetBoard();
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

function play(col) {
	var data = {
		"currentPlayer": "y",
		"board": {
			"numCols": NUM_COLS,
			"numRows": NUM_ROWS,
			"rows": board
		},
		"column": col
	};

	playerIsWaiting = true;
	var request = $.ajax({
		url: "/game/next",
		method: "POST",
		data: JSON.stringify(data),
		dataType: "json"
	});

	request.done(function(msg) {
		if(msg.exception != undefined){
			// Error
			if(msg.exception.code == "COLUMN_FULL" || msg.exception.code == "OUT_OF_BOUNDS"){
				playerIsWaiting = false;
				return;
			}else{
				alert("Could not play col '" + col + "' because: " + msg.exception);
				return;
			}
		}
		board = msg.playerBoard.rows;
		gameState = msg.gameState;
		// animate play to our row
		var playerDiscTween = drawDisc(player1, col, msg.playerRow);
		// if we won, animate we won and end game
		if (gameState == GAME_STATE.PLAYER_1_WON) {
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
				var opponentTween = drawDisc(player2, aiCol, aiRow, playerDiscTween);
				if (gameState == GAME_STATE.PLAYER_2_WON) {
					// if opponent won, animate opponent won and end game
					showText("The bot won!");
					playerIsWaiting = false;
				} else if (gameState == GAME_STATE.DRAW) {
					gameState == GAME_STATE.DRAW;
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

function drawDisc(player, col, row, parentTween) {
	var key = "player1Circle";
	var group = player1Discs;
	if (player == player2) {
		key = "player2Circle";
		group = player2Discs;
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
		parentTween.chain(tween)
		autoStart = false;
	}
	tween.to({y:y}, duration, Phaser.Easing.Linear.None, autoStart);
	return tween
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