package com.waltsoft.tx_purchase.exception.exception_handler;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class SecurityExceptionHandlerTest {

    @InjectMocks
    private SecurityExceptionHandler handler;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HandlerMethod handlerMethod;

    @BeforeEach
    void setUp() {
        Mockito.lenient().when(response.isCommitted()).thenReturn(false);
    }

    @Test
    @DisplayName("404: Should intercept NoResourceFoundException and return Not Found")
    void shouldHandleNotFoundException() throws IOException {
        Exception ex = new NoResourceFoundException(HttpMethod.GET, "/missing-resource");

        handler.handle(response, ex, handlerMethod);

        Mockito.verify(response).sendError(Mockito.eq(HttpStatus.NOT_FOUND.value()), Mockito.anyString());
    }

    @ParameterizedTest(name = "400: Should intercept {1} and return Bad Request")
    @MethodSource("provideBadRequestExceptions")
    @DisplayName("400: Should intercept exceptions related to bad request and return Bad Request status and response message")
    void shouldHandleBadRequestExceptions(Exception exception, String expectedMessage) throws IOException {
        handler.handle(response, exception, handlerMethod);

        Mockito.verify(response).sendError(HttpStatus.BAD_REQUEST.value(), expectedMessage);
    }

    private static Stream<Arguments> provideBadRequestExceptions() {

        String constraintViolationExceptionMsg = "Database constraint violation";
        ConstraintViolationException constraintViolationException = Mockito.mock(ConstraintViolationException.class);
        Mockito.when(constraintViolationException.getMessage()).thenReturn(constraintViolationExceptionMsg);

        String methodArgumentNotValidExceptionMsg = "Validation failed";
        MethodArgumentNotValidException methodArgumentNotValidException = Mockito.mock(MethodArgumentNotValidException.class);
        Mockito.when(methodArgumentNotValidException.getMessage()).thenReturn(methodArgumentNotValidExceptionMsg);

        String illegalArgumentExceptionMsg = "Validation failed";
        IllegalArgumentException illegalArgumentException = Mockito.mock(IllegalArgumentException.class);
        Mockito.when(illegalArgumentException.getMessage()).thenReturn(illegalArgumentExceptionMsg);

        return Stream.of(Arguments.of(illegalArgumentException, illegalArgumentExceptionMsg),
                Arguments.of(constraintViolationException, constraintViolationExceptionMsg),
                Arguments.of(methodArgumentNotValidException, methodArgumentNotValidExceptionMsg));
    }

    @Test
    @DisplayName("500: Should return Internal Server Error for any unmapped exception, without error message")
    void shouldHandleGenericExceptionAsInternalServerError() throws IOException {
        String sensitiveMessage = "SQLState: 23505; Error: Duplicate entry for key 'PRIMARY' at line 1";

        Exception ex = new RuntimeException(sensitiveMessage);

        handler.handle(response, ex, handlerMethod);

        Mockito.verify(response)
                .sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());

        Mockito.verify(response, Mockito.never()).sendError(Mockito.anyInt(), Mockito.eq(sensitiveMessage));
    }

    @Test
    @DisplayName("Safety: Should not call sendError if response is already committed")
    void shouldNotSendErrorIfResponseIsCommitted() throws IOException {
        Mockito.when(response.isCommitted()).thenReturn(true);

        Exception ex = new IllegalArgumentException("Error after commit");

        handler.handle(response, ex, handlerMethod);

        Mockito.verify(response, Mockito.never()).sendError(Mockito.anyInt(), Mockito.anyString());
    }
}