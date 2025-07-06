package com.example.ImageHandling.config;

import com.example.ImageHandling.infrastructure.soap.checkvat.CheckVatPortType;
import com.example.ImageHandling.infrastructure.soap.checkvat.CheckVatService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.function.client.WebClient;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Holder;

@Configuration
@EnableScheduling
public class AppConfig {

	@Bean
	public WebClient.Builder webClientBuilder() {
		return WebClient.builder();
	}

	@Bean
	public CheckVatService checkVatService() {
		return new CheckVatService();
	}
	@Bean
	public CheckVatPortType checkVatPortType() {
		return new CheckVatPortType() {
			@Override
			public void checkVat( Holder<String> countryCode, Holder<String> vatNumber, Holder<XMLGregorianCalendar> requestDate, Holder<Boolean> valid, Holder<String> name, Holder<String> address) {

			}

			@Override
			public void checkVatApprox(Holder<String> countryCode, Holder<String> vatNumber, Holder<String> traderName, Holder<String> traderCompanyType, Holder<String> traderStreet, Holder<String> traderPostcode, Holder<String> traderCity, String requesterCountryCode, String requesterVatNumber, Holder<XMLGregorianCalendar> requestDate, Holder<Boolean> valid, Holder<String> traderAddress, Holder<String> traderNameMatch, Holder<String> traderCompanyTypeMatch, Holder<String> traderStreetMatch, Holder<String> traderPostcodeMatch, Holder<String> traderCityMatch, Holder<String> requestIdentifier) {

			}
		};
	}
}
