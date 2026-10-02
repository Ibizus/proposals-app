package org.propflow.back.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GeneralControllerAdvice {

    @ResponseBody
    @ExceptionHandler
    @ResponseStatus
    public String entityNotFoundHandler(EntityNotFoundException entityNotFoundException){
        return entityNotFoundException.getMessage();
    }

    @ResponseBody
    @ExceptionHandler
    @ResponseStatus
    public String notCouplingIdException(NotCouplingIdException notCouplingIdException){
        return notCouplingIdException.getMessage();
    }

    @ResponseBody
    @ExceptionHandler
    @ResponseStatus
    public String entityNotFoundHandler(Exception exception){
        return exception.getMessage();
    }
}