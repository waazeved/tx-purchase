package com.waltsoft.tx_purchase.controller.purchase;

import com.waltsoft.tx_purchase.business.exchange_rate.exception.NoExchangeRateDataRuntimeException;
import com.waltsoft.tx_purchase.business.exchange_rate.exception.UnavailableExchangeRateApiRuntimeException;
import com.waltsoft.tx_purchase.business.purchase.PurchaseService;
import com.waltsoft.tx_purchase.controller.PurchaseController;
import com.waltsoft.tx_purchase.dto.purchase.PurchaseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@SpringBootTest(
        properties = {
                "spring.liquibase.enabled=false",
                "spring.main.web-application-type=servlet",
                "spring.autoconfigure.exclude=" +
                        "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration"
        })
@AutoConfigureMockMvc
class FindPurchaseByIdAndCurrencyTest {

    private static final String CURRENCY = "Brazil-Real";
    private static final String URI_TEMPLATE = PurchaseController.PATH + "/{id}/currency/{currency}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PurchaseService purchaseService;

    @Test
    @DisplayName("Should return 200 OK and purchase data when found and converted")
    void shouldReturnPurchaseSuccessfully() throws Exception {
        UUID id = UUID.randomUUID();

        String description = "Gaming Console";
        LocalDate date = LocalDate.now();
        BigDecimal amount = new BigDecimal("499.99");
        BigDecimal convertedAmount = new BigDecimal("1000.99");
        BigDecimal exchangeRateValue = new BigDecimal("5.1");


        PurchaseDto expectedDto = new PurchaseDto(
                id,
                description,
                date,
                amount,
                exchangeRateValue,
                convertedAmount
        );

        Mockito.when(purchaseService.findDtoByIdAndCurrency(id, CURRENCY))
                .thenReturn(expectedDto);

        mockMvc.perform(MockMvcRequestBuilders.get(URI_TEMPLATE, id, CURRENCY)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(id.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value(description))
                .andExpect(MockMvcResultMatchers.jsonPath("$.date").value(date.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.amount").value(amount.doubleValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exchangeRateValue").value(exchangeRateValue.doubleValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.convertedAmount").value(convertedAmount.doubleValue()));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when purchase ID does not exist")
    void shouldReturnBadRequestWhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();

        Mockito.when(purchaseService.findDtoByIdAndCurrency(id, CURRENCY))
                .thenThrow(new IllegalArgumentException("Purchase not found"));

        mockMvc.perform(MockMvcRequestBuilders.get(URI_TEMPLATE, id, CURRENCY))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 422 Unprocessable Entity when there is no exchange rate data for the purchase date")
    void shouldReturnUnprocessableEntityWhenNoExchangeRateData() throws Exception {
        UUID id = UUID.randomUUID();

        Mockito.when(purchaseService.findDtoByIdAndCurrency(id, CURRENCY))
                .thenThrow(new NoExchangeRateDataRuntimeException("There is no exchange rate data available for this date"));

        mockMvc.perform(MockMvcRequestBuilders.get(URI_TEMPLATE, id, CURRENCY))
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("Should return 503 Service Unavailable when USA treasury API is down or circuit breaker is open")
    void shouldReturnServiceUnavailableWhenApiFails() throws Exception {
        UUID id = UUID.randomUUID();

        Mockito.when(purchaseService.findDtoByIdAndCurrency(id, CURRENCY))
                .thenThrow(new UnavailableExchangeRateApiRuntimeException("Exchange rate service is currently unavailable"));

        mockMvc.perform(MockMvcRequestBuilders.get(URI_TEMPLATE, id, CURRENCY))
                .andExpect(MockMvcResultMatchers.status().isServiceUnavailable());
    }

    @Test
    @DisplayName("Should return 500 Internal Server Error when service fails unexpectedly")
    void shouldReturnInternalServerErrorOnFailure() throws Exception {
        UUID id = UUID.randomUUID();

        Mockito.when(purchaseService.findDtoByIdAndCurrency(Mockito.any(), Mockito.anyString()))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(MockMvcRequestBuilders.get(URI_TEMPLATE, id, CURRENCY))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());
    }
}