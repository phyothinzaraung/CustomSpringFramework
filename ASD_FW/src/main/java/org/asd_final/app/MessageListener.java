package org.asd_final.app;

import org.asd_final.EventListener;
import org.asd_final.Service;

public class MessageListener {
    @EventListener
    public void eventListener(SendMessageEvent event){
        System.out.println("Received " + event.message);
    }
}
