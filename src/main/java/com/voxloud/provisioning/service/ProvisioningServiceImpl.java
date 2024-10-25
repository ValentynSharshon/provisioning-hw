package com.voxloud.provisioning.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.voxloud.provisioning.entity.Device;
import com.voxloud.provisioning.exception.ResourceNotFoundException;
import com.voxloud.provisioning.exception.ReadPropertiesFromFileException;
import com.voxloud.provisioning.repository.DeviceRepository;
import com.voxloud.provisioning.util.PropertyFileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProvisioningServiceImpl implements ProvisioningService {

    private final DeviceRepository deviceRepository;

    public String getProvisioningFile(String macAddress) throws Exception {
        Device device = deviceRepository.findDeviceByMacAddress(macAddress)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "GET_PROVISIONING_FILE",
                        String.valueOf(HttpStatus.NOT_FOUND.value()),
                        String.format("Device with MAC address: %s not found.", macAddress)
                ));

        Map<String, Object> properties = readFromPropertiesFile();

        if (Device.DeviceModel.CONFERENCE.equals(device.getModel())) {
            return processConferenceProperties(device, properties);
        } else if (Device.DeviceModel.DESK.equals(device.getModel())) {
            return processDeskProps(device, properties);
        } else {
            log.error("Device model not found.");
            throw new ResourceNotFoundException(
                    "GET_PROVISIONING_FILE",
                    String.valueOf(HttpStatus.NOT_FOUND.value()),
                    "Device model not found."
            );
        }
    }

    private Map<String, Object> readFromPropertiesFile() throws ReadPropertiesFromFileException {
        Map<String, Object> propertyMap;

        try {
            Properties properties = PropertiesLoaderUtils.loadAllProperties("application.properties");
            propertyMap = PropertyFileUtils.processProperties(properties, "provisioning");
        } catch (IOException ex) {
            log.error("Error during processing properties file.", ex);
            throw new ReadPropertiesFromFileException(
                    "GET_PROVISIONING_FILE -> READ_FROM_PROPERTIES_FILE",
                    String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()),
                    "Error during processing properties file.");
        }

        return propertyMap;
    }

    private String processDeskProps(Device device, Map<String, Object> propertyMap) {
        if (!StringUtils.isEmpty(device.getOverrideFragment())) {
            Map<String, Object> overrideFragmentMap = Arrays.stream(device.getOverrideFragment().split("\\n"))
                    .map(s -> s.split("="))
                    .collect(Collectors.toMap(s -> s[0], s -> s[1]));

            PropertyFileUtils.processOverrideFragment(overrideFragmentMap, propertyMap);
        }

        addPropsFromDB(device, propertyMap);

        return processMapToPropsFile(propertyMap);
    }

    private String processMapToPropsFile(Map<String, Object> propertyMap) {
        StringBuilder responseBuilder = new StringBuilder();

        for (Object key : propertyMap.keySet()) {
            String keyStr = key.toString();
            Object value = propertyMap.get(keyStr);

            if (propertyMap.containsKey(keyStr)) {
                if (value instanceof String && ((String) value).contains(",")) {
                    value = Arrays.asList(((String) value).split(","));
                }
                responseBuilder.append(keyStr)
                        .append("=")
                        .append(value)
                        .append(System.lineSeparator());
            }
        }

        String response = responseBuilder.toString();

        return Optional.of(response).filter(str -> str.endsWith(System.lineSeparator()))
                .map(str -> str.substring(0, str.length() - 1))
                .orElse(response);
    }

    private String processConferenceProperties(Device device, Map<String, Object> propertyMap) throws ReadPropertiesFromFileException {
        try {
            if (!StringUtils.isEmpty(device.getOverrideFragment())) {
                String overrideFragment = device.getOverrideFragment();
                ObjectReader reader = new ObjectMapper().readerFor(Map.class);
                Map<String, Object> overrideFragmentMap = reader.readValue(overrideFragment);
                PropertyFileUtils.processOverrideFragment(overrideFragmentMap, propertyMap);
            }
            addPropsFromDB(device, propertyMap);
            return new ObjectMapper().writeValueAsString(propertyMap);
        } catch (JsonProcessingException ex) {
            log.error("Error processing JSON file.", ex);
            throw new ReadPropertiesFromFileException(
                    "GET_PROVISIONING_FILE -> PROCESS_CONFERENCE_PROPERTIES",
                    String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()),
                    "Error processing JSON file."
            );
        }
    }

    private void addPropsFromDB(Device device, Map<String, Object> properties) {
        properties.put("username", device.getUsername());
        properties.put("password", device.getPassword());
    }
}
