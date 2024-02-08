package com.local.ema.trade.details.calculator;

import com.local.ema.trade.details.model.PriceParameter;
import com.local.ema.trade.details.model.StockPriceDetails;

import java.math.MathContext;
import java.util.List;

public interface PriceMovementCalculatorInterface {

    <T extends PriceParameter> List<T> priceMovementCalculatorAndGetPriceDetails(List<StockPriceDetails> stockPriceDetails, MathContext mc);
}
