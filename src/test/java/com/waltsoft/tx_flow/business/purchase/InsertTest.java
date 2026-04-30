package com.waltsoft.tx_flow.business.purchase;

import com.waltsoft.tx_flow.dto.purchase.PurchaseInsertDto;
import com.waltsoft.tx_flow.entity.purchase.Purchase;
import com.waltsoft.tx_flow.test_container.PostgreSQLContainerTest;
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
class InsertTest extends PostgreSQLContainerTest {

    @Autowired
    private PurchaseServiceImpl purchaseService;

    @ParameterizedTest
    @MethodSource("shouldInsertNewPurchase")
    @DisplayName("Should insert new purchase")
    void shouldInsertNewPurchase(BigDecimal amount, BigDecimal roundedAmount) {

        String description = "New Purchase";
        LocalDate date = LocalDate.now();

        PurchaseInsertDto insertDto = new PurchaseInsertDto(
                description,
                date,
                amount
        );

        UUID id = this.purchaseService.insert(insertDto);

        Optional<Purchase> optionalPurchase = this.purchaseService.findById(id);

        Assertions.assertTrue(optionalPurchase.isPresent());

        Purchase purchase = optionalPurchase.get();

        Assertions.assertEquals(purchase.getDescription(), description);
        Assertions.assertEquals(purchase.getDate(), date);
        Assertions.assertEquals(purchase.getAmount(), roundedAmount);
    }

    private static Stream<Arguments> shouldInsertNewPurchase() {
        return Stream.of(Arguments.of(new BigDecimal("15.00"), new BigDecimal("15.00")),
                Arguments.of(new BigDecimal("30.05"), new BigDecimal("30.05")),
                Arguments.of(new BigDecimal("60.555"), new BigDecimal("60.56")),
                Arguments.of(new BigDecimal("60.554"), new BigDecimal("60.55")),
                Arguments.of(new BigDecimal("9.556"), new BigDecimal("9.56"))
        );
    }

    @Test
    @DisplayName("Should maintain exact decimal precision during summation")
    void shouldMaintainDecimalPrecisionDuringSummation() {
        String description = "New Purchase";
        LocalDate date = LocalDate.now();
        BigDecimal amount1 = new BigDecimal("0.1");
        BigDecimal amount2 = new BigDecimal("0.2");

        PurchaseInsertDto insertDto1 = new PurchaseInsertDto(
                description,
                date,
                amount1
        );

        PurchaseInsertDto insertDto2 = new PurchaseInsertDto(
                description,
                date,
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
