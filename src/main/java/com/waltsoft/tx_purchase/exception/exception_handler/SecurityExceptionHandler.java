package com.waltsoft.tx_purchase.exception.exception_handler;

import com.waltsoft.tx_purchase.business.exchange_rate.exception.NoExchangeRateDataException;
import com.waltsoft.tx_purchase.business.exchange_rate.exception.UnavailableExchangeRateApiRuntimeException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Log LOG = LogFactory.getLog(SecurityExceptionHandler.class);


    @ExceptionHandler(value = Exception.class)
    public void handle(final HttpServletResponse response,
                       final Exception exception,
                       final HandlerMethod handlerMethod) throws IOException {
        handle(response, exception);
    }

    void handle(final HttpServletResponse response, final Exception exception) throws IOException {

        switch (exception) {

            case Exception e when isNotFound(e) -> sendError(response, HttpStatus.NOT_FOUND, e.getMessage());

            case Exception e when isBadRequest(e) -> sendError(response, HttpStatus.BAD_REQUEST, e.getMessage());

            case Exception e when isUnprocessableEntity(e) ->
                    sendError(response, HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());

            case Exception e when isUnavailableService(e) ->
                    sendError(response, HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());

            default -> {
                LOG.error(exception);
                sendError(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    private boolean isNotFound(final Exception exception) {
        return exception instanceof NoResourceFoundException;
    }

    private boolean isBadRequest(final Exception exception) {
        return exception instanceof ConstraintViolationException || exception instanceof IllegalArgumentException || exception instanceof MethodArgumentNotValidException;
    }

    private boolean isUnprocessableEntity(Exception exception) {
        return exception instanceof NoExchangeRateDataException;
    }

    private boolean isUnavailableService(Exception exception) {
        return exception instanceof UnavailableExchangeRateApiRuntimeException;
    }

    private void sendError(final HttpServletResponse response, final HttpStatus status) throws IOException {
        sendError(response, status, status.getReasonPhrase());
    }

    private void sendError(final HttpServletResponse response,
                           final HttpStatus status,
                           final String msg) throws IOException {

        if (!response.isCommitted()) {
            response.sendError(status.value(), msg);
        }
    }
}