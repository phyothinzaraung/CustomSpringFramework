package org.asd_final.app.email;

public class EmailSender implements IEmailSender{

    private static EmailSender emailSender;

    private EmailSender(){
        if (emailSender != null) {
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    public static EmailSender getEmailSender() {
        if(emailSender == null){
            synchronized (EmailSender.class){
                if(emailSender== null){
                    emailSender = new EmailSender();
                }
            }
        }
        return emailSender;
    }

    @Override
    public void sendEmail() {
        System.out.println("Sending email via Production Server.");
    }
}
