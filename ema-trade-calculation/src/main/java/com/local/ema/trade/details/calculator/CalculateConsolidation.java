package com.local.ema.trade.details.calculator;

import com.local.ema.trade.details.model.StockPriceDetails;
import com.local.ema.trade.details.util.StaticUtil;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CalculateConsolidation {


    /**
     * helper method to calculate price consolidations.
     * Rules:
     * a.) should be latest 3 days fulfilling the criteria for price consolidation
     * Criteria:
     * a.) low < fast ema . If low is higher than fast ema, then it should not be more than 0.003% higher than fast ema.
     * b.) high > fast ema . If high is lower than slow ema, then it should not be more than 0.003% lower than slow ema.
     * O.003% is a random small number that I chose.
     * Exit:
     * a.) low > fast ema Or high < slow ema for 2 consecutive days.
     *
     * @param stockPriceDetails
     * @param mc
     * @return
     */
    public <T> Set<T> priceMovementCalculatorAndGetPriceDetails(Set<T> priceParametersForConsolidationList, List<StockPriceDetails> stockPriceDetails, MathContext mc) {
        int indexWhereConsolidationStarted = 0;
        boolean hasPriceConsolidationStarted = false;
        boolean isListBeingPopulated = false;
        BigDecimal cutOffPercentage = new BigDecimal(3).divide(new BigDecimal(1000), mc);
        int priceConsolidationCounter = 0;
        int priceConsolidationDeviationCounter = 0;

        //keep a list with temp items.any record that satisfies the "consolidation" condition, should first go here.
        //the items should be moved to the "main" list after there is "break" in the consolidation, and the list has minimum 3 items.
        //once "break" is encountered, then reset the 3 counters.
        List<StockPriceDetails> tempStockPriceDetails = new ArrayList<>();
        boolean isPriceGoingUp = false;
        boolean isPriceComingDown = false;
        int checkIfThereIsChangeInTrend = 0;
        BigDecimal cutOffPercentageForTrend = new BigDecimal(5).divide(new BigDecimal(10), mc);

        for (int i=0; i<stockPriceDetails.size(); i++) {
            StockPriceDetails priceDetails = stockPriceDetails.get(i);
            if(i==0){
                tempStockPriceDetails.add(priceDetails);
                continue;
            }
            // get the latest element from the temp list
            StockPriceDetails stockPriceDetailsToBeComparedWith = tempStockPriceDetails.get(tempStockPriceDetails.size()-1);

            //Compare the low and high of current price detail with the one present in the list
            //this will help in deciding the direction of the trend
            //and based on that next set of comparisons will be derived.


            if((priceDetails.getLow().compareTo(stockPriceDetailsToBeComparedWith.getLow())>0 &&
                    priceDetails.getHigh().compareTo(stockPriceDetailsToBeComparedWith.getHigh())>0) ||
                    (priceDetails.getLow().compareTo(stockPriceDetailsToBeComparedWith.getLow())<0 &&
                         priceDetails.getHigh().compareTo(stockPriceDetailsToBeComparedWith.getHigh())<0)) {
                //check if this is the first time this has happened
                if(checkIfThereIsChangeInTrend >= 2){
                    if(tempStockPriceDetails.size()>=3){
                        // here add the logic to move the elements from "tempStockPriceDetails" to the "priceParametersForConsolidationList"
                        for(StockPriceDetails temp : tempStockPriceDetails){
                            priceParametersForConsolidationList.add((T) StaticUtil.createPriceParametersForPriceConsolidation(temp));
                        }
                    }
                    tempStockPriceDetails.clear();
                    tempStockPriceDetails.add(priceDetails);
                    checkIfThereIsChangeInTrend = 0;
                    continue;
                }
                if(hasPriceConsolidationStarted){
                    ++checkIfThereIsChangeInTrend;
                }
            } else {
                hasPriceConsolidationStarted = true;
                tempStockPriceDetails.add(priceDetails);
            }

            if(!hasPriceConsolidationStarted){
                tempStockPriceDetails.clear();
                tempStockPriceDetails.add(priceDetails);
            }

            /*if ((priceDetails.getLowFastEmaPercentage().compareTo(BigDecimal.ZERO) < 0 ||
                    priceDetails.getLow().subtract(priceDetails.getFastEMA()).divide(priceDetails.getFastEMA(), mc).compareTo(cutOffPercentage) < 0)
            && (priceDetails.getHigh().compareTo(priceDetails.getSlowEMA()) > 0 ||
                    priceDetails.getSlowEMA().subtract(priceDetails.getHigh()).divide(priceDetails.getSlowEMA(), mc).compareTo(cutOffPercentage) < 0)) {
                if(!hasPriceConsolidationStarted){
                    indexWhereConsolidationStarted = i;
                    hasPriceConsolidationStarted = true;
                }

                ++priceConsolidationCounter;

                if(isListBeingPopulated){
                    PriceParametersForConsolidation priceParametersForConsolidation = StaticUtil.createPriceParametersForPriceConsolidation(priceDetails);
                    priceParametersForConsolidationList.add((T) priceParametersForConsolidation);
                }

                if(priceConsolidationCounter>=3 && !isListBeingPopulated){
                    int counter =0;
                    while(counter<priceConsolidationCounter){
                        PriceParametersForConsolidation priceParametersForConsolidation = StaticUtil.createPriceParametersForPriceConsolidation(stockPriceDetails.get(indexWhereConsolidationStarted));
                        priceParametersForConsolidationList.add((T) priceParametersForConsolidation);
                        ++counter;
                        ++indexWhereConsolidationStarted;
                    }
                    isListBeingPopulated = true;
                }
                priceConsolidationDeviationCounter = 0;
            } else if(priceDetails.getLow().compareTo(priceDetails.getFastEMA())<0 && priceDetails.getHigh().compareTo(priceDetails.getSlowEMA())<0) {
                ++priceConsolidationDeviationCounter;
            } else if((priceDetails.getLowFastEmaPercentage().compareTo(BigDecimal.ZERO) >0 || priceDetails.getHigh().compareTo(priceDetails.getSlowEMA()) < 0)  && hasPriceConsolidationStarted
                    && priceConsolidationDeviationCounter<2) {
                PriceParametersForConsolidation priceParametersForConsolidation = StaticUtil.createPriceParametersForPriceConsolidation(priceDetails);
                priceParametersForConsolidationList.add((T) priceParametersForConsolidation);
                ++priceConsolidationDeviationCounter;
            } else if(priceConsolidationDeviationCounter==2) {
                hasPriceConsolidationStarted = false;
                priceConsolidationDeviationCounter = 0;
            }*/
        }

        return priceParametersForConsolidationList;
    }
}
