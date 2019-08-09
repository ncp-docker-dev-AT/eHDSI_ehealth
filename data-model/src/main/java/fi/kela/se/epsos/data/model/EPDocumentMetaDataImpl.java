package fi.kela.se.epsos.data.model;

@SuppressWarnings({"unused", "WeakerAccess"})
public class EPDocumentMetaDataImpl extends EPSOSDocumentMetaDataImpl implements EPDocumentMetaData {

    private String description;

    private ProductMetadata product;

    private boolean dispensable;

    public EPDocumentMetaDataImpl(EPSOSDocumentMetaData metaData, String description) {
        this(metaData, description, null);
    }

    public EPDocumentMetaDataImpl(EPSOSDocumentMetaData metaData, String description, ProductMetadata product) {
        this(metaData, description, product, false);
    }

    public EPDocumentMetaDataImpl(EPSOSDocumentMetaData metaData, String description, boolean dispensable) {
        this(metaData, description, null, dispensable);
    }

    public EPDocumentMetaDataImpl(EPSOSDocumentMetaData metaData, String description, ProductMetadata product, boolean dispensable) {
        super(metaData);
        this.product = product;
        this.dispensable = dispensable;
        this.description = description;
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
