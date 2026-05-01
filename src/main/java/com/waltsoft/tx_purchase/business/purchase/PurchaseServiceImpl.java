package com.waltsoft.tx_purchase.business.purchase;

import com.waltsoft.tx_purchase.business.basic.BasicEntityService;
import com.waltsoft.tx_purchase.dto.purchase.PurchaseDto;
import com.waltsoft.tx_purchase.dto.purchase.PurchaseInsertDto;
import com.waltsoft.tx_purchase.entity.purchase.Purchase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.UUID;

@Service
class PurchaseServiceImpl implements PurchaseService, BasicEntityService<Purchase, UUID> {

    public static final int AMOUNT_ROUND_SACALE = 2;
    private final PurchaseRepository repository;

    @Autowired
    public PurchaseServiceImpl(final PurchaseRepository repository) {
        this.repository = repository;
    }

    @Override
    public JpaRepository<Purchase, UUID> getRepository() {
        return repository;
    }


    @Override
    public UUID insert(final PurchaseInsertDto insertDto) {
        BigDecimal amount = insertDto.amount();
        BigDecimal roundedAmount = amount.setScale(AMOUNT_ROUND_SACALE, RoundingMode.HALF_UP);
        Purchase purchase = new Purchase(insertDto.description(), roundedAmount, insertDto.dateTime());
        repository.save(purchase);
        return purchase.getId();
    }

    Optional<BigDecimal> sumAllAmounts() {
        return repository.sumAllAmounts();
    }

    public PurchaseDto findDtoById(UUID id) {
        Optional<Purchase> purchaseOptional = findById(id);

        if (purchaseOptional.isEmpty()) {
            throw new IllegalArgumentException("Purchase id does not exist");
        }

        Purchase purchase = purchaseOptional.get();
        return new PurchaseDto(purchase);
    }
}
