package com.local.ema.trade.details.util;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.local.ema.trade.details.model.PriceParameterForEMATrendChange;
import com.local.ema.trade.details.model.PriceParametersForConsolidation;
import com.local.ema.trade.details.model.PriceParametersForCorrection;
import com.local.ema.trade.details.model.PriceParametersForIntermediateCorrection;
import com.local.ema.trade.details.model.PriceParametersForUptrend;
import com.local.ema.trade.details.model.PriceParameter;
import com.local.ema.trade.details.model.StockPriceDetails;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.MathContext;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StaticUtil {


    private static final String[] possibleDatePatterns = {"MM/dd/yyyy", "M/d/yyyy", "MM/dd/yy", "MM/dd/yy", "dd/MM/yy", "dd/MM/yyyy"};

    private static final CsvMapper mapper = new CsvMapper();

    private static final String INTERMEDIATE_CORRECTION_OUTPUT_LOCATION = "src/main/resources/lowBackToSLowEMA/";

    private static final String EMA_TREND_CHANGE_OUTPUT_LOCATION = "src/main/resources/EMATrendChange/";

    /**
     * Method to read files from a directory. Meant to be used to bypass AWS connection issues.
     *
     * @param path The path from where we are reading the files
     * @return The list of files
     */
    public static List<File> getFilesAtPath(String path) {
        try {
            Path filesPath = Paths.get(path);
            if (filesPath.toFile().exists()) {
                try (Stream<Path> walk = Files.walk(filesPath)) {
                    List<File> newFiles =
                            walk.map(Path::toFile).filter(File::isFile).collect(Collectors.toList());
                    return newFiles;
                }
            }
        } catch (IOException e) {
            System.out.println("some exception "+e.getMessage());
        }
        return Collections.emptyList();
    }

    public static LocalDate parseToLocalDate(String str) throws DateTimeParseException {
        if (str == null) {
            throw new IllegalArgumentException("Date must not be null");
        }

        for (String possibleDatePattern : possibleDatePatterns) {
            try {
                return LocalDate.parse(
                        str,
                        DateTimeFormatter.ofPattern(possibleDatePattern));
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    public static PriceParametersForUptrend createPriceParametersForUptrend(StockPriceDetails priceDetails){
        return PriceParametersForUptrend.builder()
                .date(priceDetails.getDate())
                .open(priceDetails.getOpen())
                .high(priceDetails.getHigh())
                .low(priceDetails.getLow())
                .close(priceDetails.getClose())
                .dayRange(priceDetails.getDayRange())
                .openCloseRange(priceDetails.getOpenCloseRange())
                .incrementalPercentageCloseAppreciation(priceDetails.getPercentageCloseAppreciation())
                .emaDiffPercentage(priceDetails.getEmaDiffPercentage())
                .emaAppreciation(priceDetails.getEmaAppreciation())
                .highFastEmaPercentage(priceDetails.getHighFastEmaPercentage())
                .lowFastEmaPercentage(priceDetails.getLowFastEmaPercentage())
                .build();
    }

    public static PriceParametersForCorrection createPriceParametersForCorrection(StockPriceDetails priceDetails){
        return PriceParametersForCorrection.builder()
                .date(priceDetails.getDate())
                .open(priceDetails.getOpen())
                .high(priceDetails.getHigh())
                .low(priceDetails.getLow())
                .close(priceDetails.getClose())
                .dayRange(priceDetails.getDayRange())
                .openCloseRange(priceDetails.getOpenCloseRange())
                .incrementalPercentageCloseAppreciation(priceDetails.getPercentageCloseAppreciation())
                .emaDiffPercentage(priceDetails.getEmaDiffPercentage())
                .emaAppreciation(priceDetails.getEmaAppreciation())
                .highFastEmaPercentage(priceDetails.getHighFastEmaPercentage())
                .lowFastEmaPercentage(priceDetails.getLowFastEmaPercentage())
                .build();
    }

    public static PriceParametersForConsolidation createPriceParametersForPriceConsolidation(StockPriceDetails priceDetails){
        return PriceParametersForConsolidation.builder()
                .date(priceDetails.getDate())
                .open(priceDetails.getOpen())
                .close(priceDetails.getClose())
                .volume(priceDetails.getVolume())
                .dayRange(priceDetails.getDayRange())
                .openCloseRange(priceDetails.getOpenCloseRange())
                .incrementalPercentageCloseAppreciation(priceDetails.getPercentageCloseAppreciation())
                .emaDiffPercentage(priceDetails.getEmaDiffPercentage())
                .build();
    }

    public static PriceParametersForIntermediateCorrection createPriceParametersForIntermediateCorrection(StockPriceDetails priceDetails, MathContext mc){
        return PriceParametersForIntermediateCorrection.builder()
                .date(priceDetails.getDate())
                .open(priceDetails.getOpen())
                .low(priceDetails.getLow())
                .close(priceDetails.getClose())
                .openCloseRange(priceDetails.getOpenCloseRange())
                .emaDiffPercentage(priceDetails.getEmaDiffPercentage())
                .lowFastEmaPercentage(priceDetails.getLowFastEmaPercentage())
                .lowSlowEmaPercentage(priceDetails.getLow().subtract(priceDetails.getSlowEMA()).divide(priceDetails.getLow(), mc))
                .emaAppreciation(priceDetails.getEmaAppreciation())
                .build();
    }

    public static void createCsvFile(List<? extends PriceParameter> priceParameter, String fileName, String location) {
        File file = new File(location+fileName);
        CsvSchema.Builder schemaBuilder = CsvSchema.builder();
        schemaBuilder.setUseHeader(true);
        JavaTimeModule javaTimeModule=new JavaTimeModule();
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ISO_DATE));
        mapper.registerModule(javaTimeModule);
        // create header
        schemaBuilder.addColumn("date");
        schemaBuilder.addColumn("open");
        schemaBuilder.addColumn("high");
        schemaBuilder.addColumn("low");
        schemaBuilder.addColumn("close");
        schemaBuilder.addColumn("dayRange");
        schemaBuilder.addColumn("openCloseRange");
        schemaBuilder.addColumn("incrementalPercentageCloseAppreciation");
        schemaBuilder.addColumn("emaDiffPercentage");
        schemaBuilder.addColumn("lowFastEmaPercentage");
        schemaBuilder.addColumn("highFastEmaPercentage");
        schemaBuilder.addColumn("emaAppreciation");
        if(priceParameter.get(0) instanceof PriceParametersForUptrend) {
            schemaBuilder.addColumn("priceAppreciationWhenLowIsLowerThanPreviousDay");
        } else if (priceParameter.get(0) instanceof PriceParametersForCorrection) {
            schemaBuilder.addColumn("priceAppreciationWhenHighIsHigherThanPreviousDay");
        }

        schemaBuilder.addColumn("netCloseAppreciationWhenPriceComesBackToFastEma");
        schemaBuilder.addColumn("netMaxPriceAppreciation");
        schemaBuilder.addColumn("netPriceAppreciation");
        // header created
        try (PrintWriter printWriter = new PrintWriter(file)) {
            CsvSchema schema =
                    schemaBuilder
                            .build()
                            .withLineSeparator(System.lineSeparator())
                            .withArrayElementSeparator("/")
                            .withHeader();
            mapper.writer(schema).writeValues(printWriter).write(priceParameter);
        } catch (IOException e) {
            System.out.println("Exception!!"+ e.getMessage());
        } finally {
            schemaBuilder.clearColumns();
        }
    }

    public static void createFileForEMATrendChange(List<PriceParameterForEMATrendChange> priceParameterForEMATrendChanges, String fileName) {
        File file = new File(EMA_TREND_CHANGE_OUTPUT_LOCATION+fileName);
        CsvSchema.Builder schemaBuilder = CsvSchema.builder();
        schemaBuilder.setUseHeader(true);
        JavaTimeModule javaTimeModule=new JavaTimeModule();
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ISO_DATE));
        mapper.registerModule(javaTimeModule);
        // create header
        schemaBuilder.addColumn("date");
        schemaBuilder.addColumn("open");
        schemaBuilder.addColumn("close");
        schemaBuilder.addColumn("volume");
        schemaBuilder.addColumn("emaDiffPercentage");
        schemaBuilder.addColumn("netPriceMovement");
        // header created
        try (PrintWriter printWriter = new PrintWriter(file)) {
            CsvSchema schema =
                    schemaBuilder
                            .build()
                            .withLineSeparator(System.lineSeparator())
                            .withArrayElementSeparator("/")
                            .withHeader();
            mapper.writer(schema).writeValues(printWriter).write(priceParameterForEMATrendChanges);
        } catch (IOException e) {
            System.out.println("Exception!!"+ e.getMessage());
        } finally {
            schemaBuilder.clearColumns();
        }
    }

    public static void createFileForIntermediatePriceCorrection(List<PriceParametersForIntermediateCorrection> priceParametersForIntermediateCorrections, String fileName){
        File file = new File(INTERMEDIATE_CORRECTION_OUTPUT_LOCATION+fileName);
        CsvSchema.Builder schemaBuilder = CsvSchema.builder();
        schemaBuilder.setUseHeader(true);
        JavaTimeModule javaTimeModule=new JavaTimeModule();
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ISO_DATE));
        mapper.registerModule(javaTimeModule);
        // create header
        schemaBuilder.addColumn("date");
        schemaBuilder.addColumn("open");
        schemaBuilder.addColumn("low");
        schemaBuilder.addColumn("close");
        schemaBuilder.addColumn("openCloseRange");
        schemaBuilder.addColumn("emaDiffPercentage");
        schemaBuilder.addColumn("lowFastEmaPercentage");
        schemaBuilder.addColumn("lowSlowEmaPercentage");
        schemaBuilder.addColumn("emaAppreciation");
        schemaBuilder.addColumn("netPriceAppreciationBeforeLowFallsToSlowEMA");
        // header created
        try (PrintWriter printWriter = new PrintWriter(file)) {
            CsvSchema schema =
                    schemaBuilder
                            .build()
                            .withLineSeparator(System.lineSeparator())
                            .withArrayElementSeparator("/")
                            .withHeader();
            mapper.writer(schema).writeValues(printWriter).write(priceParametersForIntermediateCorrections);
        } catch (IOException e) {
            System.out.println("Exception!!"+ e.getMessage());
        } finally {
            schemaBuilder.clearColumns();
        }
    }

    //TODO not using this method.
    /*public static void createCsvFileForPriceConsolidation(List<PriceParametersForConsolidation> priceParametersForConsolidationList, String fileName) {
        File file = new File(CONSOLIDATION_OUTPUT_LOCATION +fileName);
        CsvSchema.Builder schemaBuilder = CsvSchema.builder();
        schemaBuilder.setUseHeader(true);
        JavaTimeModule javaTimeModule=new JavaTimeModule();
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ISO_DATE));
        mapper.registerModule(javaTimeModule);
        // create header
        schemaBuilder.addColumn("date");
        schemaBuilder.addColumn("open");
        schemaBuilder.addColumn("close");
        schemaBuilder.addColumn("volume");
        schemaBuilder.addColumn("dayRange");
        schemaBuilder.addColumn("openCloseRange");
        schemaBuilder.addColumn("incrementalPercentageCloseAppreciation");
        schemaBuilder.addColumn("emaDiffPercentage");
        // header created
        try (PrintWriter printWriter = new PrintWriter(file)) {
            CsvSchema schema =
                    schemaBuilder
                            .build()
                            .withLineSeparator(System.lineSeparator())
                            .withArrayElementSeparator("/")
                            .withHeader();
            mapper.writer(schema).writeValues(printWriter).write(priceParametersForConsolidationList);
        } catch (IOException e) {
            System.out.println("Exception!!"+ e.getMessage());
        } finally {
            schemaBuilder.clearColumns();
        }
    }*/
}
