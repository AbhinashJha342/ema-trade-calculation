package com.local.ema.trade.details.converter;

import com.local.ema.trade.details.util.StaticUtil;
import com.opencsv.bean.AbstractBeanField;
import com.opencsv.bean.CsvCustomBindByName;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class GenericPropertyConverter<T> extends AbstractBeanField<String, T> {

  private static final Logger LOG = LoggerFactory.getLogger(GenericPropertyConverter.class);

  @Override
  protected Object convert(String value) throws CsvDataTypeMismatchException {
    if (value == null) {
      return null;
    }

    String cleanedValue = value.replace("'", "").replace("\"", "").replace("%", "");

    Class<?> fieldType = this.getField().getType();
    try {
      if(StringUtils.isNotBlank(cleanedValue)){
        if (Boolean.class.equals(fieldType)) {
          return BooleanUtils.toBooleanObject(cleanedValue);
        } else if (String.class.equals(fieldType)) {
          return cleanedValue;
        } else if (BigDecimal.class.equals(fieldType)) {
          return new BigDecimal(cleanedValue);
        } else if (LocalDate.class.equals(fieldType)) {
          return StaticUtil.parseToLocalDate(cleanedValue);
        }
      }
    } catch (DateTimeParseException e) {
      String headerName = this.getField().getDeclaredAnnotation(CsvCustomBindByName.class).column();
      String message = String.format("Field '%s' - %s.", headerName, e.getMessage());
      LOG.info(message);
      throw new CsvDataTypeMismatchException(cleanedValue, fieldType, message);
    } catch (NumberFormatException e) {
      String headerName = this.getField().getDeclaredAnnotation(CsvCustomBindByName.class).column();
      String message =
          String.format(
              "Field '%s' - Unable to convert '%s' to '%s'.",
              headerName, cleanedValue, fieldType.getSimpleName());
      LOG.info(message);
      throw new CsvDataTypeMismatchException(cleanedValue, fieldType, message);
    }

    return null;
  }
}
