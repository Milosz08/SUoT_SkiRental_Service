/*
 * Copyright (c) 2023 by multiple authors
 * Silesian University of Technology
 *
 *  File name: RedirectOnPostMethodRequestFilter.java
 *  Last modified: 24/01/2023, 13:06
 *  Project name: ski-rental-service
 *
 * This project was written for the purpose of a subject taken in the study of Computer Science.
 * This project is not commercial in any way and does not represent a viable business model
 * of the application. Project created for educational purposes only.
 */

package pl.polsl.skirentalservice.filter.method;

import jakarta.servlet.http.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.annotation.*;
import jakarta.servlet.ServletException;

import java.io.IOException;

import static org.apache.commons.lang3.StringUtils.isNumeric;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@WebFilter(urlPatterns = {
    "/owner/delete-equipment-type",
}, initParams = @WebInitParam(name = "mood", value = "awake"))
public class RedirectOnPostMethodRequestFilter extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
        throws IOException, ServletException {
        final String userId = req.getParameter("id");
        if (req.getMethod().equals("POST") || !isNumeric(userId)) {
            res.sendRedirect("/owner/add-equipment");
            return;
        }
        chain.doFilter(req, res);
    }
}