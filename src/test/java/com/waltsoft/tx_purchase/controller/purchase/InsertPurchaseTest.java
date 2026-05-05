package com.waltsoft.tx_purchase.controller.purchase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waltsoft.tx_purchase.business.purchase.PurchaseService;
import com.waltsoft.tx_purchase.controller.PurchaseController;
import com.waltsoft.tx_purchase.dto.purchase.PurchaseInsertDto;
import com.waltsoft.tx_purchase.dto.purchase.PurchaseInsertedDto;
import org.apache.commons.text.CharacterPredicates;
import org.apache.commons.text.RandomStringGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
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
import java.util.stream.Stream;

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

    private static final BigDecimal AMOUNT = new BigDecimal("10.00");
    private static final String DESCRIPTION = "IPhone";

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
                DESCRIPTION,
                LocalDate.now(),
                AMOUNT
        );

        Mockito.when(purchaseService.insert(ArgumentMatchers.any(PurchaseInsertDto.class)))
                .thenReturn(expectedInsertedDto);

        mockMvc.perform(MockMvcRequestBuilders.post(PurchaseController.PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(insertDto)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().string(expectedInsertedDtoAsJson));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when date is null")
    void shouldReturnBadRequestWhenDateIsNull() throws Exception {
        PurchaseInsertDto dto = new PurchaseInsertDto(DESCRIPTION, null, AMOUNT);
        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(MockMvcRequestBuilders.post(PurchaseController.PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verifyNoInteractions(purchaseService);
    }

    @ParameterizedTest
    @NullSource
    @MethodSource("shouldReturnBadRequestWhenDescriptionIsInvalid")
    @DisplayName("Should return 400 Bad Request when description is invalid")
    void shouldReturnBadRequestWhenDescriptionIsInvalid(String description) throws Exception {
        PurchaseInsertDto dto = new PurchaseInsertDto(description, LocalDate.now(), AMOUNT);
        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(MockMvcRequestBuilders.post(PurchaseController.PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verifyNoInteractions(purchaseService);
    }

    private static Stream<Arguments> shouldReturnBadRequestWhenDescriptionIsInvalid() {

        RandomStringGenerator randomStringGenerator = new RandomStringGenerator.Builder()
                .withinRange('0', 'z')
                .filteredBy(CharacterPredicates.LETTERS, CharacterPredicates.DIGITS)
                .get();

        String tooLongDescription = randomStringGenerator.generate(PurchaseInsertDto.MAX_DESCRIPTION_SIZE + 1);


        return Stream.of(
                Arguments.of(" "),
                Arguments.of(""),
                Arguments.of(tooLongDescription)
        );
    }

    @ParameterizedTest
    @NullSource
    @MethodSource("shouldReturnBadRequestWhenAmountIsInvalid")
    @DisplayName("Should return 400 Bad Request when amount is invalid")
    void shouldReturnBadRequestWhenAmountIsInvalid(BigDecimal amount) throws Exception {
        PurchaseInsertDto dto = new PurchaseInsertDto(DESCRIPTION, LocalDate.now(), amount);
        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(MockMvcRequestBuilders.post(PurchaseController.PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verifyNoInteractions(purchaseService);
    }

    private static Stream<Arguments> shouldReturnBadRequestWhenAmountIsInvalid() {
        return Stream.of(
                Arguments.of(new BigDecimal("0.00")),
                Arguments.of(new BigDecimal("-0.01")),
                Arguments.of(new BigDecimal("-50.00"))
        );
    }

    @Test
    @DisplayName("Should return 500 Internal Server Error when service fails")
    void shouldReturnInternalServerErrorOnUnexpectedException() throws Exception {
        PurchaseInsertDto insertDto = new PurchaseInsertDto(
                DESCRIPTION,
                LocalDate.now(),
                AMOUNT
        );

        Mockito.when(purchaseService.insert(ArgumentMatchers.any(PurchaseInsertDto.class)))
                .thenThrow(new RuntimeException("Generic error"));

        mockMvc.perform(MockMvcRequestBuilders.post(PurchaseController.PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(insertDto)))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());
    }
}