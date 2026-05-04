package com.waltsoft.tx_purchase.business.purchase;

import com.waltsoft.tx_purchase.business.basic.BasicEntityService;
import com.waltsoft.tx_purchase.business.exchange_rate.ExchangeRateService;
import com.waltsoft.tx_purchase.dto.purchase.PurchaseDto;
import com.waltsoft.tx_purchase.dto.purchase.PurchaseInsertDto;
import com.waltsoft.tx_purchase.dto.purchase.PurchaseInsertedDto;
import com.waltsoft.tx_purchase.entity.purchase.Purchase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
class PurchaseServiceImpl implements PurchaseService, BasicEntityService<Purchase, UUID> {

    private static final int AMOUNT_ROUND_SCALE = 2;

    private final PurchaseRepository repository;
    private final ExchangeRateService exchangeRateService;

    @Autowired
    public PurchaseServiceImpl(final PurchaseRepository repository, ExchangeRateService exchangeRateService) {
        this.repository = repository;
        this.exchangeRateService = exchangeRateService;
    }

    @Override
    public JpaRepository<Purchase, UUID> getRepository() {
        return repository;
    }


    @Override
    public PurchaseInsertedDto insert(final PurchaseInsertDto insertDto) {
        BigDecimal amount = insertDto.amount();
        BigDecimal roundedAmount = amount.setScale(AMOUNT_ROUND_SCALE, RoundingMode.HALF_UP);

        String description = insertDto.description().trim();

        Purchase purchase = new Purchase(description, roundedAmount, insertDto.dateTime());
        repository.save(purchase);
        return new PurchaseInsertedDto(purchase.getId());
    }

    Optional<BigDecimal> sumAllAmounts() {
        return repository.sumAllAmounts();
    }

    @Override
    public PurchaseDto findDtoByIdAndCurrency(UUID id, String currency) {
        Optional<Purchase> purchaseOptional = findById(id);

        if (purchaseOptional.isEmpty()) {
            throw new IllegalArgumentException("Purchase id does not exist");
        }

        Purchase purchase = purchaseOptional.get();
        BigDecimal amount = purchase.getAmount();
        LocalDate date = purchase.getDate();

        BigDecimal convertedAmount = this.exchangeRateService
                .convertAmountByCurrencyAndDate(amount, currency, date);

        return new PurchaseDto(purchase, convertedAmount);
    }
}
