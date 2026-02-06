package com.revpay.entity;

import com.revpay.enums.Role;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email"),
                @UniqueConstraint(columnNames = "phone")
        })
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(unique = true)
    private String phone;

    private String profilePictureUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private boolean emailVerified = false;

    @Column(nullable = false)
    private boolean phoneVerified = false;

    // Relationships
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Account> accounts = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentMethod> paymentMethods = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notification> notifications = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "reset_token_expiry")
    private Instant resetTokenExpiry;

    // ✅ No-args constructor (Required by JPA)
    public User() {
    }

    public User(Long id, String fullName, String email, String password, String phone,
                String profilePictureUrl, Role role, boolean enabled,
                boolean emailVerified, boolean phoneVerified,
                List<Account> accounts, List<PaymentMethod> paymentMethods,
                List<Notification> notifications,
                Instant createdAt, Instant updatedAt) {

        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.profilePictureUrl = profilePictureUrl;
        this.role = role;
        this.enabled = enabled;
        this.emailVerified = emailVerified;
        this.phoneVerified = phoneVerified;
        this.accounts = accounts != null ? accounts : new ArrayList<>();
        this.paymentMethods = paymentMethods != null ? paymentMethods : new ArrayList<>();
        this.notifications = notifications != null ? notifications : new ArrayList<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // =========================
    // Manual Builder
    // =========================

    public static class Builder {
        private Long id;
        private String fullName;
        private String email;
        private String password;
        private String phone;
        private String profilePictureUrl;
        private Role role;
        private boolean enabled = true;
        private boolean emailVerified = false;
        private boolean phoneVerified = false;
        private List<Account> accounts = new ArrayList<>();
        private List<PaymentMethod> paymentMethods = new ArrayList<>();
        private List<Notification> notifications = new ArrayList<>();
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder fullName(String fullName) { this.fullName = fullName; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder password(String password) { this.password = password; return this; }
        public Builder phone(String phone) { this.phone = phone; return this; }
        public Builder profilePictureUrl(String url) { this.profilePictureUrl = url; return this; }
        public Builder role(Role role) { this.role = role; return this; }
        public Builder enabled(boolean enabled) { this.enabled = enabled; return this; }
        public Builder emailVerified(boolean verified) { this.emailVerified = verified; return this; }
        public Builder phoneVerified(boolean verified) { this.phoneVerified = verified; return this; }
        public Builder accounts(List<Account> accounts) { this.accounts = accounts; return this; }
        public Builder paymentMethods(List<PaymentMethod> methods) { this.paymentMethods = methods; return this; }
        public Builder notifications(List<Notification> notifications) { this.notifications = notifications; return this; }

        public User build() {
            return new User(
                    id,
                    fullName,
                    email,
                    password,
                    phone,
                    profilePictureUrl,
                    role,
                    enabled,
                    emailVerified,
                    phoneVerified,
                    accounts,
                    paymentMethods,
                    notifications,
                    createdAt,
                    updatedAt
            );
        }
    }

    // Static method to start builder
    public static Builder builder() {
        return new Builder();
    }

    // =========================
    // Getters & Setters
    // =========================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }

    public boolean isPhoneVerified() { return phoneVerified; }
    public void setPhoneVerified(boolean phoneVerified) { this.phoneVerified = phoneVerified; }

    public List<Account> getAccounts() { return accounts; }
    public void setAccounts(List<Account> accounts) { this.accounts = accounts; }

    public List<PaymentMethod> getPaymentMethods() { return paymentMethods; }
    public void setPaymentMethods(List<PaymentMethod> paymentMethods) { this.paymentMethods = paymentMethods; }

    public List<Notification> getNotifications() { return notifications; }
    public void setNotifications(List<Notification> notifications) { this.notifications = notifications; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }

    public Instant getResetTokenExpiry() { return resetTokenExpiry; }
    public void setResetTokenExpiry(Instant resetTokenExpiry) { this.resetTokenExpiry = resetTokenExpiry; }

}