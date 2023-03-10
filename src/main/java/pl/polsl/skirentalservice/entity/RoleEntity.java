/*
 * Copyright (c) 2023 by MILOSZ GILGA <http://miloszgilga.pl>
 * Silesian University of Technology
 *
 *  File name: RoleEntity.java
 *  Last modified: 27/01/2023, 11:57
 *  Project name: ski-rental-service
 *
 * This project was written for the purpose of a subject taken in the study of Computer Science.
 * This project is not commercial in any way and does not represent a viable business model
 * of the application. Project created for educational purposes only.
 */

package pl.polsl.skirentalservice.entity;

import lombok.NoArgsConstructor;

import jakarta.persistence.*;

import pl.polsl.skirentalservice.core.db.EntityInjector;
import pl.polsl.skirentalservice.core.db.AuditableEntity;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@Entity
@EntityInjector
@Table(name = "roles")
@NoArgsConstructor
public class RoleEntity extends AuditableEntity {

    @Column(name = "role_name")     private String roleName;
    @Column(name = "alias")         private Character alias;
    @Column(name = "role_eng")      private String roleEng;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public String getRoleName() {
        return roleName;
    }

    void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public Character getAlias() {
        return alias;
    }

    void setAlias(Character alias) {
        this.alias = alias;
    }

    String getRoleEng() {
        return roleEng;
    }

    void setRoleEng(String roleEng) {
        this.roleEng = roleEng;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return "{" +
            "roleName='" + roleName +
            ", alias=" + alias +
            ", roleEng='" + roleEng +
            '}';
    }
}
