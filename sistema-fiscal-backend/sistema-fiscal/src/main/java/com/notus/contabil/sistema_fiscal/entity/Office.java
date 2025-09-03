package com.notus.contabil.sistema_fiscal.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "offices", schema = "public")
public class Office {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    public Office() {}
    public Office(String name) { this.name = name; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Office)) return false;
        Office office = (Office) o;
        return Objects.equals(id, office.id) && Objects.equals(name, office.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
