package it.ute.QAUTE.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class HttpClientConfig {

    @Bean("fastApiWebClient")
    public WebClient fastApiWebClient(
            @Value("${ai.chat.base-url:http://100.77.190.21:8000}") String baseUrl
    ) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5_000)
                .responseTimeout(Duration.ofSeconds(60))
                .doOnConnected(conn -> {
                    conn.addHandlerLast(new ReadTimeoutHandler(60, TimeUnit.SECONDS));
                    conn.addHandlerLast(new WriteTimeoutHandler(60, TimeUnit.SECONDS));
                });

        return WebClient.builder()
                .baseUrl(baseUrl)                  // ví dụ: http://100.81.7.73:8000  (để .uri("/chat") ở client)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(c -> c.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)) // tăng nếu reply dài
                        .build())
                .build();
    }
}
