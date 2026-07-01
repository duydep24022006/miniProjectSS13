package com.example.miniproject.exception;

/**
 * Ném ra khi không tìm thấy tài nguyên trong database (trả về 404)
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " không tìm thấy với ID: " + id);
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
