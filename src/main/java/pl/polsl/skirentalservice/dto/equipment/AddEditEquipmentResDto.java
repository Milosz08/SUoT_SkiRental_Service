/*
 * Copyright (c) 2023 by multiple authors
 * Silesian University of Technology
 *
 *  File name: AddEditEquipmentResDto.java
 *  Last modified: 24/01/2023, 15:31
 *  Project name: ski-rental-service
 *
 * This project was written for the purpose of a subject taken in the study of Computer Science.
 * This project is not commercial in any way and does not represent a viable business model
 * of the application. Project created for educational purposes only.
 */

package pl.polsl.skirentalservice.dto.equipment;

import lombok.*;
import java.util.List;

import pl.polsl.skirentalservice.dto.*;
import pl.polsl.skirentalservice.core.ValidatorBean;

import static pl.polsl.skirentalservice.util.Gender.*;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@Data
@NoArgsConstructor
public class AddEditEquipmentResDto {
    private FormValueInfoTupleDto name;
    private FormValueInfoTupleDto description;
    private FormValueInfoTupleDto total;
    private FormValueInfoTupleDto size;
    private FormSelectsDto types = new FormSelectsDto();
    private FormSelectsDto brands = new FormSelectsDto();
    private FormSelectsDto colors = new FormSelectsDto();
    private List<FormSelectTupleDto> genders = getGendersWithUnisex();

    // TODO: dodawnie dodatkowych pól do obsługi cen

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public AddEditEquipmentResDto(ValidatorBean validator, AddEditEquipmentReqDto reqDto) {
        this.name = validator.validateField(reqDto, "name", reqDto.getName());
        this.description = validator.validateField(reqDto, "description", reqDto.getDescription());
        this.total = validator.validateField(reqDto, "total", reqDto.getTotal());
        this.size = validator.validateField(reqDto, "size", reqDto.getSize());
        this.types = validator.validateSelectField(reqDto, "type", this.types, reqDto.getType());
        this.brands = validator.validateSelectField(reqDto, "brand", this.brands, reqDto.getBrand());
        this.colors = validator.validateSelectField(reqDto, "color", this.colors, reqDto.getColor());
        this.genders = getSelectedGenderWithUnisex(reqDto.getGender());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void insertTypesSelects(List<FormSelectTupleDto> selects) {
        selects.add(0, new FormSelectTupleDto(true, "none", "wybierz lub dodaj typ..."));
        types.getSelects().addAll(selects);
        setSelectedField(types);
    }

    public void insertBrandsSelects(List<FormSelectTupleDto> selects) {
        selects.add(0, new FormSelectTupleDto(true, "none", "wybierz lub dodaj markę..."));
        brands.getSelects().addAll(selects);
        setSelectedField(brands);
    }

    public void insertColorsSelects(List<FormSelectTupleDto> selects) {
        selects.add(0, new FormSelectTupleDto(true, "none", "wybierz lub dodaj kolor..."));
        colors.getSelects().addAll(selects);
        setSelectedField(colors);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void setSelectedField(FormSelectsDto attr) {
        for (final FormSelectTupleDto select: attr.getSelects()) {
            if (select.getValue().equals(attr.getSelected())) select.setIsSelected("selected");
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // TODO: generacja metody toString()
}