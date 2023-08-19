package application;

import framework.Autowired;
import framework.Qualifier;
import framework.Service;
import framework.Value;

@Service
public class BankService {
    @Autowired
    @Qualifier(name="application.Logger")
    private Logger logger;
    @Autowired
    private EmailSender emailSender;
    private SmsSender smsSender;
    private WhatsAppSender whatsAppSender;

    @Value(name="bankname")
    String bankName;

    public BankService() {
    }

    @Autowired
    public BankService(WhatsAppSender whatsAppSender) {
        this.whatsAppSender = whatsAppSender;
    }

    public void setEmailService(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    @Autowired
    public void setSmsSender(SmsSender smsSender) {
        this.smsSender = smsSender;
    }

    public void deposit() {
        emailSender.sendEmail(bankName+ "  deposit");
        smsSender.sendSMS("deposit");
        whatsAppSender.sendWhatsAppMessage("deposit");
        logger.log("deposit");
    }
}