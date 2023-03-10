/*
 * Copyright (c) 2023 by MILOSZ GILGA <http://miloszgilga.pl>
 * Silesian University of Technology
 *
 *  File name: SellerPersistNewRentServlet.java
 *  Last modified: 09/02/2023, 01:20
 *  Project name: ski-rental-service
 *
 * This project was written for the purpose of a subject taken in the study of Computer Science.
 * This project is not commercial in any way and does not represent a viable business model
 * of the application. Project created for educational purposes only.
 */

package pl.polsl.skirentalservice.domain.seller.rent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.modelmapper.ModelMapper;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import pl.polsl.skirentalservice.util.*;
import pl.polsl.skirentalservice.dto.*;
import pl.polsl.skirentalservice.dto.login.LoggedUserDataDto;
import pl.polsl.skirentalservice.dto.customer.CustomerDetailsResDto;
import pl.polsl.skirentalservice.dto.rent.InMemoryRentDataDto;
import pl.polsl.skirentalservice.dto.rent.CartSingleEquipmentDataDto;
import pl.polsl.skirentalservice.core.ConfigBean;
import pl.polsl.skirentalservice.core.db.HibernateUtil;
import pl.polsl.skirentalservice.core.ModelMapperGenerator;
import pl.polsl.skirentalservice.core.mail.MailSocketBean;
import pl.polsl.skirentalservice.core.mail.MailRequestPayload;
import pl.polsl.skirentalservice.dao.employer.EmployerDao;
import pl.polsl.skirentalservice.dao.employer.IEmployerDao;
import pl.polsl.skirentalservice.dao.equipment.EquipmentDao;
import pl.polsl.skirentalservice.dao.equipment.IEquipmentDao;
import pl.polsl.skirentalservice.entity.*;
import pl.polsl.skirentalservice.pdf.RentPdfDocument;
import pl.polsl.skirentalservice.pdf.dto.RentPdfDocumentDataDto;

