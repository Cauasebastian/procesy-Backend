package com.procesy.procesy.exception;

import com.mysql.cj.jdbc.exceptions.MysqlDataTruncation;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String message = "Ocorreu uma violação de integridade de dados.";
        if (ex.getCause() != null && ex.getCause().getCause() instanceof MysqlDataTruncation) {
            message = "Os dados fornecidos são inválidos ou incompletos.";
        } else if (ex.getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
            org.hibernate.exception.ConstraintViolationException constraintException = (org.hibernate.exception.ConstraintViolationException) ex.getCause();
            if (constraintException.getConstraintName().contains("email")) {
                message = "Email já está em uso.";
            }
        }
        return new ResponseEntity<>(message, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        return new ResponseEntity<>("Erro interno do servidor.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}