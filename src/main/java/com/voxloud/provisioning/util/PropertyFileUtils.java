package com.voxloud.provisioning.util;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class PropertyFileUtils {

    private PropertyFileUtils() {
    }

    public static Map<String, Object> processProperties(Properties properties, String prefix) {
//        Map<String, Object> propertiesMap = new HashMap<>();
//
//        for (Object key : properties.keySet()) {
//            String keyStr = key.toString();
//            if (keyStr.startsWith(prefix)) {
//                Object value = properties.getProperty(keyStr);
//                if (value != null && ((String) value).contains(",")) {
//                    value = Arrays.asList(((String) value).split(","));
//                }
//                propertiesMap.put(keyStr.replace(prefix + ".", ""), value);
//            }
//        }
//
//        return propertiesMap;

        return properties.keySet().stream()
                .map(Object::toString)
                .filter(key -> key.startsWith(prefix))
                .collect(Collectors.toMap(
                        key -> key.replace(prefix + ".", ""),
                        key -> {
                            String value = properties.getProperty(key);
                            return (value != null && value.contains(","))
                                    ? Arrays.asList(value.split(","))
                                    : value;
                        }
                ));
    }

    public static void processOverrideFragment(Map<String, Object> newProperties, Map<String, Object> existingProperties) {
//        for (Object key : newProperties.keySet()) {
//            String keyStr = key.toString();
//            Object value = newProperties.get(keyStr);
//
//            if (existingProperties.containsKey(keyStr)) {
//                if (value instanceof String && ((String) value).contains(",")) {
//                    value = Arrays.asList(((String) value).split(","));
//                }
//                existingProperties.replace(keyStr, value);
//            }
//        }

        newProperties.forEach((key, value) -> {
            if (existingProperties.containsKey(key)) {
                Object updatedValue = (value instanceof String && ((String) value).contains(","))
                        ? Arrays.asList(((String) value).split(","))
                        : value;
                existingProperties.replace(key, updatedValue);
            }
        });
    }
}
