package com.local.ema.trade.details.calculator;

import com.local.ema.trade.details.model.PriceParameter;
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

public class CalculateUptrend implements PriceMovementCalculatorInterface{

    /**
     * reads the list of object with stock price details. checks for the uptrend using following rules : <br>
     * 1. low - fast ema % > 0.
     * 2. maintains a "indexWhereTrendStarted" counter to keep track of the day when condition 1.) satisfies.
     * 3. increments "lastIndexWhenLowWasAboveFastEma" which is used to calculate "netPriceAppreciation" later.
     * 3. increments a counter(trendInterruptedCounter) when low - fast ema % < 0.
     * 4. The counter "trendInterruptedCounter" is reset to 0, if condition 1.) is satisfied again.
     * 5. once the "trendInterruptedCounter" reaches 2, and condition 1.) is not satisfied in the immediate next iteration, then <br>
     *      a. this day marks the end of trend.
     *      b. calculate the close-close %age movement between "indexWhereTrendStarted" and the "lastIndexWhenLowWasAboveFastEma".
     *      c. creates a map using the value of (b.) as key, and value are the indexes("indexWhereTrendStarted" and "lastIndexWhenLowWasAboveFastEma") separated by "-".
     */

    @Override
    public <T extends PriceParameter> List<T> priceMovementCalculatorAndGetPriceDetails(List<StockPriceDetails> stockPriceDetails, MathContext mc) {
        List<PriceParametersForUptrend> priceParametersForUptrendList = new ArrayList<>();
        Map<BigDecimal, String> mapOfPriceParametersWithNetPriceMvmt = new HashMap<>();
        int indexWhereTrendStarted = 0;
        boolean doesTrendExists = false;
        boolean isStartAlreadySet = false;
        int trendInterruptedCounter = 0;
        int lastIndexWhenLowWasAboveFastEma = 0;

        // calculate intermediate stopping points
        int intermediateTrendStart = 0;


        for (StockPriceDetails priceDetails : stockPriceDetails) {
            if (priceDetails.getLowFastEmaPercentage().compareTo(BigDecimal.ZERO) > 0) {
                if (!priceParametersForUptrendList.isEmpty()) {
                    priceParametersForUptrendList = (List<PriceParametersForUptrend>) CalculatorUtil.calculateWhenPriceIsInTrend(priceParametersForUptrendList, priceDetails, intermediateTrendStart, mc);
                } else {
                    PriceParametersForUptrend parameters = StaticUtil.createPriceParametersForUptrend(priceDetails);
                    priceParametersForUptrendList.add(parameters);

                }
                doesTrendExists = true;
                trendInterruptedCounter = 0;
                if (!isStartAlreadySet) {
                    indexWhereTrendStarted = priceParametersForUptrendList.size() - 1;
                    intermediateTrendStart = indexWhereTrendStarted;
                }
                isStartAlreadySet = true;
                lastIndexWhenLowWasAboveFastEma = priceParametersForUptrendList.size() - 1;
            } else if (doesTrendExists && trendInterruptedCounter < 2) {
                priceParametersForUptrendList = (List<PriceParametersForUptrend>) CalculatorUtil.calculateWhenTrendIsInterrupted(priceParametersForUptrendList, priceDetails, intermediateTrendStart, mc);
                ++trendInterruptedCounter;
            } else if (trendInterruptedCounter == 2) {
                doesTrendExists = false;
                isStartAlreadySet = false;
                priceParametersForUptrendList = (List<PriceParametersForUptrend>) CalculatorUtil.calculateWhenTrendEnds(priceParametersForUptrendList, indexWhereTrendStarted,
                        mapOfPriceParametersWithNetPriceMvmt, lastIndexWhenLowWasAboveFastEma, mc);
                trendInterruptedCounter = 0;
            }
        }

        if(doesTrendExists){
            priceParametersForUptrendList = (List<PriceParametersForUptrend>) CalculatorUtil.updateListIfTrendContinuesAfterSampleEnds(priceParametersForUptrendList,indexWhereTrendStarted,mapOfPriceParametersWithNetPriceMvmt,lastIndexWhenLowWasAboveFastEma,mc);
        }

        return (List<T>) CalculatorUtil.retrievePriceHistoryOfUptrends(mapOfPriceParametersWithNetPriceMvmt, priceParametersForUptrendList,mc);
    }
}
