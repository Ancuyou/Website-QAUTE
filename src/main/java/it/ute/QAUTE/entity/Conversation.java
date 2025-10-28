package it.ute.QAUTE.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "Conversation")
public class Conversation {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="Id")
    private Long id;
    @Column(name="UserProfileId", nullable=false)
    private Integer userProfileId;

    @Column(name="ConsultantProfileId", nullable=false)
    private Integer consultantProfileId;

    @Column(name="AIEnabled", nullable=false)
    private Boolean aiEnabled=true; // cho phép AI trả lời
}
