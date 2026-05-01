package com.waltsoft.tx_purchase.business.purchase;


import com.waltsoft.tx_purchase.entity.purchase.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
interface PurchaseRepository extends JpaRepository<Purchase, UUID> {

    @Query("SELECT SUM(p.amount) FROM Purchase p")
    Optional<BigDecimal> sumAllAmounts();

}
