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
public class PriceParametersForConsolidation implements Comparable<PriceParametersForConsolidation>{

    private LocalDate date;

    private BigDecimal open;

    private BigDecimal close;

    private BigDecimal volume;

    private BigDecimal dayRange;

    private BigDecimal openCloseRange;

    private BigDecimal incrementalPercentageCloseAppreciation;

    private BigDecimal emaDiffPercentage;

    @Override
    public int compareTo(PriceParametersForConsolidation o) {
        return getDate().compareTo(o.getDate());
    }
}
