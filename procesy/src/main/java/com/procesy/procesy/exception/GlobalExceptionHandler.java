// GlobalExceptionHandler.java
package com.procesy.procesy.exception;

import com.mysql.cj.jdbc.exceptions.MysqlDataTruncation;
import jakarta.annotation.security.RolesAllowed;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String message = "Ocorreu uma violação de integridade de dados.";

        Throwable cause = ex.getMostSpecificCause();

        if (cause instanceof MysqlDataTruncation) {
            message = "Os dados fornecidos são inválidos ou incompletos.";
        } else if (cause instanceof ConstraintViolationException constraintEx) {
            if (constraintEx.getConstraintName() != null && constraintEx.getConstraintName().toLowerCase().contains("email")) {
                message = "Email já está em uso.";
            }
        }

        return new ResponseEntity<>(message, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDeniedException(AccessDeniedException ex) {
        return new ResponseEntity<>("Acesso negado: " + ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return new ResponseEntity<>("Recurso não encontrado: " + ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        ex.printStackTrace(); // útil para depuração; remova em produção
        return new ResponseEntity<>("Erro interno do servidor.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
