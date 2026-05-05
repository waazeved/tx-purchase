package com.waltsoft.tx_purchase.dto.purchase;

import java.io.Serializable;
import java.util.UUID;

public record PurchaseInsertedDto(
        UUID id
) implements Serializable {
}