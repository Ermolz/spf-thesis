package com.example.freelance.common.util;

import org.slf4j.MDC;

public class MdcUtil {

    private static final String TRACE_ID_KEY = "traceId";
    private static final String USER_ID_KEY = "userId";
    private static final String OPERATION_KEY = "operation";
    private static final String REQUEST_METHOD_KEY = "requestMethod";
    private static final String REQUEST_PATH_KEY = "requestPath";

    public static void setTraceId(String traceId) {
        if (traceId != null && !traceId.isEmpty()) {
            MDC.put(TRACE_ID_KEY, traceId);
        }
    }

    public static String getTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }

    public static void setUserId(Long userId) {
        if (userId != null) {
            MDC.put(USER_ID_KEY, userId.toString());
        }
    }

    public static void setOperation(String operation) {
        if (operation != null && !operation.isEmpty()) {
            MDC.put(OPERATION_KEY, operation);
        }
    }

    public static void setRequestMethod(String method) {
        if (method != null && !method.isEmpty()) {
            MDC.put(REQUEST_METHOD_KEY, method);
        }
    }

    public static void setRequestPath(String path) {
        if (path != null && !path.isEmpty()) {
            MDC.put(REQUEST_PATH_KEY, path);
        }
    }

    public static void clearTraceId() {
        MDC.remove(TRACE_ID_KEY);
    }

    public static void clearUserId() {
        MDC.remove(USER_ID_KEY);
    }

    public static void clearOperation() {
        MDC.remove(OPERATION_KEY);
    }

    public static void clearRequestValues() {
        MDC.remove(REQUEST_METHOD_KEY);
        MDC.remove(REQUEST_PATH_KEY);
    }

    public static void clearAll() {
        clearTraceId();
        clearUserId();
        clearOperation();
        clearRequestValues();
    }

    public static void clearCustomValues() {
        clearUserId();
        clearOperation();
    }
}

