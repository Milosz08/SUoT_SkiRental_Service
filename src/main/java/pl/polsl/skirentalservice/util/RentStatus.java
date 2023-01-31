/*
 * Copyright (c) 2023 by multiple authors
 * Silesian University of Technology
 *
 *  File name: RentStatus.java
 *  Last modified: 28/01/2023, 17:32
 *  Project name: ski-rental-service
 *
 * This project was written for the purpose of a subject taken in the study of Computer Science.
 * This project is not commercial in any way and does not represent a viable business model
 * of the application. Project created for educational purposes only.
 */

package pl.polsl.skirentalservice.util;

import lombok.*;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@Getter
@AllArgsConstructor
public enum RentStatus {
    OPENED      ("otwarty", "opened"),
    RENTED      ("wypożyczony", "rented"),
    RETURNED    ("zwrócony", "returned");
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final String status;
    private final String statusEng;
}