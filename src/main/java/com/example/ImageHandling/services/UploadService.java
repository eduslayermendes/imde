package com.example.ImageHandling.services;

import com.example.ImageHandling.domains.dto.UploadInvoicesDTO;
import com.example.ImageHandling.exception.CustomTimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadService {

	public static final int TIMEOUT = 70;

	private final InvoiceUploadService invoiceUploadService;


//	@Transactional(timeout=TIMEOUT,rollbackFor=Exception.class)
//	public Optional<UploadInvoicesDTO> uploadInvoice(UploadInvoicesDTO uploadInvoicesDTO) {
//
//	}

	@Transactional(timeout = TIMEOUT, rollbackFor = Exception.class)
	public Optional<UploadInvoicesDTO> uploadAndExtract(
			MultipartFile file,
			String patternName,
			String uploadComment,
			String costCenter,
			List<String> errors) throws Exception {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		SecurityContext context = SecurityContextHolder.getContext();

		Future<List<UploadInvoicesDTO>> future = executor.submit(() -> {
			try {
				SecurityContextHolder.setContext(context);
				return invoiceUploadService.uploadAndExtractInvoice(file, patternName, uploadComment, costCenter, errors);
			} finally {
				SecurityContextHolder.clearContext();
			}
		});

		try {
			List<UploadInvoicesDTO> resultList = future.get(TIMEOUT - 10, TimeUnit.SECONDS);
			if (resultList != null && !resultList.isEmpty()) {
				return Optional.of(resultList.get(0));
			} else {
				return Optional.empty();
			}
		} catch (TimeoutException e) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			log.error("Upload and extract process exceeded the timeout of {} seconds.", TIMEOUT - 10);
			throw new CustomTimeoutException(String.format("Upload and extract process exceeded the timeout of %s seconds.", TIMEOUT - 10), e);
		} catch (ExecutionException e) {
			log.error("Error occurred during upload and extract process.", e);
			throw e.getCause() instanceof Exception ? (Exception) e.getCause() : new RuntimeException(e.getCause());
		} finally {
			executor.shutdownNow();
		}
	}

}
