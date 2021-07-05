package fi.kela.se.epsos.data.model;

@SuppressWarnings({"unused", "WeakerAccess"})
public class EPDocumentMetaDataImpl extends EPSOSDocumentMetaDataImpl implements EPDocumentMetaData {

    private String description;

    private ProductMetadata product;

    private boolean dispensable;

    private String atcCode;
    private String doseFormCode;
    private String strength;
    private String substitution;

    public EPDocumentMetaDataImpl(EPSOSDocumentMetaData metaData, String description) {
        this(metaData, description, (ProductMetadata)null);
    }

    public EPDocumentMetaDataImpl(EPSOSDocumentMetaData metaData, String description, ProductMetadata product) {
        this(metaData, description, product, new EpListParam(false, null, null, null, null));
    }

    public EPDocumentMetaDataImpl(EPSOSDocumentMetaData metaData, String description, EpListParam epListParam) {
        this(metaData, description, null, epListParam);
    }

    public EPDocumentMetaDataImpl(EPSOSDocumentMetaData metaData, String description, ProductMetadata product, EpListParam epListParam) {
        super(metaData);
        this.description = description;
        this.product = product;
        this.dispensable = epListParam.isDispensable();
        this.atcCode = epListParam.getAtcCode();
        this.doseFormCode = epListParam.getDoseFormCode();
        this.strength = epListParam.getStrength();
        this.substitution = epListParam.getSubstitution();
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

    @Override
    public String getSubstitution() { return substitution; }

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
