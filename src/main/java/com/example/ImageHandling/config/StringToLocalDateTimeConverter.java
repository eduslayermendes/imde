package com.example.ImageHandling.config;

/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 3/19/2025
 */

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A converter to map MongoDB String format (yyyyMMdd) to LocalDateTime.
 */
@ReadingConverter
public class StringToLocalDateTimeConverter implements Converter<String, LocalDateTime> {

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

	@Override
	public LocalDateTime convert(String source) {
		return LocalDateTime.parse(source + "T00:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME);
	}
}

