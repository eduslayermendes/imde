package com.example.ImageHandling.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class PDFUtils {

    public static String pdfToBase64(Path pdfPath) throws IOException {
        byte[] pdfBytes = Files.readAllBytes(pdfPath);
        return Base64.getEncoder().encodeToString(pdfBytes);
    }

    public static void base64ToPdf(String base64, Path outputPath) throws IOException {
        byte[] pdfBytes = Base64.getDecoder().decode(base64);
        Files.write(outputPath, pdfBytes);
    }

    public static byte[] base64ToByteArray(String base64) {
        return Base64.getDecoder().decode(base64);
    }

    public static String byteArrayToBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }
}
