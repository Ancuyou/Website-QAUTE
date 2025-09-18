package it.ute.QAUTE.Exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = AppException.class)
    public String handlingAppException(AppException ex,
                                       HttpServletRequest req,
                                       Model model) {
        ErrorCode code = ex.getErrorCode();

        model.addAttribute("errorCode", code != null ? code.name() : "UNKNOWN");
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("path", req.getRequestURI());
        model.addAttribute("timestamp", ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")));

        return "pages/error";
    }

}
