package application;

import framework.Service;

@Service
public class SmsSender {
    public void sendSMS(String content) {
        System.out.println("sending sms: "+content);
    }
}