import static pl.polsl.skirentalservice.exception.AlreadyExistException.TooMuchEquipmentsException;
import static pl.polsl.skirentalservice.exception.NotFoundException.AnyEquipmentsInCartNotFoundException;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@WebServlet("/seller/persist-new-rent")
public class SellerPersistNewRentServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(SellerPersistNewRentServlet.class);
    private final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
    private final ModelMapper modelMapper = ModelMapperGenerator.getModelMapper();

    @EJB private MailSocketBean mailSocketBean;
    @EJB private ConfigBean config;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        final HttpSession httpSession = req.getSession();
        final var rentData = (InMemoryRentDataDto) httpSession.getAttribute(SessionAttribute.INMEMORY_NEW_RENT_DATA.getName());
        if (Objects.isNull(rentData)) {
            res.sendRedirect("/seller/customers");
            return;
        }
        final AlertTupleDto alert = new AlertTupleDto(true);
        final String loggedUser = Utils.getLoggedUserLogin(req);
        final var loggedEmployer = (LoggedUserDataDto) httpSession.getAttribute(SessionAttribute.LOGGED_USER_DETAILS.getName());

        try (final Session session = sessionFactory.openSession()) {
            if (rentData.getEquipments().isEmpty()) throw new AnyEquipmentsInCartNotFoundException();
            try {
                session.beginTransaction();

                final IEmployerDao employerDao = new EmployerDao(session);
                final IEquipmentDao equipmentDao = new EquipmentDao(session);

                final RentEntity rent = modelMapper.map(rentData, RentEntity.class);
                rent.setEquipments(new HashSet<>());

                final Set<RentEquipmentEntity> equipmentEntities = new HashSet<>();
                for (final CartSingleEquipmentDataDto cartData : rentData.getEquipments()) {
                    final Integer eqCount = equipmentDao.findAllEquipmentsInCartCount(cartData.getId());
                    if (eqCount < Integer.parseInt(cartData.getCount())) throw new TooMuchEquipmentsException();

                    final RentEquipmentEntity equipment = modelMapper.map(cartData, RentEquipmentEntity.class);
                    final EquipmentEntity refEquipment = session.get(EquipmentEntity.class, cartData.getId());
                    equipment.setId(null);
                    equipment.setTotalPrice(cartData.getPriceUnits().getTotalPriceNetto());
                    equipment.setDepositPrice(cartData.getPriceUnits().getTotalDepositPriceNetto());
                    equipment.setEquipment(refEquipment);
                    equipment.setRent(rent);
                    equipmentEntities.add(equipment);

                    equipmentDao.decreaseAvailableSelectedEquipmentCount(cartData.getId(), cartData.getCount());
                }
                rent.setId(null);
                rent.setIssuedDateTime(LocalDateTime.parse(rentData.getIssuedDateTime().replace(' ', 'T')));
                rent.setTotalPrice(rentData.getPriceUnits().getTotalPriceNetto());
                rent.setTotalDepositPrice(rentData.getPriceUnits().getTotalDepositPriceNetto());

                rent.setStatus(RentStatus.RENTED);
                rent.setCustomer(session.get(CustomerEntity.class, rentData.getCustomerId()));
                rent.setEmployer(session.get(EmployerEntity.class, loggedEmployer.getId()));
                rent.setEquipments(equipmentEntities);

                final RentReturnEmailPayloadDataDto emailPayload = modelMapper.map(rentData, RentReturnEmailPayloadDataDto.class);
                modelMapper.map(rentData.getCustomerDetails(), emailPayload);

                final PriceUnitsDto priceUnits = rentData.getPriceUnits();
                final BigDecimal totalWithTax = priceUnits.getTotalPriceBrutto().add(priceUnits.getTotalDepositPriceBrutto());
                emailPayload.setTotalPriceWithDepositBrutto(totalWithTax);
                emailPayload.setRentTime(rentData.getDays() +  " dni, " + rentData.getHours() + " godzin");

                for (final CartSingleEquipmentDataDto equipmentDataDto : rentData.getEquipments()) {
                    final var equipment = modelMapper.map(equipmentDataDto, EmailEquipmentPayloadDataDto.class);
                    emailPayload.getRentEquipments().add(equipment);
                }
                final String emailTopic = "SkiRent Service | Nowe wypo??yczenie: " + rentData.getIssuedIdentifier();
                final String description = Objects.isNull(rentData.getDescription()) ? "<i>Brak danych</i>" : rentData.getDescription();

                final Map<String, Object> templateVars = new HashMap<>();
                templateVars.put("rentIdentifier", rentData.getIssuedIdentifier());
                templateVars.put("additionalDescription", description);
                templateVars.put("data", emailPayload);

                final RentPdfDocumentDataDto rentPdfDataDto = modelMapper.map(rentData, RentPdfDocumentDataDto.class);
                final CustomerDetailsResDto customerDetails = rentData.getCustomerDetails();
                rentPdfDataDto.setTotalSumPriceNetto(priceUnits.getTotalPriceNetto()
                    .add(priceUnits.getTotalDepositPriceNetto()).toString());
                rentPdfDataDto.setTotalSumPriceBrutto(priceUnits.getTotalPriceBrutto()
                    .add(priceUnits.getTotalDepositPriceBrutto()).toString());

                modelMapper.map(rentData.getCustomerDetails(), rentPdfDataDto);
                rentPdfDataDto.setAddress(customerDetails.address() + ", " + customerDetails.cityWithPostCode());
                rentPdfDataDto.setRentTime(rentData.getDays() +  " dni, " + rentData.getHours() + " godzin");

                final RentPdfDocument rentPdfDocument = new RentPdfDocument(config.getUploadsDir(), rentPdfDataDto);
                rentPdfDocument.generate();

                final MailRequestPayload customerPayload = MailRequestPayload.builder()
                    .messageResponder(rentData.getCustomerDetails().fullName())
                    .subject(emailTopic)
                    .templateName("add-new-rent-customer.template.ftl")
                    .templateVars(templateVars)
                    .attachmentsPaths(Set.of(rentPdfDocument.getPath()))
                    .build();
                mailSocketBean.sendMessage(rentData.getCustomerDetails().email(), customerPayload, req);
                LOGGER.info("Successful send rent email message for customer. Payload: {}", customerPayload);

                final MailRequestPayload employerPayload = MailRequestPayload.builder()
                    .messageResponder(loggedEmployer.getFullName())
                    .subject(emailTopic)
                    .templateName("add-new-rent-employer.template.ftl")
                    .templateVars(templateVars)
                    .attachmentsPaths(Set.of(rentPdfDocument.getPath()))
                    .build();
                mailSocketBean.sendMessage(loggedEmployer.getEmailAddress(), employerPayload, req);
                LOGGER.info("Successful send rent email message for employer. Payload: {}", employerPayload);

                final Map<String, Object> ownerTemplateVars = new HashMap<>(templateVars);
                ownerTemplateVars.put("employerFullName", loggedEmployer.getFullName());

                final MailRequestPayload ownerPayload = MailRequestPayload.builder()
                    .subject(emailTopic)
                    .templateName("add-new-rent-owner.template.ftl")
                    .templateVars(ownerTemplateVars)
                    .attachmentsPaths(Set.of(rentPdfDocument.getPath()))
                    .build();
                for (final OwnerMailPayloadDto owner : employerDao.findAllEmployersMailSenders()) {
                    ownerPayload.setMessageResponder(owner.fullName());
                    mailSocketBean.sendMessage(owner.email(), ownerPayload, req);
                }
                LOGGER.info("Successful send rent email message for owner/owners. Payload: {}", ownerPayload);

                session.persist(rent);
                session.getTransaction().commit();
                alert.setType(AlertType.INFO);
                alert.setMessage(
                    "Wypo??yczenie o numerze <strong>" + rentData.getIssuedIdentifier() + "</strong> zosta??o pomy??lnie " +
                    "z??o??one w systemie. Szczeg????y z??o??onego wypo??yczenia znajdziesz r??wnie?? w wiadomo??ci email."
                );
                httpSession.setAttribute(SessionAlert.COMMON_RENTS_PAGE_ALERT.getName(), alert);
                httpSession.removeAttribute(SessionAttribute.INMEMORY_NEW_RENT_DATA.getName());
                LOGGER.info("Successfuly persist new rent by: {} in database. Rent data: {}", loggedUser, rentData);
                res.sendRedirect("/seller/rents");
            } catch (RuntimeException ex) {
                Utils.onHibernateException(session, LOGGER, ex);
            }
        } catch (RuntimeException ex) {
            alert.setMessage(ex.getMessage());
            httpSession.setAttribute(SessionAlert.SELLER_COMPLETE_RENT_PAGE_ALERT.getName(), alert);
            LOGGER.error("Failure persist new rent by: {} in database. Rent data: {}. Cause: {}", loggedUser, rentData,
                ex.getMessage());
            res.sendRedirect("/seller/complete-rent-equipments");
        }
    }
}
