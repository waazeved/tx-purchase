package com.waltsoft.tx_purchase.business.exchange_rate.api;

import com.waltsoft.tx_purchase.test_container.ContainerTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@DisplayName("ExchangeRateApi Priority Tests")
class ExchangeRateApiTest extends ContainerTest {

    @Autowired
    private List<ExchangeRateApi> exchangeRateApis;

    @Test
    @DisplayName("Should not have duplicate priorities among implementations")
    void shouldNotHaveDuplicatePriorities() {
        Set<Integer> priorities = new HashSet<>();
        for (ExchangeRateApi api : exchangeRateApis) {
            boolean isNew = priorities.add(api.getPriority());
            Assertions.assertTrue(isNew, "Duplicate priority found: " + api.getPriority() + " in " + api.getClass().getSimpleName());
        }
    }
}
