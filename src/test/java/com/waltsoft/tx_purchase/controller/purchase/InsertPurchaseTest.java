package com.waltsoft.tx_purchase.controller.purchase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waltsoft.tx_purchase.business.purchase.PurchaseService;
import com.waltsoft.tx_purchase.controller.PurchaseController;
import com.waltsoft.tx_purchase.dto.purchase.PurchaseInsertDto;
import com.waltsoft.tx_purchase.dto.purchase.PurchaseInsertedDto;
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
        PurchaseInsertedDto expectedInsertedDto = new PurchaseInsertedDto(UUID.randomUUID());
        String expectedInsertedDtoAsJson = objectMapper.writeValueAsString(expectedInsertedDto);

        PurchaseInsertDto insertDto = new PurchaseInsertDto(
                "PlayStation",
                LocalDate.now(),
                new BigDecimal("499.99")
        );

        Mockito.when(purchaseService.insert(ArgumentMatchers.any(PurchaseInsertDto.class)))
                .thenReturn(expectedInsertedDto);

        mockMvc.perform(MockMvcRequestBuilders.post(PurchaseController.PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(insertDto)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().string(expectedInsertedDtoAsJson));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "null",
            "\"2024-13-01\"",
            "\"01/01/2024\"",
            "\"not-a-date\"",
            "\"2024-02-30\"",
            "\"\"",
            "\" \""
    })
    @DisplayName("Should return 400 Bad Request when date is invalid")
    void shouldReturnBadRequestWhenDateIsInvalid(String date) throws Exception {
        String json = """
                {
                    "description": "Sneakers",
                    "date": %s,
                    "amount": 10.00
                }
                """.formatted(date);

        mockMvc.perform(MockMvcRequestBuilders.post(PurchaseController.PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verifyNoInteractions(purchaseService);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "null",
            "\" \"",
            "\"\"",
            "10.00",
            "10"
    })
    @DisplayName("Should return 400 Bad Request when description is invalid")
    void shouldReturnBadRequestWhenDescriptionIsInvalid(String description) throws Exception {
        String json = """
                {
                    "description": %s,
                    "date": "2026-05-03",
                    "amount": 10.00
                }
                """.formatted(description);

        mockMvc.perform(MockMvcRequestBuilders.post(PurchaseController.PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verifyNoInteractions(purchaseService);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "null",
            "\" \"",
            "\"\"",
            "\"10.00\"",
            "\"10\"",
            "0.00",
            "-0.01",
            "-1.00",
            "-50.00"
    })
    @DisplayName("Should return 400 Bad Request when amount is invalid")
    void shouldReturnBadRequestWhenAmountIsInvalid(String amount) throws Exception {
        String json = """
                {
                    "description": "Nike Sneakers",
                    "date": "2026-05-03",
                    "amount": %s
                }
                """.formatted(amount);

        mockMvc.perform(MockMvcRequestBuilders.post(PurchaseController.PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
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