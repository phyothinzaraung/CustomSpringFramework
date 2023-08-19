package application;
import framework.Autowired;
import framework.SpringFramework;

public class Application implements Runnable {
    @Autowired
    private BankService bankService;

    public static void main(String[] args) {
        SpringFramework.run(Application.class);
    }

    @Override
    public void run() {
        bankService.deposit();
    }
}

