/*
 * Copyright (c) 2023 by MILOSZ GILGA <http://miloszgilga.pl>
 * Silesian University of Technology
 *
 *  File name: LoginServlet.java
 *  Last modified: 30/01/2023, 20:11
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
import at.favre.lib.crypto.bcrypt.BCrypt;

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
import pl.polsl.skirentalservice.dto.login.LoginFormReqDto;
import pl.polsl.skirentalservice.dto.login.LoginFormResDto;
import pl.polsl.skirentalservice.dto.logout.LogoutModalDto;
import pl.polsl.skirentalservice.core.ValidatorBean;
import pl.polsl.skirentalservice.core.db.HibernateUtil;
import pl.polsl.skirentalservice.dao.employer.EmployerDao;
import pl.polsl.skirentalservice.dao.employer.IEmployerDao;

import static pl.polsl.skirentalservice.exception.NotFoundException.UserNotFoundException;
import static pl.polsl.skirentalservice.exception.CredentialException.InvalidCredentialsException;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginServlet.class);
    private final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    @EJB private ValidatorBean validator;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        final LogoutModalDto logoutModal = Utils.getFromSessionAndDestroy(req, SessionAttribute.LOGOUT_MODAL.getName(),
            LogoutModalDto.class);
        req.setAttribute("logoutModal", logoutModal);
        req.setAttribute("alertData", Utils.getAndDestroySessionAlert(req, SessionAlert.LOGIN_PAGE_ALERT));
        req.setAttribute("loginData", Utils.getFromSessionAndDestroy(req, getClass().getName(), LoginFormResDto.class));
        req.setAttribute("title", PageTitle.LOGIN_PAGE.getName());
        req.getRequestDispatcher("/WEB-INF/pages/login.jsp").forward(req, res);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        final AlertTupleDto alert = new AlertTupleDto(true);
        final HttpSession httpSession = req.getSession();

        final LoginFormReqDto reqDto = new LoginFormReqDto(req);
        final LoginFormResDto resDto = new LoginFormResDto(validator, reqDto);
        if (validator.someFieldsAreInvalid(reqDto)) {
            httpSession.setAttribute(getClass().getName(), resDto);
            res.sendRedirect("/login");
            return;
        }
        try (final Session session = sessionFactory.openSession()) {
            try {
                session.beginTransaction();
                final IEmployerDao employerDao = new EmployerDao(session);

                final String password = employerDao.findEmployerPassword(reqDto.getLoginOrEmail()).orElseThrow(() -> {
                    throw new UserNotFoundException(reqDto, LOGGER);
                });
                if (!(BCrypt.verifyer().verify(reqDto.getPassword().toCharArray(), password).verified)) {
                    throw new InvalidCredentialsException(reqDto, LOGGER);
                }
                final var employer = employerDao.findLoggedEmployerDetails(reqDto.getLoginOrEmail()).orElseThrow(() -> {
                    throw new UserNotFoundException(reqDto, LOGGER);
                });
                session.getTransaction().commit();
                httpSession.setAttribute(SessionAttribute.LOGGED_USER_DETAILS.getName(), employer);
                httpSession.removeAttribute(getClass().getName());
                LOGGER.info("Successful logged on {} account. Account data: {}", employer.getRoleEng(), employer);
                if (employer.getIsFirstAccess() && employer.getRoleAlias().equals(UserRole.SELLER.getAlias())) {
                    res.sendRedirect("/first-access");
                    return;
                }
                res.sendRedirect("/" + employer.getRoleEng() + "/dashboard");
            } catch (RuntimeException ex) {
                Utils.onHibernateException(session, LOGGER, ex);
            }
        } catch (RuntimeException ex) {
            alert.setMessage(ex.getMessage());
            httpSession.setAttribute(getClass().getName(), resDto);
            httpSession.setAttribute(SessionAlert.LOGIN_PAGE_ALERT.getName(), alert);
            res.sendRedirect("/login");
        }
    }
}
