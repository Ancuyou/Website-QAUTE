package it.ute.QAUTE.entity;

import java.util.Date;

import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name="InvalidatedToken")
public class InvalidatedToken {
    @Id
    @Column(name="InvalidatedTokenId")
    String invalidatedTokenId;
    @Column(name="ExpiryTime")
    Date expiryTime;
}
