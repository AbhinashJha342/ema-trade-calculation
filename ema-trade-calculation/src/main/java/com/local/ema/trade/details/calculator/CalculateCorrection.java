package com.local.ema.trade.details.calculator;

import com.local.ema.trade.details.model.PriceParameter;
import com.local.ema.trade.details.model.PriceParametersForCorrection;
import com.local.ema.trade.details.model.StockPriceDetails;
import com.local.ema.trade.details.util.CalculatorUtil;
import com.local.ema.trade.details.util.StaticUtil;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateCorrection implements PriceMovementCalculatorInterface {

    /**
     * reads the list of object with stock price details. checks for the uptrend using following rules : <br>
     * 1. high - fast ema % < 0.
     * 2. keeps a counter(trendInterruptedCounter) of 2 days when high - fast ema % > 0.
     * 3. maintains a indexWhereTrendStarted counter to keep track of the day when first the above 2 conditions met.
     * 4. till above 2 condition satisfies, keeps creating object with some stock price details and adds to another list.
     * 5. once the buffer of 2 days exceeds, then <br>
     *      a. this day marks the end of trend.
     *      b. calculate the close-close %age movement of the stock price during these days.
     *      c. creates a map using the value of (b.) as key, and the list of objects in the range from indexWhereTrendStarted to the day trend breaks.
     * 6. resets the trendInterruptedCounter to 0.
     */
    @Override
    public <T extends PriceParameter> List<T> priceMovementCalculatorAndGetPriceDetails(List<StockPriceDetails> stockPriceDetails, MathContext mc) {
        List<PriceParametersForCorrection> targetList = new ArrayList<>();
        Map<BigDecimal, String> mapOfPriceParametersWithNetPriceMvmt = new HashMap<>();
        int indexWhereTrendStarted = 0;
        boolean doesTrendExists = false;
        boolean isStartAlreadySet = false;
        int trendInterruptedCounter = 0;
        int lastIndexWhenHighWasBelowSlowEma = 0;

        // calculate intermediate stopping points
        int intermediateTrendStart = 0;


        for (StockPriceDetails priceDetails : stockPriceDetails) {
            BigDecimal highToFastEMADiff = priceDetails.getHigh().subtract(priceDetails.getFastEMA());
            if (highToFastEMADiff.signum() == -1) {
                if (!targetList.isEmpty()) {
                    targetList = (List<PriceParametersForCorrection>) CalculatorUtil.calculateWhenPriceIsInTrend(targetList, priceDetails, indexWhereTrendStarted, mc);
                } else {
                    PriceParametersForCorrection parameters = StaticUtil.createPriceParametersForCorrection(priceDetails);
                    targetList.add(parameters);
                }
                doesTrendExists = true;
                trendInterruptedCounter = 0;
                if (!isStartAlreadySet) {
                    indexWhereTrendStarted = targetList.size() - 1;
                    intermediateTrendStart = indexWhereTrendStarted;
                }
                isStartAlreadySet = true;
                lastIndexWhenHighWasBelowSlowEma = targetList.size() - 1;
            } else if (doesTrendExists && trendInterruptedCounter < 2) {
                targetList = (List<PriceParametersForCorrection>) CalculatorUtil.calculateWhenTrendIsInterrupted(targetList, priceDetails, intermediateTrendStart, mc);
                ++trendInterruptedCounter;
            } else if (trendInterruptedCounter == 2) {
                doesTrendExists = false;
                isStartAlreadySet = false;
                targetList = (List<PriceParametersForCorrection>) CalculatorUtil.calculateWhenTrendEnds(targetList, indexWhereTrendStarted,
                        mapOfPriceParametersWithNetPriceMvmt, lastIndexWhenHighWasBelowSlowEma, mc);
                trendInterruptedCounter = 0;
            }
        }

        if(doesTrendExists){
            targetList = (List<PriceParametersForCorrection>) CalculatorUtil.updateListIfTrendContinuesAfterSampleEnds(targetList,indexWhereTrendStarted,mapOfPriceParametersWithNetPriceMvmt,lastIndexWhenHighWasBelowSlowEma,mc);
        }

        return (List<T>) retrievePriceHistoryOfCorrection(mapOfPriceParametersWithNetPriceMvmt, targetList);
    }

    /**
     * helper method to extract the price movement details from a map where the key is netPriceAppreciation and value is <br>
     *
     * @param mapOfPriceParametersWithNetMvmtInUptrend
     * @param priceParametersForCorrection
     * @return
     */
    private static List<PriceParametersForCorrection> retrievePriceHistoryOfCorrection(Map<BigDecimal, String> mapOfPriceParametersWithNetMvmtInUptrend, List<PriceParametersForCorrection> priceParametersForCorrection){
        List<PriceParametersForCorrection> filteredForUptrendPriceParameters = new ArrayList<>(priceParametersForCorrection);
        for(Map.Entry<BigDecimal, String> priceMovementDetails : mapOfPriceParametersWithNetMvmtInUptrend.entrySet()) {
            String[] indexes = priceMovementDetails.getValue().split("-");
            BigDecimal netMaxPriceAppreciation = priceMovementDetails.getKey();
            int lastIndexWhenLowWasAboveFastEma = Integer.parseInt(indexes[1]);
            PriceParametersForCorrection intermediateTrendEnd = filteredForUptrendPriceParameters.get(lastIndexWhenLowWasAboveFastEma);
            intermediateTrendEnd.setNetMaxPriceAppreciation(netMaxPriceAppreciation);
        }

        return filteredForUptrendPriceParameters;
    }
    /**
     * helper method to extract the price movement details from a map where the key is netPriceCorrection and value is <br>
     * string which contains the start and end index,separated by hyphen("-"),  when a trend existed
     * example: "4-10".
     * @param mapOfPriceParametersWithNetMvmtInUptrend
     * @param priceParametersForCorrections
     * @return
     */
    /*private static List<PriceParametersForCorrection> retrievePriceHistoryOfCorrection(Map<BigDecimal, String> mapOfPriceParametersWithNetMvmtInUptrend, List<PriceParametersForCorrection> priceParametersForCorrections){
        List<PriceParametersForCorrection> filteredForUptrendPriceParameters = new ArrayList<>();
        for(Map.Entry<BigDecimal, String> priceMovementDetails : mapOfPriceParametersWithNetMvmtInUptrend.entrySet()) {
            String[] indexes = priceMovementDetails.getValue().split("-");
            BigDecimal priceAppreciation = priceMovementDetails.getKey();
            int startIndex = Integer.parseInt(indexes[0]);
            int endIndex = Integer.parseInt(indexes[1]);
            // adding the netPriceCorrection to the object which contains price details when the trend ended.
            // this is helpful when creating the csv to add the netPriceCorrection in the row where trend ends.
            PriceParametersForCorrection intermediateTrendEnd = priceParametersForCorrections.get(endIndex);
            intermediateTrendEnd.setNetPriceAppreciation(priceAppreciation);
            int counter = startIndex;
            while(counter<=endIndex) {
                //use the object that has been updated above with netPriceAppreciation details.
                if(counter == endIndex) {
                    filteredForUptrendPriceParameters.add(intermediateTrendEnd);
                    counter++;
                    continue;
                }
                filteredForUptrendPriceParameters.add(priceParametersForCorrections.get(counter));
                counter++;
            }
        }

        return filteredForUptrendPriceParameters;
    }*/
}
