package com.gnomon.epsos.servlet;

import com.gnomon.epsos.model.Patient;
import com.gnomon.epsos.service.EpsosHelperService;
import com.liferay.portal.model.User;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.opensaml.saml2.core.Assertion;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsentServlet.class);

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) {

        LOGGER.info("Servlet Consent Document");

        try (OutputStream outputStream = res.getOutputStream()) {
            byte[] pdf;
            HttpSession session = req.getSession();

            Assertion hcpAssertion = (Assertion) session.getAttribute("hcpAssertion");
            LOGGER.info("HCP Assertions: '{}'", hcpAssertion.getID());

            User user = (User) session.getAttribute("user");
            LOGGER.info("User: '{}'", user.getEmailAddress());

            Patient patient = (Patient) session.getAttribute("patient");
            pdf = EpsosHelperService.getConsentReport(user.getLanguageId(), user.getFullName(), patient);

            res.setContentType("application/pdf");
            res.setHeader("Content-Disposition", "inline; filename=architect.pdf");
            res.setHeader("Cache-Control", "no-cache");
            res.setDateHeader("Expires", 0);
            res.setHeader("Pragma", "No-cache");

            outputStream.write(pdf);
            outputStream.flush();

        } catch (Exception ex) {

            LOGGER.error("Exception: '{}'", ex.getMessage(), ex);
            res.setContentType("text/html");
            res.setHeader("Cache-Control", "no-cache");
            res.setDateHeader("Expires", 0);
            res.setHeader("Pragma", "No-cache");

            try (OutputStream outputStream = res.getOutputStream()) {
                outputStream.write(ex.getMessage().getBytes());
                outputStream.flush();
            } catch (IOException e) {
                LOGGER.error(ExceptionUtils.getStackTrace(e));
            }
        }
    }
}
