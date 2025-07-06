package com.example.ImageHandling.domains.dto;

import com.example.ImageHandling.domains.CostCenter;
import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 1/31/2025
 */
@Data
public class UploadObject {
	MultipartFile[] files;
	List<String> patternNames;
	String uploadComment;
	CostCenter costCenter;

}
