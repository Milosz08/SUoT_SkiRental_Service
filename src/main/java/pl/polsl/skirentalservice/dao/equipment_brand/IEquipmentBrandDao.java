/*
 * Copyright (c) 2023 by MILOSZ GILGA <http://miloszgilga.pl>
 * Silesian University of Technology
 *
 *  File name: IEquipmentBrandDto.java
 *  Last modified: 20/02/2023, 21:21
 *  Project name: ski-rental-service
 *
 * This project was written for the purpose of a subject taken in the study of Computer Science.
 * This project is not commercial in any way and does not represent a viable business model
 * of the application. Project created for educational purposes only.
 */

package pl.polsl.skirentalservice.dao.equipment_brand;

import java.util.List;
import java.util.Optional;

import pl.polsl.skirentalservice.dto.FormSelectTupleDto;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public interface IEquipmentBrandDao {
    List<FormSelectTupleDto> findAllEquipmentBrands();
    Optional<String> getEquipmentBrandNameById(Object brandId);

    boolean checkIfEquipmentBrandExistByName(String brandName);
    boolean checkIfEquipmentBrandHasAnyConnections(Object brandId);

    void deleteEquipmentBrandById(Object brandId);
}
