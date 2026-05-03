package com.waltsoft.tx_purchase.controller.purchase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waltsoft.tx_purchase.business.purchase.PurchaseService;
import com.waltsoft.tx_purchase.controller.PurchaseController;
import com.waltsoft.tx_purchase.dto.purchase.PurchaseInsertDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatchers;
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
class InsertPurchaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PurchaseService purchaseService;

    @Test
    @DisplayName("Should insert purchase successfully and return status 201 Created")
    void shouldInsertPurchaseSuccessfully() throws Exception {
        UUID expectedId = UUID.randomUUID();

        PurchaseInsertDto insertDto = new PurchaseInsertDto(
                "PlayStation",
                LocalDate.now(),
                new BigDecimal("499.99")
        );

        Mockito.when(purchaseService.insert(ArgumentMatchers.any(PurchaseInsertDto.class)))
                .thenReturn(expectedId);

        mockMvc.perform(MockMvcRequestBuilders.post(PurchaseController.PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(insertDto)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().string("\"" + expectedId + "\""));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.00", "-0.01", "-1.00", "-50.00"})
    @DisplayName("Should return 400 Bad Request when amount is 0 or negative")
    void shouldReturnBadRequestWhenAmountIsInvalid(String amountValue) throws Exception {
        PurchaseInsertDto invalidDto = new PurchaseInsertDto(
                "Samsung Galaxy",
                LocalDate.now(),
                new BigDecimal(amountValue)
        );

        mockMvc.perform(MockMvcRequestBuilders.post(PurchaseController.PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verifyNoInteractions(purchaseService);
    }

    @Test
    @DisplayName("Should return 500 Internal Server Error when service fails")
    void shouldReturnInternalServerErrorOnUnexpectedException() throws Exception {
        PurchaseInsertDto insertDto = new PurchaseInsertDto(
                "MacBook",
                LocalDate.now(),
                new BigDecimal("10.00")
        );

        Mockito.when(purchaseService.insert(ArgumentMatchers.any(PurchaseInsertDto.class)))
                .thenThrow(new RuntimeException("Generic error"));

        mockMvc.perform(MockMvcRequestBuilders.post(PurchaseController.PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(insertDto)))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());
    }
}