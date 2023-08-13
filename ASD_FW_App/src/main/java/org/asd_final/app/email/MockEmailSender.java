package org.asd_final.app.email;

public class MockEmailSender implements IEmailSender{

    private static MockEmailSender mockEmailSender;

    private MockEmailSender(){
        if (mockEmailSender != null){
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    public static synchronized MockEmailSender getMockEmailSender() {
        if(mockEmailSender == null){
            mockEmailSender = new MockEmailSender();
        }
        return mockEmailSender;
    }

    @Override
    public void sendEmail() {
        System.out.println("Sending email via Testing Server.");
    }
}
