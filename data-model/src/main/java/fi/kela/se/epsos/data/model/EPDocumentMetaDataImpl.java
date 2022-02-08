package fi.kela.se.epsos.data.model;

import tr.com.srdc.epsos.data.model.SubstitutionCodeEnum;

@SuppressWarnings({"unused", "WeakerAccess"})
public class EPDocumentMetaDataImpl extends EPSOSDocumentMetaDataImpl implements EPDocumentMetaData {

    private final String description;
    private final ProductMetadata product;
    private final boolean dispensable;
    private final String atcCode;
    private final String atcName;
    private final String doseFormCode;
    private final String doseFormName;
    private final String strength;
    private final SubstitutionMetaData substitution;

    public EPDocumentMetaDataImpl(EPSOSDocumentMetaData metaData, String description) {
        this(metaData, description, (ProductMetadata) null);
    }

    public EPDocumentMetaDataImpl(EPSOSDocumentMetaData metaData, String description, ProductMetadata product) {
        this(metaData, description, product, new EpListParam(false, null, null, null, null, null, null));
    }

    public EPDocumentMetaDataImpl(EPSOSDocumentMetaData metaData, String description, EpListParam epListParam) {
        this(metaData, description, null, epListParam);
    }

    public EPDocumentMetaDataImpl(EPSOSDocumentMetaData metaData, String description, ProductMetadata product, EpListParam epListParam) {
        super(metaData);
        this.product = product;
        this.description = description;
        this.dispensable = epListParam.isDispensable();
        this.atcCode = epListParam.getAtcCode();
        this.atcName = epListParam.getAtcName();
        this.doseFormCode = epListParam.getDoseFormCode();
        this.doseFormName = epListParam.getDoseFormName();
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
    public String getAtcCode() {
        return atcCode;
    }

    @Override
    public String getAtcName() {
        return atcName;
    }

    @Override
    public String getDoseFormCode() {
        return doseFormCode;
    }

    @Override
    public String getDoseFormName() {
        return doseFormName;
    }

    @Override
    public String getStrength() {
        return strength;
    }

    @Override
    public SubstitutionMetaData getSubstitution() {
        return substitution;
    }

    public static class SimpleProductMetadata implements ProductMetadata {

        private final String productCode;

        private final String productName;

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

    public static class SimpleSubstitutionMetadata implements SubstitutionMetaData {

        private final SubstitutionCodeEnum substitutionCode;

        public SimpleSubstitutionMetadata(SubstitutionCodeEnum substitutionCode) {
            this.substitutionCode = substitutionCode;
        }

        @Override
        public String getSubstitutionCode() {
            return substitutionCode.name();
        }

        @Override
        public String getSubstitutionDisplayName() {
            return substitutionCode.getDisplayName();
        }
    }
}
