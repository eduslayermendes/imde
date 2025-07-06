package com.example.ImageHandling.config;

/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 3/19/2025
 */
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@ReadingConverter
public class StringToLocalDateConverter implements Converter<String, LocalDate> {

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

	@Override
	public LocalDate convert(String source) {
		return LocalDate.parse(source, FORMATTER);
	}
}

