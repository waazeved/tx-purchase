package com.waltsoft.tx_flow.business.purchase;

import com.waltsoft.tx_flow.dto.purchase.PurchaseInsertDto;

import java.util.UUID;

public interface PurchaseService {

    UUID insert(PurchaseInsertDto insertDto);

}
