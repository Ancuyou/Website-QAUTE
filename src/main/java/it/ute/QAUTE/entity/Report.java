package it.ute.QAUTE.entity;

import jakarta.persistence.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="Id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "Reason", nullable = false, length = 500)
    private ReportReason reason;

    @Column(name="Description",columnDefinition = "TEXT")
    private String description;

    @Column(name = "ContentType")
    private String contentType;

    @Column(name = "ContentId")
    private Long contentId;

    @ManyToOne
    @JoinColumn(name = "ReporterId", nullable = false)
    private Account reporter;

    @Enumerated(EnumType.STRING)
    @Column(name="Status",nullable = false)
    private ReportStatus status;

    @Column(name="CreatedAt",nullable = false)
    private LocalDateTime createdAt;

    @Transient
    private Question question;

    @Transient
    private Answer answer;

    @Transient
    private Messages message;

    public Report() {
        this.createdAt = LocalDateTime.now();
        this.status = ReportStatus.PENDING;
    }

    public enum ReportReason {
        SPAM_ADS,              // Spam/Quảng cáo
        HATEFUL_LANGUAGE,      // Ngôn từ thù hận, xúc phạm
        ADULT_CONTENT,         // Nội dung 18+
        MISINFORMATION,        // Thông tin sai lệch, nguy hiểm
        HARASSMENT,            // Quấy rối
        OTHER                  // Khác
    }

    public enum ReportStatus {
        PENDING,
        PROCESSED,
    }
}
