/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package RunTestsAssignment;

/**
 *
 * @author xab
 */
import java.lang.reflect.*;
import java.lang.annotation.*;
import java.util.*;

// RunTests.java
public class RunTests {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java RunTests <className>");
            return;
        }
        String className = args[0];
        try {
            // Load the class by name and create an instance using the default constructor.
            Class<?> cls = Class.forName(className);
            Object instance = cls.getConstructor().newInstance();
            
            // For each declared method, if it is non private, non static and annotated with testable,
            // run the test according to its specification.
            Method[] methods = cls.getDeclaredMethods();
            for (Method method : methods) {
                // Skip static or private methods.
                if (Modifier.isPrivate(method.getModifiers()) || Modifier.isStatic(method.getModifiers()))
                    continue;
                if (method.getAnnotation(Testable.class) == null)
                    continue;
                
                Specification spec = method.getAnnotation(Specification.class);
                Report.TEST_RESULT result = null;  

                // Retrieve the specification of the arguments.
                String[] specArgTypes = spec.argTypes();
                String[] specArgValues = spec.argValues();
                Class<?>[] methodParamTypes = method.getParameterTypes();

                // Check the number of parameters and provided specification arrays.
                if (methodParamTypes.length != specArgTypes.length || methodParamTypes.length != specArgValues.length) {
                    result = Report.TEST_RESULT.WrongArgs;
                    Report.report(result, method.getName(), spec);
                    continue;
                }
                
                // Prepare an array for the converted arguments.
                Object[] invocationArgs = new Object[methodParamTypes.length];
                boolean argConversionError = false;
                for (int i = 0; i < methodParamTypes.length; i++) {
                    // Determine the expected type from the specification.
                    Class<?> expectedType = getClassForSpecType(specArgTypes[i]);
                    // Check compatibility between the method's parameter and the expected type.
                    if (expectedType == null || !areCompatible(methodParamTypes[i], expectedType)) {
                        argConversionError = true;
                        break;
                    }
                    try {
                        invocationArgs[i] = convertValue(specArgTypes[i], specArgValues[i]);
                    } catch (Exception e) {
                        argConversionError = true;
                        break;
                    }
                }
                if (argConversionError) {
                    result = Report.TEST_RESULT.WrongArgs;
                    Report.report(result, method.getName(), spec);
                    continue;
                }
                
                // Check return type against the specification.
                String specResType = spec.resType();
                Object expectedResult = null;
                if (specResType.equals("")) {
                    // Specification indicates no return value. The method must be void.
                    if (method.getReturnType() != void.class) {
                        result = Report.TEST_RESULT.WrongResultType;
                        Report.report(result, method.getName(), spec);
                        continue;
                    }
                } else {
                    // Determine the expected return type.
                    Class<?> expectedReturnType = getClassForSpecType(specResType);
                    if (expectedReturnType == null || !areCompatible(method.getReturnType(), expectedReturnType)) {
                        result = Report.TEST_RESULT.WrongResultType;
                        Report.report(result, method.getName(), spec);
                        continue;
                    }
                    // Try converting the expected result.
                    try {
                        expectedResult = convertValue(specResType, spec.resVal());
                    } catch (Exception e) {
                        result = Report.TEST_RESULT.WrongResultType;
                        Report.report(result, method.getName(), spec);
                        continue;
                    }
                }
                
                // Invoke the method and check the outcome.
                try {
                    Object actualResult = method.invoke(instance, invocationArgs);
                    if (method.getReturnType() == void.class) {
                        // For void methods, we consider the test succeeded if no exception was thrown.
                        result = Report.TEST_RESULT.TestSucceeded;
                    } else {
                        if ((actualResult == null && expectedResult == null)
                            || (actualResult != null && actualResult.equals(expectedResult))) {
                            result = Report.TEST_RESULT.TestSucceeded;
                        } else {
                            result = Report.TEST_RESULT.TestFailed;
                        }
                    }
                } catch (Exception e) {
                    // If any exception occurs during invocation, treat it as a test failure.
                    result = Report.TEST_RESULT.TestFailed;
                }
                // Report the result exactly once per testable method.
                Report.report(result, method.getName(), spec);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Maps the specification string to a Class.
    private static Class<?> getClassForSpecType(String type) {
        switch (type) {
            case "int": return int.class;
            case "double": return double.class;
            case "bool": return boolean.class;
            case "string": return String.class;
            default: return null;
        }
    }
    
    // Checks if the method parameter type is compatible with the expected type from the specification.
    private static boolean areCompatible(Class<?> methodParam, Class<?> specType) {
        // If the method parameter is primitive, compare directly.
        if (methodParam.isPrimitive()) {
            return methodParam.equals(specType);
        } else {
            if (specType.isPrimitive()) {
                if (specType == int.class && methodParam.equals(Integer.class)) return true;
                if (specType == double.class && methodParam.equals(Double.class)) return true;
                if (specType == boolean.class && methodParam.equals(Boolean.class)) return true;
                return false;
            }
            return methodParam.equals(specType);
        }
    }
    
    // Converts a string value to an object of the type specified by the type string.
    private static Object convertValue(String type, String value) throws Exception {
        switch (type) {
            case "int":
                return Integer.parseInt(value);
            case "double":
                return Double.parseDouble(value);
            case "bool":
                // Boolean.parseBoolean returns false for any string that is not "true"
                return Boolean.parseBoolean(value);
            case "string":
                return value;
            default:
                throw new Exception("Unsupported type: " + type);
        }
    }
}
