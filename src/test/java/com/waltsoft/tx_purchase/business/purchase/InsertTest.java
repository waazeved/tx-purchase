package com.waltsoft.tx_purchase.business.purchase;

import com.waltsoft.tx_purchase.dto.purchase.PurchaseInsertDto;
import com.waltsoft.tx_purchase.entity.purchase.Purchase;
import com.waltsoft.tx_purchase.test_container.ContainerTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Transactional
class InsertTest extends ContainerTest {

    private static final String DESCRIPTION = "  New Purchase  ";
    private static final LocalDate DATE = LocalDate.now();

    @Autowired
    private PurchaseServiceImpl purchaseService;

    @ParameterizedTest
    @MethodSource("shouldInsertNewPurchaseWithRoundedAmount")
    @DisplayName("Should insert new purchase with rounded amount")
    void shouldInsertNewPurchaseWithRoundedAmount(BigDecimal amount, BigDecimal roundedAmount) {

        PurchaseInsertDto insertDto = new PurchaseInsertDto(
                DESCRIPTION,
                DATE,
                amount
        );

        UUID id = this.purchaseService.insert(insertDto);

        Optional<Purchase> purchaseOptional = this.purchaseService.findById(id);

        Assertions.assertTrue(purchaseOptional.isPresent());

        Purchase purchase = purchaseOptional.get();

        Assertions.assertEquals(DESCRIPTION.trim(), purchase.getDescription());
        Assertions.assertEquals(DATE, purchase.getDate());
        Assertions.assertEquals(roundedAmount, purchase.getAmount());
    }

    private static Stream<Arguments> shouldInsertNewPurchaseWithRoundedAmount() {
        return Stream.of(Arguments.of(new BigDecimal("15.00"), new BigDecimal("15.00")),
                Arguments.of(new BigDecimal("30.05"), new BigDecimal("30.05")),
                Arguments.of(new BigDecimal("60.555"), new BigDecimal("60.56")),
                Arguments.of(new BigDecimal("60.554"), new BigDecimal("60.55")),
                Arguments.of(new BigDecimal("9.556"), new BigDecimal("9.56"))
        );
    }

    @Test
    @DisplayName("Should insert new purchases and maintain exact decimal precision during summation")
    void shouldMaintainDecimalPrecisionDuringSummation() {
        BigDecimal amount1 = new BigDecimal("0.1");
        BigDecimal amount2 = new BigDecimal("0.2");

        PurchaseInsertDto insertDto1 = new PurchaseInsertDto(
                DESCRIPTION,
                DATE,
                amount1
        );

        PurchaseInsertDto insertDto2 = new PurchaseInsertDto(
                DESCRIPTION,
                DATE,
                amount2
        );

        this.purchaseService.insert(insertDto1);
        this.purchaseService.insert(insertDto2);

        Optional<BigDecimal> optionalSum = purchaseService.sumAllAmounts();

        Assertions.assertTrue(optionalSum.isPresent());

        BigDecimal sum = optionalSum.get();
        BigDecimal expectedSum = new BigDecimal("0.30");
        Assertions.assertEquals(0, expectedSum.compareTo(sum));
    }

}
