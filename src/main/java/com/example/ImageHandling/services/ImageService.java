package com.example.ImageHandling.services;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;


/**
 * Service class for handling Images.
 */
@Service
public class ImageService {

    /**
     * Extracts text from an input OpenCV Mat image using Tesseract OCR.
     *
     * @param image    The input OpenCV Mat image.
     * @param language The language code for Tesseract OCR.
     * @return Extracted text from the image.
     * @throws TesseractException If Tesseract encounters an error during OCR.
     * @throws IOException        If there's an error reading the image.
     */
    public String extractText(Mat image, String language) throws TesseractException, IOException {

        // Initialize Tesseract OCR engine
        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath("src/main/resources/tessdata"); // Set path to Tesseract trained data
        tesseract.setOcrEngineMode(3); // Set OCR engine mode (3 for LSTM)
        tesseract.setPageSegMode(6); // Set page segmentation mode (6 for OCR engine)
        tesseract.setLanguage(language); // Set language for OCR

        // Convert OpenCV Mat image to byte array
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg", image, matOfByte); // Encode Mat image to JPEG format
        byte[] byteArray = matOfByte.toArray(); // Convert MatOfByte to byte array

        // Convert byte array back to BufferedImage
        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(byteArray));

        // Perform OCR on the BufferedImage
        String text = tesseract.doOCR(bufferedImage).replaceAll("\\n", " "); // Extract text and replace newlines with spaces

        // Normalize whitespace
        text = text.replaceAll("\\s+", " "); // Replace multiple spaces and other whitespace characters with a single space

        return text.trim(); // Trim leading and trailing whitespace and return extracted text
    }
}
