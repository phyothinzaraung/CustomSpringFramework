package framework;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import org.reflections.Reflections;

public class FWContext {

	private static Map<String, Object> serviceObjectMap = new HashMap<>();

	public FWContext() {

	}

	public void readServiceClasses(){
		try {
			Reflections reflections = new Reflections("application");
			// find and instantiate all classes annotated with the @Service annotation
			Set<Class<?>> servicetypes = reflections.getTypesAnnotatedWith(Service.class);
			for (Class<?> serviceClass : servicetypes) {
				serviceObjectMap.put(serviceClass.getName(), (Object) serviceClass.getDeclaredConstructor().newInstance());
			}

			performDI();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void performDI() {
		//List<Object> serviceObjectsWithConstructorInjection = new ArrayList<>();
		Properties properties = ConfigFileReader.getConfigProperties();
		try {
			for (Object serviceObject : serviceObjectMap.values()) {
				Object newServiceObject = performConstructorInjection(serviceObject);
				if (newServiceObject != null){
					performFieldInjection(newServiceObject);
					performSetterInjection(newServiceObject);
					performValueInjection(newServiceObject, properties);
				}
				else{
					performFieldInjection(serviceObject);
					performSetterInjection(serviceObject);
					performValueInjection(serviceObject, properties);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void performFieldInjection(Object serviceObject) throws IllegalAccessException {
		// find annotated fields
		Field[] fields = serviceObject.getClass().getDeclaredFields();
		for (Field field : fields) {
			if (field.isAnnotationPresent(Autowired.class)) {
				if (field.isAnnotationPresent(Qualifier.class)) {
					//field injection by name
					//Annotation annotation=field.getAnnotation(Qualifier.class);
					// get the name of the Qualifier annotation
					String className = field.getAnnotation(Qualifier.class).name();
					//get the object instance of this type
					Object instance = getServiceBeanWithName(className);
					//do the injection
					field.setAccessible(true);
					field.set(serviceObject, instance);
				}
				else {
					// field injection by type
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
				method.invoke(serviceObject,instance);
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
				Object serviceClassInstance =(Object) constructor.newInstance(parameterInstance);
				serviceObjectMap.put(serviceClassInstance.getClass().getName(), serviceClassInstance);
				return serviceClassInstance;
			}
		}
		return null;
	}

	public Object getServiceBeanOftype(Class interfaceClass) {

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
				Field[] fields = serviceObject.getClass().getDeclaredFields();
				// find annotated fields
				for (Field field : fields) {
					if (field.isAnnotationPresent(Value.class)) {
						// get the type of the field
//						Class<?> theFieldType = field.getType();
//						if (field.getType().getName().contentEquals("java.lang.String")) {
							// get attribute value
							String attrValue = field.getAnnotation(Value.class).name();
							// get the property value
							String toBeInjectedString = properties.getProperty(attrValue);
							// do the injection
							field.setAccessible(true);
							field.set(serviceObject, toBeInjectedString);
//						}
					}
				}
			}

}
