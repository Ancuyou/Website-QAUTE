package it.ute.QAUTE.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(name="BlackList")
public class BlackList {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int id;
    @Column(nullable = false)
    private String deviceId;
    @Column(nullable = false)
    private String deviceName;
    @Column(nullable = false)
    private Date unblockAt;
    @Column(nullable = false)
    private boolean block=false;
    @Column(nullable = false)
    private Date blockAt;
}
