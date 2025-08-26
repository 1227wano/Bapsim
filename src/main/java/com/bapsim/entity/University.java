package com.bapsim.entity;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "University")
public class University {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UNI_ID")
    private Integer uniId;
    
    @Column(name = "UNI_NAME", length = 30, nullable = false)
    private String uniName;
    
    @OneToMany(mappedBy = "university")
    private List<Member> members;
    
    @OneToMany(mappedBy = "university")
    private List<Cafeterias> cafeterias;
    
    // Constructors
    public University() {}
    
    public University(String uniName) {
        this.uniName = uniName;
    }
    
    // Getters and Setters
    public Integer getUniId() {
        return uniId;
    }
    
    public void setUniId(Integer uniId) {
        this.uniId = uniId;
    }
    
    public String getUniName() {
        return uniName;
    }
    
    public void setUniName(String uniName) {
        this.uniName = uniName;
    }
    
    public List<Member> getMembers() {
        return members;
    }
    
    public void setMembers(List<Member> members) {
        this.members = members;
    }
    
    public List<Cafeterias> getCafeterias() {
        return cafeterias;
    }
    
    public void setCafeterias(List<Cafeterias> cafeterias) {
        this.cafeterias = cafeterias;
    }
}
