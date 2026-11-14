package me.stky.backwork.service;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class to inspect classes using Java Reflection.
 */
public class ReflectionUtils {

    /**
     * Retrieves the names of all public methods declared in a class, including inherited methods.
     * This is generally the safest method for a command dispatcher, as it includes standard methods like toString().
     *
     * @param clazz The class to inspect.
     * @return A list of method names (Strings).
     */
    public static List<String> getAllDeclaredMethodNames(Class<?> clazz) {
        return java.util.Arrays.stream(clazz.getDeclaredMethods())
                .map(Method::getName)
                .collect(Collectors.toList());
    }
}