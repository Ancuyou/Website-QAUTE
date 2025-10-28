package it.ute.QAUTE.service;

import it.ute.QAUTE.entity.Messages;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface AIService {
    @Async("aiExecutor")
    void replyMessage(Messages userMsg);

    String aiReply(String text);

    @Async("aiExecutor")
    CompletableFuture<Integer> predictAsync(String text);

    List<Integer> predictBatch(List<String> comments);
}
