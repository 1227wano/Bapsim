package com.bapsim.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "Restaurants")
public class Restaurants {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RES_NO")
    private Long resNo;
    
    @Column(name = "RES_NAME", length = 100, nullable = false)
    private String resName;
    
    @Column(name = "ADDRESS", length = 2000, nullable = false)
    private String address;
    
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
    
    // 식권 관련 복잡한 필드들 제거 (단순화)
    
    @Column(name = "CREATED_ID", length = 100, nullable = false)
    private String createdId;
    
    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "UPDATED_ID", length = 100, nullable = false)
    private String updatedId;
    
    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "restaurant", fetch = FetchType.EAGER)
    @JsonIgnoreProperties({"restaurant", "hibernateLazyInitializer"})
    private List<Menus> menus;
    
    // Constructors
    public Restaurants() {}
    
    // Getters and Setters
    public Long getResNo() {
        return resNo;
    }
    
    public void setResNo(Long resNo) {
        this.resNo = resNo;
    }
    
    public String getResName() {
        return resName;
    }
    
    public void setResName(String resName) {
        this.resName = resName;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
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
    
    public List<Menus> getMenus() {
        return menus;
    }
    
    public void setMenus(List<Menus> menus) {
        this.menus = menus;
    }
}
