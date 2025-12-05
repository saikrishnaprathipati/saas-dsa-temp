package uk.gov.saas.dsa.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="AUTHORITIES")
public class Authorities {

    @Id
    @Column(name="ID")
//    @NotBlank(message = "ID is mandatory")
    private int id;

    @Column(name="NAME")
//    @NotBlank(message = "Name is mandatory")
    private String name;

    @Column(name="DESCRIPTION")
    private String description;

    @Column(name="ENABLED")
//    @NotBlank(message = "Enabled is mandatory")
    private int enabled;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getEnabled() {
        return enabled;
    }

    public void setEnabled(int enabled) {
        this.enabled = enabled;
    }


    
}
