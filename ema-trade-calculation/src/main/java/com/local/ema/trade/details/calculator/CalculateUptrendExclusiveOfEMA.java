package com.local.ema.trade.details.calculator;

import com.local.ema.trade.details.model.PriceParametersForUptrend;
import com.local.ema.trade.details.model.StockPriceDetails;
import com.local.ema.trade.details.util.CalculatorUtil;
import com.local.ema.trade.details.util.StaticUtil;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateUptrendExclusiveOfEMA {

    /**
     * helper method to find an uptrend and get corresponding price details.
     * this calculation is not tied against "11-d EMA".
     * Uses the below logic to find the uptrend:
     * 1.) tags the first incoming price detail as the "starting point" for comparison.
     * 2.) checks if :a.) the "new" low is lower than the low of the start. b.) the "new" high is higher than the start.
     * 3.) If both the condition satisfies then increment the "new" price as the "starting point".
     * 4.) If at 2.) , condition a.) satisfies, but b.) does not, then create a counter to keep track of this behavior.
     * 5.) when this counter reaches value 2, then that marks end of a trend.
     * 6.) If at 2.) condition a.) is unsatisfied, then that marks end of trend.
     *
     * @param stockPriceDetails
     * @param mc
     * @return
     */
    public List<PriceParametersForUptrend> findUptrendAndGetPriceMovementDetails(List<StockPriceDetails> stockPriceDetails, MathContext mc){
        List<PriceParametersForUptrend> priceParametersForUptrendList = new ArrayList<>();
        Map<BigDecimal, String> mapOfPriceParametersWithNetMvmtInUptrend = new HashMap<>();
        int indexWhereTrendStarted = 0;
        boolean doesTrendExists = false;
        boolean isStartAlreadySet = false;
        int trendInterruptedCounter = 0;
        int lastIndexWhenLowWasAboveFastEma = 0;

        // calculate intermediate stopping points
        int intermediateTrendStart = 0;


        for (int i=0; i<stockPriceDetails.size(); i++) {
            StockPriceDetails priceDetails = stockPriceDetails.get(i);
            if (priceDetails.getLowFastEmaPercentage().compareTo(BigDecimal.ZERO) > 0) {
                doesTrendExists = true;
                PriceParametersForUptrend parameters = StaticUtil.createPriceParametersForUptrend(priceDetails);
                priceParametersForUptrendList.add(parameters);
                trendInterruptedCounter = 0;
                if (!isStartAlreadySet) {
                    indexWhereTrendStarted = priceParametersForUptrendList.size() - 1;
                    intermediateTrendStart = indexWhereTrendStarted;
                }
                isStartAlreadySet = true;
                lastIndexWhenLowWasAboveFastEma = priceParametersForUptrendList.size() - 1;
            } else if (priceDetails.getLowFastEmaPercentage().compareTo(BigDecimal.ZERO) <= 0 && trendInterruptedCounter < 2 && doesTrendExists) {
                PriceParametersForUptrend parameters = StaticUtil.createPriceParametersForUptrend(priceDetails);
                BigDecimal closeWhereTrendStarted = priceParametersForUptrendList.get(intermediateTrendStart).getClose();
                BigDecimal intermediateCloseLevel = parameters.getClose();
                BigDecimal netCloseAppreciationWhenPriceComesBackToFastEma = (intermediateCloseLevel.subtract(closeWhereTrendStarted)).divide(closeWhereTrendStarted, mc);
                parameters.setNetCloseAppreciationWhenPriceComesBackToFastEma(netCloseAppreciationWhenPriceComesBackToFastEma.multiply(BigDecimal.valueOf(100)));
                priceParametersForUptrendList.add(parameters);
                ++trendInterruptedCounter;
            } else if (trendInterruptedCounter == 2) {
                doesTrendExists = false;
                isStartAlreadySet = false;
                BigDecimal closeWhereTrendStarted = priceParametersForUptrendList.get(indexWhereTrendStarted).getClose();
                BigDecimal closeWhereCloseWasLastAboveFastEMAForThisTrend = priceParametersForUptrendList.get(lastIndexWhenLowWasAboveFastEma).getClose();
                //get the "probable" max %age price movement for this trend.
                BigDecimal netAppreciationFromStartOfTrendToLastIndexWherePriceIsAboveFastEMA = (closeWhereCloseWasLastAboveFastEMAForThisTrend.subtract(closeWhereTrendStarted)).divide(closeWhereTrendStarted, mc);
                mapOfPriceParametersWithNetMvmtInUptrend.put(netAppreciationFromStartOfTrendToLastIndexWherePriceIsAboveFastEMA.multiply(BigDecimal.valueOf(100)), indexWhereTrendStarted+"-"+lastIndexWhenLowWasAboveFastEma);

                // update the last "priceParameter" to include the net %age movement
                PriceParametersForUptrend parameters = priceParametersForUptrendList.get(priceParametersForUptrendList.size()-1);
                parameters.setNetPriceAppreciation((parameters.getClose().subtract(closeWhereTrendStarted).divide(closeWhereTrendStarted, mc)).multiply(BigDecimal.valueOf(100)));
                trendInterruptedCounter = 0;
            }
        }

        if(doesTrendExists){
            BigDecimal closeWhereTrendStarted = priceParametersForUptrendList.get(indexWhereTrendStarted).getClose();
            BigDecimal closeWhereCloseWasLastAboveFastEMAForThisTrend = priceParametersForUptrendList.get(lastIndexWhenLowWasAboveFastEma).getClose();
            BigDecimal netAppreciationFromStartOfTrendToLastIndexWherePriceIsAboveFastEMA = (closeWhereCloseWasLastAboveFastEMAForThisTrend.subtract(closeWhereTrendStarted)).divide(closeWhereTrendStarted, mc);
            mapOfPriceParametersWithNetMvmtInUptrend.put(netAppreciationFromStartOfTrendToLastIndexWherePriceIsAboveFastEMA.multiply(BigDecimal.valueOf(100)), indexWhereTrendStarted+"-"+lastIndexWhenLowWasAboveFastEma);

            // get the closeAppreciation %
            PriceParametersForUptrend parameters = priceParametersForUptrendList.get(priceParametersForUptrendList.size()-1);
            BigDecimal netCloseAppreciationForThisTrend = (parameters.getClose().subtract(closeWhereTrendStarted)).divide(closeWhereTrendStarted, mc);
            parameters.setNetPriceAppreciation(netCloseAppreciationForThisTrend.multiply(BigDecimal.valueOf(100)));
        }

        return CalculatorUtil.retrievePriceHistoryOfUptrends(mapOfPriceParametersWithNetMvmtInUptrend, priceParametersForUptrendList, mc);
    }
}
