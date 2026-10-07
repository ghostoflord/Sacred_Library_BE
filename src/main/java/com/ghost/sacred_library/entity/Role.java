package com.ghost.sacred_library.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "roles", uniqueConstraints = @UniqueConstraint(name = "uk_roles_name", columnNames = "name"))
public class Role {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String name;

    protected Role() { }
    public Role(String name) { this.name = name; }
    public Long getId() { return id; }
    public String getName() { return name; }
}
