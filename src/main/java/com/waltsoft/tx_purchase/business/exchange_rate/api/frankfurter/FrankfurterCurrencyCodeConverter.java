package com.waltsoft.tx_purchase.business.exchange_rate.api.frankfurter;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
class FrankfurterCurrencyCodeConverter {

    private static final Map<String, String> CURRENCY_CODE_MAP;

    static {
        CURRENCY_CODE_MAP = new HashMap<>();
        CURRENCY_CODE_MAP.put("Australia-Dollar", "AUD");
        CURRENCY_CODE_MAP.put("Bulgaria-Lev", "BGN");
        CURRENCY_CODE_MAP.put("Brazil-Real", "BRL");
        CURRENCY_CODE_MAP.put("Canada-Dollar", "CAD");
        CURRENCY_CODE_MAP.put("Switzerland-Franc", "CHF");
        CURRENCY_CODE_MAP.put("China-Yuan Renminbi", "CNY");
        CURRENCY_CODE_MAP.put("Czech Republic-Koruna", "CZK");
        CURRENCY_CODE_MAP.put("Denmark-Krone", "DKK");
        CURRENCY_CODE_MAP.put("Euro Zone-Euro", "EUR");
        CURRENCY_CODE_MAP.put("United Kingdom-Pound", "GBP");
        CURRENCY_CODE_MAP.put("Hong Kong-Dollar", "HKD");
        CURRENCY_CODE_MAP.put("Hungary-Forint", "HUF");
        CURRENCY_CODE_MAP.put("Indonesia-Rupiah", "IDR");
        CURRENCY_CODE_MAP.put("Israel-New Shekel", "ILS");
        CURRENCY_CODE_MAP.put("India-Rupee", "INR");
        CURRENCY_CODE_MAP.put("Iceland-Krona", "ISK");
        CURRENCY_CODE_MAP.put("Japan-Yen", "JPY");
        CURRENCY_CODE_MAP.put("South Korea-Won", "KRW");
        CURRENCY_CODE_MAP.put("Mexico-Peso", "MXN");
        CURRENCY_CODE_MAP.put("Malaysia-Ringgit", "MYR");
        CURRENCY_CODE_MAP.put("Norway-Krone", "NOK");
        CURRENCY_CODE_MAP.put("New Zealand-Dollar", "NZD");
        CURRENCY_CODE_MAP.put("Philippines-Peso", "PHP");
        CURRENCY_CODE_MAP.put("Poland-Zloty", "PLN");
        CURRENCY_CODE_MAP.put("Romania-Leu", "RON");
        CURRENCY_CODE_MAP.put("Russia-Ruble", "RUB");
        CURRENCY_CODE_MAP.put("Sweden-Krona", "SEK");
        CURRENCY_CODE_MAP.put("Singapore-Dollar", "SGD");
        CURRENCY_CODE_MAP.put("Thailand-Baht", "THB");
        CURRENCY_CODE_MAP.put("Turkey-Lira", "TRY");
        CURRENCY_CODE_MAP.put("United States-Dollar", "USD");
        CURRENCY_CODE_MAP.put("South Africa-Rand", "ZAR");
    }

    public Optional<String> convert(String currency) {
        String currencyCode = CURRENCY_CODE_MAP.get(currency);
        return Optional.ofNullable(currencyCode);
    }
}
