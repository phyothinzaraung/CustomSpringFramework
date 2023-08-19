package application;

import framework.Service;

@Service
public class WhatsAppSender {
    public void sendWhatsAppMessage(String content) {
        System.out.println("sending whatsApp message: "+content);
    }
}
