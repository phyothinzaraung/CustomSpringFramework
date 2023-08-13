package org.asd_final;

import org.asd_final.app.CustomerService;
import org.asd_final.app.MessageListener;
import org.asd_final.app.SendMessageEvent;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

@FWApplication
public class Application implements Runnable{
    @Autowired
    CustomerService customerService;

//    @Scheduled(fixedRate = 1000)
//    public void welcome() {
//        Date date = Calendar.getInstance().getTime();
//        DateFormat timeFormatter = DateFormat.getTimeInstance(DateFormat.DEFAULT);
//        String current_time = timeFormatter.format(date);
//        System.out.println("This task runs at " + current_time);
//    }
//
//    @Scheduled(cron = "1 0")
//    public void scheduledTaskWithCron() {
//        System.out.println("Scheduled task with cron expression executed.");
//    }

    public static void main(String[] args) throws InstantiationException, IllegalAccessException {

//        FWContext fwContext = new FWContext();
//
//        // Create and register an event listener
//        MessageListener myEventListener = new MessageListener();
//        fwContext.subscribeEventListener(myEventListener);
//
//        // Publish an event
//        SendMessageEvent myEvent = new SendMessageEvent("Hello, World!");
//        fwContext.publishEvent(myEvent);
//        FWContext.run(Application.class, "prod");

        //FWContext fwContext = new FWContext();
        FWContext.run(Application.class, "prod");

        //performAsyncMethods(fwContext);
    }
    @Override
    public void run() {
        customerService.print();
    }

    public void runAsyncMethods() throws InstantiationException, IllegalAccessException {
        FWContext fwContext = new FWContext();
        fwContext.start(Application.class, "prod");
    }

    @Async
    public void performAsyncMethod() {
        System.out.println("Async method started.");
        try {
            Thread.sleep(3000); // Simulating some async work
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Async method completed.");
    }

    public static void performAsyncMethods(FWContext fwContext) {
        // Call the async method from the Application class
        Application application = new Application();
        application.performAsyncMethod();

        // Use FWContext to execute async methods
        Reflections reflections = new Reflections(Application.class.getPackageName());
        fwContext.performAsyncMethods(reflections);

        // Wait for async methods to complete
        try {
            Thread.sleep(5000); // Give some time for async methods to finish
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
