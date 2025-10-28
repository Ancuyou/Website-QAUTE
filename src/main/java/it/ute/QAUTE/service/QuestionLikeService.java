package it.ute.QAUTE.service;

import it.ute.QAUTE.entity.User;
import org.springframework.transaction.annotation.Transactional;

public interface QuestionLikeService {
    @Transactional
    boolean toggleLike(Integer questionId, User user);

    long getLikeCount(Integer questionId);

    boolean isLikedByUser(Integer questionId, User user);

    @Transactional
    void incrementViews(Integer questionId);

    long getTotalLikesForUser(User user);
}
