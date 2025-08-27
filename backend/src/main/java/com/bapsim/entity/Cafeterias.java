package com.bapsim.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "Cafeterias")
public class Cafeterias {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CAFE_NO")
    private Long cafeNo;
    
    @Column(name = "UNI_ID", nullable = false)
    private Integer uniId;
    
    @Column(name = "BUILD_NAME", length = 100, nullable = false)
    private String buildName;
    
    @Column(name = "PHONE_NO", nullable = false)
    private Long phoneNo;
    
    @Column(name = "OPEN_TIME", nullable = false)
    private LocalDateTime openTime;
    
    @Column(name = "CLOSE_TIME", nullable = false)
    private LocalDateTime closeTime;
    
    @Column(name = "RUN_YN", length = 1, nullable = false)
    private String runYn;
    
    @Column(name = "DEL_YN", length = 1, nullable = false)
    private String delYn;
    
    @Column(name = "VISITOR", nullable = false)
    private Long visitor;
    
    @Column(name = "CREATED_ID", length = 100, nullable = false)
    private String createdId;
    
    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "UPDATED_ID", length = 100, nullable = false)
    private String updatedId;
    
    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UNI_ID", insertable = false, updatable = false)
    @JsonIgnoreProperties({"cafeterias", "hibernateLazyInitializer"})
    private University university;
    
    @OneToMany(mappedBy = "cafeteria", fetch = FetchType.EAGER)
    @JsonIgnoreProperties({"cafeteria", "hibernateLazyInitializer"})
    private List<Menus> menus;
    
    // Constructors
    public Cafeterias() {}
    
    // Getters and Setters
    public Long getCafeNo() {
        return cafeNo;
    }
    
    public void setCafeNo(Long cafeNo) {
        this.cafeNo = cafeNo;
    }
    
    public Integer getUniId() {
        return uniId;
    }
    
    public void setUniId(Integer uniId) {
        this.uniId = uniId;
    }
    
    public String getBuildName() {
        return buildName;
    }
    
    public void setBuildName(String buildName) {
        this.buildName = buildName;
    }
    
    public Long getPhoneNo() {
        return phoneNo;
    }
    
    public void setPhoneNo(Long phoneNo) {
        this.phoneNo = phoneNo;
    }
    
    public LocalDateTime getOpenTime() {
        return openTime;
    }
    
    public void setOpenTime(LocalDateTime openTime) {
        this.openTime = openTime;
    }
    
    public LocalDateTime getCloseTime() {
        return closeTime;
    }
    
    public void setCloseTime(LocalDateTime closeTime) {
        this.closeTime = closeTime;
    }
    
    public String getRunYn() {
        return runYn;
    }
    
    public void setRunYn(String runYn) {
        this.runYn = runYn;
    }
    
    public String getDelYn() {
        return delYn;
    }
    
    public void setDelYn(String delYn) {
        this.delYn = delYn;
    }
    
    public Long getVisitor() {
        return visitor;
    }
    
    public void setVisitor(Long visitor) {
        this.visitor = visitor;
    }
    
    public String getCreatedId() {
        return createdId;
    }
    
    public void setCreatedId(String createdId) {
        this.createdId = createdId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getUpdatedId() {
        return updatedId;
    }
    
    public void setUpdatedId(String updatedId) {
        this.updatedId = updatedId;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public University getUniversity() {
        return university;
    }
    
    public void setUniversity(University university) {
        this.university = university;
    }
    
    public List<Menus> getMenus() {
        return menus;
    }
    
    public void setMenus(List<Menus> menus) {
        this.menus = menus;
    }
}
