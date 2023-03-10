/*
 * Copyright (c) 2023 by MILOSZ GILGA <http://miloszgilga.pl>
 * Silesian University of Technology
 *
 *  File name: ReturnPdfDocument.java
 *  Last modified: 09/02/2023, 01:02
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

import pl.polsl.skirentalservice.pdf.dto.ReturnPdfDocumentDataDto;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public class ReturnPdfDocument extends PdfHandler implements IPdfGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReturnPdfDocument.class);

    private final String uploadsDir;
    private String issuerIdentifier;
    private ReturnPdfDocumentDataDto returnDataDto;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public ReturnPdfDocument(String uploadsDir, ReturnPdfDocumentDataDto returnDataDto) {
        this.uploadsDir = uploadsDir;
        this.returnDataDto = returnDataDto;
    }

    public ReturnPdfDocument(String uploadsDir, String issuerIdentifier) {
        this.uploadsDir = uploadsDir;
        this.issuerIdentifier = issuerIdentifier;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void generate() throws RuntimeException {
        final String filePath = uploadsDir + File.separator + "return-fvs" + File.separator;
        try {
            Files.createDirectories(Paths.get(filePath));
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage());
        }
        final String file = filePath + File.separator + returnDataDto.getIssuedIdentifier().replaceAll("/", "") + ".pdf";
        try (final PdfWriter pdfWriter = new PdfWriter(file)) {
            final Document document = createDocument(pdfWriter);

            final Map<String, String> customerDetails = new LinkedHashMap<>();
            customerDetails.put("Imi?? i nazwisko", returnDataDto.getFullName());
            customerDetails.put("Nr PESEL", returnDataDto.getPesel());
            customerDetails.put("Nr telefonu", returnDataDto.getPhoneNumber());
            customerDetails.put("Adres email", returnDataDto.getEmail());
            customerDetails.put("Adres", returnDataDto.getAddress());

            final Map<String, String> returnDocDetails = new LinkedHashMap<>();
            returnDocDetails.put("Data wypo??yczenia", returnDataDto.getRentDate().replace("T", " "));
            returnDocDetails.put("Data zwrotu", returnDataDto.getReturnDate().replace("T", " "));
            returnDocDetails.put("Czas wypo??yczenia", returnDataDto.getRentTime());
            returnDocDetails.put("Warto???? podatku VAT", returnDataDto.getTax() + "%");

            document.add(generateHeader("zwrotu", returnDataDto.getIssuedIdentifier(),
                returnDataDto.getReturnDate().replace("T", " ")));
            document.add(generateHorizontalDivider());
            document.add(generateDetails(customerDetails, returnDocDetails, "zwrotu"));
            document.add(generateHorizontalDivider());
            document.add(generateHeader("Zwr??cone sprz??ty:"));
            document.add(generateEquipments(returnDataDto.getEquipments()));
            generatePriceUnits(document, returnDataDto.getPriceUnits());
            document.add(generatePricesDivider());
            generateSummaryPrices(document, returnDataDto.getTotalSumPriceBrutto(), returnDataDto.getTotalSumPriceNetto());
            document.add(generateHorizontalDivider());
            generateDescription(document, returnDataDto.getDescription());

            document.close();
            LOGGER.info("Pdf FV return file was successfully generated in: {}. Pdf data: {}", file, returnDataDto);
        } catch (IOException ex) {
            LOGGER.error("Issue with reading pdf FV return file with path: {}", file);
            throw new RuntimeException(ex.getMessage());
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void remove() throws RuntimeException {
        final String fileName = issuerIdentifier.replaceAll("/", "") + ".pdf";
        final File deletedPdf = new File(uploadsDir + File.separator + "return-fvs" + File.separator + fileName);
        if (deletedPdf.delete()) {
            LOGGER.info("Pdf FV return file was sucessfully removed from system.");
        } else {
            LOGGER.error("Unable to remove pdf FV return file from system.");
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public String getPath() {
        final String issuerId = Objects.isNull(returnDataDto) ? issuerIdentifier : returnDataDto.getIssuedIdentifier();
        return uploadsDir + File.separator + "return-fvs" + File.separator + issuerId.replaceAll("/", "") + ".pdf";
    }
}
