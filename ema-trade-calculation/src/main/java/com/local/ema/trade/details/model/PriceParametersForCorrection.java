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
public class PriceParametersForCorrection implements Comparable<PriceParametersForCorrection> , PriceParameter {

    private LocalDate date;

    private BigDecimal open;

    private BigDecimal high;

    private BigDecimal low;

    private BigDecimal close;

    private BigDecimal dayRange;

    private BigDecimal openCloseRange;

    private BigDecimal incrementalPercentageCloseAppreciation;

    private BigDecimal emaDiffPercentage;

    private BigDecimal lowFastEmaPercentage;

    private BigDecimal highFastEmaPercentage;

    private BigDecimal emaAppreciation;

    private BigDecimal priceAppreciationWhenHighIsHigherThanPreviousDay;

    private BigDecimal netMaxPriceAppreciation;

    private BigDecimal netCloseAppreciationWhenPriceComesBackToFastEma;

    private BigDecimal netPriceAppreciation;


    @Override
    public int compareTo(PriceParametersForCorrection o) {
        return getDate().compareTo(o.getDate());
    }
}
