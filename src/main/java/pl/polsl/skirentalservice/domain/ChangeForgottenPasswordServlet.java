/*
 * Copyright (c) 2023 by MILOSZ GILGA <http://miloszgilga.pl>
 * Silesian University of Technology
 *
 *  File name: ChangeForgottenPasswordServlet.java
 *  Last modified: 06/02/2023, 18:14
 *  Project name: ski-rental-service
 *
 * This project was written for the purpose of a subject taken in the study of Computer Science.
 * This project is not commercial in any way and does not represent a viable business model
 * of the application. Project created for educational purposes only.
 */

package pl.polsl.skirentalservice.domain;

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

import java.util.Objects;
import java.io.IOException;

import pl.polsl.skirentalservice.util.Utils;
import pl.polsl.skirentalservice.util.AlertType;
import pl.polsl.skirentalservice.util.PageTitle;
import pl.polsl.skirentalservice.util.SessionAlert;
import pl.polsl.skirentalservice.dto.AlertTupleDto;
import pl.polsl.skirentalservice.dto.change_password.ChangeForgottenPasswordReqDto;
import pl.polsl.skirentalservice.dto.change_password.ChangeForgottenPasswordResDto;
import pl.polsl.skirentalservice.core.ValidatorBean;
import pl.polsl.skirentalservice.core.db.HibernateUtil;
import pl.polsl.skirentalservice.dao.employer.EmployerDao;
import pl.polsl.skirentalservice.dao.employer.IEmployerDao;
import pl.polsl.skirentalservice.dao.ota_token.OtaTokenDao;
import pl.polsl.skirentalservice.dao.ota_token.IOtaTokenDao;

import static pl.polsl.skirentalservice.exception.CredentialException.OtaTokenNotFoundException;
import static pl.polsl.skirentalservice.exception.CredentialException.PasswordMismatchException;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@WebServlet("/change-forgotten-password")
public class ChangeForgottenPasswordServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChangeForgottenPasswordServlet.class);
    private final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    @EJB private ValidatorBean validator;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        AlertTupleDto alert = Utils.getAndDestroySessionAlert(req, SessionAlert.CHANGE_FORGOTTEN_PASSWORD_PAGE_ALERT);
        if (Objects.isNull(alert)) alert = new AlertTupleDto();
        final String token = req.getParameter("token");

        try (final Session session = sessionFactory.openSession()) {
            try {
                session.beginTransaction();
                final IOtaTokenDao otaTokenDao = new OtaTokenDao(session);

                final var details = otaTokenDao.findTokenRelatedToEmployer(token).orElseThrow(() -> {
                    throw new OtaTokenNotFoundException(req, token, LOGGER);
                });
                req.setAttribute("employerData", details);
                session.getTransaction().commit();
            } catch (RuntimeException ex) {
                Utils.onHibernateException(session, LOGGER, ex);
            }
        } catch (RuntimeException ex) {
            alert.setActive(true);
            alert.setDisableContent(true);
            alert.setMessage(ex.getMessage());
        }
        req.setAttribute("changePassData", Utils.getFromSessionAndDestroy(req, getClass().getName(),
            ChangeForgottenPasswordResDto.class));
        req.setAttribute("alertData", alert);
        req.setAttribute("title", PageTitle.CHANGE_FORGOTTEN_PASSWORD_PAGE.getName());
        req.getRequestDispatcher("/WEB-INF/pages/change-forgotten-password.jsp").forward(req, res);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        final String token = req.getParameter("token");
        final AlertTupleDto alert = new AlertTupleDto(true);
        final HttpSession httpSession = req.getSession();

        final ChangeForgottenPasswordReqDto reqDto = new ChangeForgottenPasswordReqDto(req);
        final ChangeForgottenPasswordResDto resDto = new ChangeForgottenPasswordResDto(validator, reqDto);
        if (validator.someFieldsAreInvalid(reqDto)) {
            httpSession.setAttribute(getClass().getName(), resDto);
            res.sendRedirect("/change-forgotten-password?token=" + token);
            return;
        }
        try (final Session session = sessionFactory.openSession()) {
            if (!reqDto.getPassword().equals(reqDto.getPasswordRepeat())) {
                throw new PasswordMismatchException("has??o", "powt??rz has??o");
            }
            try {
                session.beginTransaction();

                final IOtaTokenDao otaTokenDao = new OtaTokenDao(session);
                final IEmployerDao employerDao = new EmployerDao(session);

                final var details = otaTokenDao.findTokenDetails(token).orElseThrow(() -> {
                    throw new OtaTokenNotFoundException(req, token, LOGGER);
                });
                employerDao.updateEmployerPassword(Utils.generateHash(reqDto.getPassword()), details.id());
                otaTokenDao.manuallyExpiredOtaToken(details.tokenId());

                session.getTransaction().commit();
                alert.setMessage("Has??o do Twojego konta zosta??o pomy??lnie zmienione.");
                alert.setType(AlertType.INFO);
                LOGGER.info("Successful change password for employer account. Details: {}", details);
                httpSession.setAttribute(SessionAlert.LOGIN_PAGE_ALERT.getName(), alert);
                httpSession.removeAttribute(getClass().getName());
                res.sendRedirect("/login");
            } catch (RuntimeException ex) {
                Utils.onHibernateException(session, LOGGER, ex);
            }
        } catch (RuntimeException ex) {
            if (!(ex instanceof PasswordMismatchException)) alert.setDisableContent(true);
            alert.setMessage(ex.getMessage());
            httpSession.setAttribute(getClass().getName(), resDto);
            httpSession.setAttribute(SessionAlert.CHANGE_FORGOTTEN_PASSWORD_PAGE_ALERT.getName(), alert);
            res.sendRedirect("/change-forgotten-password?token=" + token);
        }
    }
}
