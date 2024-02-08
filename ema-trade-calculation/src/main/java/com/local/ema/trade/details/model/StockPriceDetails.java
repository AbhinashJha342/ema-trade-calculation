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
public class StockPriceDetails {

    @CsvCustomBindByName(column = "Date", converter = GenericPropertyConverter.class)
    private LocalDate date;

    @CsvCustomBindByName(column = "Open", converter = GenericPropertyConverter.class)
    private BigDecimal open;

    @CsvCustomBindByName(column = "High", converter = GenericPropertyConverter.class)
    private BigDecimal high;

    @CsvCustomBindByName(column = "Low", converter = GenericPropertyConverter.class)
    private BigDecimal low;

    @CsvCustomBindByName(column = "Close", converter = GenericPropertyConverter.class)
    private BigDecimal close;

    @CsvCustomBindByName(column = "volume", converter = GenericPropertyConverter.class)
    private BigDecimal volume;

    @CsvCustomBindByName(column="high - low %", converter = GenericPropertyConverter.class)
    private BigDecimal dayRange;

    @CsvCustomBindByName(column="close-close", converter = GenericPropertyConverter.class)
    private BigDecimal percentageCloseAppreciation;

    @CsvCustomBindByName(column="open-close %", converter = GenericPropertyConverter.class)
    private BigDecimal openCloseRange;

    @CsvCustomBindByName(column="11-d ema", converter = GenericPropertyConverter.class)
    private BigDecimal fastEMA;

    @CsvCustomBindByName(column="22-d ema", converter = GenericPropertyConverter.class)
    private BigDecimal slowEMA;

    @CsvCustomBindByName(column="EMA diff %", converter = GenericPropertyConverter.class)
    private BigDecimal emaDiffPercentage;

    @CsvCustomBindByName(column="low - fast ema %", converter = GenericPropertyConverter.class)
    private BigDecimal lowFastEmaPercentage;

    @CsvCustomBindByName(column="high-fast ema %", converter = GenericPropertyConverter.class)
    private BigDecimal highFastEmaPercentage;

    @CsvCustomBindByName(column="close below last day", converter = GenericPropertyConverter.class)
    private Boolean closeBelowLastDay;

    @CsvCustomBindByName(column="EMA appreciation %", converter = GenericPropertyConverter.class)
    private BigDecimal emaAppreciation;
}
