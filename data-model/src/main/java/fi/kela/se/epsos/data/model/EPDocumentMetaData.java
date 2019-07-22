package fi.kela.se.epsos.data.model;

/**
 * EPrescriptionDocumentMetaData interface
 *
 * @author mimyllyv
 */
public interface EPDocumentMetaData extends EPSOSDocumentMetaData {

    ProductMetadata getProduct();

    default boolean hasProduct() {
        return getProduct() == null;
    }

    boolean isDispensable();

    interface ProductMetadata {

        String getProductCode();

        String getProductName();
    }
}
