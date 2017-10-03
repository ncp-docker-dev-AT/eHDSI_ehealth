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

    private static final Logger LOGGER = LoggerFactory.getLogger(PDP.class);

    private PDP() {
    }

    public static ResponseType getDecision(RequestType request) {

        ResponseType response = new ResponseType();
        ResultType result = new ResultType();
        response.getResult().add(result);
        result.setDecision(DecisionType.DENY);
        String patientId = "";

        String SQL_SELECT_CONSENT = "SELECT * FROM consent WHERE patientId = ?";

        for (AttributesType attributes : request.getAttributes()) {
            for (AttributeType attribute : attributes.getAttribute()) {
                if (StringUtils.equals(attribute.getAttributeId(), "patient-id")) {
                    patientId = (String) attribute.getAttributeValue().get(0).getContent().get(0);
                }
            }
        }

        try {
            try (PreparedStatement preparedStatement = PatientDBConnector.getPreparedStatement(SQL_SELECT_CONSENT)) {

                preparedStatement.setString(1, patientId);
                try (ResultSet rs = preparedStatement.executeQuery()) {

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
                        int i;
                        label:
                        for (i = 0; i < allOf.getMatch().size(); i++) {
                            MatchType match = allOf.getMatch().get(i);
                            String matchId = match.getMatchId();
                            String attrValue = (String) match.getAttributeValue().getContent().get(0);
                            String attrDesignatorAttrId = match.getAttributeDesignator().getAttributeId();
                            Object attrDesignatorValue = null;
                            if (StringUtils.equals(attrDesignatorAttrId, "urn:oasis:names:tc:xacml:1.0:environment:current-dateTime")) {
                                attrDesignatorValue = new Date();
                            } else {
                                for (AttributeType attributeType : request.getAttributes().get(0).getAttribute()) {
                                    if (attributeType.getAttributeId().equals(attrDesignatorAttrId)) {
                                        attrDesignatorValue = attributeType.getAttributeValue().get(0).getContent().get(0);
                                    }
                                }
                            }

                            DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

                            switch (matchId) {
                                case "urn:oasis:names:tc:xacml:1.0:function:string-equal":
                                    if (!attrValue.equals(attrDesignatorValue)) {
                                        break label;
                                    }
                                    break;
                                case "urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than": {
                                    Date date1 = formatter.parse(attrValue);
                                    Date date2 = (Date) attrDesignatorValue;
                                    if (!date1.before(date2)) {
                                        break label;
                                    }
                                    break;
                                }
                                case "urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than": {
                                    Date date1 = formatter.parse(attrValue);
                                    Date date2 = (Date) attrDesignatorValue;
                                    if (!date1.after(date2)) {
                                        break label;
                                    }
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
            }
        } catch (SQLException | JAXBException | ParseException e) {
            LOGGER.error("{}: '{}'", e.getClass(), e.getMessage(), e);
        }
        return response;
    }
}
