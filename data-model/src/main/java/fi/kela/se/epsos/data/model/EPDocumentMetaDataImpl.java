package fi.kela.se.epsos.data.model;

@SuppressWarnings({"unused", "WeakerAccess"})
public class EPDocumentMetaDataImpl extends EPSOSDocumentMetaDataImpl implements EPDocumentMetaData {

    private String description;

    private ProductMetadata product;

    private boolean dispensable;

    private String atcCode;
    private String doseFormCode;
    private String strength;

    public EPDocumentMetaDataImpl(EPSOSDocumentMetaData metaData, String description) {
        this(metaData, description, null);
    }

    public EPDocumentMetaDataImpl(EPSOSDocumentMetaData metaData, String description, ProductMetadata product) {
        this(metaData, description, product, false, null, null, null);
    }

    public EPDocumentMetaDataImpl(EPSOSDocumentMetaData metaData, String description, boolean dispensable, String atcCode, String doseFormCode, String strength) {
        this(metaData, description, null, dispensable, atcCode, doseFormCode, strength);
    }

    public EPDocumentMetaDataImpl(EPSOSDocumentMetaData metaData, String description, ProductMetadata product, boolean dispensable, String atcCode, String doseFormCode, String strength) {
        super(metaData);
        this.product = product;
        this.dispensable = dispensable;
        this.description = description;
        this.atcCode = atcCode;
        this.doseFormCode = doseFormCode;
        this.strength = strength;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public ProductMetadata getProduct() {
        return product;
    }

    @Override
    public boolean isDispensable() {
        return dispensable;
    }

    @Override
    public String getAtcCode() { return atcCode; }

    @Override
    public String getDoseFormCode() { return doseFormCode; }

    @Override
    public String getStrength() { return strength; }

    public static class SimpleProductMetadata implements ProductMetadata {

        private String productCode;

        private String productName;

        public SimpleProductMetadata(String productCode, String productName) {
            this.productCode = productCode;
            this.productName = productName;
        }

        @Override
        public String getProductCode() {
            return productCode;
        }

        @Override
        public String getProductName() {
            return productName;
        }

    }
}
