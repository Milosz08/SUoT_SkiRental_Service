/*
 * Copyright (c) 2023 by MILOSZ GILGA <http://miloszgilga.pl>
 * Silesian University of Technology
 *
 *  File name: RentStatusConverter.java
 *  Last modified: 08/02/2023, 17:10
 *  Project name: ski-rental-service
 *
 * This project was written for the purpose of a subject taken in the study of Computer Science.
 * This project is not commercial in any way and does not represent a viable business model
 * of the application. Project created for educational purposes only.
 */

package pl.polsl.skirentalservice.converter;

import java.util.Objects;

import jakarta.persistence.AttributeConverter;

import pl.polsl.skirentalservice.util.RentStatus;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public class RentStatusConverter implements AttributeConverter<RentStatus, String> {

    @Override
    public String convertToDatabaseColumn(RentStatus rentStatus) {
        return rentStatus.getStatus();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public RentStatus convertToEntityAttribute(String name) {
        if (Objects.isNull(name)) return RentStatus.OPENED;
        return switch (name) {
            case "wypożyczony" -> RentStatus.RENTED;
            case "zwrócony" -> RentStatus.RETURNED;
            default -> RentStatus.OPENED;
        };
    }
}
