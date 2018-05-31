/**
 * Copyright (c) 2009-2011 University of Cardiff and others
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 * <p>
 * Contributors:
 * University of Cardiff - initial API and implementation
 * -
 */

package org.openhealthtools.openatna.audit.persistence.model.codes;

import org.hibernate.annotations.GenericGenerator;
import org.openhealthtools.openatna.audit.persistence.model.PersistentEntity;

import javax.persistence.*;


/**
 * Entity for coded value types.
 * NOTE: equality between codes is a slightly fuzzy business.
 * Codes need to be unique, but both the code system and code system name are optional.
 * This makes determining equality slightly tricky. Added to this, two null values in the DB
 * do not equate, so two Codes with the same code, and null for the system and system name are not equal.
 */


@Entity
@Table(name = "codes")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
        name = "codetype",
        discriminatorType = DiscriminatorType.STRING
)
public abstract class CodeEntity extends PersistentEntity {

    private Long id;
    private Integer version;
    private CodeType type;
    private String code;
    private String codeSystem;
    private String codeSystemName;
    private String displayName;
    private String originalText;

    protected CodeEntity() {
    }

    protected CodeEntity(CodeType type) {
        this.type = type;
    }

    protected CodeEntity(CodeType type, String code) {
        this.type = type;
        this.code = code;
    }

    protected CodeEntity(CodeType type, String code, String codeSystem) {
        this.type = type;
        this.code = code;
        this.codeSystem = codeSystem;
    }

    protected CodeEntity(CodeType type, String code, String codeSystem, String codeSystemName) {
        this.type = type;
        this.code = code;
        this.codeSystem = codeSystem;
        this.codeSystemName = codeSystemName;
    }

    protected CodeEntity(CodeType type, String code, String codeSystem, String codeSystemName,
                         String displayName) {
        this.type = type;
        this.code = code;
        this.codeSystem = codeSystem;
        this.codeSystemName = codeSystemName;
        this.displayName = displayName;
    }

    protected CodeEntity(CodeType type, String code, String codeSystem, String codeSystemName,
                         String displayName, String originalText) {
        this.type = type;
        this.code = code;
        this.codeSystem = codeSystem;
        this.codeSystemName = codeSystemName;
        this.displayName = displayName;
        this.originalText = originalText;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO,
            generator="native"
    )
    @GenericGenerator(
            name = "native",
            strategy = "native"
    )
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Version
    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    /**
     * Gets the value of the code property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getCode() {
        return code;
    }

    /**
     * Sets the value of the code property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCode(String value) {
        this.code = value;
    }

    /**
     * Gets the value of the displayName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the value of the displayName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDisplayName(String value) {
        this.displayName = value;
    }

    /**
     * Gets the value of the originalText property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getOriginalText() {
        return originalText;
    }

    /**
     * Sets the value of the originalText property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setOriginalText(String value) {
        this.originalText = value;
    }

    /**
     * Gets the value of the codeSystem property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getCodeSystem() {
        return codeSystem;
    }

    /**
     * Sets the value of the codeSystem property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCodeSystem(String value) {
        this.codeSystem = value;
    }

    /**
     * Gets the value of the codeSystemName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getCodeSystemName() {
        return codeSystemName;
    }

    /**
     * Sets the value of the codeSystemName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCodeSystemName(String value) {
        this.codeSystemName = value;
    }

    public CodeType getType() {
        return type;
    }

    public void setType(CodeType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CodeEntity)) {
            return false;
        }
        CodeEntity that = (CodeEntity) o;

        if (getCode() != null ? !getCode().equals(that.getCode()) : that.getCode() != null) {
            return false;
        }
        if (getCodeSystem() != null ? !getCodeSystem().equals(that.getCodeSystem()) : that.getCodeSystem() != null) {
            return false;
        }
        if (getCodeSystemName() != null ? !getCodeSystemName().equals(that.getCodeSystemName()) : that.getCodeSystemName() != null) {
            return false;
        }
        return getType() == that.getType();
    }

    @Override
    public int hashCode() {
        int result = getType() != null ? getType().hashCode() : 0;
        result = 31 * result + (getCode() != null ? getCode().hashCode() : 0);
        result = 31 * result + (getCodeSystem() != null ? getCodeSystem().hashCode() : 0);
        result = 31 * result + (getCodeSystemName() != null ? getCodeSystemName().hashCode() : 0);
        result = 31 * result + (getType() != null ? getType().hashCode() : 0);
        return result;
    }

    public String toString() {
        return "[" + getClass().getName() +
                " id=" +
                getId() +
                ", version=" +
                getVersion() +
                ", type=" +
                getType() +
                ", code=" +
                getCode() +
                ", code system=" +
                getCodeSystem() +
                ", code system name=" +
                getCodeSystemName() +
                ", original text=" +
                getOriginalText() +
                ", display name=" +
                getDisplayName() +
                "]";
    }

    public enum CodeType {
        ACTIVE_PARTICIPANT,
        AUDIT_SOURCE,
        EVENT_ID,
        EVENT_TYPE,
        PARTICIPANT_OBJECT_ID_TYPE
    }
}
