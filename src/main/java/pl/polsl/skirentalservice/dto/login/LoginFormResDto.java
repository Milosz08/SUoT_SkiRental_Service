/*
 * Copyright (c) 2022 by multiple authors
 * Silesian University of Technology
 *
 *  File name: LoginFormResDto.java
 *  Last modified: 27/12/2022, 22:59
 *  Project name: ski-rental-service
 *
 * This project was written for the purpose of a subject taken in the study of Computer Science.
 * This project is not commercial in any way and does not represent a viable business model
 * of the application. Project created for educational purposes only.
 */

package pl.polsl.skirentalservice.dto.login;

import lombok.*;

import pl.polsl.skirentalservice.dto.FormValueInfoTupleDto;

//----------------------------------------------------------------------------------------------------------------------

@Data
@Builder
@AllArgsConstructor
public class LoginFormResDto {
    private FormValueInfoTupleDto login;
    private FormValueInfoTupleDto password;

    //------------------------------------------------------------------------------------------------------------------

    public LoginFormResDto() {
        this.login = new FormValueInfoTupleDto();
        this.password = new FormValueInfoTupleDto();
    }
}