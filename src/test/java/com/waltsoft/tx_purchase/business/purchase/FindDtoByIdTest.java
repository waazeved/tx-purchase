package com.waltsoft.tx_purchase.business.purchase;

import com.waltsoft.tx_purchase.dto.purchase.PurchaseDto;
import com.waltsoft.tx_purchase.entity.purchase.Purchase;
import com.waltsoft.tx_purchase.test_container.PostgreSQLContainerTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Transactional
class FindDtoByIdTest extends PostgreSQLContainerTest {

    private static final String DESCRIPTION = "New Purchase";
    private static final LocalDate DATE = LocalDate.now();
    private static final BigDecimal AMOUNT = new BigDecimal("0.3");

    @Autowired
    private PurchaseServiceImpl purchaseService;

    @Test
    @DisplayName("Should find purchaseDto by ID")
    void shouldFindPurchaseDtoById() {

        Purchase purchase = makePurchase();

        UUID id = purchase.getId();

        PurchaseDto purchaseDto = this.purchaseService.findDtoById(id);

        Assertions.assertNotNull(purchaseDto);

        Assertions.assertEquals(DESCRIPTION, purchaseDto.description());
        Assertions.assertEquals(DATE, purchaseDto.date());
        Assertions.assertEquals(AMOUNT, purchaseDto.amount());
    }

    @Test
    @DisplayName("Should not find purchaseDto by nonexistent ID and throws exception")
    void shouldNotFindPurchaseDtoByNonexistentIdAndThrowsException() {

        makePurchase();

        boolean isThereOneSavedPurchase = this.purchaseService.count()==1;

        Assertions.assertTrue(isThereOneSavedPurchase);

        UUID nonexistentId = UUID.randomUUID();

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> this.purchaseService.findDtoById(nonexistentId));
    }

    private Purchase makePurchase() {

        Purchase purchase = new Purchase(
                DESCRIPTION,
                AMOUNT,
                DATE
        );

        return this.purchaseService.save(purchase);
    }

}
