package com.example.backend_clean_ops.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "\"Tenant\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant extends BaseEntity {

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "company_number", length = 50)
    private String companyNumber;

    @Column(name = "contact_name", nullable = false, length = 255)
    private String contactName;

    @Column(name = "contact_email", nullable = false, length = 255)
    private String contactEmail;

    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    @Column(name = "address_line_1", nullable = false, length = 255)
    private String addressLine1;

    @Column(name = "address_line_2", length = 255)
    private String addressLine2;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "postcode", nullable = false, length = 20)
    private String postcode;

    @Builder.Default
    @Column(name = "country", nullable = false, length = 100)
    private String country = "United Kingdom";

    @Builder.Default
    @Column(name = "subscription_plan", nullable = false, length = 50)
    private String subscriptionPlan = "starter";

    @Builder.Default
    @Column(name = "subscription_status", nullable = false, length = 50)
    private String subscriptionStatus = "trial";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
