package com.local.ema.trade.details.calculator;

import com.local.ema.trade.details.model.PriceParametersForIntermediateCorrection;
import com.local.ema.trade.details.model.StockPriceDetails;
import com.local.ema.trade.details.util.StaticUtil;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

public class CalculateIntermediateCorrections {

    /**
     * helper method to calculate when low comes back to slow EMA after coming back to fast EMA using following steps:
     * a.) take a boolean value to flag when the low goes below fast ema.
     *
     * @return PriceParametersForIntermediateCorrection
     */
    public static List<PriceParametersForIntermediateCorrection> calculatePriceParametersWhenLowComesBackToSlowEma(List<StockPriceDetails> stockPriceDetails, MathContext mc){
        List<PriceParametersForIntermediateCorrection> priceParametersForIntermediateCorrectionList = new ArrayList<>();
        boolean ifLowHasBreachedFastEma = false;
        int indexWhereThePriceCameBackToFastEMA = 0;

        for(int i=0; i< stockPriceDetails.size(); i++) {
            StockPriceDetails stockDetail = stockPriceDetails.get(i);
            if(ifLowHasBreachedFastEma){
                PriceParametersForIntermediateCorrection priceParametersForIntermediateCorrection = StaticUtil.createPriceParametersForIntermediateCorrection(stockDetail, mc);
                priceParametersForIntermediateCorrectionList.add(priceParametersForIntermediateCorrection);
                if(stockDetail.getLow().compareTo(stockDetail.getSlowEMA())<0){
                    BigDecimal closeWherePriceCameBackToFastEMA = stockPriceDetails.get(indexWhereThePriceCameBackToFastEMA).getClose();
                    priceParametersForIntermediateCorrection.setNetPriceAppreciationBeforeLowFallsToSlowEMA((priceParametersForIntermediateCorrection.getClose().subtract(closeWherePriceCameBackToFastEMA)
                            .divide(closeWherePriceCameBackToFastEMA, mc)).multiply(BigDecimal.valueOf(100)));
                    ifLowHasBreachedFastEma = false;
                    continue;
                }
            }

            if(!ifLowHasBreachedFastEma && stockDetail.getLowFastEmaPercentage().compareTo(BigDecimal.ZERO)<0 && stockDetail.getLow().compareTo(stockDetail.getSlowEMA())>0){
                ifLowHasBreachedFastEma = true;
                indexWhereThePriceCameBackToFastEMA = i;
                PriceParametersForIntermediateCorrection priceParametersForIntermediateCorrection = StaticUtil.createPriceParametersForIntermediateCorrection(stockDetail, mc);
                priceParametersForIntermediateCorrectionList.add(priceParametersForIntermediateCorrection);
            }

        }

        if(ifLowHasBreachedFastEma){
            BigDecimal closeWherePriceCameBackToFastEMA = stockPriceDetails.get(indexWhereThePriceCameBackToFastEMA).getClose();
            PriceParametersForIntermediateCorrection priceParametersForIntermediateCorrection = priceParametersForIntermediateCorrectionList.get(priceParametersForIntermediateCorrectionList.size()-1);
            priceParametersForIntermediateCorrection.setNetPriceAppreciationBeforeLowFallsToSlowEMA((priceParametersForIntermediateCorrection.getClose().subtract(closeWherePriceCameBackToFastEMA)
                    .divide(closeWherePriceCameBackToFastEMA, mc)).multiply(BigDecimal.valueOf(100)));
        }

        return priceParametersForIntermediateCorrectionList;
    }
}
