package Misc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Message {
	private String from = "";
	private String toHost = "";
	private String toChan = "";
	private String message = "";
	
	public Message(String msg){
		//:ShaneK!webchat@doc-72-47-40-190.we.ok.cebridge.net PRIVMSG #dreamincode :?
		msg = msg.trim();
		try {
			Pattern regex = Pattern.compile(":(.*)!(.*?) (?:.*?) (.*?) :(.*)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
			Matcher regexMatcher = regex.matcher(msg);
			if (regexMatcher.find()) {
				from = regexMatcher.group(1);
				toHost = regexMatcher.group(2);
				toChan = regexMatcher.group(3);
				message = regexMatcher.group(4);
			} 
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public String getFrom(){
		return this.from;
	}
	
	public String getToHost(){
		return this.toHost;
	}
	
	public String getToChan(){
		return this.toChan;
	}
	
	public String getText(){
		return this.message;
	}
	
	public String toString(){
		return "To: "+from+"\nTo Host: "+toHost+"\nTo Channel: "+toChan+"\nMessage: "+message;
	}
}
