package application;

import framework.Service;

@Service
public class Logger {
    public void log(String message){
        System.out.println("Logger: "+message);
    }
}
