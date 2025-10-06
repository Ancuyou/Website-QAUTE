package it.ute.QAUTE.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.context.annotation.Profile;

import java.util.Date;
@Entity
@Getter
@Setter
@Builder
@Table(name="Account")
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AccountID")
    private int accountID;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "ProfileID", referencedColumnName = "ProfileID", nullable = false, unique = true)
    private Profiles profile;

    @Column(name = "Username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "Password", nullable = false, length = 255)
    private String password;

    @Column(name = "Email", nullable = false, unique = true, length = 100)
    private String email;

    @Enumerated(EnumType.STRING)   // Map enum trong DB sang Enum trong Java
    @Column(name = "Role", nullable = false)
    private Role role;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CreatedDate", updatable = false, insertable = false)
    private Date createdDate;
    public enum Role {
        Admin,
        Manager,
        Consultant,
        User;
    }
}
