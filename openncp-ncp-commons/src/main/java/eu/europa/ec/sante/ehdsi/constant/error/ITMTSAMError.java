package eu.europa.ec.sante.ehdsi.constant.error;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, property="@class")
@JsonSubTypes(value = {
                @JsonSubTypes.Type(value = TMError.class),
                @JsonSubTypes.Type(value = TSAMError.class),
                @JsonSubTypes.Type(value = TSAMErrorCtx.class),
                @JsonSubTypes.Type(value = TMErrorCtx.class)})
public interface ITMTSAMError {

    /**
     * @return String - code
     */
    String getCode();


    /**
     * @return String - Description
     */
    String getDescription();


    /*
     * @return String in format code:description
     */
    @Override
    String toString();
}
