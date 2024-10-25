package com.voxloud.provisioning.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseBody
    public ErrorResponse handleException(HttpServletRequest request, ResourceNotFoundException exp) {
        log.error("Resource not found exception.", exp);

        return getErrorResponse(exp.getErrorPlaceInfo(), exp.getCode(), exp.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(ReadPropertiesFromFileException.class)
    @ResponseBody
    public ErrorResponse handleDatabaseException(HttpServletRequest request, ReadPropertiesFromFileException exp) {
        log.error("Read from properties file exception.", exp);

        return getErrorResponse(exp.getErrorPlaceInfo(), exp.getCode(), exp.getMessage());
    }


    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ErrorResponse methodArgumentExceptionHandler(HttpServletRequest request, MethodArgumentNotValidException exp) {
        log.error("Method argument exception.", exp);

        return getErrorResponse(
                "UNKNOWN",
                "METHOD ARGUMENT EXCEPTION",
                getErrorMessage(exp.getBindingResult())
        );
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ErrorResponse genericExceptionHandler(HttpServletRequest request, Exception exp) {
        log.error("Global exception.", exp);

        return getErrorResponse(
                "UNKNOWN",
                "GLOBAL EXCEPTION",
                exp.getMessage()
        );
    }

    public static String getErrorMessage(BindingResult bindingResult) {
        StringBuilder message = new StringBuilder();
        List<ObjectError> objectErrors = bindingResult.getGlobalErrors();

        for (ObjectError objectError : objectErrors) {
            message.append(objectError.getObjectName())
                    .append(" : ")
                    .append(" [")
                    .append(objectError.getDefaultMessage())
                    .append("] ");
        }

        return message.toString();
    }

    private ErrorResponse getErrorResponse(String code, String feature, String cause) {
        ErrorResponse response = new ErrorResponse();
        response.setErrorPlaceInfo(feature);
        response.setCode(code);
        response.setCause(cause);

        return response;
    }
}
