package it.ute.QAUTE.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="Profiles")
@Getter
@Setter
public class Profiles {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ProfileID")
    private int profileID;
    @Column(name = "FullName", nullable = false, length = 100)
    private String fullName;
    @Column(name = "Phone")
    private String phone;
    @Column(name="Avatar")
    private String avatar;
}
