package com.local.ema.trade.details.calculator;

import com.local.ema.trade.details.model.PriceParameterForEMATrendChange;
import com.local.ema.trade.details.model.StockPriceDetails;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

public class CalculateChangeInEMATrend {


    public List<PriceParameterForEMATrendChange> calculateEMATrendReversal(List<StockPriceDetails> stockPriceDetails, MathContext mc) {
        List<PriceParameterForEMATrendChange> priceParameterForEMATrendChanges = new ArrayList<>();
        boolean isPriceInUptrend = false;
        boolean isPriceInDowntrend = false;
        int indexWhereUptrendStarted = 0;
        int indexWhereDowntrendStarted = 0;
        BigDecimal highestClosingForUptrend = null;
        BigDecimal highestClosingForDowntrend = null;

        for(int i=0; i<stockPriceDetails.size(); i++){
            StockPriceDetails priceDetails = stockPriceDetails.get(i);
            if(priceDetails.getFastEMA().compareTo(priceDetails.getSlowEMA()) > 0){
                PriceParameterForEMATrendChange priceParameterForEMATrendChange = PriceParameterForEMATrendChange.builder()
                        .date(priceDetails.getDate()).open(priceDetails.getOpen()).close(priceDetails.getClose())
                        .volume(priceDetails.getVolume()).emaDiffPercentage(priceDetails.getEmaDiffPercentage()).build();
                if(priceDetails.getLowFastEmaPercentage().signum() != -1){
                    highestClosingForUptrend = priceDetails.getClose();
                }
                if(!isPriceInUptrend){
                    indexWhereUptrendStarted = i;
                    isPriceInUptrend = true;
                    highestClosingForUptrend = priceDetails.getClose();
                }
                if(isPriceInDowntrend){
                    isPriceInDowntrend = false;
                    PriceParameterForEMATrendChange priceParameterWhereUptrendStarted = priceParameterForEMATrendChanges.get(indexWhereDowntrendStarted);
                    priceParameterForEMATrendChange.setNetPriceMovement(highestClosingForDowntrend.subtract(priceParameterWhereUptrendStarted.getClose()).divide(priceParameterWhereUptrendStarted.getClose(), mc).multiply(BigDecimal.valueOf(100)));
                }
                priceParameterForEMATrendChanges.add(priceParameterForEMATrendChange);
            } else if (priceDetails.getFastEMA().compareTo(priceDetails.getSlowEMA()) < 0) {
                PriceParameterForEMATrendChange priceParameterForEMATrendChange = PriceParameterForEMATrendChange.builder()
                        .date(priceDetails.getDate()).open(priceDetails.getOpen()).close(priceDetails.getClose())
                        .volume(priceDetails.getVolume()).emaDiffPercentage(priceDetails.getEmaDiffPercentage())
                        .build();
                if(priceDetails.getHigh().compareTo(priceDetails.getFastEMA()) < 0) {
                    highestClosingForDowntrend = priceDetails.getClose();
                }
                if(isPriceInUptrend){
                    PriceParameterForEMATrendChange priceParameterWhereUptrendStarted = priceParameterForEMATrendChanges.get(indexWhereUptrendStarted);
                    priceParameterForEMATrendChange.setNetPriceMovement(highestClosingForUptrend.subtract(priceParameterWhereUptrendStarted.getClose()).divide(priceParameterWhereUptrendStarted.getClose(), mc).multiply(BigDecimal.valueOf(100)));
                    isPriceInUptrend = false;
                }
                priceParameterForEMATrendChanges.add(priceParameterForEMATrendChange);
                if(!isPriceInDowntrend){
                    isPriceInDowntrend = true;
                    indexWhereDowntrendStarted = i;
                    highestClosingForDowntrend = priceDetails.getClose();
                }
            }
        }

        if(isPriceInDowntrend){
            PriceParameterForEMATrendChange priceParameterWhereUptrendStarted = priceParameterForEMATrendChanges.get(indexWhereDowntrendStarted);
            PriceParameterForEMATrendChange priceParameterForEMATrendChange = priceParameterForEMATrendChanges.get(priceParameterForEMATrendChanges.size()-1);
            priceParameterForEMATrendChange.setNetPriceMovement(priceParameterForEMATrendChange.getClose().subtract(priceParameterWhereUptrendStarted.getClose())
                    .divide(priceParameterWhereUptrendStarted.getClose(), mc).multiply(BigDecimal.valueOf(100)));
        } else if (isPriceInUptrend) {
            PriceParameterForEMATrendChange priceParameterWhereUptrendStarted = priceParameterForEMATrendChanges.get(indexWhereUptrendStarted);
            PriceParameterForEMATrendChange priceParameterForEMATrendChange = priceParameterForEMATrendChanges.get(priceParameterForEMATrendChanges.size()-1);
            priceParameterForEMATrendChange.setNetPriceMovement(priceParameterForEMATrendChange.getClose().subtract(priceParameterWhereUptrendStarted.getClose())
                    .divide(priceParameterWhereUptrendStarted.getClose(), mc).multiply(BigDecimal.valueOf(100)));
        }

        return priceParameterForEMATrendChanges;
    }


}
