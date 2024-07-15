package com.ckxnhat.truyenhay.controllers;

import com.ckxnhat.truyenhay.exception.AppException;
import com.ckxnhat.truyenhay.type.response.ErrorApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@ControllerAdvice
@RestController
public class ErrorController {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<?> handleAppException(AppException e) {
        ErrorApiResponse errorResponse = new ErrorApiResponse();
        errorResponse.setStatus(e.getStatus());
        errorResponse.setMessage(e.getMessage());
        return ResponseEntity.status(e.getStatusCode()).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        ErrorApiResponse errorResponse = new ErrorApiResponse();
        errorResponse.setStatus("error");
        errorResponse.setMessage("Internal server error");
        System.out.println(e.getMessage());
        return ResponseEntity.status(500).body(errorResponse);
    }

}
