package it.ute.QAUTE.api;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FastApiClient {

    @Qualifier("fastApiWebClient")
    private final WebClient webClient;

    public Mono<String> chatWithModel(String message) {
        Map<String, Object> body = Map.of(
                "messages", message,
                "max_new_tokens", 200,
                "temperature", 1.0,
                "top_p", 0.9,
                "repetition_penalty", 1.1,
                "no_repeat_ngram_size", 4
        );

        return webClient.post()
                .uri("/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .map(res -> (String) res.get("reply"));
    }
    public String chatBlocking(String message, Duration timeout) {
        return chatWithModel(message)
                .timeout(timeout)
                .onErrorReturn("Xin lỗi, hệ thống đang bận. Vui lòng thử lại sau.")
                .block();
    }

    public Mono<Integer> predictToxicAsync(String text) {
        Map<String, String> body = Map.of("text", text);

        return webClient.post()
                .uri("/predict")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .map(res -> {
                    Object value = res.get("label");
                    if (value instanceof Number) {
                        return ((Number) value).intValue();
                    } else if (value instanceof String) {
                        try {
                            return Integer.parseInt((String) value);
                        } catch (NumberFormatException e) {
                            return 0;
                        }
                    } else {
                        return 0;
                    }
                });
    }

    public int predictToxic(String text) {
        Integer result = predictToxicAsync(text)
                .timeout(Duration.ofSeconds(10))
                .onErrorReturn(0)
                .block();

        return result != null ? result : 0;
    }

}
