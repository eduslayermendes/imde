package com.example.ImageHandling.config;

import com.example.ImageHandling.domains.RegexPattern;
import com.example.ImageHandling.services.RegexPatternService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@Profile("!test")
public class InitialDataLoader implements InitializingBean {

    private final RegexPatternService regexPatternService;

    private static final Logger logger = LoggerFactory.getLogger(InitialDataLoader.class);


    public InitialDataLoader(RegexPatternService regexPatternService) {
        this.regexPatternService = regexPatternService;
    }

    @Override
    public void afterPropertiesSet() {
            try {
                logger.info("InitialDataLoader initialized");
                // Verifies if there's a PT pattern in the database
                String patternName = "PT";
                if (!regexPatternService.existsPatternWithName(patternName)) {
                    RegexPattern ptPattern = createPTPattern();
					regexPatternService.createRegexPattern(ptPattern);
					logger.info("PT pattern created");
                }
            } catch (Exception e) {
                String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
                logger.error("Failed to initialize data loader, Error message: {}", rootCauseMessage, e);
            }

    }

    private RegexPattern createPTPattern() {
        RegexPattern pattern = new RegexPattern();
        pattern.setName("PT");
        pattern.setCreatedAt(LocalDateTime.now());
        return pattern;
    }


}
