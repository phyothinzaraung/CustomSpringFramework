package application;

import framework.Profile;
import framework.Service;
import framework.Value;

@Service
@Profile(value="One")
public class EmailSenderImpl implements EmailSender{
	@Value(name="message")
	String theMessage;

	public void sendEmail(String content) {
		System.out.println("sending email: "+content+" , message="+theMessage);
		
	}

}
