package com.example.ImageHandling.domains.dto;

import com.example.ImageHandling.domains.CostCenter;
import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 1/31/2025
 */
public record UploadObjectRecord( @NonNull MultipartFile[] files,
								  @NonNull List<String> patternNames,
								  @NonNull String uploadComment,
								  @NonNull CostCenter costCenter) {

}
