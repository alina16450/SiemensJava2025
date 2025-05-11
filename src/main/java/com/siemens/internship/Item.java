package com.siemens.internship;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String status;
    private String email;

    public void setEmail(String email) {
        if(email == null || email.isEmpty()){
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (!email.matches("^[A-Za-z][A-Za-z0-9._-]*@[A-Za-z0-9][A-Za-z0-9._-]*\\.[A-Za-z]+$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        this.email = email;
    }

}
