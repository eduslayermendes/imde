package com.example.ImageHandling.services;

/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 8/27/2024
 */

import com.example.ImageHandling.exception.DecoderApiInternalServerErrorException;
import com.example.ImageHandling.exception.DecoderApiNotFoundContentException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;

@Slf4j
@Service
public class BarcodeQRCodeDecoderApiCaller {

	private final WebClient webClient;

	private static final Logger logger = LoggerFactory.getLogger(BarcodeQRCodeDecoderApiCaller.class);

	public BarcodeQRCodeDecoderApiCaller( WebClient.Builder webClientBuilder, @Value( "${decoderApi.address}" ) String baseUrl ) {
		this.webClient = webClientBuilder.baseUrl( baseUrl ).build();
	}

	public Mono<String> callDecodeApi( MultipartFile imageFile ) throws IOException {

		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

		body.add( "image", new ByteArrayResource( imageFile.getBytes() ) {

			@Override
			public String getFilename() {
				return imageFile.getOriginalFilename();
			}
		} );

		logger.info( "*** Calling decode API for image file: {}", imageFile.getOriginalFilename() );

		return webClient.post()
			.uri( "/decode" )
			.contentType( MediaType.MULTIPART_FORM_DATA )
			.body( BodyInserters.fromMultipartData( body ) )
			.retrieve()
			.onStatus( HttpStatus::is4xxClientError, clientResponse -> {
				logger.error( "Decoder API response error: {}", clientResponse.statusCode() );
				return Mono.error( new DecoderApiNotFoundContentException( "Decoder API response error: " + clientResponse.statusCode() ) );
			} )
			.onStatus( HttpStatus::is5xxServerError, clientResponse -> {
				logger.error( "Decoder API response internal server error: {}", clientResponse.statusCode() );
				return Mono.error( new DecoderApiInternalServerErrorException( "Decoder API internal server error occurred: " + clientResponse.statusCode() ) );
			} )
			.bodyToMono( new ParameterizedTypeReference<List<Map<String, Object>>>() {

			} )
			.flatMap( decodedObjects -> Mono.justOrEmpty( decodedObjects.stream()
				.filter( obj -> "QRCODE".equals( obj.get( "type" ) ) )
				.map( obj -> (String) obj.get( "data" ) )
				.findFirst() ) )
			.timeout( Duration.ofSeconds( 15 ) );
	}
}








/*@Slf4j
@Service
public class BarcodeQRCodeDecoderApiCaller {

	private final WebClient webClient;

	public BarcodeQRCodeDecoderApiCaller( WebClient.Builder webClientBuilder, @Value( "${decoderapi.address}" ) String baseUrl ) {
		this.webClient = webClientBuilder.baseUrl( baseUrl ).build();
	}

	public Mono<String> callDecodeApi( MultipartFile imageFile ) throws IOException {

		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

		body.add( "image", new ByteArrayResource( imageFile.getBytes() ) {
			@Override
			public String getFilename() {
				return imageFile.getOriginalFilename();
			}
		} );
		logger.info( "*** Calling decode API for image file: {}", imageFile.getOriginalFilename() );

		return webClient.post()
			.uri( "/decode" )
			.contentType( MediaType.MULTIPART_FORM_DATA )
			.body( BodyInserters.fromMultipartData( body ) )
			.retrieve()
			.bodyToMono( List.class )
			.flatMap( decodedObjects -> {
				Optional<String> qrCodeData = ( (List<Map<String, Object>>) decodedObjects ).stream()
					.filter( obj -> "QRCODE".equals( obj.get( "type" ) ) )
					.map( obj -> (String) obj.get( "data" ) )
					.findFirst();

				return qrCodeData.map( Mono::just ).orElseGet( Mono::empty );
			} );
	}

}*/
