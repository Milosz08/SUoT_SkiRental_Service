/*
 * Copyright (c) 2023 by MILOSZ GILGA <http://miloszgilga.pl>
 * Silesian University of Technology
 *
 *  File name: OwnerDeleteEquipmentColorServlet.java
 *  Last modified: 30/01/2023, 18:14
 *  Project name: ski-rental-service
 *
 * This project was written for the purpose of a subject taken in the study of Computer Science.
 * This project is not commercial in any way and does not represent a viable business model
 * of the application. Project created for educational purposes only.
 */

package pl.polsl.skirentalservice.domain.owner.attribute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import pl.polsl.skirentalservice.util.Utils;
import pl.polsl.skirentalservice.util.AlertType;
import pl.polsl.skirentalservice.util.SessionAttribute;
import pl.polsl.skirentalservice.dto.AlertTupleDto;
import pl.polsl.skirentalservice.dto.attribute.AttributeModalResDto;
import pl.polsl.skirentalservice.core.db.HibernateUtil;
import pl.polsl.skirentalservice.dao.equipment_color.EquipmentColorDao;
import pl.polsl.skirentalservice.dao.equipment_color.IEquipmentColorDao;

import static pl.polsl.skirentalservice.exception.NotFoundException.EquipmentColorNotFoundException;
import static pl.polsl.skirentalservice.exception.AlreadyExistException.EquipmentColorHasConnectionsException;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@WebServlet("/owner/delete-equipment-color")
public class OwnerDeleteEquipmentColorServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(OwnerDeleteEquipmentColorServlet.class);
    private final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        final String colorId = req.getParameter("id");
        final String loggedUser = Utils.getLoggedUserLogin(req);

        final AlertTupleDto alert = new AlertTupleDto(true);
        final HttpSession httpSession = req.getSession();
        final AttributeModalResDto resDto = new AttributeModalResDto();

        resDto.setAlert(alert);
        resDto.setModalImmediatelyOpen(true);

        try (final Session session = sessionFactory.openSession()) {
            try {
                session.beginTransaction();
                final IEquipmentColorDao equipmentDetailsDao = new EquipmentColorDao(session);

                final String deletedColor = equipmentDetailsDao.getEquipmentColorNameById(colorId).orElseThrow(() -> {
                    throw new EquipmentColorNotFoundException();
                });

                resDto.getActiveFirstPage().setActive(false);
                resDto.getActiveSecondPage().setActive(true);

                if (equipmentDetailsDao.checkIfEquipmentColorHasAnyConnections(colorId)) {
                    throw new EquipmentColorHasConnectionsException();
                }
                equipmentDetailsDao.deleteEquipmentColorById(colorId);

                alert.setType(AlertType.INFO);
                alert.setMessage(
                    "Usuwanie koloru sprz??tu narciarskiego: <strong>" + deletedColor + "</strong> zako??czone sukcesem."
                );
                session.getTransaction().commit();
                LOGGER.info("Successful deleted equipment color by: {}. Color: {}", loggedUser, deletedColor);
            } catch (RuntimeException ex) {
                Utils.onHibernateException(session, LOGGER, ex);
            }
        } catch (RuntimeException ex) {
            alert.setActive(true);
            alert.setMessage(ex.getMessage());
            LOGGER.error("Failure delete equipment color by: {}. Cause: {}", loggedUser, ex.getMessage());
        }
        httpSession.setAttribute(SessionAttribute.EQ_COLORS_MODAL_DATA.getName(), resDto);
        res.sendRedirect(StringUtils.defaultIfBlank(req.getParameter("redirect"), "/owner/add-equipment"));
    }
}
