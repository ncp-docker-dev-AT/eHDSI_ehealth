package eu.europa.ec.sante.ehdsi.constant.error;

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
