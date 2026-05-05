package com.waltsoft.tx_purchase.business.purchase;

import com.waltsoft.tx_purchase.business.exchange_rate.ExchangeRateService;
import com.waltsoft.tx_purchase.business.exchange_rate.exception.NoExchangeRateDataRuntimeException;
import com.waltsoft.tx_purchase.dto.purchase.PurchaseDto;
import com.waltsoft.tx_purchase.entity.purchase.Purchase;
import com.waltsoft.tx_purchase.test_container.ContainerTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Transactional
class FindDtoByIdTest extends ContainerTest {

    private static final String DESCRIPTION = "New Purchase";
    private static final LocalDate DATE = LocalDate.now();
    private static final BigDecimal AMOUNT = new BigDecimal("1");
    private static final String CURRENCY = "Brazil-Real";

    @Autowired
    private PurchaseServiceImpl purchaseService;
    @MockitoBean
    private ExchangeRateService exchangeRateService;

    @Test
    @DisplayName("Should find purchaseDto by ID")
    void shouldFindPurchaseDtoById() {
        BigDecimal expectedAmount = new BigDecimal("100");

        Purchase purchase = makePurchase(expectedAmount);
        UUID id = purchase.getId();

        BigDecimal expectedExchangeRateValue = new BigDecimal("2.05");
        Optional<BigDecimal> exchangeRateValueOptional = Optional.of(expectedExchangeRateValue);

        BigDecimal expectedConvertedAmount = new BigDecimal("205.00");

        Mockito.when(exchangeRateService.findExchangeRateValueByCurrencyAndDate(CURRENCY, DATE))
                .thenReturn(exchangeRateValueOptional);

        PurchaseDto purchaseDto = this.purchaseService.findDtoByIdAndCurrency(id, CURRENCY);

        Assertions.assertNotNull(purchaseDto);
        Assertions.assertEquals(DESCRIPTION, purchaseDto.description());
        Assertions.assertEquals(DATE, purchaseDto.date());
        Assertions.assertEquals(expectedAmount, purchaseDto.amount());
        Assertions.assertEquals(expectedExchangeRateValue, purchaseDto.exchangeRateValue());
        Assertions.assertEquals(expectedConvertedAmount, purchaseDto.convertedAmount());
    }

    @Test
    @DisplayName("Should not find purchaseDto by nonexistent ID and throws exception")
    void shouldNotFindPurchaseDtoByNonexistentIdAndThrowsException() {

        makePurchase();

        boolean isThereOneSavedPurchase = this.purchaseService.count()==1;

        Assertions.assertTrue(isThereOneSavedPurchase);

        UUID nonexistentId = UUID.randomUUID();

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> this.purchaseService.findDtoByIdAndCurrency(nonexistentId, CURRENCY));
    }


    @Test
    @DisplayName("Should throw NoExchangeRateDataRuntimeException when there is no exchange rate data available")
    void shouldThrowExceptionWhenThereIsNoExchangeRateAvailable() {
        Purchase purchase = makePurchase();
        UUID id = purchase.getId();

        Optional<BigDecimal> exchangeRateValueOptional = Optional.empty();

        Mockito.when(exchangeRateService.findExchangeRateValueByCurrencyAndDate(CURRENCY, DATE))
                .thenReturn(exchangeRateValueOptional);

        Assertions.assertThrows(NoExchangeRateDataRuntimeException.class,
                () -> this.purchaseService.findDtoByIdAndCurrency(id, CURRENCY));
    }

    private Purchase makePurchase() {
        return makePurchase(AMOUNT);
    }

    private Purchase makePurchase(BigDecimal amount) {

        Purchase purchase = new Purchase(
                DESCRIPTION,
                amount,
                DATE
        );

        return this.purchaseService.save(purchase);
    }

}
