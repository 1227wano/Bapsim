package com.bapsim.entity;

import javax.persistence.*;

@Entity
@Table(name = "Price")
public class Price {
    
    @Id
    @Column(name = "MENU_NO", nullable = false)
    private Long menuNo;
    
    @Column(name = "PRICE", nullable = false)
    private Long price;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MENU_NO", insertable = false, updatable = false)
    private Menus menu;
    
    // Constructors
    public Price() {}
    
    // Getters and Setters
    public Long getMenuNo() {
        return menuNo;
    }
    
    public void setMenuNo(Long menuNo) {
        this.menuNo = menuNo;
    }
    
    public Long getPrice() {
        return price;
    }
    
    public void setPrice(Long price) {
        this.price = price;
    }
    
    public Menus getMenu() {
        return menu;
    }
    
    public void setMenu(Menus menu) {
        this.menu = menu;
    }
}
