// IPhone SE 640 wide, so @80px, 7x80=560px; or @90px 7x90=630px

var game = new Phaser.Game(600, 600, Phaser.AUTO, 'connect-4-trainer-div', {
	create: create
});

// Dimensions
var spriteWidth = 8;
var spriteHeight = 8;
var numRows = 6;
var numCols = 7;

// Board
var canvas;
var canvasBG;
var canvasGrid;
var canvasSprite;
var canvasZoom = 80;
var dropIndicator;

// Disc
var discs = [];
var disc1Bmd;
var disc2Bmd;

// Game state
var GAME_STATE = {
	PLAYER_1_TURN: 0,
	PLAYER_2_TURN: 1,
	PLAYER_1_WON: 2,
	PLAYER_2_WON: 3,
	DRAW: 4
};
var gameState = GAME_STATE.PLAYER_1_TURN;
var waitForServer = true;
var text;

// Board
var board;

var Player = function(symbol, colour) {
	this.symbol = symbol;
	this.colour = colour;
};
var player1 = new Player("y", "#fff700");// Human
var player2 = new Player("r", "#e33333");// AI

function resetBoard() {
	board = [];
	for (var y = 0; y < numRows; y++) {
		var a = [];
		for (var x = 0; x < numCols; x++) {
			a.push('.');
		}
		board.push(a);
	}
	for ( var i in discs) {
		discs[i].destroy();// TODO kill?
	}

	gameState = GAME_STATE.PLAYER_1_TURN;
	waitForServer = false;
	if (text) {
		text.kill();
	}
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
	createEventListeners();

	resetBoard();
}

function createBoard() {
	game.create.grid('board', numCols * canvasZoom, numRows * canvasZoom, canvasZoom, canvasZoom, 'rgba(0,191,243,0.8)');

	canvas = game.make.bitmapData(numCols * canvasZoom, numRows * canvasZoom);
	canvasBG = game.make.bitmapData(canvas.width + 2, canvas.height + 2);

	canvasBG.rect(0, 0, canvasBG.width, canvasBG.height, '#fff');
	canvasBG.rect(1, 1, canvasBG.width - 2, canvasBG.height - 2, '#3f5c67');

	var x = 19;
	var y = 19;

	canvasBG.addToWorld(x, y);
	canvasSprite = canvas.addToWorld(x + 1, y + 1);
	canvasGrid = game.add.sprite(x + 1, y + 1, 'board');
	canvasGrid.crop(new Phaser.Rectangle(0, 0, spriteWidth * canvasZoom, spriteHeight * canvasZoom));

	var dropIndicatorBitmap = game.make.bitmapData(canvasZoom / 2, canvasZoom / 2);
	dropIndicatorBitmap.circle(canvasZoom / 4, 0, canvasZoom / 4, player1.colour);
	dropIndicator = dropIndicatorBitmap.addToWorld(-50, 0); // draw off

	disc1Bmd = game.make.bitmapData(canvasZoom - 4, canvasZoom - 4);
	disc1Bmd.circle((canvasZoom - 4) / 2, (canvasZoom - 4) / 2, (canvasZoom - 4) / 2, player1.colour);
	disc2Bmd = game.make.bitmapData(canvasZoom - 4, canvasZoom - 4);
	disc2Bmd.circle((canvasZoom - 4) / 2, (canvasZoom - 4) / 2, (canvasZoom - 4) / 2, player2.colour);
}

function createEventListeners() {
	game.input.mouse.capture = true;
	game.input.onUp.add(onUp, this);
	game.input.addMoveCallback(mouseMove, this);
}

function onUp(pointer) {
	var y = game.math.snapToFloor(pointer.y - canvasSprite.y, canvasZoom) / canvasZoom;
	if (y < 0 || y >= numRows) { return; }

	var x = game.math.snapToFloor(pointer.x - canvasSprite.x, canvasZoom) / canvasZoom;
	if (x < 0 || x >= numCols) { return; }
	if (!waitForServer && gameState == GAME_STATE.PLAYER_1_TURN) {
		play(x);
	} else if (gameState == GAME_STATE.PLAYER_1_WON || gameState == GAME_STATE.PLAYER_2_WON || gameState == GAME_STATE.DRAW) {
		resetBoard();
	}
}

function mouseMove(pointer) {
	if (waitForServer) { return; }
	var col = getCol(pointer);
	if (col < 0) {
		col = 0;
	} else if (col >= numCols) {
		col = numCols - 1;
	}
	var xPos = col * canvasZoom + canvasZoom / 2;
	dropIndicator.x = xPos;
}

function play(col) {
	var data = {
		"currentPlayer": "y",
		"board": {
			"numCols": numCols,
			"numRows": numRows,
			"rows": board
		},
		"column": col
	};

	waitForServer = true;
	var request = $.ajax({
		url: "/game/next",
		method: "POST",
		data: JSON.stringify(data),
		dataType: "json"
	});

	request.done(function(msg) {
		board = msg.playerBoard.rows;
		// animate play to our row
		drawDisc(player1, col, msg.playerRow);
		// check the state
		gameState = msg.gameState;
		// if we won, animate we won and end game
		if (gameState == GAME_STATE.PLAYER_1_WON) {
			// TODO animate win
			showText("You won!");
		} else {
			var aiCol = msg.aiCol;
			var aiRow = msg.aiRow;
			// if ai null AND is draw, then draw and end game
			if (aiCol == null && gameState == GAME_STATE.DRAW) {
				showText("It's a draw!");
			} else {
				board = msg.aiBoard.rows;
				drawDisc(player2, aiCol, aiRow);
				if (gameState == GAME_STATE.PLAYER_2_WON) {
					// if opponent won, animate opponent won and end game
					showText("The bot won!");
				} else if (gameState == GAME_STATE.DRAW) {
					gameState == GAME_STATE.DRAW;
					showText("It's a draw!");
				}
			}
		}
		// else
		waitForServer = false;
	});

	request.fail(function(jqXhr, textStatus) {
		alert("Request failed: " + jqXhr.responseText);
		waitForServer = false;
	});
}

function drawDisc(player, col, row) {
	var bmd = player == player1 ? disc1Bmd : disc2Bmd;
	var disc = bmd.addToWorld(canvasSprite.x + 2 + col * canvasZoom, canvasSprite.y + 2 + (numRows - row - 1) * canvasZoom);
	discs.push(disc);
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