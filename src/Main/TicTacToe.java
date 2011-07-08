package Main;

public class TicTacToe {
	private int n = 5;
	private int line_length = 3; //Required x's or o's in a row for a win
	private String[][] gameBoard = null;
	private String[] players = new String[2];
	private static final String UNDERLINE = "\u001F"; // IRC character for
														// making underlined
														// text
	private int moveCount = 0;
	private int playerTurn = 0;

	public TicTacToe(String playerX, String playerO, int size) {
		players[0] = playerX;
		players[1] = playerO;
		moveCount = 0;
		n = size;
		if(n == 3)
			line_length = 3;
		else
			line_length = 4;
		gameBoard = new String[n][n];
	}

	public String printBoardLine(int line) {
		line--;
		if (gameBoard[0].length < line || line < 0)
			return null;
		String returnMe = ".";
		if (line != (gameBoard[0].length - 1))
			returnMe += UNDERLINE;
		for (int i = 0; i < gameBoard.length; i++) {
			String tempLine = gameBoard[i][line];
			if (tempLine != null && !tempLine.isEmpty()) {
				returnMe += tempLine;
			} else {
				if (line != (gameBoard[0].length - 1))
					returnMe += "_";
				else
					returnMe += " ";
			}
			if (i + 1 < gameBoard.length)
				returnMe += "|";
		}
		return returnMe;
	}

	public GameState placeTile(String player, int x, int y) throws Exception {
		String piece = " ";
		System.out.println("User's trying to place tile at "+x+", "+y);
		if (player.equals(players[0]))
			piece = "x";
		else if (player.equals(players[1]))
			piece = "o";
		else
			throw new Exception(player + ": You are not playing.");
		if(!isPlayersTurn(player))
			throw new Exception(player+": It's not your turn yet.");
		if (gameBoard.length <= x || x < 0 || gameBoard[0].length <= y || y < 0)
			throw new Exception(player + ": The move you tried to make is not valid. Please try again.");
		if(gameBoard[x][y] != null && !gameBoard[x][y].trim().isEmpty())
			throw new Exception(player+": The move you tried to make is illegal, the square is taken.");
		gameBoard[x][y] = piece;
		moveCount++;
		this.alternateTurn();
		return checkGameState(x, y, piece);
	}

	public int getLineCount() {
		return gameBoard[0].length;
	}

	public GameState checkGameState(int x, int y, String piece) {
		/**
		 * Totally jacked this algorithm from here:
		 * http://stackoverflow.com/questions/1056316/algorithm-for-determining-tic-tac-toe-game-over-java
		 * 
		 * If that's not okay then I'm sorry, but it's about the bot, not the
		 * game, right?
		 */

		int magic = 0;
		int rowLength = gameBoard.length;
		
		if (moveCount == (Math.pow(n,2) - 1)){
			System.out.println((Math.pow(n,2) - 1)+" moves required for a stalemate.");
			System.out.println(moveCount+" moves made.");
			return GameState.STALEMATE;
		}

		// check col
		for (int i = 0; i < rowLength; i++) {
			if (gameBoard[x][i] == null || !gameBoard[x][i].equals(piece))
				break;
			if (i == line_length - 1) {
				if(piece.equals("x"))
					return GameState.X_WINS;
				else
					return GameState.O_WINS;
			}
		}

		// check row
		magic = 0;
		for (int i = 0; i < rowLength; i++) {
			if (gameBoard[i][y] == null || !gameBoard[i][y].equals(piece))
				magic = 0;
			else
				magic++;
			if (magic == line_length) {
				if(piece.equals("x"))
					return GameState.X_WINS;
				else
					return GameState.O_WINS;
			}
		}

		magic = 0;
		// check diag
		if (x == y) {
			// we're on a diagonal
			for (int i = 0; i < rowLength; i++) {
				if (gameBoard[i][i] == null || !gameBoard[i][i].equals(piece))
					magic = 0;
				else
					magic++;
				if (magic == line_length) {
					if(piece.equals("x"))
						return GameState.X_WINS;
					else
						return GameState.O_WINS;
				}
			}
		}

		// check anti diag (thanks rampion)
		int[] magik = new int[3];
		for (int i = 0; i < rowLength-1; i++) {
			if(line_length == 5){
				if(gameBoard[rowLength - i][i] == null || !gameBoard[rowLength - i][i].equals(piece)){
					magik[0] = 0;
				}else{
					magik[0]++;
				}
				if(gameBoard[rowLength - (i+1)][i] == null || !gameBoard[rowLength - (i+1)][i].equals(piece)){
					magik[1] = 0;
				}else{
					magik[1]++;
				}
				if(gameBoard[rowLength - i][i-1] == null || !gameBoard[rowLength - (i+1)][i-1].equals(piece)){
					magik[2] = 0;
				}else{
					magik[2]++;
				}
			}else if(line_length == 3){
				magik = null;
				if(gameBoard[line_length - i][i] == null || !gameBoard[line_length - i][i].equals(piece)){
					magic = 0;
				}else{
					magic++;
				}
			}
			if (magic == line_length) {
				if(piece.equals("x"))
					return GameState.X_WINS;
				else
					return GameState.O_WINS;
			}
		}
		magik = null;

		return GameState.GAME_NOT_OVER;
	}

	public enum GameState {
		X_WINS, O_WINS, STALEMATE, GAME_NOT_OVER
	}
	
	public boolean isPlayersTurn(String player){
		return players[playerTurn].equals(player);
	}
	
	public String getPlayerWhoseTurnItIs(){
		return players[playerTurn];
	}
	
	private void alternateTurn(){
		if(playerTurn == 0)
			playerTurn = 1;
		else
			playerTurn = 0;
	}
}
