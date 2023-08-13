package org.asd_final;

import org.reflections.Reflections;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class FWContext {

    private static List<Object> serviceObjectList = new ArrayList<>();

    private static Properties properties = new Properties();

    private Object applicationInstance;

    private Map<Class<?>, List<Object>> eventListeners = new HashMap<>();

    public void setApplicationInstance(Object applicationInstance) {
        this.applicationInstance = applicationInstance;
    }

    public void loadProperties() {
        try (InputStream input = FWContext.class.getClassLoader().getResourceAsStream("application.properties")) {
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Object getServiceBeanOftype(Class interfaceClass) {
        Object service = null;
        try {
            for (Object theClass : serviceObjectList) {
                System.out.println(theClass);
                Class<?>[] interfaces = theClass.getClass().getInterfaces();

                for (Class<?> theInterface : interfaces) {
                    if (theInterface.getName().contentEquals(interfaceClass.getName()))
                        service = theClass;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return service;
    }

    public void start(Class<?> clazz, String profile) throws InstantiationException, IllegalAccessException {
        Reflections reflections = new Reflections(clazz.getPackageName());
        scanAndInstantiateServiceClasses(reflections, profile);
        performDependencyInjection();
        performValueInjection();
        loadProperties();
        performRunnable(reflections);
        startScheduling();
        performAsyncMethods(reflections);
    }

    private void scanAndInstantiateServiceClasses(Reflections reflections, String profile) {
        Set<Class<?>> serviceTypes = reflections.getTypesAnnotatedWith(Service.class);
        for (Class<?> serviceClass : serviceTypes) {
            Profile profileAnnotation = serviceClass.getAnnotation(Profile.class);
            if (profileAnnotation == null || profileAnnotation.value().equals(profile)) {
                try {
                    serviceObjectList.add(serviceClass.getDeclaredConstructor().newInstance());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void performDependencyInjection() {
        for (Object service : serviceObjectList) {
            for (Field field : service.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    field.setAccessible(true);
                    Class<?> fieldType = field.getType();
                    if (field.isAnnotationPresent(Qualifier.class)) {
                        String qualifierValue = field.getAnnotation(Qualifier.class).value();
                        Object dependency = getServiceBeanOfTypeWithQualifier(fieldType, qualifierValue);
                        if (dependency != null) {
                            try {
                                field.set(service, dependency);
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        Object dependency = getServiceBeanOftype(fieldType);
                        if (dependency != null) {
                            try {
                                field.set(service, dependency);
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    private void performValueInjection() {
        for (Object service : serviceObjectList) {
            for (Field field : service.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Value.class)) {
                    Value valueAnnotation = field.getAnnotation(Value.class);
                    String propertyKey = valueAnnotation.value();

                    if (properties.containsKey(propertyKey)) {
                        String propertyValue = properties.getProperty(propertyKey);
                        field.setAccessible(true);
                        try {
                            field.set(service, propertyValue);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private Object getServiceBeanOfTypeWithQualifier(Class<?> interfaceClass, String qualifierValue) {
        for (Object service : serviceObjectList) {
            if (interfaceClass.isInstance(service)) {
                Qualifier qualifier = service.getClass().getAnnotation(Qualifier.class);
                if (qualifier != null && qualifier.value().equals(qualifierValue)) {
                    return service;
                }
            }
        }
        return null;
    }

    public static List<Object> getServiceObjectList() {
        return serviceObjectList;
    }

    void performRunnable(Reflections reflections) throws InstantiationException, IllegalAccessException {
        Set<Class<?>> apps = reflections.getTypesAnnotatedWith(FWApplication.class);
        if (apps.size() > 1 || apps.size() == 0) {
            System.out.println("FWApplication must declare only one with Runnable");
            return;
        }
        Class<?> appClass = apps.iterator().next();
        Object applicationInstance = appClass.newInstance();
        Runnable runnable = (Runnable) appClass.newInstance();

        //performDependencyInjection();
        injectFields(runnable);
        setApplicationInstance(applicationInstance);

        runnable.run();
    }

    public static void run(Class<?> applicationClass, String profile) {
        FWContext fWContext = new FWContext();
        try {
            fWContext.start(applicationClass, profile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void injectValueField(Object instance, Field field) throws IllegalAccessException {
        if (field.isAnnotationPresent(Value.class)) {
            Value valueAnnotation = field.getAnnotation(Value.class);
            String propertyKey = valueAnnotation.value();

            if (properties.containsKey(propertyKey)) {
                String propertyValue = properties.getProperty(propertyKey);
                field.setAccessible(true);
                try {
                    field.set(instance, propertyValue);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected void injectFields(Object instance) throws IllegalAccessException {
        for (Field field : instance.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)) {
                if (field.isAnnotationPresent(Qualifier.class)) {
                    injectFieldWithQualifier(instance, field);
                } else {
                    injectField(instance, field, "");
                }
            } else if (field.isAnnotationPresent(Value.class)) {
                injectValueField(instance, field);
            }
        }
    }

    private boolean isValueReadFromProperties(String value) {
        return (value.startsWith("${") && value.endsWith("}"));
    }

    private String readFromProperties(String value) {
        if (!isValueReadFromProperties(value)) {
            return "";
        }


        String propertyName = value.substring(2, value.length() - 1);
        String propertyValue = properties.getProperty(propertyName);

        return propertyValue;
    }

    private void injectFieldWithQualifier(Object instance, Field field) throws IllegalAccessException {
//        Annotation[] annotations = field.getAnnotations();
//        for (Annotation annotation : annotations){
//
//            if(annotation.annotationType().getName().equals(Qualifier.class.getName())) {
//                Qualifier q = (Qualifier)annotation;
//
//                injectField(instance, field,q.value());
//            }
//        }
    }


    private void injectField(Object instance, Field field, String name) throws IllegalAccessException {
        Class<?> fieldType = field.getType();
        Object fieldInstance = getServiceBeanOftype(fieldType);
        setField(field, instance, fieldInstance);
    }

    private void injectFieldWithoutQualifier(Object instance, Field field) throws IllegalAccessException {
        injectField(instance, field, "");
    }

    private void setField(Field field, Object instance, Object fieldInstance) throws IllegalAccessException {
        field.setAccessible(true);
        field.set(instance, fieldInstance);
    }

    public void startScheduling() {
        if (applicationInstance != null) {
            performScheduledMethods(applicationInstance);
        }
    }

    private void performScheduledMethods(Object instance) {
        Class<?> appClass = instance.getClass();

        for (Method method : appClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Scheduled.class)) {
                performScheduledMethod(method, instance);
            }
        }
    }

    private void performScheduledMethod(Method method, Object instance) {
        Scheduled scheduledAnnotation = method.getAnnotation(Scheduled.class);
        long fixedRate = scheduledAnnotation.fixedRate();
        String cronExpression = scheduledAnnotation.cron();
        if (fixedRate > 0) {
            scheduleMethodWithFixedRate(instance, method, fixedRate);
        }
        if (!cronExpression.isEmpty()) {
            scheduleMethodWithCron(instance, method, cronExpression);
        }
    }

    private void scheduleMethodWithFixedRate(Object instance, Method method, long fixedRate) {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    method.invoke(instance);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, fixedRate);
    }

    private void scheduleMethodWithCron(Object instance, Method method, String cronExpression) {

        String[] cronParts = cronExpression.split(" ");
        if (cronParts.length == 2) {
            int seconds = Integer.parseInt(cronParts[0]);
            int minutes = Integer.parseInt(cronParts[1]);

            if (seconds < 60 && minutes < 60) {
                int totalSecond = seconds + (minutes * 60);
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            method.invoke(instance);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }, totalSecond * 1000);
            }
        }
    }

    public void subscribeEventListener(Object listener) {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(EventListener.class)) {
                Class<?> eventType = method.getParameterTypes()[0];
                eventListeners.computeIfAbsent(eventType, key -> new ArrayList<>()).add(listener);
            }
        }
    }

    public void publishEvent(Object event) {
        List<Object> listeners = eventListeners.get(event.getClass());
        if (listeners != null) {
            for (Object listener : listeners) {
                try {
                    for (Method method : listener.getClass().getDeclaredMethods()) {
                        if (method.isAnnotationPresent(EventListener.class) &&
                                method.getParameterTypes()[0].equals(event.getClass())) {
                            method.setAccessible(true);
                            method.invoke(listener, event);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void performAsyncMethods(Reflections reflections) {
        Set<Class<?>> apps = reflections.getTypesAnnotatedWith(FWApplication.class);
        if (apps.size() != 1) {
            System.out.println("FWApplication must declare only one with Runnable");
            return;
        }
        Class<?> appClass = apps.iterator().next();
        Object applicationInstance = null;

        try {
            applicationInstance = appClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return;
        }

        for (Method method : appClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Async.class)) {
                executeAsyncMethod(method, applicationInstance);
            }
        }
    }

    private void executeAsyncMethod(Method method, Object instance) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                method.invoke(instance);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
