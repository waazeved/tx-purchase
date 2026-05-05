package com.waltsoft.tx_purchase.controller;

import com.waltsoft.tx_purchase.business.purchase.PurchaseService;
import com.waltsoft.tx_purchase.dto.purchase.PurchaseDto;
import com.waltsoft.tx_purchase.dto.purchase.PurchaseInsertDto;
import com.waltsoft.tx_purchase.dto.purchase.PurchaseInsertedDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(PurchaseController.PATH)
@Tag(name = "Purchases", description = "API for purchase management and currency conversion")
public class PurchaseController {

    public static final String PATH = "/api/purchase";
    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @PostMapping
    @Operation(summary = "Insert new purchase", description = "Creates a new purchase record and returns the generated UUID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Purchase created successfully",
                    content = @Content(schema = @Schema(implementation = PurchaseInsertedDto.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Validation error in the sent data (e.g., null values or amounts below 0.01)", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error - Unexpected server error", content = @Content)
    })
    public ResponseEntity<PurchaseInsertedDto> insertPurchase(
            @Valid
            @RequestBody
            PurchaseInsertDto insertDto) {

        PurchaseInsertedDto insertedDto = purchaseService.insert(insertDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(insertedDto);
    }

    @GetMapping("/{id}/currency/{currency}")
    @Operation(
            summary = "Find purchase with currency conversion",
            description = """
                    Returns purchase data and the converted amount for the specified currency.
                    
                    **Important:** The `currency` parameter must match a value from the **'Country - Currency Description'** column of the official Treasury dataset.
                    
                    Consult the available values here: [Treasury Reporting Rates of Exchange](https://fiscaldata.treasury.gov/datasets/treasury-reporting-rates-exchange/treasury-reporting-rates-of-exchange)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Purchase found and conversion performed successfully",
                    content = @Content(schema = @Schema(implementation = PurchaseDto.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Purchase ID does not exist or invalid parameters", content = @Content),
            @ApiResponse(responseCode = "422", description = "Unprocessable Entity - No exchange rate data available for the currency and purchase date", content = @Content),
            @ApiResponse(responseCode = "503", description = "Service Unavailable - Exchange rate service is unavailable", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error - Unexpected server error", content = @Content)
    })
    public ResponseEntity<PurchaseDto> findPurchaseByIdAndCurrency(
            @PathVariable
            UUID id,
            @PathVariable
            String currency) {

        PurchaseDto purchaseDto = purchaseService.findDtoByIdAndCurrency(id, currency);
        return ResponseEntity.ok(purchaseDto);
    }
}