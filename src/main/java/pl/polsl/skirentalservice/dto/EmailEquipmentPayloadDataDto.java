/*
 * Copyright (c) 2023 by MILOSZ GILGA <http://miloszgilga.pl>
 * Silesian University of Technology
 *
 *  File name: EmailEquipmentPayloadDataDto.java
 *  Last modified: 08/02/2023, 23:29
 *  Project name: ski-rental-service
 *
 * This project was written for the purpose of a subject taken in the study of Computer Science.
 * This project is not commercial in any way and does not represent a viable business model
 * of the application. Project created for educational purposes only.
 */

package pl.polsl.skirentalservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import pl.polsl.skirentalservice.entity.EquipmentEntity;
import pl.polsl.skirentalservice.dto.deliv_return.RentReturnEquipmentRecordResDto;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@Data
@NoArgsConstructor
public class EmailEquipmentPayloadDataDto {
    private String count;
    private String name;
    private String typeAndModel;
    private BigDecimal priceNetto;
    private BigDecimal priceBrutto;
    private BigDecimal depositPriceNetto;
    private BigDecimal depositPriceBrutto;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public EmailEquipmentPayloadDataDto(EquipmentEntity eqEntity, RentReturnEquipmentRecordResDto eqDto) {
        name = eqEntity.getName();
        count = eqDto.count().toString();
        typeAndModel = eqEntity.getModel() + ", " + eqEntity.getEquipmentType().getName();
        depositPriceNetto = eqDto.depositPriceNetto();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return "{" +
            "count='" + count +
            ", name='" + name +
            ", typeAndModel='" + typeAndModel +
            ", priceNetto=" + priceNetto +
            ", priceBrutto=" + priceBrutto +
            ", depositPriceNetto=" + depositPriceNetto +
            ", depositPriceBrutto=" + depositPriceBrutto +
            '}';
    }
}
