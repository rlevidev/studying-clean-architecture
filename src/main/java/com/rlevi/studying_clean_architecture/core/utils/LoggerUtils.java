package com.rlevi.studying_clean_architecture.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Map;
import java.util.UUID;

public class LoggerUtils {
    
    private static final String REQUEST_ID_KEY = "requestId";
    private static final String USER_EMAIL_KEY = "userEmail";
    private static final String ENDPOINT_KEY = "endpoint";
    private static final String EXECUTION_TIME_KEY = "executionTime";

    /**
     * Starts logging a request, configuring MDC with contextual information.
     *
     * @param logger SLF4J logger
     * @param endpoint Request endpoint (e.g., "POST /api/v1/users/register")
     * @param userEmail User email (can be null for public endpoints)
     */
    public static void startRequest(Logger logger, String endpoint, String userEmail) {
        String requestId = UUID.randomUUID().toString();
        
        MDC.put(REQUEST_ID_KEY, requestId);
        MDC.put(ENDPOINT_KEY, endpoint);
        if (userEmail != null) {
            MDC.put(USER_EMAIL_KEY, userEmail);
        }
        
        logger.info("Request started - {}", endpoint);
    }
    
    /**
     * Records the start of an operation.
     *
     * @param logger SLF4J logger
     * @param operationName Operation name
     * @param context Operation context
     */
    public static void startOperation(Logger logger, String operationName, Map<String, Object> context) {
        logger.info("Starting operation - {} - {}", operationName, context);
    }
    
    /**
     * Records the success of an operation.
     *
     * @param logger SLF4J logger
     * @param message Descriptive message of the successful operation
     * @param additionalInfo Additional information to be included in the log
     */
    public static void logSuccess(Logger logger, String message, Map<String, Object> additionalInfo) {
        if (additionalInfo != null && !additionalInfo.isEmpty()) {
            logger.info("{} - {}", message, additionalInfo);
        } else {
            logger.info(message);
        }
    }
    
    /**
     * Records the error of an operation.
     *
     * @param logger SLF4J logger
     * @param message Descriptive error message
     * @param throwable Exception that caused the error
     * @param additionalInfo Additional information to be included in the log
     */
    public static void logError(Logger logger, String message, Throwable throwable, Map<String, Object> additionalInfo) {
        if (additionalInfo != null && !additionalInfo.isEmpty()) {
            logger.error("{} - {}", message, additionalInfo, throwable);
        } else {
            logger.error(message, throwable);
        }
    }
    
    /**
     * Records a warning.
     *
     * @param logger SLF4J logger
     * @param message Warning message
     * @param additionalInfo Additional information to be included in the log
     */
    public static void logWarning(Logger logger, String message, Map<String, Object> additionalInfo) {
        if (additionalInfo != null && !additionalInfo.isEmpty()) {
            logger.warn("{} - {}", message, additionalInfo);
        } else {
            logger.warn(message);
        }
    }
    
    /**
     * Records detailed information (debug).
     *
     * @param logger SLF4J logger
     * @param message Debug message
     * @param additionalInfo Additional information to be included in the log
     */
    public static void logDebug(Logger logger, String message, Map<String, Object> additionalInfo) {
        if (logger.isDebugEnabled()) {
            if (additionalInfo != null && !additionalInfo.isEmpty()) {
                logger.debug("{} - {}", message, additionalInfo);
            } else {
                logger.debug(message);
            }
        }
    }
    
    /**
     * Ends request logging, clearing the MDC.
     *
     * @param logger SLF4J logger
     */
    public static void endRequest(Logger logger) {
        String requestId = MDC.get(REQUEST_ID_KEY);
        String endpoint = MDC.get(ENDPOINT_KEY);
        
        logger.info("Request completed - {}", endpoint);
        
        // Limpa o MDC
        MDC.clear();
    }
    
    /**
     * Records the execution time of an operation.
     *
     * @param logger SLF4J logger
     * @param operationName Operation name
     * @param executionTimeMillis Execution time in milliseconds
     * @param additionalInfo Additional information
     */
    public static void logExecutionTime(Logger logger, String operationName, long executionTimeMillis, Map<String, Object> additionalInfo) {
        MDC.put(EXECUTION_TIME_KEY, String.valueOf(executionTimeMillis));
        
        if (additionalInfo != null && !additionalInfo.isEmpty()) {
            logger.info("Operation completed - {} ({}ms) - {}", operationName, executionTimeMillis, additionalInfo);
        } else {
            logger.info("Operation completed - {} ({}ms)", operationName, executionTimeMillis);
        }
        
        MDC.remove(EXECUTION_TIME_KEY);
    }
    
    /**
     * Creates a logger for a specific class.
     *
     * @param clazz Class for which the logger will be created
     * @return Logger configured for the class
     */
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }
    
    /**
     * Records input data validation.
     *
     * @param logger SLF4J logger
     * @param validationType Validation type (e.g., "email", "password", "name")
     * @param isValid Validation result
     * @param value Value that was validated (do not include sensitive values like passwords)
     */
    public static void logValidation(Logger logger, String validationType, boolean isValid, String value) {
        if (isValid) {
            logger.debug("Validation passed - {} for value: {}", validationType, value);
        } else {
            logger.warn("Validation failed - {} for value: {}", validationType, value);
        }
    }
    
    /**
     * Records access to protected resources.
     *
     * @param logger SLF4J logger
     * @param resource Accessed resource
     * @param hasAccess Indicates whether access was allowed
     * @param userRole Role of the user who attempted access
     */
    public static void logAccess(Logger logger, String resource, boolean hasAccess, String userRole) {
        if (hasAccess) {
            logger.info("Access granted - Resource: {}, Role: {}", resource, userRole);
        } else {
            logger.warn("Access denied - Resource: {}, Role: {}", resource, userRole);
        }
    }
}
