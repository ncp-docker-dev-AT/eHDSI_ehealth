package se.sb.epsos.web.model;

import java.io.Serializable;

public class QuantityVO implements Serializable {

    private static final long serialVersionUID = -7701638759511204075L;
    private String quantityValue;
    private String quantityUnit;
    private String quantityUnitUcum;

    public QuantityVO(String quantityValue, String quantityUnit, String quantityUnitUcum) {
        super();
        this.quantityValue = quantityValue;
        this.quantityUnit = quantityUnit;
        this.quantityUnitUcum = quantityUnitUcum;
    }

    public QuantityVO(QuantityVO vo) {
        this(vo.quantityValue, vo.quantityUnit, vo.quantityUnitUcum);
    }

    public String getQuantityValue() {
        return quantityValue;
    }

    public void setQuantityValue(String quantityValue) {
        this.quantityValue = quantityValue;
    }

    public String getQuantityUnit() {
        return quantityUnit;
    }

    public void setQuantityUnit(String quantityUnit) {
        this.quantityUnit = quantityUnit;
    }

    public String getQuantityUnitUcum() {
        return quantityUnitUcum;
    }

    public void setQuantityUnitUcum(String quantityUnitUcum) {
        this.quantityUnitUcum = quantityUnitUcum;
    }
}
