/*
 * Copyright (c) 2023 by MILOSZ GILGA <http://miloszgilga.pl>
 * Silesian University of Technology
 *
 *  File name: OwnerDeleteEquipmentServlet.java
 *  Last modified: 06/02/2023, 19:49
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

import pl.polsl.skirentalservice.util.Utils;
import pl.polsl.skirentalservice.util.AlertType;
import pl.polsl.skirentalservice.util.SessionAlert;
import pl.polsl.skirentalservice.dto.AlertTupleDto;
import pl.polsl.skirentalservice.core.ConfigBean;
import pl.polsl.skirentalservice.core.db.HibernateUtil;
import pl.polsl.skirentalservice.dao.equipment.EquipmentDao;
import pl.polsl.skirentalservice.dao.equipment.IEquipmentDao;
import pl.polsl.skirentalservice.entity.EquipmentEntity;

import static pl.polsl.skirentalservice.exception.NotFoundException.EquipmentNotFoundException;
import static pl.polsl.skirentalservice.exception.AlreadyExistException.EquipmenHasOpenedRentsException;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@WebServlet("/owner/delete-equipment")
public class OwnerDeleteEquipmentServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(OwnerDeleteEquipmentServlet.class);
    private final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    @EJB private ConfigBean config;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        final String equipmentId = req.getParameter("id");
        final AlertTupleDto alert = new AlertTupleDto(true);
        final String loggedUser = Utils.getLoggedUserLogin(req);

        final HttpSession httpSession = req.getSession();
        try (final Session session = sessionFactory.openSession()) {
            try {
                session.beginTransaction();
                final IEquipmentDao equipmentDao = new EquipmentDao(session);

                if (equipmentDao.checkIfEquipmentHasOpenedRents(equipmentId)) {
                    throw new EquipmenHasOpenedRentsException();
                }
                final EquipmentEntity equipmentEntity = session.getReference(EquipmentEntity.class, equipmentId);
                if (Objects.isNull(equipmentEntity)) throw new EquipmentNotFoundException(equipmentId);

                final String uploadsDir = config.getUploadsDir() + File.separator + "bar-codes";
                final File barcodeFile = new File(uploadsDir, equipmentEntity.getBarcode() + ".png");
                if (barcodeFile.exists()) {
                    if (!barcodeFile.delete()) throw new RuntimeException("Nieudane usuni??cie kodu kreskowego.");
                }
                session.remove(equipmentEntity);
                alert.setType(AlertType.INFO);
                alert.setMessage(
                    "Pomy??lnie usuni??to sprz??t narciarski z ID <strong>#" + equipmentId + "</strong> z systemu."
                );
                session.getTransaction().commit();
                LOGGER.info("Equipment with id: {} was succesfuly removed from system by {}.", equipmentId, loggedUser);
            } catch (RuntimeException ex) {
                Utils.onHibernateException(session, LOGGER, ex);
            }
        } catch (RuntimeException ex) {
            alert.setMessage(ex.getMessage());
            LOGGER.error("Unable to remove equipment with id: {} by: {}. Cause: {}", loggedUser, equipmentId,
                ex.getMessage());
        }
        httpSession.setAttribute(SessionAlert.COMMON_EQUIPMENTS_PAGE_ALERT.getName(), alert);
        res.sendRedirect("/owner/equipments");
    }
}
