package com.example.ImageHandling.utils;

import com.example.ImageHandling.domains.Invoices;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Slf4j
public class DateUtils {

    private static final Logger logger = LoggerFactory.getLogger(DateUtils.class);

    private static final List<Locale> LOCALES = Arrays.asList(
        new Locale ("en", "UK"),
        new Locale("es", "ES"),
        new Locale("pt", "PT"),
        Locale.ITALIAN
    );

    public static LocalDate formatDate2(String dateStr, String formatDate) {


        for (Locale locale : LOCALES) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatDate, locale);
                LocalDate date = LocalDate.parse(dateStr, formatter);
                return LocalDate.parse(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            } catch (DateTimeParseException e) {
                log.error( e.getMessage() );
                // Continua para o próximo locale se a data não puder ser analisada
            }
        }
        throw new IllegalArgumentException("Unsupported date format: " + dateStr);
    }

    public static String formatDate(String dateStr, String formatDate) {
        for (Locale locale : LOCALES) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatDate, locale);
                LocalDate date = LocalDate.parse(dateStr, formatter);
                logger.info("Date '{}' with the date format '{}' formatted into '{}'", dateStr, formatDate, date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (DateTimeParseException e) {
                String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
                logger.error("Date not formatted. {}", rootCauseMessage, e);

            }
        }
        throw new IllegalArgumentException("Cannot convert the date " + dateStr + " using the date format " + formatDate);
    }

    // To modify the locale date based on the locale
    public static LocalDate formatDate( String dateStr ) {
        LocalDate date = null;
        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern( "yyyyMMdd" );
            date = LocalDate.parse( dateStr, inputFormatter );
            logger.info("Formatted date {} to {}", dateStr, date);
            return date ;
        }
        catch ( DateTimeParseException e ) {
            String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
            logger.error( "Error on converting the dates {}, Error message: {}", dateStr, rootCauseMessage, e );
            return date;  // Return the original date string if parsing fails
        }
    }


}
