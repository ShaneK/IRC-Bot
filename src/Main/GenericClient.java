package Main;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

import Interface.ServerCallbackClass;
import Misc.Constants;

/**
 * @author Shane
 *
 * Generic client for use with generic server. Automagically determines if server is encrypted and responds appropriately.
 */

public class GenericClient implements Runnable {
	private String IP = null;
	private int port = 0;
	private boolean encrypted = false;
	private Socket connection = null;
	private BufferedReader serverIn;
	private DataOutputStream serverOut;
	private ServerCallbackClass callbackMachine = null;
	private int messagesSent = 0;
	private int messagesReceived = 0;
	private BufferedInputStream serverInputForByteReading;
	
	//Has to do with reading bytes instead of strings
	private boolean readingBytes = false;
	private int numOfBytes = 0;
	
	public GenericClient(String serverIP, int port, ServerCallbackClass scc) {
		this.IP = serverIP;
		this.port = port;
		try{
			connection = new Socket(serverIP, port);
			serverIn = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			serverOut = new DataOutputStream(connection.getOutputStream());
			serverInputForByteReading = new BufferedInputStream(connection.getInputStream());
			this.callbackMachine = scc;
			if(scc != null)
				scc.connectedToServer();
			(new Thread(this)).start();
		}catch(Exception e){
			e.printStackTrace();
			if(this.callbackMachine != null)
				this.callbackMachine.couldNotConnectToServer();
		}
	}
	
	public void sendMessage(String message){
		try{
			this.serverOut.writeBytes(message+"\n");
			this.callbackMachine.sentMessageToServer(message, ++messagesSent);
		}catch(Exception e){
			e.printStackTrace();
			if(this.callbackMachine != null){
				this.callbackMachine.serverConnectionDied(messagesSent, messagesReceived);
			}
		}
	}

	@Override
	public void run() {
		System.out.println("Being awesome.");
		try{
			while(true){
				if(readingBytes){
					System.out.println("Reading bytes now1");
					byte[] message = new byte[numOfBytes];
					System.out.println("Declared variable, reading into byte[]");
					serverInputForByteReading.read(message);
					System.out.println("Read Bytes, saying OKAY");
					serverOut.writeBytes(Constants.OKAY+'\n');
					System.out.println("Said OKAY; Bytes read: "+new String(message));
				}else{
					System.out.println("Waiting for message");
					String message = serverIn.readLine();
					if(this.callbackMachine != null){
						this.callbackMachine.receivedMessageFromServer(message, ++messagesReceived);
					}
				}
			}
		}catch(Exception e){
			if(this.callbackMachine != null){
				this.callbackMachine.serverConnectionDied(messagesSent, ++messagesReceived);
			}
		}
	}
	
	public int getPort(){
		return this.port;
	}
	
	public String getIP(){
		return this.IP;
	}
	
	public boolean isEncrypted(){
		return this.encrypted;
	}
}
