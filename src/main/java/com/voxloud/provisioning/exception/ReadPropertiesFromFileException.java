package com.voxloud.provisioning.exception;

import lombok.Getter;

@Getter
public class ReadPropertiesFromFileException extends Exception {

    private final String errorPlaceInfo;
    private final String code;
    private final String message;

    public ReadPropertiesFromFileException(String errorPlaceInfo, String code, String message) {
        super(message);
        this.errorPlaceInfo = errorPlaceInfo;
        this.code = code;
        this.message = message;
    }
}
