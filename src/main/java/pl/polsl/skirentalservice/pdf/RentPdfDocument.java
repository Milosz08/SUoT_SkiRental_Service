/*
 * Copyright (c) 2023 by MILOSZ GILGA <http://miloszgilga.pl>
 * Silesian University of Technology
 *
 *  File name: RentPdfDocument.java
 *  Last modified: 09/02/2023, 01:26
 *  Project name: ski-rental-service
 *
 * This project was written for the purpose of a subject taken in the study of Computer Science.
 * This project is not commercial in any way and does not represent a viable business model
 * of the application. Project created for educational purposes only.
 */

package pl.polsl.skirentalservice.pdf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.layout.Document;
import com.itextpdf.kernel.pdf.PdfWriter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.LinkedHashMap;

import pl.polsl.skirentalservice.pdf.dto.RentPdfDocumentDataDto;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public class RentPdfDocument extends PdfHandler implements IPdfGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReturnPdfDocument.class);

    private final String uploadsDir;
    private String issuerIdentifier;
    private RentPdfDocumentDataDto rentPdfDto;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public RentPdfDocument(String uploadsDir, RentPdfDocumentDataDto rentPdfDto) {
        this.uploadsDir = uploadsDir;
        this.rentPdfDto = rentPdfDto;
    }

    public RentPdfDocument(String uploadsDir, String issuerIdentifier) {
        this.uploadsDir = uploadsDir;
        this.issuerIdentifier = issuerIdentifier;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void generate() throws RuntimeException {
        final String filePath = uploadsDir + File.separator + "rent-fvs" + File.separator;
        try {
            Files.createDirectories(Paths.get(filePath));
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage());
        }
        final String file = filePath + File.separator + rentPdfDto.getIssuedIdentifier().replaceAll("/", "") + ".pdf";
        try (final PdfWriter pdfWriter = new PdfWriter(file)) {
            final Document document = createDocument(pdfWriter);

            final Map<String, String> customerDetails = new LinkedHashMap<>();
            customerDetails.put("Imi?? i nazwisko", rentPdfDto.getFullName());
            customerDetails.put("Nr PESEL", rentPdfDto.getPesel());
            customerDetails.put("Nr telefonu", rentPdfDto.getPhoneNumber());
            customerDetails.put("Adres email", rentPdfDto.getEmail());
            customerDetails.put("Adres", rentPdfDto.getAddress());

            final Map<String, String> rentDocDetails = new LinkedHashMap<>();
            rentDocDetails.put("Data utworzenia dokumentu", rentPdfDto.getIssuedDate());
            rentDocDetails.put("Data wypo??yczenia", rentPdfDto.getRentDate());
            rentDocDetails.put("Przewidywana data zwrotu", rentPdfDto.getReturnDate());
            rentDocDetails.put("Przewidywany czas wypo??yczenia", rentPdfDto.getRentTime());
            rentDocDetails.put("Warto???? podatku VAT", rentPdfDto.getTax() + "%");

            document.add(generateHeader("wypo??yczenia", rentPdfDto.getIssuedIdentifier(), rentPdfDto.getIssuedDate()));
            document.add(generateHorizontalDivider());
            document.add(generateDetails(customerDetails, rentDocDetails, "wypo??yczenia"));
            document.add(generateHorizontalDivider());
            document.add(generateHeader("Wypo??yczone sprz??ty:"));
            document.add(generateEquipments(rentPdfDto.getEquipments()));
            generatePriceUnits(document, rentPdfDto.getPriceUnits());
            document.add(generatePricesDivider());
            generateSummaryPrices(document, rentPdfDto.getTotalSumPriceBrutto(), rentPdfDto.getTotalSumPriceNetto());
            document.add(generateHorizontalDivider());
            generateDescription(document, rentPdfDto.getDescription());

            document.close();
            LOGGER.info("Pdf FV rent file was successfully generated in: {}. Pdf data: {}", file, rentPdfDto);
        } catch (IOException ex) {
            LOGGER.error("Issue with reading pdf FV rent file with path: {}", file);
            throw new RuntimeException(ex.getMessage());
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void remove() throws RuntimeException {
        final String fileName = issuerIdentifier.replaceAll("/", "") + ".pdf";
        final File deletedPdf = new File(uploadsDir + File.separator + "rent-fvs" + File.separator + fileName);
        if (deletedPdf.delete()) {
            LOGGER.info("Pdf FV rent file was sucessfully removed from system.");
        } else {
            LOGGER.error("Unable to remove pdf FV rent file from system.");
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public String getPath() {
        final String issuerId = Objects.isNull(rentPdfDto) ? issuerIdentifier : rentPdfDto.getIssuedIdentifier();
        return uploadsDir + File.separator + "rent-fvs" + File.separator + issuerId.replaceAll("/", "") + ".pdf";
    }
}
