package com.example.ImageHandling.controller;

import com.example.ImageHandling.domains.dto.ErrorResponse;
import com.example.ImageHandling.services.ExtractionService;
import com.example.ImageHandling.services.RegexPatternService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.TesseractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.example.ImageHandling.utils.ResponseUtil.createErrorResponse;

/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 9/20/2024
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ExtractorController {

	private static final Logger logger = LoggerFactory.getLogger(ExtractorController.class);
	private final RegexPatternService regexPatternService;
	private final ExtractionService extractionService;

	/**
	 * Endpoint to extract text from an image file.
	 * This method processes the uploaded image file, extracts text using the specified language, and returns the extracted text.
	 * @param imageFile The image file to process
	 * @param language The language to use for text extraction
	 * @return Response entity containing the extracted text or an error message
	 */
	@PostMapping("/extract-text-from-image")
	public ResponseEntity<?> extractTextFromImage(@RequestParam("imageFiles") MultipartFile imageFile, @RequestParam("language") String language) throws TesseractException, IOException {
			String extractedText = regexPatternService.extractTextFromImage( imageFile, language );
			Map<String, Object> response = new HashMap<>();
			response.put("extractedText", extractedText);
			return ResponseEntity.ok(response);
	}

	/**
	 * Endpoint to extract text from a PDF file.
	 * This method processes the uploaded PDF file and extracts text from it.
	 * @param pdfFile The PDF file to process
	 * @return Response entity containing the extracted text or an error message
	 */
	@PostMapping("/extract-text")
	public ResponseEntity<?> extractTextFromPdf(@RequestParam("pdfFiles") MultipartFile pdfFile) throws IOException {
		Map<String, Object> response = new HashMap<>();
			String extractedText = extractionService.extractTextFromPDF(pdfFile);
			response.put("extractedText", extractedText);
			return ResponseEntity.status(HttpStatus.OK).body(response);

	}

	@PostMapping("/format-invoice-date")
	public ResponseEntity<?> formatInvoiceDate(@RequestBody Map<String, String> requestBody) {
		String date = requestBody.get("date");
		String formatDate = requestBody.get("formatDate");
			String formattedDate = String.valueOf( regexPatternService.formatDate(date, formatDate) );
			Map<String, String> response = new HashMap<>();
			response.put("formattedDate", formattedDate);
			return ResponseEntity.ok(response);

	}

}
