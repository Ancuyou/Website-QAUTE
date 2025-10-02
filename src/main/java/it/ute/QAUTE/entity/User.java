package it.ute.QAUTE.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserID")
    private int userID;

    @OneToOne
    @JoinColumn(name = "ProfileID", referencedColumnName = "ProfileID")
    private Profiles profile;

    @Column(name = "StudentCode", length = 20)
    private String studentCode;

    @Column(name = "RoleName", nullable = false, length = 50)
    private String roleName;
}
