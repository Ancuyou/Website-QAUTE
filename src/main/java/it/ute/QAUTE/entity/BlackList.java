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
    @Column(name="Id")
    private int id;
    @Column(name="DeviceId",nullable = false)
    private String deviceId;
    @Column(name="DeviceName",nullable = false)
    private String deviceName;
    @Column(name="UnblockAt",nullable = false)
    private Date unblockAt;
    @Column(name="Block",nullable = false)
    private boolean block=false;
    @Column(name="BlockAt",nullable = false)
    private Date blockAt;
}
