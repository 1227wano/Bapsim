package com.bapsim.entity;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "Member")
public class Member {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_NO")
    private Long userNo;
    
    @Column(name = "UNI_ID", nullable = false)
    private Integer uniId;
    
    @Column(name = "USER_ID", length = 100, nullable = false)
    private String userId;
    
    @Column(name = "USER_PASS", length = 500, nullable = false)
    private String userPass;
    
    @Column(name = "USER_NAME", length = 100, nullable = false)
    private String userName;
    
    @Column(name = "USER_EMAIL", length = 100)
    private String userEmail;
    
    @Column(name = "USER_PHONE", length = 20)
    private String userPhone;
    
    @Column(name = "USER_TYPE", length = 20, nullable = false)
    private String userType;
    
    @Column(name = "USER_STATUS", length = 20, nullable = false)
    private String userStatus;
    
    @Column(name = "CREATED_ID", length = 100, nullable = false)
    private String createdId;
    
    @Column(name = "CREATED_AT", nullable = false)
    private java.time.LocalDateTime createdAt;
    
    @Column(name = "UPDATED_ID", length = 100, nullable = false)
    private String updatedId;
    
    @Column(name = "UPDATED_AT", nullable = false)
    private java.time.LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UNI_ID", insertable = false, updatable = false)
    private University university;
    
    @OneToMany(mappedBy = "user")
    private List<Payment> payments;
    
    @OneToMany(mappedBy = "user")
    private List<PointHistory> pointHistories;
    
    @OneToMany(mappedBy = "user")
    private List<AIService> aiServices;
    
    // Constructors
    public Member() {}
    
    public Member(Integer uniId, String userId, String userPass) {
        this.uniId = uniId;
        this.userId = userId;
        this.userPass = userPass;
    }
    
    // Getters and Setters
    public Long getUserNo() {
        return userNo;
    }
    
    public void setUserNo(Long userNo) {
        this.userNo = userNo;
    }
    
    public Integer getUniId() {
        return uniId;
    }
    
    public void setUniId(Integer uniId) {
        this.uniId = uniId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getUserPass() {
        return userPass;
    }
    
    public void setUserPass(String userPass) {
        this.userPass = userPass;
    }
    
    public University getUniversity() {
        return university;
    }
    
    public void setUniversity(University university) {
        this.university = university;
    }
    
    public List<Payment> getPayments() {
        return payments;
    }
    
    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }
    
    public List<PointHistory> getPointHistories() {
        return pointHistories;
    }
    
    public void setPointHistories(List<PointHistory> pointHistories) {
        this.pointHistories = pointHistories;
    }
    
    public List<AIService> getAiServices() {
        return aiServices;
    }
    
    public void setAiServices(List<AIService> aiServices) {
        this.aiServices = aiServices;
    }
    
    // Additional Getters and Setters
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getUserEmail() {
        return userEmail;
    }
    
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
    
    public String getUserPhone() {
        return userPhone;
    }
    
    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }
    
    public String getUserType() {
        return userType;
    }
    
    public void setUserType(String userType) {
        this.userType = userType;
    }
    
    public String getUserStatus() {
        return userStatus;
    }
    
    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }
    
    public String getCreatedId() {
        return createdId;
    }
    
    public void setCreatedId(String createdId) {
        this.createdId = createdId;
    }
    
    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getUpdatedId() {
        return updatedId;
    }
    
    public void setUpdatedId(String updatedId) {
        this.updatedId = updatedId;
    }
    
    public java.time.LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
