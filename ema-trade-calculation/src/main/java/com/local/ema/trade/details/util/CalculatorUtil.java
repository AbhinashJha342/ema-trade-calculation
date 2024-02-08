package com.local.ema.trade.details.util;

import com.local.ema.trade.details.model.PriceParameter;
import com.local.ema.trade.details.model.PriceParametersForCorrection;
import com.local.ema.trade.details.model.PriceParametersForUptrend;
import com.local.ema.trade.details.model.StockPriceDetails;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CalculatorUtil {

    private CalculatorUtil(){}

    public static List<? extends PriceParameter> calculateWhenPriceIsInTrend(List<? extends PriceParameter> targetList, StockPriceDetails priceDetails, int indexWhereTrendStarted, MathContext mc){
        List<PriceParametersForUptrend> priceParametersForUptrendList = isTheListPriceParametersForUptrend(targetList);
        List<PriceParametersForCorrection> priceParametersForCorrections = isTheListPriceParametersForCorrection(targetList);
        if(CollectionUtils.isNotEmpty(priceParametersForUptrendList)){
            PriceParametersForUptrend parameters = StaticUtil.createPriceParametersForUptrend(priceDetails);
            //checking how much the price moved before price goes lower than the day before
            PriceParametersForUptrend lastPriceParameterCreated = priceParametersForUptrendList.get(priceParametersForUptrendList.size()-1);
            BigDecimal lowFromLastPriceParameter = lastPriceParameterCreated.getLow();
            if(parameters.getLow().compareTo(lowFromLastPriceParameter) < 0){
                PriceParametersForUptrend parametersForUptrend = priceParametersForUptrendList.get(indexWhereTrendStarted);
                parameters.setPriceAppreciationWhenLowIsLowerThanPreviousDay(
                        lowFromLastPriceParameter.subtract(parametersForUptrend.getLow())
                                .divide(parametersForUptrend.getLow(), mc)
                                .multiply(BigDecimal.valueOf(100)));
                }
            priceParametersForUptrendList.add(parameters);
            return priceParametersForUptrendList;
        } else if (CollectionUtils.isNotEmpty(priceParametersForCorrections)) {
            PriceParametersForCorrection parameters = StaticUtil.createPriceParametersForCorrection(priceDetails);
            //checking how much the price corrected before price any day when high was higher than previous day
            PriceParametersForCorrection lastPriceParameterCreated = priceParametersForCorrections.get(targetList.size()-1);
            BigDecimal highFromLastPriceParameter = lastPriceParameterCreated.getHigh();
            if(parameters.getHigh().compareTo(highFromLastPriceParameter) < 0){
                PriceParametersForCorrection parametersForCorrection = priceParametersForCorrections.get(indexWhereTrendStarted);
                parameters.setPriceAppreciationWhenHighIsHigherThanPreviousDay(
                        highFromLastPriceParameter.subtract(parametersForCorrection.getHigh())
                                .divide(parametersForCorrection.getHigh(), mc)
                                .multiply(BigDecimal.valueOf(100)));

            }
            priceParametersForCorrections.add(parameters);
            return priceParametersForCorrections;
        }

        return targetList;
    }

    public static List<? extends PriceParameter> calculateWhenTrendIsInterrupted(List<? extends PriceParameter> targetList, StockPriceDetails priceDetails, int intermediateTrendStart, MathContext mc) {
        List<PriceParametersForUptrend> priceParametersForUptrendList = isTheListPriceParametersForUptrend(targetList);
        List<PriceParametersForCorrection> priceParametersForCorrections = isTheListPriceParametersForCorrection(targetList);
        BigDecimal closeWhereTrendStarted;
        BigDecimal intermediateCloseLevel;
        if(CollectionUtils.isNotEmpty(priceParametersForUptrendList)){
            closeWhereTrendStarted = priceParametersForUptrendList.get(intermediateTrendStart).getClose();
            PriceParametersForUptrend parameters = StaticUtil.createPriceParametersForUptrend(priceDetails);
            intermediateCloseLevel = parameters.getClose();
            BigDecimal netCloseAppreciationWhenPriceComesBackToFastEma = (intermediateCloseLevel.subtract(closeWhereTrendStarted)).divide(closeWhereTrendStarted, mc);
            parameters.setNetCloseAppreciationWhenPriceComesBackToFastEma(netCloseAppreciationWhenPriceComesBackToFastEma.multiply(BigDecimal.valueOf(100)));
            priceParametersForUptrendList.add(parameters);
            return priceParametersForUptrendList;
        } else if (CollectionUtils.isNotEmpty(priceParametersForCorrections)) {
            closeWhereTrendStarted = priceParametersForCorrections.get(intermediateTrendStart).getClose();
            PriceParametersForCorrection parameters = StaticUtil.createPriceParametersForCorrection(priceDetails);
            intermediateCloseLevel = parameters.getClose();
            BigDecimal netCloseAppreciationWhenPriceComesBackToFastEma = (intermediateCloseLevel.subtract(closeWhereTrendStarted)).divide(closeWhereTrendStarted, mc);
            parameters.setNetCloseAppreciationWhenPriceComesBackToFastEma(netCloseAppreciationWhenPriceComesBackToFastEma.multiply(BigDecimal.valueOf(100)));
            priceParametersForCorrections.add(parameters);
            return priceParametersForCorrections;
        }

        return targetList;
    }

    public  static List<? extends PriceParameter> calculateWhenTrendEnds(List<? extends PriceParameter> targetList, int indexWhereTrendStarted, Map<BigDecimal, String> mapOfPriceParametersWithNetPriceMvmt, int lastIndexWhenLowWasAboveFastEma, MathContext mc) {
        List<PriceParametersForUptrend> priceParametersForUptrendList = isTheListPriceParametersForUptrend(targetList);
        List<PriceParametersForCorrection> priceParametersForCorrections = isTheListPriceParametersForCorrection(targetList);
        BigDecimal closeWhereTrendStarted = null;
        BigDecimal closeWhereCloseWasLastAboveFastEMAForThisTrend = null;
        if(CollectionUtils.isNotEmpty(priceParametersForUptrendList)){
            closeWhereTrendStarted = priceParametersForUptrendList.get(indexWhereTrendStarted).getClose();
            closeWhereCloseWasLastAboveFastEMAForThisTrend = priceParametersForUptrendList.get(lastIndexWhenLowWasAboveFastEma).getClose();
            // update the last "priceParameter" to include the net %age movement
            PriceParametersForUptrend parameters = (PriceParametersForUptrend) targetList.get(targetList.size() - 1);
            parameters.setNetPriceAppreciation((parameters.getClose().subtract(closeWhereTrendStarted).divide(closeWhereTrendStarted, mc)).multiply(BigDecimal.valueOf(100)));
            targetList = new ArrayList<>(priceParametersForUptrendList);
        } else if (CollectionUtils.isNotEmpty(priceParametersForCorrections)) {
            closeWhereTrendStarted = priceParametersForCorrections.get(indexWhereTrendStarted).getClose();
            closeWhereCloseWasLastAboveFastEMAForThisTrend = priceParametersForCorrections.get(lastIndexWhenLowWasAboveFastEma).getClose();
            // update the last "priceParameter" to include the net %age movement
            PriceParametersForCorrection parameters = (PriceParametersForCorrection) targetList.get(targetList.size() - 1);
            parameters.setNetPriceAppreciation((parameters.getClose().subtract(closeWhereTrendStarted).divide(closeWhereTrendStarted, mc)).multiply(BigDecimal.valueOf(100)));
            targetList = new ArrayList<>(priceParametersForCorrections);
        }
        //get the "probable" max %age price movement for this trend.
        if(null!=closeWhereCloseWasLastAboveFastEMAForThisTrend) {
            BigDecimal netAppreciationFromStartOfTrendToLastIndexWherePriceIsAboveFastEMA = (closeWhereCloseWasLastAboveFastEMAForThisTrend.subtract(closeWhereTrendStarted)).divide(closeWhereTrendStarted, mc);
            mapOfPriceParametersWithNetPriceMvmt.put(netAppreciationFromStartOfTrendToLastIndexWherePriceIsAboveFastEMA.multiply(BigDecimal.valueOf(100)), indexWhereTrendStarted + "-" + lastIndexWhenLowWasAboveFastEma);
        }

        return targetList;
    }

    public static List<? extends PriceParameter> updateListIfTrendContinuesAfterSampleEnds(List<? extends PriceParameter> targetList, int indexWhereTrendStarted, Map<BigDecimal,
            String> mapOfPriceParametersWithNetPriceMvmt, int lastIndexWhenLowWasAboveFastEma, MathContext mc){
        List<PriceParametersForUptrend> priceParametersForUptrendList = isTheListPriceParametersForUptrend(targetList);
        List<PriceParametersForCorrection> priceParametersForCorrections = isTheListPriceParametersForCorrection(targetList);
        BigDecimal closeWhereTrendStarted = null;
        BigDecimal closeWhereCloseWasLastAboveFastEMAForThisTrend = null;
        BigDecimal netCloseAppreciationForThisTrend;
        if(CollectionUtils.isNotEmpty(priceParametersForUptrendList)){
            closeWhereTrendStarted = priceParametersForUptrendList.get(indexWhereTrendStarted).getClose();
            closeWhereCloseWasLastAboveFastEMAForThisTrend = priceParametersForUptrendList.get(lastIndexWhenLowWasAboveFastEma).getClose();
            PriceParametersForUptrend parameters = priceParametersForUptrendList.get(priceParametersForUptrendList.size()-1);
            netCloseAppreciationForThisTrend = (parameters.getClose().subtract(closeWhereTrendStarted)).divide(closeWhereTrendStarted, mc);
            parameters.setNetPriceAppreciation(netCloseAppreciationForThisTrend.multiply(BigDecimal.valueOf(100)));
            targetList = new ArrayList<>(priceParametersForUptrendList);
        } else if (CollectionUtils.isNotEmpty(priceParametersForCorrections)) {
            closeWhereTrendStarted = priceParametersForCorrections.get(indexWhereTrendStarted).getClose();
            closeWhereCloseWasLastAboveFastEMAForThisTrend = priceParametersForCorrections.get(lastIndexWhenLowWasAboveFastEma).getClose();
            PriceParametersForCorrection parameters = priceParametersForCorrections.get(priceParametersForCorrections.size()-1);
            netCloseAppreciationForThisTrend = (parameters.getClose().subtract(closeWhereTrendStarted)).divide(closeWhereTrendStarted, mc);
            parameters.setNetPriceAppreciation(netCloseAppreciationForThisTrend.multiply(BigDecimal.valueOf(100)));
            targetList = new ArrayList<>(priceParametersForCorrections);
        }

        if(closeWhereCloseWasLastAboveFastEMAForThisTrend!=null){
            BigDecimal netAppreciationFromStartOfTrendToLastIndexWherePriceIsAboveFastEMA = (closeWhereCloseWasLastAboveFastEMAForThisTrend.subtract(closeWhereTrendStarted)).divide(closeWhereTrendStarted, mc);
            mapOfPriceParametersWithNetPriceMvmt.put(netAppreciationFromStartOfTrendToLastIndexWherePriceIsAboveFastEMA.multiply(BigDecimal.valueOf(100)), indexWhereTrendStarted+"-"+lastIndexWhenLowWasAboveFastEma);
        }
        return targetList;
    }

    /**
     * helper method to extract the price movement details from a map where the key is netPriceAppreciation and value is <br>
     *
     * @param mapOfPriceParametersWithNetMvmtInUptrend
     * @param priceParametersForUptrendList
     * @return
     */
    public static List<PriceParametersForUptrend> retrievePriceHistoryOfUptrends(Map<BigDecimal, String> mapOfPriceParametersWithNetMvmtInUptrend, List<PriceParametersForUptrend> priceParametersForUptrendList, MathContext mc){
        List<PriceParametersForUptrend> filteredForUptrendPriceParameters = new ArrayList<>(priceParametersForUptrendList);
        for(Map.Entry<BigDecimal, String> priceMovementDetails : mapOfPriceParametersWithNetMvmtInUptrend.entrySet()) {
            String[] indexes = priceMovementDetails.getValue().split("-");
            BigDecimal netMaxPriceAppreciation = priceMovementDetails.getKey();
            int lastIndexWhenLowWasAboveFastEma = Integer.parseInt(indexes[1]);
            PriceParametersForUptrend intermediateTrendEnd = filteredForUptrendPriceParameters.get(lastIndexWhenLowWasAboveFastEma);
            intermediateTrendEnd.setNetMaxPriceAppreciation(netMaxPriceAppreciation);
        }

        return filteredForUptrendPriceParameters;
    }

    private static List<PriceParametersForUptrend> isTheListPriceParametersForUptrend(List<? extends PriceParameter> targetList){
        return targetList.stream().filter(PriceParametersForUptrend.class::isInstance).map(PriceParametersForUptrend.class::cast).collect(Collectors.toList());
    }

    private static List<PriceParametersForCorrection> isTheListPriceParametersForCorrection(List<? extends PriceParameter> targetList){
        return targetList.stream().filter(PriceParametersForCorrection.class::isInstance).map(PriceParametersForCorrection.class::cast).collect(Collectors.toList());
    }
}
