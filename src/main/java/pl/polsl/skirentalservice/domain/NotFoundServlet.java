/*
 * Copyright (c) 2023 by MILOSZ GILGA <http://miloszgilga.pl>
 * Silesian University of Technology
 *
 *  File name: NotFoundServlet.java
 *  Last modified: 20/01/2023, 05:11
 *  Project name: ski-rental-service
 *
 * This project was written for the purpose of a subject taken in the study of Computer Science.
 * This project is not commercial in any way and does not represent a viable business model
 * of the application. Project created for educational purposes only.
 */

package pl.polsl.skirentalservice.domain;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import pl.polsl.skirentalservice.util.PageTitle;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@WebServlet("/404")
public class NotFoundServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setAttribute("title", PageTitle.NOT_FOUND_4O4_PAGE.getName());
        req.getRequestDispatcher("/WEB-INF/pages/_not-found.jsp").forward(req, res);
    }
}
