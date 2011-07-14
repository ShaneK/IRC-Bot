package Main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import Interface.ServerCallbackClass;
import Main.TicTacToe.GameState;
import Misc.Message;

public class Main implements ServerCallbackClass {
	//A handle on the client
	public static GenericClient client = null;
	
	//My bot's information
	public static final String botID = "ShaneKBot"; //Display name
	public static final String myID = "ShaneK"; //My name
	public static final String realName = "Bender Bending Rodriguez"; //Bot's "real name"
	
	//Host and (main)Channel
	public static final String host = "irc.choopa.net";
	public static final String mainChannel = "#dreamincode";
	public static String channel = mainChannel;

	
	//Related to knots and crosses
	private TicTacToe game = null;
	private String players[] = new String[2];
	private boolean playingGame = false;
	private boolean playingInPrivate = true;
	private int boardSize = 3;
	private int gameDelay = 2000;
	
	//Related to the future of my bot
	private static final String future_path = "future.info";
	private ArrayList<String> future = null;
	private boolean future_has_been_loaded = false;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		client = new GenericClient(host, 6667, new Main());
		client.sendMessage("NICK "+botID);
		client.sendMessage("USER "+myID+" "+host+" bla :"+realName+"");
//		client.sendMessage("JOIN "+channel);
	}

	@Override
	public void connectedToServer() {
		System.out.println("Connected!");
	}

	@Override
	public void couldNotConnectToServer() {
		System.out.println("Failed to connect.");
		
	}

	@Override
	public void disconnectedFromServer(int messagesSentTotal,
			int messagesReceivedTotal) {
		System.out.println("Disconnected from the server. Messages sent total: "+messagesSentTotal+" Messages Received Total: "+messagesReceivedTotal);
		
	}

	@Override
	public void receivedMessageFromServer(String message, int messagesReceived) {
		System.out.println("Server said: "+message);
		
		//PONG on PING
		if(message.startsWith("PING ")){
			ping(message);
			return;
		}
		
		//The creation of a message
		if(!message.startsWith(":")) return;
		Message m = new Message(message);
		
		//We'll join on the VERSION message
		if(message.contains("VERSION") && m.getToChan().equals(botID)){
			client.sendMessage("JOIN "+mainChannel);
			return;
		}

		//TODO: Delete when done debugging
		System.out.println(m);
		
		//So we don't keep accessing m.getText();
		String msg = m.getText().trim().toLowerCase();
		
		//Command processing time!
		if(msg.startsWith(botID.toLowerCase()+":")){
			if(msg.contains("quit")){
				//Quitting
				quit(m);
			}else if(msg.contains("hello")){
				//Greeting who greeted me
				command_greetings(m);
			}else if(msg.contains("message me instead")){
				//Changes the channel variable so it PMs m.getFrom() instead of the entire channel
				command_change_channel(m);
			}else if(msg.contains("talk to everyone")){
				//Undoes previous command
				command_back_to_main_channel(m);
			}else if(msg.contains("new game")){
				//Creates a new game
				command_new_game(m, msg);
			}else if(this.playingGame && msg.contains("mark")){
				//Makes a move in the game
				command_make_move(m, msg);
			}else if(this.playingGame && msg.contains("refresh board")){
				//Refreshes the board
				command_refresh_the_board(m, msg);
			}else if(this.playingGame && msg.contains("kill the game")){
				//Kills the game
				command_kill_the_game(m, msg);
			}else if(msg.contains("test")){
				//Test the game
				command_test(m, msg);
			}else if(msg.contains("future")){
				if(msg.contains(": add future")){
					//Add something to the future plans
					//Syntax: add future <x>
					command_add_future(m, msg);
				}else if(msg.contains(": remove future")){
					//Remove something from the future plans
					//Syntax: remove future <line#>
					command_remove_future(m, msg);
				}else{
					//Display future plans for ShaneKBot
					command_display_future(m, msg);
				}
			}
		}else if(m.getToChan().trim().toLowerCase().equals(botID.trim().toLowerCase())){
			private_message_relay(m, msg);
		}
	}
	
	private void command_remove_future(Message m, String msg) {
		if(!m.getFrom().equals("ShaneK")) return;
		try {
			Pattern regex = Pattern.compile(": (?:add|remove) future (\\d*)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE);
			Matcher regexMatcher = regex.matcher(msg);
			int future_to_remove = -1;
			if (regexMatcher.find()) {
				future_to_remove = Integer.parseInt(regexMatcher.group(1));
				if(!future_has_been_loaded)
					load_future();
				if(!(future_to_remove > -1 && future_to_remove < future.size())) return;
				String removed = future.remove(future_to_remove);
				save_future();
				sendMessage(mainChannel, "Removed future on \"line\" "+future_to_remove+". ("+removed+")", 2000);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void command_add_future(Message m, String msg) {
		if(!m.getFrom().equals("ShaneK")) return;
		try {
			Pattern regex = Pattern.compile(": (?:add|remove) future (.*)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE);
			Matcher regexMatcher = regex.matcher(msg);
			String future_string = null;
			if (regexMatcher.find()) {
				future_string = regexMatcher.group(1);
				if(!future_has_been_loaded)
					load_future();
				future.add(future_string);
				save_future();
				sendMessage(mainChannel, "New future goal added successfully.");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void private_message_relay(Message m, String msg) {
		if(this.playingGame && this.playingInPrivate){
			String target = players[0];
			if(m.getFrom().equals(players[0]))
				target = players[1];
			//else it defaults to player[0] already
			sendMessage(target, "<"+m.getFrom()+"> "+m.getText(), gameDelay);
		}
	}

	private void command_display_future(Message m, String msg) {
		if(!future_has_been_loaded){
			load_future();
		}
		int length = future.size();
		String output = "Things ShaneK needs to do to me: ";
		if(length == 0){
			sendMessage(mainChannel, output+"Nothing, apparently. (ArrayList empty!)");
			return;
		}
		for(int i = 0; i < length; i++){
			output += future.get(i)+"; ";
			if(output.length() >= 256){
				sendMessage(mainChannel, output, 2000);
				output = "";
			}
		}
		sendMessage(mainChannel, output, 2000);
	}

	private void load_future() {
		try {
			File f = new File(future_path);
			f.createNewFile(); //Only happens if it doesn't exist already
			BufferedReader reader = new BufferedReader(new FileReader(f));
			String line;
			future = new ArrayList<String>();
			while((line = reader.readLine()) != null){
				future.add(line);
			}
			reader.close();
			line = null;
			future_has_been_loaded = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void save_future(){
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(future_path)));
			String output = "";
			for(String line:future){
				output += line+"\n";
			}
			writer.write(output);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	private void command_test(Message m, String msg) {
		String from = m.getFrom();
		sendMessage(from, "Running test on game for unexpected bugs.", 0);
		sendMessage(from, "Testing stage 1: Creating game. (3x3)", 0);
		try{
			String x = "ShaneX";
			String o = "ShaneO";
			TicTacToe ttt = new TicTacToe(x, o, 3);
			sendMessage(from, "Testing stage 2: Placing tile X on square 0,0", 0);
			GameState state = ttt.placeTile("ShaneX", 0, 0);
			sendMessage(from, "GameState: "+state+"; Testing stage 3: Placing tile O on square 2,0", 0);
			state = ttt.placeTile("ShaneO", 2, 0);
			sendMessage(from, "GameState: "+state+"; Testing stage 4: Placing tile X on square 1,1", 0);
			state = ttt.placeTile("ShaneX", 1,1);
			sendMessage(from, "GameState: "+state+"; Testing stage 5: Placing tile O on square 1,0", 0);
			state = ttt.placeTile("ShaneO", 1, 0);
			sendMessage(from, "GameState: "+state+"; Testing stage 6: Placing tile X on square 2,2", 0);
			state = ttt.placeTile("ShaneX", 2, 2);
			sendMessage(from, "GameState: "+state+"; Testing stage PART 1 over.", 0);
			sendMessage(from, "Testing stage PART 2: creating new game (5x5)", 0);
			ttt = new TicTacToe(x, o, 5);
			sendMessage(from, "Testing stage part two step 1: Placing tile X on 0,0");
			state = ttt.placeTile(x, 0, 0);
			sendMessage(from, "GameState: "+state+"; Testing stage part two step 2: placing tile O on 4,0");
			state = ttt.placeTile(o, 4, 0);
			sendMessage(from, "GameState: "+state+"; Testing stage PART 2 over.", 0);
		}catch(Exception e){
			sendMessage(from, e.getMessage(), 2000);
		}
	}

	private void command_kill_the_game(Message m, String msg) {
		if(playingGame){
			String from = m.getFrom();
			if(from.equals("ShaneK") || from.equals(players[0]) || from.equals(players[1])){
				if(playingInPrivate){
					sendMessage(players, "The game has been ordered dead. We will be clearing this and forgetting it ever happened. -Jedi mind trick- This game never happened!", gameDelay);
				}else{
					sendMessage(mainChannel, "The game has been ordered dead. We will be clearing this and forgetting it ever happened. -Jedi mind trick- This game never happened!", gameDelay);
				}
				clearGame();
			}
		}
	}

	private void command_refresh_the_board(Message m, String msg) {
		if(playingInPrivate){
			sendMessage(players, m.getFrom()+": Okay!", gameDelay);
			drawBoard();
			sendMessage(players, "The competitors are "+players[0]+" (x) and "+players[1]+" (o).", gameDelay);
		}else{
			sendMessage(mainChannel, m.getFrom()+": Okay!", gameDelay);
			drawBoard();
			sendMessage(mainChannel, "The competitors are "+players[0]+" (x) and "+players[1]+" (o).", gameDelay);
		}
	}

	private void command_make_move(Message m, String msg) {
		try {
			Pattern regex = Pattern.compile("(\\d*),(\\d*)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
			Matcher regexMatcher = regex.matcher(msg);
			if (regexMatcher.find()) {
				System.out.println(regexMatcher.group(1));
				System.out.println(regexMatcher.group(2));
				try{
					TicTacToe.GameState state = game.placeTile(m.getFrom(), Integer.parseInt(regexMatcher.group(1)), Integer.parseInt(regexMatcher.group(2)));
					drawBoard();
					if(state == TicTacToe.GameState.GAME_NOT_OVER){
						if(playingInPrivate){
							sendMessage(players, "Alright, now it's time for "+game.getPlayerWhoseTurnItIs()+"'s turn!", gameDelay);
						}else{
							sendMessage(mainChannel, "Alright, now it's time for "+game.getPlayerWhoseTurnItIs()+"'s turn!", gameDelay);
						}
					}else if(state == TicTacToe.GameState.STALEMATE){
						if(playingInPrivate){
							sendMessage(players, "Nobody wins. Game over, it's a draw.", gameDelay);
							sendMessage(mainChannel, "Nobody wins. Game over, it's a draw.", 2000);
						}else{
							sendMessage(mainChannel, "Nobody wins. Game over, it's a draw.", gameDelay);
						}
						clearGame();
					}else if(state == TicTacToe.GameState.X_WINS){
						if(playingInPrivate){
							sendMessage(players, players[0]+" (X) wins!", gameDelay);
							sendMessage(mainChannel, players[0]+" (X) wins!", 2000);
						}else{
							sendMessage(mainChannel, players[0]+" (X) wins!", gameDelay);
						}
						clearGame();
					}else if(state == TicTacToe.GameState.O_WINS){
						if(playingInPrivate){
							sendMessage(players, players[1]+" (O) wins!", gameDelay);
							sendMessage(mainChannel, players[1]+" (O) wins!", 2000);
						}else{
							sendMessage(mainChannel, players[1]+" (O) wins!", gameDelay);
						}
						clearGame();
					}
				}catch(Exception e){
					if(playingInPrivate){
						sendMessage("ShaneK", e.getMessage(), gameDelay);
					}else{
						sendMessage(mainChannel, e.getMessage(), gameDelay);
					}
				}
			} 
		} catch (PatternSyntaxException ex) {
			// Syntax error in the regular expression
		}
	}

	private void command_new_game(Message m, String msg) {
		if(this.playingGame){
			sendMessage(mainChannel, m.getFrom()+": Sorry, I'm in a game already!", 2000);
		}else{
			if(msg.contains("in pm") || msg.contains("in private")){
				playingInPrivate = true;
			}else if(msg.contains("in public")){
				playingInPrivate = false;
			}
			if(msg.contains("with delay")){
				gameDelay = 2000;
			}else if(msg.contains("no delay")){
				gameDelay = 0;
			}
			if(msg.contains("size 5")){
				boardSize = 5;
			}else if(msg.contains("size 3")){
				boardSize = 3;
			}
			if(players[0] == null || players[0].trim().isEmpty()){
				sendMessage(mainChannel, m.getFrom()+": So, you'd like to play a game would you? Well, we need 1 other person!", gameDelay);
				players[0] = m.getFrom();
			}else if(players[1] == null || players[1].trim().isEmpty()){
				if(players[0].equals(m.getFrom())){
					sendMessage(mainChannel, m.getFrom()+": You cannot play yourself, sorry.", gameDelay);
				}else{
					players[1] = m.getFrom();
					playingGame = true;
					game = new TicTacToe(players[0], players[1], boardSize);
					if(playingInPrivate){
						sendMessage(players, "Hello ladies and gentlemen! It's time to play tic-tac-toe! Today our competitors are "+players[0]+" (x) versus "+players[1]+" (o)! The board size is "+boardSize+"x"+boardSize+".", gameDelay);
					}else{
						sendMessage(mainChannel, "Hello ladies and gentlemen! It's time to play tic-tac-toe! Today our competitors are "+players[0]+" (x) versus "+players[1]+" (o)! The board size is "+boardSize+"x"+boardSize+".", gameDelay);
					}
					drawBoard();
					if(playingInPrivate){
						sendMessage(players, "Okay, "+game.getPlayerWhoseTurnItIs()+", you're up first!", gameDelay);
					}else{
						sendMessage(mainChannel, "Okay, "+game.getPlayerWhoseTurnItIs()+", you're up first!", gameDelay);
					}
				}
			}else{
				sendMessage(mainChannel, "Í'm sorry, but I can only moderate one game at a time. Otherwise there'd be a lot of spam!", gameDelay);
			}
		}
	}

	private void command_back_to_main_channel(Message m) {
		channel = mainChannel;
		sendMessage(channel, "Okay, "+m.getFrom()+", I'm talking to everyone again!", 2000);
	}

	private void command_change_channel(Message m) {
		if(m.getFrom().equals("Supercore"))
			sendMessage(mainChannel, m.getFrom()+": No! Not again! Not after the stuff that happened last time...", 2000);
		else{
			channel = m.getFrom();
			sendMessage(channel, "Okay, "+channel+", I'm talking to you only!", 2000);
		}
	}

	private void command_greetings(Message m) {
		sendMessage(channel, "Oh, hello thar "+m.getFrom()+"!", 2000);
	}

	public void ping(String message){
		String[] pong = message.split(" ");
		client.sendMessage("PONG "+pong[1]);
	}
	
	public void quit(Message m){
		if(m.getFrom().equals("ShaneK")){
			client.sendMessage("QUIT :ShaneK made me do it.");
			System.exit(0);
		}
	}
	
	public void drawBoard(){
		int rowCount = game.getLineCount();
		for(int i = 1; i <= rowCount; i++){
			if(playingInPrivate){
				sendMessage(players, game.printBoardLine(i), gameDelay);
			}else{
				sendMessage(mainChannel, game.printBoardLine(i), gameDelay);
			}
		}
		try{
		Thread.sleep(2000); }catch(Exception e){}
	}
	
	public void clearGame(){
		players[0] = null;
		players[1] = null;
		playingGame = false;
		game = null;
	}
	
	public void sendMessage(String channel, String message, int delay){
		client.sendMessage("PRIVMSG "+channel+" :"+message);
		try{
			Thread.sleep(delay);
		}catch(Exception e){
			
		}
	}
	
	public void sendMessage(String channel, String message){
		sendMessage(channel, message, 0);
	}
	
	public void sendMessage(String[] channels, String message, int delay){
		for(int i = 0; i < channels.length; i++){
			client.sendMessage("PRIVMSG "+channels[i]+" :"+message);
			try{
				Thread.sleep(delay);
			}catch(Exception e){
				
			}
		}
	}

	@Override
	public void sentMessageToServer(String message, int messagesSent) {
		System.out.println("I said: "+message);
	}

	@Override
	public void serverConnectionDied(int messagesSentTotal,
			int messagesReceivedTotal) {
		System.out.println("Server connection died. Messages sent total: "+messagesSentTotal+" Messages Received Total: "+messagesReceivedTotal);
		
	}

}
