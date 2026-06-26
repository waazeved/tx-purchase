package com.waltsoft.tx_purchase.business.exchange_rate.api.frankfurter;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class FrankfurterCurrencyCodeMapper {

    private static final Map<String, String> CURRENCY_MAP;

    static {
        Map<String, String> aMap = new HashMap<>();
        aMap.put("Euro Zone-Euro", "EUR");
        aMap.put("United States-Dollar", "USD");
        aMap.put("Japan-Yen", "JPY");
        aMap.put("United Kingdom-Pound", "GBP");
        aMap.put("Australia-Dollar", "AUD");
        aMap.put("Canada-Dollar", "CAD");
        aMap.put("Switzerland-Franc", "CHF");
        aMap.put("China-Yuan Renminbi", "CNY");
        aMap.put("Hong Kong-Dollar", "HKD");
        aMap.put("New Zealand-Dollar", "NZD");
        aMap.put("Sweden-Krona", "SEK");
        aMap.put("South Korea-Won", "KRW");
        aMap.put("Singapore-Dollar", "SGD");
        aMap.put("Norway-Krone", "NOK");
        aMap.put("Mexico-Peso", "MXN");
        aMap.put("India-Rupee", "INR");
        aMap.put("Russia-Ruble", "RUB");
        aMap.put("South Africa-Rand", "ZAR");
        aMap.put("Brazil-Real", "BRL");
        aMap.put("Turkey-Lira", "TRY");
        aMap.put("Taiwan-New Taiwan Dollar", "TWD");
        aMap.put("Denmark-Krone", "DKK");
        aMap.put("Poland-Zloty", "PLN");
        aMap.put("Thailand-Baht", "THB");
        aMap.put("Indonesia-Rupiah", "IDR");
        aMap.put("Czech Republic-Koruna", "CZK");
        aMap.put("Hungary-Forint", "HUF");
        aMap.put("Iceland-Krona", "ISK");
        aMap.put("Israel-New Shekel", "ILS");
        aMap.put("Philippines-Peso", "PHP");
        aMap.put("Romania-Leu", "RON");
        aMap.put("Malaysia-Ringgit", "MYR");
        aMap.put("Bulgaria-Lev", "BGN");
        aMap.put("Chile-Peso", "CLP");
        aMap.put("Colombia-Peso", "COP");
        aMap.put("Peru-Nuevo Sol", "PEN");
        aMap.put("Saudi Arabia-Riyal", "SAR");
        aMap.put("United Arab Emirates-Dirham", "AED");
        aMap.put("Argentina-Peso", "ARS");
        aMap.put("Afghanistan-Afghani", "AFN");
        aMap.put("Albania-Lek", "ALL");
        aMap.put("Algeria-Dinar", "DZD");
        aMap.put("Angola-Kwanza", "AOA");
        aMap.put("Armenia-Dram", "AMD");
        aMap.put("Antigua & Barbuda-East Caribbean Dollar", "XCD");
        aMap.put("Bahrain-Dinar", "BHD");
        aMap.put("Bangladesh-Taka", "BDT");
        aMap.put("Barbados-Dollar", "BBD");
        aMap.put("Belarus-Ruble", "BYN");
        aMap.put("Bolivia-Boliviano", "BOB");
        aMap.put("Bosnia & Herzegovina-Convertible Mark", "BAM");
        aMap.put("Botswana-Pula", "BWP");
        aMap.put("Costa Rica-Colon", "CRC");
        aMap.put("Dominican Republic-Peso", "DOP");
        aMap.put("Egypt-Pound", "EGP");
        aMap.put("Georgia-Lari", "GEL");
        aMap.put("Ghana-Cedi", "GHS");
        aMap.put("Guatemala-Quetzal", "GTQ");
        aMap.put("Honduras-Lempira", "HNL");
        aMap.put("Iraq-Dinar", "IQD");
        aMap.put("Jamaica-Dollar", "JMD");
        aMap.put("Jordan-Dinar", "JOD");
        aMap.put("Kazakhstan-Tenge", "KZT");
        aMap.put("Kenya-Shilling", "KES");
        aMap.put("Kuwait-Dinar", "KWD");
        aMap.put("Lebanon-Pound", "LBP");
        aMap.put("Morocco-Dirham", "MAD");
        aMap.put("Nigeria-Naira", "NGN");
        aMap.put("Oman-Rial", "OMR");
        aMap.put("Pakistan-Rupee", "PKR");
        aMap.put("Panama-Balboa", "PAB");
        aMap.put("Paraguay-Guarani", "PYG");
        aMap.put("Qatar-Riyal", "QAR");
        aMap.put("Sri Lanka-Rupee", "LKR");
        aMap.put("Tunisia-Dinar", "TND");
        aMap.put("Ukraine-Hryvnia", "UAH");
        aMap.put("Uruguay-Peso", "UYU");
        aMap.put("Uzbekistan-Soum", "UZS");
        aMap.put("Venezuela-Bolivar Soberano", "VES");
        aMap.put("Vietnam-Dong", "VND");
        aMap.put("Aruba-Florin", "AWG");
        aMap.put("Azerbaijan-Manat", "AZN");
        aMap.put("Bahamas-Dollar", "BSD");
        aMap.put("Belize-Dollar", "BZD");
        aMap.put("Bermuda-Dollar", "BMD");
        aMap.put("Brunei-Dollar", "BND");
        aMap.put("Burundi-Franc", "BIF");
        aMap.put("Cambodia-Riel", "KHR");
        aMap.put("Cape Verde-Escudo", "CVE");
        aMap.put("Cayman Islands-Dollar", "KYD");
        aMap.put("Central African Republic-CFA Franc", "XAF");
        aMap.put("Cuba-Peso", "CUP");
        aMap.put("Eritrea-Nakfa", "ERN");
        aMap.put("Ethiopia-Birr", "ETB");
        aMap.put("Fiji Islands-Dollar", "FJD");
        aMap.put("Gambia-Dalasi", "GMD");
        aMap.put("Guinea-Franc", "GNF");
        aMap.put("Guyana-Dollar", "GYD");
        aMap.put("Haiti-Gourde", "HTG");
        aMap.put("Iran-Rial", "IRR");
        aMap.put("Kyrgyzstan-Som", "KGS");
        aMap.put("Laos-Kip", "LAK");
        aMap.put("Liberia-Dollar", "LRD");
        aMap.put("Libya-Dinar", "LYD");
        aMap.put("Macao-Pataca", "MOP");
        aMap.put("Macedonia-Denar", "MKD");
        aMap.put("Madagascar-Ariary", "MGA");
        aMap.put("Malawi-Kwacha", "MWK");
        aMap.put("Maldives-Rufiyaa", "MVR");
        aMap.put("Mauritania-Ouguiya", "MRU");
        aMap.put("Mauritius-Rupee", "MUR");
        aMap.put("Moldova-Leu", "MDL");
        aMap.put("Mongolia-Tugrik", "MNT");
        aMap.put("Mozambique-Metical", "MZN");
        aMap.put("Myanmar-Kyat", "MMK");
        aMap.put("Namibia-Dollar", "NAD");
        aMap.put("Nepal-Rupee", "NPR");
        aMap.put("Nicaragua-Cordoba Oro", "NIO");
        aMap.put("Papua New Guinea-Kina", "PGK");
        aMap.put("Rwanda-Franc", "RWF");
        aMap.put("Samoa-Tala", "WST");
        aMap.put("Seychelles-Rupee", "SCR");
        aMap.put("Sierra Leone-Leone", "SLE");
        aMap.put("Solomon Islands-Dollar", "SBD");
        aMap.put("Somalia-Shilling", "SOS");
        aMap.put("Suriname-Dollar", "SRD");
        aMap.put("Tajikistan-Somoni", "TJS");
        aMap.put("Tanzania-Shilling", "TZS");
        aMap.put("Tonga-Pa'anga", "TOP");
        aMap.put("Trinidad & Tobago-Dollar", "TTD");
        aMap.put("Uganda-Shilling", "UGX");
        aMap.put("Vanuatu-Vatu", "VUV");
        aMap.put("Yemen-Rial", "YER");
        aMap.put("Zambia-Kwacha", "ZMW");
        CURRENCY_MAP = Collections.unmodifiableMap(aMap);
    }

    public String parseCurrencyToFrankfurterFormat(String currency) {
        // First, try to find a direct match in the comprehensive map
        String isoCode = CURRENCY_MAP.get(currency);
        return isoCode;// Indicate that the currency could not be parsed
    }
}
