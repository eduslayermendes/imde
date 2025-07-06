package com.example.ImageHandling.config;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 1/16/2025
 */
public class LibraryLoader {
	public static void loadLibrary(String libraryName) {
		try {
			String resourcePath = "/opencv_java490.dll";
			InputStream in = LibraryLoader.class.getResourceAsStream(resourcePath);
			if (in == null) {
				throw new RuntimeException("Library " + libraryName + " not found in resources.");
			}
			File temp = File.createTempFile(libraryName, "");
			Files.copy(in, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
			System.load(temp.getAbsolutePath());
			temp.deleteOnExit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
