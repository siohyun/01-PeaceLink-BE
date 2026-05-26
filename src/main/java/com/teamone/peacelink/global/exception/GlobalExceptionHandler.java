package com.teamone.peacelink.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import com.teamone.peacelink.global.exception.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnsupportedLanguageException.class)
    public ResponseEntity<?> handleUnsupportedLanguage(UnsupportedLanguageException e) {
        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "errorType", "UNSUPPORTED_LANGUAGE",
                "message", e.getMessage()
        ));
    }

    @ExceptionHandler(NoiseTooLoudException.class)
    public ResponseEntity<?> handleNoiseTooLoud(NoiseTooLoudException e) {
        return ResponseEntity.ok(Map.of(
                "success", false,
                "errorType", "NOISE_TOO_LOUD",
                "message", "주변이 너무 시끄럽습니다. 조용한 곳에서 다시 시도하거나 필담 모드를 사용하세요.",
                "fallbackMode", "TEXT_INPUT"
        ));
    }

    @ExceptionHandler(STTFailException.class)
    public ResponseEntity<?> handleSTTFail(STTFailException e) {
        return ResponseEntity.ok(Map.of(
                "success", false,
                "errorType", "STT_FAIL",
                "message", "음성 인식에 실패했습니다. 다시 말씀해 주세요.",
                "retry", true
        ));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntime(RuntimeException e) {
        return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
        ));
    }
}
