package application;

import framework.*;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

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

    @Scheduled(fixedRate = 5000)
    public void welcome() {
        Date date = Calendar.getInstance().getTime();
        DateFormat timeFormatter = DateFormat.getTimeInstance(DateFormat.DEFAULT);
        String currenttime = timeFormatter.format(date);
        System.out.println("This task runs at " + currenttime);
    }

    @Scheduled(cron = "8 0")
    public void welcome2() {
        Date date = Calendar.getInstance().getTime();
        DateFormat timeFormatter = DateFormat.getTimeInstance(DateFormat.DEFAULT);
        String currenttime = timeFormatter.format(date);
        System.out.println("This cron task runs at " + currenttime);
    }

}