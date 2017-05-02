/**
 * Copyright (C) 2011, 2012 SRDC Yazilim Arastirma ve Gelistirme ve Danismanlik Tic. Ltd. Sti. <epsos@srdc.com.tr>
 * <p>
 * This file is part of SRDC epSOS NCP.
 * <p>
 * SRDC epSOS NCP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * SRDC epSOS NCP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with SRDC epSOS NCP. If not, see <http://www.gnu.org/licenses/>.
 */
package tr.com.srdc.epsos.consent;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.srdc.epsos.consent.db.PatientDBConnector;
import urn.oasis.names.tc.xacml3.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PDP {

    private static Logger logger = LoggerFactory.getLogger(PDP.class);

    public static ResponseType getDecision(RequestType request) {

        ResponseType response = new ResponseType();
        ResultType result = new ResultType();
        response.getResult().add(result);
        result.setDecision(DecisionType.DENY);
        String patientId = "";
        //String SQL_SELECT_CONSENT = "SELECT * FROM consent WHERE patientId = '" + patientId + "'";
        String SQL_SELECT_CONSENT = "SELECT * FROM consent WHERE patientId = ?";

        for (AttributesType attributes : request.getAttributes()) {
            for (AttributeType attribute : attributes.getAttribute()) {
                if (StringUtils.equals(attribute.getAttributeId(), "patient-id")) {
                    patientId = (String) attribute.getAttributeValue().get(0).getContent().get(0);
                }
            }
        }

        try {
            //try (ResultSet rs = PatientDBConnector.getStatement().executeQuery()) {
            try (PreparedStatement preparedStatement = PatientDBConnector.getPreparedStatement(SQL_SELECT_CONSENT)) {

                preparedStatement.setString(1, patientId);
                ResultSet rs = preparedStatement.executeQuery();

                if (rs.next()) {
                    int isGranted = rs.getInt("granted");
                    if (isGranted == 0) {
                        return response;
                    }
                } else {
                    return response;
                }

                String xacmlDocument = rs.getString("xacmlDocument");
                ByteArrayInputStream bais = new ByteArrayInputStream(xacmlDocument.getBytes());

                JAXBContext jc = JAXBContext.newInstance("urn.oasis.names.tc.xacml3");
                Unmarshaller unmarshaller = jc.createUnmarshaller();
                PolicyType policy = (PolicyType) unmarshaller.unmarshal(bais);

                RuleType rule = (RuleType) policy.getCombinerParametersOrRuleCombinerParametersOrVariableDefinition().get(0);
                TargetType target = rule.getTarget();
                AnyOfType anyOf = target.getAnyOf().get(0);
                for (AllOfType allOf : anyOf.getAllOf()) {
                    int i = 0;
                    for (i = 0; i < allOf.getMatch().size(); i++) {
                        MatchType match = allOf.getMatch().get(i);
                        String matchId = match.getMatchId();
                        String attrValue = (String) match.getAttributeValue().getContent().get(0);
                        String attrDesignatorAttrId = match.getAttributeDesignator().getAttributeId();
                        Object attrDesignatorValue = null;
                        if (attrDesignatorAttrId.equals("urn:oasis:names:tc:xacml:1.0:environment:current-dateTime")) {
                            attrDesignatorValue = new Date();
                        } else {
                            for (AttributeType attributeType : request.getAttributes().get(0).getAttribute()) {
                                if (attributeType.getAttributeId().equals(attrDesignatorAttrId)) {
                                    attrDesignatorValue = attributeType.getAttributeValue().get(0).getContent().get(0);
                                }
                            }
                        }

                        DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

                        if (matchId.equals("urn:oasis:names:tc:xacml:1.0:function:string-equal")) {
                            if (!attrValue.equals(attrDesignatorValue)) {
                                break;
                            }
                        } else if (matchId.equals("urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than")) {
                            Date date1 = formatter.parse(attrValue);
                            Date date2 = (Date) attrDesignatorValue;
                            if (!date1.before(date2)) {
                                break;
                            }
                        } else if (matchId.equals("urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than")) {
                            Date date1 = formatter.parse(attrValue);
                            Date date2 = (Date) attrDesignatorValue;
                            if (!date1.after(date2)) {
                                break;
                            }
                        }
                    }

                    if (i == allOf.getMatch().size()) {
                        if (rule.getEffect().equals(EffectType.DENY)) {
                            result.setDecision(DecisionType.DENY);
                        } else if (rule.getEffect().equals(EffectType.PERMIT)) {
                            result.setDecision(DecisionType.PERMIT);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("", e);
        } catch (JAXBException e) {
            logger.error("", e);
        } catch (ParseException e) {
            logger.error("", e);
        }

        return response;
    }
}
