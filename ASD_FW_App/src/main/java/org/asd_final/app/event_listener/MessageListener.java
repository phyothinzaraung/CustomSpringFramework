package org.asd_final.app.event_listener;

import org.asd_final.EventListener;

public class MessageListener {

    @EventListener
    public void handleMyEvent(MessageEvent event){
        System.out.println("Received Event: " + event.getMessage());
    }
}
