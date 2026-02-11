package com.traf.core;

import jakarta.persistence.*;

@Entity
@Table(name = "plans")
@NamedQueries({
        @NamedQuery(name = "com.traf.core.Plan.findAll", query = "SELECT p FROM Plan p")
})
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "monthly_price", nullable = false)
    private double monthlyPrice;

    @Column(name= "max_credits", nullable = false)
    private int maxCredits;

    public Plan() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getMonthlyPrice() {
        return monthlyPrice;
    }

    public void setMonthlyPrice(double monthlyPrice) {
        this.monthlyPrice = monthlyPrice;
    }

    public int getMaxCredits() {
        return maxCredits;
    }

    public void setMaxCredits(int maxCredits) {
        this.maxCredits = maxCredits;
    }
}
