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
    private Long id;
    @Column(name="userProfileId", nullable=false)
    private Integer userProfileId;

    @Column(name="consultantProfileId", nullable=false)
    private Integer consultantProfileId;

    @Column(name="consultantJoined", nullable=false)
    private Boolean consultantJoined = false;

    @Column(name="aiEnabled", nullable=false)
    private Boolean aiEnabled=true; // cho phép AI trả lời
}
