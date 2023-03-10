/*
 * Copyright (c) 2023 by MILOSZ GILGA <http://miloszgilga.pl>
 * Silesian University of Technology
 *
 *  File name: CommonEquipmentsServlet.java
 *  Last modified: 31/01/2023, 03:34
 *  Project name: ski-rental-service
 *
 * This project was written for the purpose of a subject taken in the study of Computer Science.
 * This project is not commercial in any way and does not represent a viable business model
 * of the application. Project created for educational purposes only.
 */

package pl.polsl.skirentalservice.domain.common.equipment;

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

import java.util.*;
import java.io.IOException;

import org.apache.commons.lang3.math.NumberUtils;

import pl.polsl.skirentalservice.util.*;
import pl.polsl.skirentalservice.dto.PageableDto;
import pl.polsl.skirentalservice.dto.AlertTupleDto;
import pl.polsl.skirentalservice.dto.login.LoggedUserDataDto;
import pl.polsl.skirentalservice.dto.equipment.EquipmentRecordResDto;
import pl.polsl.skirentalservice.core.db.HibernateUtil;
import pl.polsl.skirentalservice.dao.equipment.EquipmentDao;
import pl.polsl.skirentalservice.dao.equipment.IEquipmentDao;
import pl.polsl.skirentalservice.paging.filter.FilterColumn;
import pl.polsl.skirentalservice.paging.filter.FilterDataDto;
import pl.polsl.skirentalservice.paging.filter.ServletFilter;
import pl.polsl.skirentalservice.paging.pagination.ServletPagination;
import pl.polsl.skirentalservice.paging.sorter.ServletSorter;
import pl.polsl.skirentalservice.paging.sorter.SorterDataDto;
import pl.polsl.skirentalservice.paging.sorter.ServletSorterField;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@WebServlet(urlPatterns = { "/owner/equipments", "/seller/equipments" })
public class CommonEquipmentsServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonEquipmentsServlet.class);
    private final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    private final Map<String, ServletSorterField> sorterFieldMap = new HashMap<>();
    private final List<FilterColumn> filterFieldMap = new ArrayList<>();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void init() {
        sorterFieldMap.put("identity", new ServletSorterField("e.id"));
        sorterFieldMap.put("name", new ServletSorterField("e.name"));
        sorterFieldMap.put("type", new ServletSorterField("t.name"));
        sorterFieldMap.put("countInStore", new ServletSorterField("e.availableCount"));
        sorterFieldMap.put("pricePerHour", new ServletSorterField("e.pricePerHour"));
        sorterFieldMap.put("priceForNextHour", new ServletSorterField("e.priceForNextHour"));
        sorterFieldMap.put("pricePerDay", new ServletSorterField("e.pricePerDay"));
        sorterFieldMap.put("valueCost", new ServletSorterField("e.valueCost"));
        filterFieldMap.add(new FilterColumn("name", "nazwie", "e.name"));
        filterFieldMap.add(new FilterColumn("type", "typie sprz??tu", "t.name"));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        final int page = NumberUtils.toInt(Objects.requireNonNullElse(req.getParameter("page"), "1"), 1);
        final int total = NumberUtils.toInt(Objects.requireNonNullElse(req.getParameter("total"), "10"), 10);

        final HttpSession httpSession = req.getSession();
        final var userDataDto = (LoggedUserDataDto) httpSession.getAttribute(SessionAttribute.LOGGED_USER_DETAILS.getName());

        final ServletSorter servletSorter = new ServletSorter(req, "e.id", sorterFieldMap);
        final SorterDataDto sorterData = servletSorter.generateSortingJPQuery(SessionAttribute.EQUIPMENTS_LIST_SORTER);
        final ServletFilter servletFilter = new ServletFilter(req, filterFieldMap);
        final FilterDataDto filterData = servletFilter.generateFilterJPQuery(SessionAttribute.EQUIPMENTS_LIST_FILTER);

        final AlertTupleDto alert = Utils.getAndDestroySessionAlert(req, SessionAlert.COMMON_EQUIPMENTS_PAGE_ALERT);
        try (final Session session = sessionFactory.openSession()) {
            try {
                session.beginTransaction();

                final IEquipmentDao equipmentDao = new EquipmentDao(session);

                final Long totalEquipments = equipmentDao.findAllEquipmentsCount(filterData);
                final ServletPagination pagination = new ServletPagination(page, total, totalEquipments);
                if (pagination.checkIfIsInvalid()) throw new RuntimeException();

                final List<EquipmentRecordResDto> equipmentsList = equipmentDao
                    .findAllPageableEquipmentRecords(new PageableDto(filterData, sorterData, page, total));

                session.getTransaction().commit();
                req.setAttribute("pagesData", pagination);
                req.setAttribute("equipmentsData", equipmentsList);
            } catch (RuntimeException ex) {
                Utils.onHibernateException(session, LOGGER, ex);
            }
        } catch (RuntimeException ex) {
            alert.setType(AlertType.ERROR);
            alert.setMessage(ex.getMessage());
        }
        req.setAttribute("alertData", alert);
        req.setAttribute("sorterData", sorterFieldMap);
        req.setAttribute("filterData", filterData);
        req.setAttribute("title", PageTitle.COMMON_EQUIPMENTS_PAGE.getName());
        req.getRequestDispatcher("/WEB-INF/pages/" + userDataDto.getRoleEng() + "/equipment/" +
            userDataDto.getRoleEng() + "-equipments.jsp").forward(req, res);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        final int page = NumberUtils.toInt(Objects.requireNonNullElse(req.getParameter("page"), "1"), 1);
        final int total = NumberUtils.toInt(Objects.requireNonNullElse(req.getParameter("total"), "10"), 10);

        final HttpSession httpSession = req.getSession();
        final var userDataDto = (LoggedUserDataDto) httpSession.getAttribute(SessionAttribute.LOGGED_USER_DETAILS.getName());

        final ServletSorter servletSorter = new ServletSorter(req, "e.id", sorterFieldMap);
        servletSorter.generateSortingJPQuery(SessionAttribute.EQUIPMENTS_LIST_SORTER);
        final ServletFilter servletFilter = new ServletFilter(req, filterFieldMap);
        servletFilter.generateFilterJPQuery(SessionAttribute.EQUIPMENTS_LIST_FILTER);

        res.sendRedirect("/" + userDataDto.getRoleEng() + "/equipments?page=" + page + "&total=" + total);
    }
}
