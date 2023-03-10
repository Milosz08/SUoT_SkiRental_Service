/*
 * Copyright (c) 2023 by MILOSZ GILGA <http://miloszgilga.pl>
 * Silesian University of Technology
 *
 *  File name: FirstAccessServlet.java
 *  Last modified: 30/01/2023, 18:16
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

import java.io.IOException;

import pl.polsl.skirentalservice.util.*;
import pl.polsl.skirentalservice.dto.AlertTupleDto;
import pl.polsl.skirentalservice.dto.login.LoggedUserDataDto;
import pl.polsl.skirentalservice.dto.first_access.FirstAccessReqDto;
import pl.polsl.skirentalservice.dto.first_access.FirstAccessResDto;
import pl.polsl.skirentalservice.core.ValidatorBean;
import pl.polsl.skirentalservice.core.db.HibernateUtil;
import pl.polsl.skirentalservice.core.ssh.SshSocketBean;
import pl.polsl.skirentalservice.dao.employer.EmployerDao;
import pl.polsl.skirentalservice.dao.employer.IEmployerDao;
import pl.polsl.skirentalservice.ssh.ExecCommandPerformer;
import pl.polsl.skirentalservice.ssh.IExecCommandPerformer;

import static pl.polsl.skirentalservice.exception.CredentialException.PasswordMismatchException;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@WebServlet("/first-access")
public class FirstAccessServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(FirstAccessServlet.class);
    private final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    @EJB private ValidatorBean validator;
    @EJB private SshSocketBean sshSocket;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setAttribute("alertData", Utils.getAndDestroySessionAlert(req, SessionAlert.FIRST_ACCESS_PAGE_ALERT));
        req.setAttribute("firstAccessData", Utils.getFromSessionAndDestroy(req, getClass().getName(), FirstAccessResDto.class));
        req.setAttribute("title", PageTitle.FIRST_ACCESS_PAGE.getName());
        req.getRequestDispatcher("/WEB-INF/pages/first-access.jsp").forward(req, res);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        final AlertTupleDto alert = new AlertTupleDto(true);

        final HttpSession httpSession = req.getSession();
        final var userDataDto = (LoggedUserDataDto) httpSession.getAttribute(SessionAttribute.LOGGED_USER_DETAILS.getName());

        final FirstAccessReqDto reqDto = new FirstAccessReqDto(req);
        final FirstAccessResDto resDto = new FirstAccessResDto(validator, reqDto);
        if (validator.someFieldsAreInvalid(reqDto)) {
            httpSession.setAttribute(getClass().getName(), resDto);
            res.sendRedirect("/first-access");
            return;
        }
        try (final Session session = sessionFactory.openSession()) {
            if (!reqDto.getPassword().equals(reqDto.getPasswordRep())) {
                throw new PasswordMismatchException("nowe has??o do konta", "powt??rz nowe has??o do konta");
            }
            if (!reqDto.getEmailPassword().equals(reqDto.getEmailPasswordRep())) {
                throw new PasswordMismatchException("nowe has??o do poczty", "powt??rz nowe has??o do poczty");
            }
            try {
                session.beginTransaction();

                final IEmployerDao employerDao = new EmployerDao(session);
                employerDao.updateEmployerFirstAccessPassword(Utils.generateHash(reqDto.getPassword()), userDataDto.getId());

                final IExecCommandPerformer commandPerformer = new ExecCommandPerformer(sshSocket);
                commandPerformer.updateMailboxPassword(userDataDto.getEmailAddress(), reqDto.getEmailPassword());

                alert.setType(AlertType.INFO);
                alert.setMessage("Twoje nowe has??o do konta oraz do poczty zosta??o pomy??lnie ustawione.");
                userDataDto.setIsFirstAccess(false);
                httpSession.setAttribute(SessionAlert.SELLER_DASHBOARD_PAGE_ALERT.getName(), alert);
                LOGGER.info("Successful changed default account password and mailbox password for user: {}", userDataDto);
                session.getTransaction().commit();
                httpSession.removeAttribute(getClass().getName());
                res.sendRedirect("/seller/dashboard");
            } catch (RuntimeException ex) {
                Utils.onHibernateException(session, LOGGER, ex);
            }
        } catch (RuntimeException ex) {
            alert.setMessage(ex.getMessage());
            httpSession.setAttribute(getClass().getName(), resDto);
            httpSession.setAttribute(SessionAlert.FIRST_ACCESS_PAGE_ALERT.getName(), alert);
            res.sendRedirect("/first-access");
        }
    }
}
