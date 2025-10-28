package it.ute.QAUTE.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "RefreshToken")
public class RefreshToken {
    @Id
    @Column(name = "RefreshId")
    String refreshId;

    @Column(name = "SignKey", nullable = false, length = 128, unique = true)
    String signKey;

    @Column(name = "DeviceName", length = 100)
    String deviceName;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CreatedAt", nullable = false, updatable = false)
    Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ExpiresAt", nullable = false)
    Date expiresAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "AccountID", nullable = false)
    private Account account;
}
