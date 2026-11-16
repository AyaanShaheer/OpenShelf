package com.openshelf.common.exception;

/**
 * Raised when a required downstream service (for example Apache Solr) cannot be reached.
 * Mapped to HTTP 503 so callers can distinguish an outage from an application bug.
 */
public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
