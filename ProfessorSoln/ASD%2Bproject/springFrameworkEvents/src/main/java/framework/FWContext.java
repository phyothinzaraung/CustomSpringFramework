package framework;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

import org.reflections.Reflections;

public class FWContext {

    private static Map<String, Object> serviceObjectMap = new HashMap<>();
    private String activeProfile;
    private Method scheduledMethod = null;
    EventContext eventContext = new EventContext();


    public FWContext() {

    }

    public void readServiceClasses() {
        try {
            Reflections reflections = new Reflections("application");
            // find and instantiate all classes annotated with the @Service annotation
            Set<Class<?>> servicetypes = reflections.getTypesAnnotatedWith(Service.class);
            for (Class<?> serviceClass : servicetypes) {
                serviceObjectMap.put(serviceClass.getName(), (Object) serviceClass.getDeclaredConstructor().newInstance());
            }
            // create a publisher in the context
            serviceObjectMap.put("publisher", new EventPublisher(eventContext));

            performDI();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void performDI() {
        List<Object> serviceObjectsWithConstructorInjection = new ArrayList<>();
        Properties properties = ConfigFileReader.getConfigProperties();
        activeProfile = properties.getProperty("activeprofile");
        try {
            for (Object serviceObject : serviceObjectMap.values()) {
                Object newServiceObject = performConstructorInjection(serviceObject);
                if (newServiceObject != null) {
                    performFieldInjection(newServiceObject);
                    performSetterInjection(newServiceObject);
                    performValueInjection(newServiceObject, properties);
                    performScheduledMethods(newServiceObject);
                    performListenerMethods(newServiceObject);
                } else {
                    performFieldInjection(serviceObject);
                    performSetterInjection(serviceObject);
                    performValueInjection(serviceObject, properties);
                    performScheduledMethods(serviceObject);
                    performListenerMethods(serviceObject);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void performFieldInjection(Object serviceObject) throws IllegalAccessException {
        // find annotated fields
        for (Field field : serviceObject.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)) {
                if (field.isAnnotationPresent(Qualifier.class)) {
                    Annotation annotation = field.getAnnotation(Qualifier.class);
                    // get the name of the Qualifier annotation
                    String className = ((Qualifier) annotation).name();
                    //get the object instance of this type
                    Object instance = getServiceBeanWithName(className);
                    //do the injection
                    field.setAccessible(true);
                    field.set(serviceObject, instance);
                } else {
                    // get the type of the field
                    Class<?> theFieldType = field.getType();
                    //get the object instance of this type
                    Object instance = getServiceBeanOftype(theFieldType);
                    //do the injection
                    field.setAccessible(true);
                    field.set(serviceObject, instance);
                }
            }
        }
    }

    private void performSetterInjection(Object serviceObject) throws IllegalAccessException, InvocationTargetException {
        Method[] methods = serviceObject.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Autowired.class)) {
                //found setter method
                Class<?>[] methodParameters = method.getParameterTypes();
                Class<?> parameterType = methodParameters[0];
                //get the object instance of this type
                Object instance = getServiceBeanOftype(parameterType);
                //do the injection
                method.invoke(serviceObject, instance);
                //Object instance = getServiceBeanOftype(theFieldType);
            }
        }
    }

    private Object performConstructorInjection(Object serviceObject) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException {
        Constructor[] constructors = serviceObject.getClass().getDeclaredConstructors();
        for (Constructor constructor : constructors) {
            if (constructor.isAnnotationPresent(Autowired.class)) {
                //found constructor
                Class<?>[] methodParameters = constructor.getParameterTypes();
                Class<?> parameterType = methodParameters[0];
                //get the object instance of this type
                Object parameterInstance = getServiceBeanOftype(parameterType);
                //do the injection
                Object serviceClassInstance = (Object) constructor.newInstance(parameterInstance);
                serviceObjectMap.put(serviceClassInstance.getClass().getName(), serviceClassInstance);
                return serviceClassInstance;
            }
        }
        return null;
    }

    public Object getServiceBeanOftype(Class interfaceClass) {
        // if the class has an interface
        List<Object> objectList = new ArrayList<Object>();
        try {
            for (Object theServiceClass : serviceObjectMap.values()) {
                Class<?>[] interfaces = theServiceClass.getClass().getInterfaces();

                for (Class<?> theInterface : interfaces) {
                    if (theInterface.getName().contentEquals(interfaceClass.getName()))
                        objectList.add(theServiceClass);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //if (objectList.size() < 1) return null;
        if (objectList.size() == 1) return objectList.get(0);
        if (objectList.size() > 1) {
            for (Object theObject : objectList) {
                String profilevalue = theObject.getClass().getAnnotation(Profile.class).value();
                if (profilevalue.contentEquals(activeProfile)) {
                    return theObject;
                }
            }
        }
        // if the class has no interface
        try {
            for (Object theClass : serviceObjectMap.values()) {
                //check class without interface
                if (theClass.getClass().getName().equals(interfaceClass.getName()))
                    return theClass;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public Object getServiceBeanWithName(String className) {
        return serviceObjectMap.get(className);
    }

    private void performValueInjection(Object serviceObject, Properties properties) throws IllegalAccessException {

        // find annotated fields
        for (Field field : serviceObject.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Value.class)) {
                // get the type of the field
                Class<?> theFieldType = field.getType();
                if (field.getType().getName().contentEquals("java.lang.String")) {
                    // get attribute value
                    String attrValue = field.getAnnotation(Value.class).name();
                    // get the property value
                    String toBeInjectedString = properties.getProperty(attrValue);
                    // do the injection
                    field.setAccessible(true);
                    field.set(serviceObject, toBeInjectedString);
                }
            }
        }
    }

    private void performScheduledMethods(Object serviceObject) throws IllegalAccessException, InvocationTargetException {
        Method[] methods = serviceObject.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Scheduled.class)) {
                //found scheduled method
                scheduledMethod = method;
                //get the fixedRate
                Annotation annotation = method.getAnnotation(Scheduled.class);
                // get the name of the Qualifier annotation
                int rate = ((Scheduled) annotation).fixedRate();

                String cron = ((Scheduled) annotation).cron();

                Timer timer = new Timer();
                if (rate > 0)
                    timer.scheduleAtFixedRate(new FrameworkTimerTask(serviceObject, method), 0, rate);

                if (cron != "") {
                    int cronrate = getCronRate(cron);
					if (cronrate > 0)
                    timer.scheduleAtFixedRate(new FrameworkTimerTask(serviceObject, method), 0, cronrate);
                }
            }
        }
    }

    public int getCronRate(String cron) {
        String[] splitresult = cron.split(" ");
        String secondsString = splitresult[0];
        String minutesString = splitresult[1];
        int seconds = Integer.parseInt(secondsString);
        int minutes = Integer.parseInt(minutesString);
        return (minutes * 60 + seconds) *1000;
    }

    private void performListenerMethods(Object serviceObject)
    {
        Method[] methods = serviceObject.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(EventListener.class)) {
                //found event listener method
                //EventListenerMethod eventListenerMethod = new EventListenerMethod(serviceObject, method);
                Class<?>[] parameters = method.getParameterTypes();
                Class parameterClass = parameters[0];
                eventContext.addEventListeners(parameterClass.getName(),serviceObject, method);
            }
        }
    }
}
