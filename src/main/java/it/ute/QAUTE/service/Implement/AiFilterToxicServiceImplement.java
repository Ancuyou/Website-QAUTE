package it.ute.QAUTE.service.Implement;

import it.ute.QAUTE.api.FastApiClient;
import it.ute.QAUTE.service.AiFilterToxicService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class AiFilterToxicServiceImplement implements AiFilterToxicService {

    private final FastApiClient fastApiClient;

    // chạy song song (đa luồng)
    @Async("aiExecutor")
    public CompletableFuture<Integer> predictAsync(String text) {
        int result = fastApiClient.predictToxic(text);
        return CompletableFuture.completedFuture(result);
    }

    public List<Integer> predictBatch(List<String> comments) {
        List<CompletableFuture<Integer>> futures = comments.stream()
                .map(this::predictAsync)
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return futures.stream().map(CompletableFuture::join).toList();
    }
}

