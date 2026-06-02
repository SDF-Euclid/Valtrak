package com.example.valtrak.Data.GameData.ExceptionHandling;

import com.example.valtrak.Data.GameData.ExceptionHandling.Exceptions.GameNotFoundException;
import com.example.valtrak.Data.GameData.ExceptionHandling.Exceptions.InvalidGameActionException;
import com.example.valtrak.Data.GameData.ExceptionHandling.Exceptions.PlayerNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * A custom RuntimeException for converting generic RuntimeExceptions into an
     * HTTP server error code
     * @return
     *  An HTTP Internal Server Error code (Error 500)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleGeneral(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e.getMessage());
        //.body("An unexpected error occurred");
    }

    @ExceptionHandler(PlayerNotFoundException.class)
    public ResponseEntity<String> handlePlayerNotFoundException(PlayerNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

    @ExceptionHandler(GameNotFoundException.class)
    public ResponseEntity<String> handleGameNotFoundException(GameNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

    @ExceptionHandler(InvalidGameActionException.class)
    public ResponseEntity<String> handleInvalidGameAction(InvalidGameActionException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }
}
