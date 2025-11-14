package com.example.freelance.common.util;

import com.example.freelance.common.dto.ApiResponse;
import com.example.freelance.common.dto.Metadata;
import com.example.freelance.common.dto.PaginationInfo;
import org.springframework.data.domain.Page;

public class ResponseUtil {

    private ResponseUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.success(data);
    }

    public static <T> ApiResponse<T> success(T data, Metadata metadata) {
        return ApiResponse.success(data, metadata);
    }

    public static <T> ApiResponse<Page<T>> success(Page<T> page) {
        PaginationInfo pagination = PaginationInfo.fromPage(page);
        Metadata metadata = Metadata.withPagination(pagination);
        return ApiResponse.success(page, metadata);
    }

    public static <T> ApiResponse<T> successWithTimestamp(T data) {
        Metadata metadata = Metadata.withTimestamp();
        return ApiResponse.success(data, metadata);
    }
}

