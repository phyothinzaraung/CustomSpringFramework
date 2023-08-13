package org.asd_final.app.event_listener;

public class MessageEvent {

    private String message;

    public MessageEvent(String message){
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
