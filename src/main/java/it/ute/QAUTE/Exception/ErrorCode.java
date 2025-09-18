package it.ute.QAUTE.Exception;


import lombok.Getter;

@Getter
public enum ErrorCode {
    // General and System Errors
    UNCATEGORIZED_EXCEPTION(500, "Internal Server Error"),
    INVALID_KEY(400, "Invalid Key"),

    // User Management Errors
    USER_EXISTED(409, "Conflict: User already exists"),
    USERNAME_INVALID(400, "Bad Request: Username must be at least {min} characters"),
    PASSWORD_INVALID(400, "Bad Request: Password must be at least {min} characters"),
    USER_NOT_EXISTED(404, "Not Found: User not found"),
    INVALID_DOB(400, "Bad Request: Your age must be at least {min}"),

    // Authentication & Authorization Errors
    UNAUTHENTICATED(401, "Unauthorized: Authentication required"),
    UNAUTHORIZED(403, "Forbidden: You do not have permission"),
    INVALID_TOKEN(401, "Unauthorized: Invalid Token"),
    TOKEN_EXPIRED(401, "Unauthorized: Token Expired"),
    TOKEN_REVOKED(401, "Unauthorized: Token Revoked")
    ;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private final int code;
    private final String message;
}
