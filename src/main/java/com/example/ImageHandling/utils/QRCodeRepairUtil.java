package com.example.ImageHandling.utils;



import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import lombok.extern.slf4j.Slf4j;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Component;
@Slf4j
@Component
public class QRCodeRepairUtil {




    public static BufferedImage repairQRCode(BufferedImage image) {
        // Convert BufferedImage to Mat
        Mat mat = bufferedImageToMat(image);

        // Return empty image if conversion failed
        if (mat.empty()) {
            log.error("Input image conversion to Mat failed, returning empty BufferedImage.");
            return new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_GRAY);
        }

        // Convert to grayscale
        Mat gray = new Mat();
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY);

        // Apply contrast enhancement (less aggressive)
        Mat contrast = new Mat();
        {

            gray.convertTo(contrast, -1, 1.2, -5); // Less aggressive contrast
        }

        // Apply bilateral filter to reduce noise while preserving edges
        Mat filtered = new Mat();
        Imgproc.bilateralFilter(contrast, filtered, 9, 75, 75);

        // Use multiple thresholding approaches and combine
        Mat otsuBinary = new Mat();
        Imgproc.threshold(filtered, otsuBinary, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);

        Mat adaptiveBinary = new Mat();
        Imgproc.adaptiveThreshold(filtered, adaptiveBinary, 255,
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 15, 3);

        // Combine the two binary images - take intersection of black regions
        Mat combined = new Mat();
        Core.bitwise_and(otsuBinary, adaptiveBinary, combined);

        // --- FILL FIRST ---
        Mat filled = FillGrayAreas(filtered, combined);

        // --- THEN CLEAN ---
        // Fill gaps using morphological operations
        Mat kernel1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2));
        Mat closed = new Mat();
        Imgproc.morphologyEx(filled, closed, Imgproc.MORPH_CLOSE, kernel1);

        // Apply selective dilation to connect broken parts
        Mat kernel2 = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(3, 3));
        Mat dilated = new Mat();
        Imgproc.dilate(closed, dilated, kernel2, new Point(-1, -1), 1);

        // Apply selective erosion to restore the original size
        Mat eroded = new Mat();
        Imgproc.erode(dilated, eroded, kernel2, new Point(-1, -1), 1);

        // Final noise cleanup
        Mat kernel3 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2));
        Mat cleaned = new Mat();
        Imgproc.morphologyEx(eroded, cleaned, Imgproc.MORPH_OPEN, kernel3);

        return matToBufferedImage(cleaned);
    }


    // Gray area fill
    private static Mat FillGrayAreas(Mat grayImage, Mat binaryImage) {
        Mat result = binaryImage.clone();
        int rows = grayImage.rows();
        int cols = grayImage.cols();

        byte[] grayData = new byte[rows * cols];
        byte[] binaryData = new byte[rows * cols];
        byte[] resultData = new byte[rows * cols];

        grayImage.get(0, 0, grayData);
        binaryImage.get(0, 0, binaryData);
        result.get(0, 0, resultData);

        // Only fill pixels that are moderately gray and have several black neighbors
        for (int y = 1; y < rows - 1; y++) {
            for (int x = 1; x < cols - 1; x++) {
                int idx = y * cols + x;
                int currentBinary = binaryData[idx] & 0xFF;
                int currentGray = grayData[idx] & 0xFF;

                // Less aggressive gray detection
                if (currentBinary > 20 && currentGray < 180 && currentGray > 80) {
                    // Check in a small neighborhood for black pixels
                    int blackNeighbors = 0;
                    int totalNeighbors = 0;
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dx = -1; dx <= 1; dx++) {
                            if (dy == 0 && dx == 0) continue;
                            int ny = y + dy;
                            int nx = x + dx;
                            if (ny >= 0 && ny < rows && nx >= 0 && nx < cols) {
                                int nIdx = ny * cols + nx;
                                totalNeighbors++;
                                if ((binaryData[nIdx] & 0xFF) < 127) {
                                    blackNeighbors++;
                                }
                            }
                        }
                    }
                    // Only fill if at least 3 of 8 neighbors are black
                    if (blackNeighbors >= 3) {
                        resultData[idx] = 0;
                    }
                }
            }
        }

        result.put(0, 0, resultData);
        return result;
    }

    private static Mat bufferedImageToMat(BufferedImage bi) {
        try
        {
            int type = bi.getType();
            if (type == BufferedImage.TYPE_INT_RGB ||
                    type == BufferedImage.TYPE_INT_ARGB ||
                    type == BufferedImage.TYPE_3BYTE_BGR) {
                byte[] pixels = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
                Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
                mat.put(0, 0, pixels);
                return mat;
            } else {
                BufferedImage convertedImg = new BufferedImage(bi.getWidth(), bi.getHeight(),
                        BufferedImage.TYPE_3BYTE_BGR);
                convertedImg.getGraphics().drawImage(bi, 0, 0, null);
                return bufferedImageToMat(convertedImg);
            }
        }
        catch (Exception e)
        {
            // Log the error and return an empty Mat
            log.error("Error converting BufferedImage to Mat: {}", e.getMessage());
            return new Mat(); // Return empty Mat on exception
        }

    }

    private static BufferedImage matToBufferedImage(Mat mat) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        BufferedImage img = new BufferedImage(mat.cols(), mat.rows(), type);
        mat.get(0, 0, ((DataBufferByte) img.getRaster().getDataBuffer()).getData());
        return img;
    }
}
