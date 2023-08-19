package application;

import framework.EventListener;
import framework.Service;

@Service
public class AccountChangeListenerB {
    @EventListener
    public void listen(AccountChangeEvent accountChangeEvent){
        System.out.println("AccountChangeListenerB: "+accountChangeEvent);
    }
}
