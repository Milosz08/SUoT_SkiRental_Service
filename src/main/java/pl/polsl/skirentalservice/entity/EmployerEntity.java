/*
 * Copyright (c) 2023 by MILOSZ GILGA <http://miloszgilga.pl>
 * Silesian University of Technology
 *
 *  File name: EmployerEntity.java
 *  Last modified: 30/01/2023, 20:15
 *  Project name: ski-rental-service
 *
 * This project was written for the purpose of a subject taken in the study of Computer Science.
 * This project is not commercial in any way and does not represent a viable business model
 * of the application. Project created for educational purposes only.
 */

package pl.polsl.skirentalservice.entity;

import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;

import java.time.LocalDate;

import pl.polsl.skirentalservice.core.db.EntityInjector;
import pl.polsl.skirentalservice.core.db.AuditableEntity;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@Entity
@EntityInjector
@Builder
@Table(name = "employeers")
@AllArgsConstructor
@NoArgsConstructor
public class EmployerEntity extends AuditableEntity {

    @Column(name = "login")                                 private String login;
    @Column(name = "password")                              private String password;
    @Column(name = "hired_date")                            private LocalDate hiredDate;
    @Column(name = "first_access", insertable = false)      private Boolean firstAccess;

    @OneToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE })
    @JoinColumn(name = "user_details_id", referencedColumnName = "id")
    private UserDetailsEntity userDetails;

    @OneToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE })
    @JoinColumn(name = "location_address_id", referencedColumnName = "id")
    private LocationAddressEntity locationAddress;

    @OneToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinColumn(name = "role_id", referencedColumnName = "id")
    private RoleEntity role;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public EmployerEntity(LocalDate hiredDate, UserDetailsEntity userDetails, LocationAddressEntity locationAddress) {
        this.hiredDate = hiredDate;
        this.userDetails = userDetails;
        this.locationAddress = locationAddress;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    String getLogin() {
        return login;
    }

    void setLogin(String login) {
        this.login = login;
    }

    String getPassword() {
        return password;
    }

    void setPassword(String password) {
        this.password = password;
    }

    LocalDate getHiredDate() {
        return hiredDate;
    }

    public void setHiredDate(LocalDate hiredDate) {
        this.hiredDate = hiredDate;
    }

    public UserDetailsEntity getUserDetails() {
        return userDetails;
    }

    void setUserDetails(UserDetailsEntity userDetails) {
        this.userDetails = userDetails;
    }

    public LocationAddressEntity getLocationAddress() {
        return locationAddress;
    }

    void setLocationAddress(LocationAddressEntity locationAddress) {
        this.locationAddress = locationAddress;
    }

    RoleEntity getRole() {
        return role;
    }

    void setRole(RoleEntity role) {
        this.role = role;
    }

    Boolean getFirstAccess() {
        return firstAccess;
    }

    void setFirstAccess(Boolean firstAccess) {
        this.firstAccess = firstAccess;
    }

    public String getEmailAddress() {
        return userDetails.getEmailAddress();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return "{" +
            "login='" + login +
            ", password='" + password +
            ", hiredDate=" + hiredDate +
            ", firstAccess=" + firstAccess +
            ", role=" + role +
            '}';
    }
}
