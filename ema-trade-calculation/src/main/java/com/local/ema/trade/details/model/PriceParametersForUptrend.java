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
public class PriceParametersForUptrend implements Comparable<PriceParametersForUptrend> , PriceParameter {

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

    private BigDecimal priceAppreciationWhenLowIsLowerThanPreviousDay;

    private BigDecimal netCloseAppreciationWhenPriceComesBackToFastEma;

    // this is the price appreciation before the trend broke
    private BigDecimal netMaxPriceAppreciation;

    // this is the price appreciation when the trend broke
    private BigDecimal netPriceAppreciation;

    @Override
    public int compareTo(PriceParametersForUptrend o) {
        return getDate().compareTo(o.getDate());
    }
}
