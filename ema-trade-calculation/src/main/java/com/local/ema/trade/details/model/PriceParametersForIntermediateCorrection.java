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
public class PriceParametersForIntermediateCorrection implements Comparable<PriceParametersForIntermediateCorrection>{

    private LocalDate date;

    private BigDecimal open;

    private BigDecimal low;

    private BigDecimal close;

    private BigDecimal openCloseRange;

    private BigDecimal emaDiffPercentage;

    private BigDecimal lowFastEmaPercentage;

    private BigDecimal lowSlowEmaPercentage;

    private BigDecimal emaAppreciation;

    private BigDecimal netPriceAppreciationBeforeLowFallsToSlowEMA;

    @Override
    public int compareTo(PriceParametersForIntermediateCorrection o) {
        return getDate().compareTo(o.getDate());
    }
}