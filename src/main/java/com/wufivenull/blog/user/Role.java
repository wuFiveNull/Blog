package com.wufivenull.blog.user;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 20)
    private RoleCode code;

    @Column(nullable = false, length = 40)
    private String name;

    protected Role() {
    }

    public Role(RoleCode code, String name) {
        this.code = code;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public RoleCode getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
