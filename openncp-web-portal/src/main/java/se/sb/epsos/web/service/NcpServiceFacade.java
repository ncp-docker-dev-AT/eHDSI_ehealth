/**
 * Copyright 2011-2013 Apotekens Service AB <epsos@apotekensservice.se>
 * <p>
 * This file is part of epSOS-WEB.
 * <p>
 * epSOS-WEB is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * <p>
 * epSOS-WEB is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with epSOS-WEB. If not, see http://www.gnu.org/licenses/.
 **/
package se.sb.epsos.web.service;

import se.sb.epsos.web.auth.AuthenticatedUser;
import se.sb.epsos.web.model.*;

import java.util.List;

public interface NcpServiceFacade {

    String about();

    void initUser(AuthenticatedUser userDetails) throws NcpServiceException;

    List<Person> queryForPatient(AuthenticatedUser userDetails, List<PatientIdVO> patientList, CountryVO country) throws NcpServiceException;

    void setTRCAssertion(TRC trc, AuthenticatedUser userDetails) throws NcpServiceException;

    List<MetaDocument> queryDocuments(Person person, String doctype, AuthenticatedUser userDetails) throws NcpServiceException;

    CdaDocument retrieveDocument(MetaDocument doc) throws NcpServiceException;

    byte[] submitDocument(Dispensation dispensation, AuthenticatedUser user, Person person, String eD_PageAsString) throws NcpServiceException;

    void bindToSession(String sessionId);
}
