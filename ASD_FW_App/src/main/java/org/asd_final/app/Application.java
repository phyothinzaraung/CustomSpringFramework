package org.asd_final.app;

import org.asd_final.Autowired;
import org.asd_final.FWApplication;
import org.asd_final.FWContext;
import org.asd_final.Scheduled;
import org.asd_final.app.domain.Account;
import org.asd_final.app.domain.AccountEntry;
import org.asd_final.app.domain.Customer;
import org.asd_final.app.service.IAccountService;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

@FWApplication
public class Application implements Runnable {
    @Autowired
    private IAccountService accountService;

    @Scheduled(fixedRate = 5000)
    public void welcome(){
        Date date = Calendar.getInstance().getTime();
        DateFormat timeFormatter = DateFormat.getTimeInstance(DateFormat.DEFAULT);
        String current_time = timeFormatter.format(date);
        System.out.println("This task runs at " + current_time);
    }

    @Scheduled(cron = "1 0")
    public void scheduledTaskWithCron() {
        System.out.println("Scheduled task with cron expression executed.");
    }

    public static void main(String[] args) {
        FWContext.run(Application.class, "prod");

        FWContext context = new FWContext();
        // Create and register an event listener
        MessageListener myEventListener = new MessageListener();
        context.subscribeEventListener(myEventListener);

        // Publish an event
        SendMessageEvent myEvent = new SendMessageEvent("ASD Final Project");
        context.publishEvent(myEvent);
    }

    @Override
    public void run() {
        // create 2 accounts;
        accountService.createAccount(1263862, "Frank Brown");
        accountService.createAccount(4253892, "John Doe");
        //use account 1;
        accountService.deposit(1263862, 240);
        accountService.deposit(1263862, 529);
        accountService.withdraw(1263862, 230);
        //use account 2;
        accountService.deposit(4253892, 12450);
        accountService.transferFunds(4253892, 1263862, 100, "payment of invoice 10232");
        // show balances

        Collection<Account> accountlist = accountService.getAllAccounts();
        Customer customer = null;
        for (Account account : accountlist) {
            customer = account.getCustomer();
            System.out.println("Statement for Account: " + account.getAccountnumber());
            System.out.println("Account Holder: " + customer.getName());
            System.out.println("-Date-------------------------"
                    + "-Description------------------"
                    + "-Amount-------------");
            for (AccountEntry entry : account.getEntryList()) {
                System.out.printf("%30s%30s%20.2f\n", entry.getDate()
                        .toString(), entry.getDescription(), entry.getAmount());
            }
            System.out.println("----------------------------------------"
                    + "----------------------------------------");
            System.out.printf("%30s%30s%20.2f\n\n", "", "Current Balance:",
                    account.getBalance());
        }
    }
}
