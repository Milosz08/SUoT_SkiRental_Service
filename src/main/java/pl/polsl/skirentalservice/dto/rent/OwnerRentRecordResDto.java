/*
 * Copyright (c) 2023 by MILOSZ GILGA <http://miloszgilga.pl>
 * Silesian University of Technology
 *
 *  File name: OwnerRentRecordResDto.java
 *  Last modified: 30/01/2023, 00:32
 *  Project name: ski-rental-service
 *
 * This project was written for the purpose of a subject taken in the study of Computer Science.
 * This project is not commercial in any way and does not represent a viable business model
 * of the application. Project created for educational purposes only.
 */

package pl.polsl.skirentalservice.dto.rent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import pl.polsl.skirentalservice.util.RentStatus;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public record OwnerRentRecordResDto(
    Long id,
    String issuedIdentifier,
    LocalDateTime issuedDateTime,
    RentStatus status,
    BigDecimal totalPriceNetto,
    BigDecimal totalPriceBrutto,
    String customerName,
    Long customerId,
    String employerName,
    Long employerId
) {
}
