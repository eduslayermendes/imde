package com.example.ImageHandling.utils;

/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 10/3/2024
 */
import javax.annotation.PostConstruct;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LocaleChecker {

	private static final Logger logger = LoggerFactory.getLogger(LocaleChecker.class);

	@PostConstruct
	public void checkDefaultLocale() {
		Locale defaultLocale = Locale.getDefault();
		logger.info("*** Default system Locale is set on the:: {}", defaultLocale);
	}
}

