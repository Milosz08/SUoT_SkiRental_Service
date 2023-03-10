/*
 * Copyright (c) 2023 by MILOSZ GILGA <http://miloszgilga.pl>
 * Silesian University of Technology
 *
 *  File name: OwnerAddEquipmentServlet.java
 *  Last modified: 09/02/2023, 01:15
 *  Project name: ski-rental-service
 *
 * This project was written for the purpose of a subject taken in the study of Computer Science.
 * This project is not commercial in any way and does not represent a viable business model
 * of the application. Project created for educational purposes only.
 */

package pl.polsl.skirentalservice.domain.owner.equipment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.modelmapper.ModelMapper;

import org.krysalis.barcode4j.impl.upcean.EAN13Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import org.apache.commons.lang3.RandomStringUtils;

import pl.polsl.skirentalservice.util.*;
import pl.polsl.skirentalservice.core.ConfigBean;
import pl.polsl.skirentalservice.core.ValidatorBean;
import pl.polsl.skirentalservice.core.db.HibernateUtil;
import pl.polsl.skirentalservice.core.ModelMapperGenerator;
import pl.polsl.skirentalservice.dto.AlertTupleDto;
import pl.polsl.skirentalservice.dto.equipment.AddEditEquipmentReqDto;
import pl.polsl.skirentalservice.dto.equipment.AddEditEquipmentResDto;
import pl.polsl.skirentalservice.dao.equipment.EquipmentDao;
import pl.polsl.skirentalservice.dao.equipment.IEquipmentDao;
import pl.polsl.skirentalservice.dao.equipment_brand.EquipmentBrandDao;
import pl.polsl.skirentalservice.dao.equipment_brand.IEquipmentBrandDao;
import pl.polsl.skirentalservice.dao.equipment_color.EquipmentColorDao;
import pl.polsl.skirentalservice.dao.equipment_color.IEquipmentColorDao;
import pl.polsl.skirentalservice.dao.equipment_type.EquipmentTypeDao;
import pl.polsl.skirentalservice.dao.equipment_type.IEquipmentTypeDao;
import pl.polsl.skirentalservice.entity.EquipmentEntity;
import pl.polsl.skirentalservice.entity.EquipmentTypeEntity;
import pl.polsl.skirentalservice.entity.EquipmentBrandEntity;
import pl.polsl.skirentalservice.entity.EquipmentColorEntity;

