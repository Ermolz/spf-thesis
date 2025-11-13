package com.example.freelance.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class FileValidationUtil {

    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".pdf", ".doc", ".docx",  // Documents
            ".jpg", ".jpeg", ".png", ".gif",  // Images
            ".zip", ".rar"  // Archives
    ));

    private static final Set<String> ALLOWED_MIME_TYPES = new HashSet<>(Arrays.asList(
            // Documents
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            // Images
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            // Archives
            "application/zip",
            "application/x-rar-compressed",
            "application/vnd.rar"
    ));


    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("File name cannot be empty");
        }

        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            log.warn("File upload rejected: Invalid extension '{}' for file '{}'", extension, originalFilename);
            throw new IllegalArgumentException(
                    String.format("File type '%s' is not allowed. Allowed types: PDF, DOC, DOCX, JPG, PNG, GIF, ZIP, RAR", extension)
            );
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            log.warn("File upload rejected: Invalid MIME type '{}' for file '{}'", contentType, originalFilename);
            throw new IllegalArgumentException(
                    String.format("File MIME type '%s' is not allowed. Please upload a valid document, image, or archive file.", contentType)
            );
        }

        if (!isExtensionMatchingMimeType(extension, contentType)) {
            log.warn("File upload rejected: Extension '{}' does not match MIME type '{}' for file '{}'", 
                    extension, contentType, originalFilename);
            throw new IllegalArgumentException("File extension does not match the file content type. Possible file type mismatch.");
        }

        log.debug("File validation passed for '{}' with type '{}'", originalFilename, contentType);
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex).toLowerCase();
        }
        return "";
    }

    private boolean isExtensionMatchingMimeType(String extension, String mimeType) {
        if (extension == null || mimeType == null) {
            return false;
        }

        extension = extension.toLowerCase();
        mimeType = mimeType.toLowerCase();

        // PDF
        if (extension.equals(".pdf") && mimeType.equals("application/pdf")) {
            return true;
        }
        // DOC
        if (extension.equals(".doc") && mimeType.equals("application/msword")) {
            return true;
        }
        // DOCX
        if (extension.equals(".docx") && mimeType.contains("wordprocessingml")) {
            return true;
        }
        // JPEG/JPG
        if ((extension.equals(".jpg") || extension.equals(".jpeg")) && 
            (mimeType.equals("image/jpeg") || mimeType.equals("image/jpg"))) {
            return true;
        }
        // PNG
        if (extension.equals(".png") && mimeType.equals("image/png")) {
            return true;
        }
        // GIF
        if (extension.equals(".gif") && mimeType.equals("image/gif")) {
            return true;
        }
        // ZIP
        if (extension.equals(".zip") && mimeType.equals("application/zip")) {
            return true;
        }
        // RAR
        if (extension.equals(".rar") && (mimeType.equals("application/x-rar-compressed") || mimeType.equals("application/vnd.rar"))) {
            return true;
        }

        return false;
    }

    public Set<String> getAllowedExtensions() {
        return new HashSet<>(ALLOWED_EXTENSIONS);
    }

    public Set<String> getAllowedMimeTypes() {
        return new HashSet<>(ALLOWED_MIME_TYPES);
    }
}

