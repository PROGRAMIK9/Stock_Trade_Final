package com.models;

import java.io.Serializable;

/**
 * Base class demonstrating inheritance
 */
public abstract class Person implements Serializable {
    protected int id;
    protected String name;
    protected String email;
    
    public Person(String email) {
        this.email = email;
    }
    
    public Person(int id, String email) {
        this.id = id;
        this.email = email;
    }
    
    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    // Abstract method to be implemented by subclasses
    public abstract String getRole();
    
    @Override
    public String toString() {
        return "Person{id=" + id + ", name='" + name + "', email='" + email + "'}";
    }
}
