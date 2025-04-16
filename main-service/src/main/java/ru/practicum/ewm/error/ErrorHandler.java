package ru.practicum.ewm.error;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.ewm.error.exeptions.DateTimeException;
import ru.practicum.ewm.error.exeptions.InvalidFormatException;
import ru.practicum.ewm.error.exeptions.NotFoundException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleDateTimerException(DateTimeException e) {
        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .reason("Incorrectly made request")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .errors(Arrays.toString(e.getStackTrace()))
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleInvalidFormatException(InvalidFormatException e) {
        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .reason("Incorrectly made request")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .errors(Arrays.toString(e.getStackTrace()))
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(NotFoundException e) {
        return ApiError.builder()
                .status(HttpStatus.NOT_FOUND.name())
                .reason("The required object was not found")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .errors(Arrays.toString(e.getStackTrace()))
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .reason("Incorrectly made request")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .errors(Arrays.toString(e.getStackTrace()))
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleCommonException(Exception e) {
        return ApiError.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.name())
                .reason("Internal Server Error")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String violations = e.getBindingResult().getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(","));
        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .reason("Incorrectly made request")
                .message(violations)
                .timestamp(LocalDateTime.now())
                .errors(Arrays.toString(e.getStackTrace()))
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .reason("Incorrectly made request")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .errors(Arrays.toString(e.getStackTrace()))
                .build();
    }
}
