package it.ute.QAUTE.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "Question")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "QuestionID")
    private int questionID;

    @Column(name = "Title", nullable = false, length = 200)
    private String title;

    @Column(name = "Content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "DateSend", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime dateSend;

    @Column(name = "Views", columnDefinition = "INT DEFAULT 0")
    private int views = 0;

    @Column(name = "FileAttachment", length = 255)
    private String fileAttachment;

    @ManyToOne
    @JoinColumn(name = "UserID", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "DepartmentID")
    private Department department;

    @ManyToOne
    @JoinColumn(name = "FieldID")
    private Field field;

    @Column(name = "Status", length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'Pending'")
    private String status = "Pending";
}