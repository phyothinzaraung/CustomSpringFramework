package application;

import framework.EventListener;
import framework.Service;

@Service
public class AccountChangeListenerA {
    @EventListener
    public void listen(AccountChangeEvent accountChangeEvent){
        System.out.println("AccountChangeListenerA: "+accountChangeEvent);
    }
}
