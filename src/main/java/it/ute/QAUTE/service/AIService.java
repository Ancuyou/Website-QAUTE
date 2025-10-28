package it.ute.QAUTE.service;

import it.ute.QAUTE.entity.Messages;
import org.springframework.scheduling.annotation.Async;

public interface AIService {
    @Async("aiExecutor")
    void replyMessage(Messages userMsg);

    String aiReply(String text);
}
