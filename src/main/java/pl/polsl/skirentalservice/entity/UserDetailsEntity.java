/*
 * Copyright (c) 2023 by MILOSZ GILGA <http://miloszgilga.pl>
 * Silesian University of Technology
 *
 *  File name: UserDetailsEntity.java
 *  Last modified: 31/01/2023, 04:50
 *  Project name: ski-rental-service
 *
 * This project was written for the purpose of a subject taken in the study of Computer Science.
 * This project is not commercial in any way and does not represent a viable business model
 * of the application. Project created for educational purposes only.
 */

package pl.polsl.skirentalservice.entity;

import lombok.NoArgsConstructor;

import jakarta.persistence.*;

import java.time.LocalDate;

import pl.polsl.skirentalservice.util.Gender;
import pl.polsl.skirentalservice.core.db.EntityInjector;
import pl.polsl.skirentalservice.core.db.AuditableEntity;
import pl.polsl.skirentalservice.converter.GenderConverter;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@Entity
@EntityInjector
@Table(name = "user_details")
@NoArgsConstructor
public class UserDetailsEntity extends AuditableEntity {

    @Column(name = "first_name")                    private String firstName;
    @Column(name = "last_name")                     private String lastName;
    @Column(name = "pesel")                         private String pesel;
    @Column(name = "phone_number")                  private String phoneNumber;
    @Column(name = "email_address")                 private String emailAddress;
    @Column(name = "born_date")                     private LocalDate bornDate;

    @Convert(converter = GenderConverter.class) @Column(name = "gender")
    private Gender gender;

    @Column(name = "phone_area_code", insertable = false, updatable = false)
    private Integer phoneAreaCode;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    String getPesel() {
        return pesel;
    }

    public void setPesel(String pesel) {
        this.pesel = pesel;
    }

    Integer getPhoneAreaCode() {
        return phoneAreaCode;
    }

    void setPhoneAreaCode(Integer phoneAreaCode) {
        this.phoneAreaCode = phoneAreaCode;
    }

    String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    LocalDate getBornDate() {
        return bornDate;
    }

    public void setBornDate(LocalDate bornDate) {
        this.bornDate = bornDate;
    }

    Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return '{' +
            "firstName='" + firstName +
            ", lastName='" + lastName +
            ", pesel='" + pesel +
            ", phoneAreaCode=" + phoneAreaCode +
            ", phoneNumber='" + phoneNumber +
            ", emailAddress='" + emailAddress +
            ", bornDate=" + bornDate +
            ", gender=" + gender +
            '}';
    }
}
