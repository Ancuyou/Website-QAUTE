package it.ute.QAUTE.entity;

import jakarta.persistence.*;
import lombok.*;
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
    @GeneratedValue(strategy = GenerationType.UUID)
    private String userId;

    @Column(nullable = false, unique = true, length = 30)
    private String username;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, unique = true, length = 50)
    private String email;

    @Column(length = 15)
    private String phone;

    private String fullName;

    private String avatar;

    @ManyToOne
    @JoinColumn(name = "roleId", nullable = false)
    private Role role;

    private String studentId;

    private LocalDateTime createdDate;
}
