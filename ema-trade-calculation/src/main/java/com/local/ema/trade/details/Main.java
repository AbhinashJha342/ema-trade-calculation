package com.local.ema.trade.details;

import com.local.ema.trade.details.calculator.CalculateChangeInEMATrend;
import com.local.ema.trade.details.calculator.CalculateConsolidation;
import com.local.ema.trade.details.calculator.CalculateCorrection;
import com.local.ema.trade.details.calculator.CalculateIntermediateCorrections;
import com.local.ema.trade.details.calculator.CalculateUptrend;
import com.local.ema.trade.details.model.PriceParameterForEMATrendChange;
import com.local.ema.trade.details.model.PriceParametersForConsolidation;
import com.local.ema.trade.details.model.PriceParametersForCorrection;
import com.local.ema.trade.details.model.PriceParametersForIntermediateCorrection;
import com.local.ema.trade.details.model.PriceParametersForUptrend;
import com.local.ema.trade.details.model.StockPriceDetails;
import com.local.ema.trade.details.parser.CSVParser;
import com.local.ema.trade.details.util.StaticUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private static final MathContext mc = new MathContext(4, RoundingMode.HALF_UP);

    private static final String UPTREND_OUTPUT_LOCATION = "src/main/resources/uptrend/";

    private static final String CORRECTION_OUTPUT_LOCATION = "src/main/resources/correction/";


    public static void main(String[] args) {
        System.out.println("starting!");
        CalculateUptrend calculateUpTrend = new CalculateUptrend();
        CSVParser csvParser = new CSVParser();
        process(csvParser, calculateUpTrend);
    }

    /**
     * reads the incoming csv file with stock details and creates an object.
     *
     * @param csvParser
     */
    private static void process(CSVParser csvParser,CalculateUptrend calculateUpTrend){
        List<File> files = StaticUtil.getFilesAtPath("src/main/resources/PANW_historical_data/");
        for(File file : files){
            List<StockPriceDetails> stockPriceDetails = csvParser.parseFile(file, StockPriceDetails.class);
            LOG.info("number of rows parsed {}", stockPriceDetails.size());
            calculationForPriceUptrend(calculateUpTrend, stockPriceDetails, file.getName());
            //calculateForPriceCorrection(stockPriceDetails, file.getName());
            calculateForPriceDetailsBeforeEMATrendChanges(stockPriceDetails, file.getName());
        }
    }

    private static void calculationForPriceUptrend(CalculateUptrend calculateUpTrend, List<StockPriceDetails> stockPriceDetails, String fileName){
        List<PriceParametersForUptrend> priceParameterForUptrends = calculateUpTrend.priceMovementCalculatorAndGetPriceDetails(stockPriceDetails, mc);
        Collections.sort(priceParameterForUptrends);
        StaticUtil.createCsvFile(priceParameterForUptrends, String.format("Price_Uptrend_%s", fileName), UPTREND_OUTPUT_LOCATION);
    }

    private static void calculateForPriceCorrection(List<StockPriceDetails> stockPriceDetails, String fileName){
        CalculateCorrection priceMovementCalculatorInterface = new CalculateCorrection();
        List<PriceParametersForCorrection> targetList = priceMovementCalculatorInterface.priceMovementCalculatorAndGetPriceDetails(stockPriceDetails, mc);
        Collections.sort(targetList);
        StaticUtil.createCsvFile(targetList, String.format("Price_Correction_%s", fileName), CORRECTION_OUTPUT_LOCATION);
    }

    private static void calculateForPriceDetailsBeforeEMATrendChanges(List<StockPriceDetails> stockPriceDetails, String fileName){
        CalculateChangeInEMATrend calculateChangeInEMATrend = new CalculateChangeInEMATrend();
        List<PriceParameterForEMATrendChange> priceParameterForEMATrendChanges = calculateChangeInEMATrend.calculateEMATrendReversal(stockPriceDetails, mc);
        Collections.sort(priceParameterForEMATrendChanges);
        StaticUtil.createFileForEMATrendChange(priceParameterForEMATrendChanges, fileName);
    }

    // TODO in process of creating the correct rules. Not using this method anymore.
    private static void calculationForPriceConsolidation(List<StockPriceDetails> stockPriceDetails, String fileName){
        CalculateConsolidation priceMovementCalculatorInterface = new CalculateConsolidation();
        Set<PriceParametersForConsolidation> targetList = new HashSet<>();
        targetList = priceMovementCalculatorInterface.priceMovementCalculatorAndGetPriceDetails(targetList, stockPriceDetails, mc);
        List<PriceParametersForConsolidation> resultList = new ArrayList<>(targetList);
        Collections.sort(resultList);
        //StaticUtil.createCsvFileForPriceConsolidation(resultList, String.format("Price_Consolidation_%s", fileName));
    }

    private static void calculateForIntermediatePriceCorrection(List<StockPriceDetails> stockPriceDetails, String fileName){
        List<PriceParametersForIntermediateCorrection> priceParametersForIntermediateCorrectionList= CalculateIntermediateCorrections.calculatePriceParametersWhenLowComesBackToSlowEma(stockPriceDetails, mc);
        Collections.sort(priceParametersForIntermediateCorrectionList);
        StaticUtil.createFileForIntermediatePriceCorrection(priceParametersForIntermediateCorrectionList, String.format("Intermediate_Price_Correction_%s", fileName));
    }

}
