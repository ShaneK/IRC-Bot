package Interface;

public interface ServerCallbackClass {
	void connectedToServer();
	void couldNotConnectToServer();
	void disconnectedFromServer(int messagesSentTotal, int messagesReceivedTotal);
	void receivedMessageFromServer(String message, int messagesReceived);
	void sentMessageToServer(String message, int messagesSent);
	void serverConnectionDied(int messagesSentTotal, int messagesReceivedTotal);
}
