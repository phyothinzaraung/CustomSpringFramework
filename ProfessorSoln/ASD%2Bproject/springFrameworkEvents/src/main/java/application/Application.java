package application;
import framework.Autowired;
import framework.SpringFramework;

import java.lang.reflect.InvocationTargetException;

public class Application implements Runnable {
    @Autowired
    private BankService bankService;

    public static void main(String[] args) {
        SpringFramework.run(Application.class);
    }

    @Override
    public void run() {
        try {
            bankService.deposit();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}

