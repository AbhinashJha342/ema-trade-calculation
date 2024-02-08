package com.local.ema.trade.details.model;

import com.local.ema.trade.details.converter.GenericPropertyConverter;
import com.opencsv.bean.CsvCustomBindByName;
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
public class PriceParametersForProcessing implements Comparable<PriceParametersForProcessing>{

    private LocalDate date;

    private BigDecimal open;

    private BigDecimal high;

    private BigDecimal low;

    private BigDecimal close;

    private BigDecimal volume;

    private BigDecimal dayRange;

    private BigDecimal percentageCloseAppreciation;

    private BigDecimal openCloseRange;

    private BigDecimal fastEMA;

    private BigDecimal slowEMA;

    private BigDecimal emaDiffPercentage;

    private BigDecimal lowFastEmaPercentage;

    private BigDecimal highFastEmaPercentage;

    private Boolean closeBelowLastDay;

    private BigDecimal emaAppreciation;

    @Override
    public int compareTo(PriceParametersForProcessing o) {
        return getDate().compareTo(o.getDate());
    }
}
