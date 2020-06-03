package com.gnomon.epsos.servlet;

import com.gnomon.epsos.model.Patient;
import com.gnomon.epsos.service.EpsosHelperService;
import com.liferay.portal.model.User;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.opensaml.saml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;

public class ConsentServlet extends HttpServlet {

    private static final long serialVersionUID = 5646088170034248115L;
    private final Logger logger = LoggerFactory.getLogger(ConsentServlet.class);

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {

        logger.info("Servlet Consent Document");

        try (OutputStream outputStream = response.getOutputStream()) {
            byte[] pdf;
            HttpSession session = request.getSession();
            //  Checking validity of the assertions (HCP and TRC)
            Assertion hcpAssertion = (Assertion) session.getAttribute("hcpAssertion");
            logger.info("HCP Assertions: '{}'", hcpAssertion.getID());

            User user = (User) session.getAttribute("user");
            logger.info("User: '{}'", user.getEmailAddress());

            Patient patient = (Patient) session.getAttribute("patient");
            pdf = EpsosHelperService.getConsentReport(user.getLanguageId(), user.getFullName(), patient);

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=architect.pdf");
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expires", 0);
            response.setHeader("Pragma", "No-cache");

            outputStream.write(pdf);
            outputStream.flush();

        } catch (Exception ex) {

            logger.error("Exception: '{}'", ex.getMessage(), ex);
            response.setContentType("text/html");
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expires", 0);
            response.setHeader("Pragma", "No-cache");

            try (OutputStream outputStream = response.getOutputStream()) {
                outputStream.write(ex.getMessage().getBytes());
                outputStream.flush();
            } catch (IOException e) {
                logger.error(ExceptionUtils.getStackTrace(e));
            }
        }
    }
}
