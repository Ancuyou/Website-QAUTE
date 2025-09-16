package it.ute.QAUTE.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String roleId;

    @Column(nullable = false, unique = true, length = 50)
    private String roleName;

    private String description;
}