import static pl.polsl.skirentalservice.exception.AlreadyExistException.EquipmentAlreadyExistException;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@WebServlet("/owner/add-equipment")
public class OwnerAddEquipmentServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(OwnerAddEquipmentServlet.class);
    private final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
    private final ModelMapper modelMapper = ModelMapperGenerator.getModelMapper();

    @EJB private ValidatorBean validator;
    @EJB private ConfigBean config;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        final AlertTupleDto alert = Utils.getAndDestroySessionAlert(req, SessionAlert.OWNER_ADD_EQUIPMENT_PAGE_ALERT);
        var resDto = Utils.getFromSessionAndDestroy(req, getClass().getName(), AddEditEquipmentResDto.class);
        if (Objects.isNull(resDto)) resDto = new AddEditEquipmentResDto();

        try (final Session session = sessionFactory.openSession()) {
            try {
                session.beginTransaction();

                final IEquipmentTypeDao equipmentTypeDao = new EquipmentTypeDao(session);
                final IEquipmentBrandDao equipmentBrandDao = new EquipmentBrandDao(session);
                final IEquipmentColorDao equipmentColorDao = new EquipmentColorDao(session);

                resDto.insertTypesSelects(equipmentTypeDao.findAllEquipmentTypes());
                resDto.insertBrandsSelects(equipmentBrandDao.findAllEquipmentBrands());
                resDto.insertColorsSelects(equipmentColorDao.findAllEquipmentColors());

                session.getTransaction().commit();
            } catch (RuntimeException ex) {
                Utils.onHibernateException(session, LOGGER, ex);
            }
        } catch (RuntimeException ex) {
            alert.setActive(true);
            alert.setMessage(ex.getMessage());
        }
        req.setAttribute("alertData", alert);
        req.setAttribute("addEditEquipmentData", resDto);
        req.setAttribute("addEditText", "Dodaj");
        req.setAttribute(SessionAttribute.EQ_TYPES_MODAL_DATA.getName(),
            Utils.getAndDestroySessionModalData(req, SessionAttribute.EQ_TYPES_MODAL_DATA));
        req.setAttribute(SessionAttribute.EQ_BRANDS_MODAL_DATA.getName(),
            Utils.getAndDestroySessionModalData(req, SessionAttribute.EQ_BRANDS_MODAL_DATA));
        req.setAttribute(SessionAttribute.EQ_COLORS_MODAL_DATA.getName(),
            Utils.getAndDestroySessionModalData(req, SessionAttribute.EQ_COLORS_MODAL_DATA));
        req.setAttribute("title", PageTitle.OWNER_ADD_EQUIPMENT_PAGE.getName());
        req.getRequestDispatcher("/WEB-INF/pages/owner/equipment/owner-add-edit-equipment.jsp").forward(req, res);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        final HttpSession httpSession = req.getSession();
        final AlertTupleDto alert = new AlertTupleDto(true);
        final String loggedUser = Utils.getLoggedUserLogin(req);

        final AddEditEquipmentReqDto reqDto = new AddEditEquipmentReqDto(req);
        final AddEditEquipmentResDto resDto = new AddEditEquipmentResDto(validator, reqDto);
        if (validator.someFieldsAreInvalid(reqDto)) {
            httpSession.setAttribute(getClass().getName(), resDto);
            res.sendRedirect("/owner/add-equipment");
            return;
        }
        try (final Session session = sessionFactory.openSession()) {
            try {
                session.beginTransaction();
                final IEquipmentDao equipmentDao = new EquipmentDao(session);

                if (equipmentDao.checkIfEquipmentModelExist(reqDto.getModel(), null)) {
                    throw new EquipmentAlreadyExistException();
                }
                final EquipmentEntity persistNewEquipment = modelMapper.map(reqDto, EquipmentEntity.class);
                persistNewEquipment.setEquipmentType(session.get(EquipmentTypeEntity.class, reqDto.getType()));
                persistNewEquipment.setEquipmentBrand(session.get(EquipmentBrandEntity.class, reqDto.getBrand()));
                persistNewEquipment.setEquipmentColor(session.get(EquipmentColorEntity.class, reqDto.getColor()));
                persistNewEquipment.setAvailableCount(Integer.parseInt(reqDto.getCountInStore()));

                boolean barcodeExist;
                String generatedBarcode;
                do {
                    generatedBarcode = Utils.getBarcodeChecksum(RandomStringUtils.randomNumeric(12));
                    barcodeExist = equipmentDao.checkIfBarCodeExist(generatedBarcode);
                } while (barcodeExist);

                final EAN13Bean barcodeGenerator = new EAN13Bean();
                final var canvas = new BitmapCanvasProvider(250, BufferedImage.TYPE_BYTE_BINARY, true, 0);
                barcodeGenerator.generateBarcode(canvas, generatedBarcode);
                final BufferedImage barcodeBufferedImage = canvas.getBufferedImage();

                final File barCodesDir = new File(config.getUploadsDir() + File.separator + "bar-codes");
                barCodesDir.mkdir();
                final File outputFile = new File(barCodesDir, generatedBarcode + ".png");
                if (outputFile.createNewFile()) {
                    ImageIO.write(barcodeBufferedImage, "png", outputFile);
                } else throw new RuntimeException("Nieudane zapisanie kodu kreskowego sprz??tu.");

                persistNewEquipment.setBarcode(generatedBarcode);
                session.persist(persistNewEquipment);
                session.getTransaction().commit();
                alert.setType(AlertType.INFO);
                alert.setMessage(
                    "Nast??pi??o pomy??lne zapisanie nowego sprz??tu oraz wygenerowanie dla niego kodu kreskowego."
                );
                httpSession.setAttribute(SessionAlert.COMMON_EQUIPMENTS_PAGE_ALERT.getName(), alert);
                httpSession.removeAttribute(getClass().getName());
                LOGGER.info("Successful created new equipment with bar code image by: {}. Equipment data: {}",
                    loggedUser, reqDto);
                res.sendRedirect("/owner/equipments");
            } catch (RuntimeException ex) {
                Utils.onHibernateException(session, LOGGER, ex);
            }
        } catch (RuntimeException ex) {
            alert.setMessage(ex.getMessage());
            httpSession.setAttribute(getClass().getName(), resDto);
            httpSession.setAttribute(SessionAlert.OWNER_ADD_EQUIPMENT_PAGE_ALERT.getName(), alert);
            LOGGER.error("Unable to create new equipment. Cause: {}", ex.getMessage());
            res.sendRedirect("/owner/add-equipment");
        }
    }
}
