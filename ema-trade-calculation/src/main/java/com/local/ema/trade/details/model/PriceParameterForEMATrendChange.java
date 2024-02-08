package com.local.ema.trade.details.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class PriceParameterForEMATrendChange implements Comparable<PriceParameterForEMATrendChange>{

    private LocalDate date;

    private BigDecimal open;

    private BigDecimal close;

    private BigDecimal volume;

    private BigDecimal emaDiffPercentage;

    private BigDecimal netPriceMovement;

    @Override
    public int compareTo(PriceParameterForEMATrendChange o) {
        return getDate().compareTo(o.getDate());
    }
}
