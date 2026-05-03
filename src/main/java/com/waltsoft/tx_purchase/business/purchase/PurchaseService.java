package com.waltsoft.tx_purchase.business.purchase;

import com.waltsoft.tx_purchase.dto.purchase.PurchaseDto;
import com.waltsoft.tx_purchase.dto.purchase.PurchaseInsertDto;

import java.util.UUID;

public interface PurchaseService {

    UUID insert(PurchaseInsertDto insertDto);

    PurchaseDto findDtoByIdAndCurrency(UUID id, String currency);
}
