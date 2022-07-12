<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:n1="urn:hl7-org:v3"
                xmlns:pharm="urn:hl7-org:pharm"
                version="2.0">

    <xsl:template name="dispensationDetailsBlock">
        <fieldset style="min-height:100px;">
            <legend>
                <!-- Dispensation details -->
                <xsl:call-template name="show-eHDSIDisplayLabel">
                    <xsl:with-param name="code" select="'130'"/>
                </xsl:call-template>
            </legend>
            <table class="ep_table">
                <tbody>
                    <tr>
                        <th>
                            <span class="td_label"><!--  Substitute:-->
                                <xsl:call-template name="show-eHDSIDisplayLabel">
                                    <xsl:with-param name="code" select="'71'"/>
                                </xsl:call-template>
                                *
                            </span>
                        </th>
                        <td colspan="2">
                            <xsl:variable name="substitutionValue">
                                <xsl:call-template name="pure-substitution-code"/>
                            </xsl:variable>
                            <input type="checkbox" id="substituted_0" name="substituted_0" style="display:inline">
                                <xsl:choose>
                                    <xsl:when test="$substitutionValue !='Yes'">
                                        <xsl:attribute name="disabled"/>
                                    </xsl:when>
                                </xsl:choose>
                            </input>
                            <!--  Substitution help text:-->
                            <xsl:call-template name="show-eHDSIDisplayLabel">
                                <xsl:with-param name="code" select="'202'"/>
                            </xsl:call-template>
                        </td>
                    </tr>
                    <tr>
                        <td rowspan="3"/>
                        <th>
                            <!--  Dispensed Product:-->
                            <span class="td_label">
                                <xsl:call-template name="show-eHDSIDisplayLabel">
                                    <xsl:with-param name="code" select="'24'"/>
                                </xsl:call-template>
                            </span>
                        </th>
                        <td>
                            <input type="text" size="50">
                                <xsl:attribute name="id">
                                    <xsl:text>dispensedProductValue_</xsl:text>
                                    <xsl:value-of select="position()-1"/>
                                </xsl:attribute>
                                <xsl:attribute name="name">
                                    <xsl:text>dispensedProductValue_</xsl:text>
                                    <xsl:value-of select="position()-1"/>
                                </xsl:attribute>
                                <xsl:attribute name="value">
                                    <xsl:value-of
                                            select="n1:consumable/n1:manufacturedProduct/n1:manufacturedMaterial/n1:name"/>
                                </xsl:attribute>
                            </input>
                        </td>
                    </tr>
                    <tr>
                        <th>
                            <!-- Dispensed Package Size:-->
                            <span class="td_label">
                                <xsl:call-template name="show-eHDSIDisplayLabel">
                                    <xsl:with-param name="code" select="'23'"/>
                                </xsl:call-template>
                            </span>
                        </th>
                        <td>
                            <xsl:call-template name="show-package-size-for-dispense">
                                <xsl:with-param name="asContent_level1" select="n1:consumable/n1:manufacturedProduct/n1:manufacturedMaterial/pharm:asContent"/>
                                <xsl:with-param name="asContent_level2" select="n1:consumable/n1:manufacturedProduct/n1:manufacturedMaterial/pharm:asContent/pharm:containerPackagedProduct/pharm:asContent"/>
                                <xsl:with-param name="asContent_level3" select="n1:consumable/n1:manufacturedProduct/n1:manufacturedMaterial/pharm:asContent/pharm:containerPackagedProduct/pharm:asContent/pharm:containerPackagedProduct/pharm:asContent"/>
                            </xsl:call-template>
                        </td>
                    </tr>
                    <tr>
                        <th>
                            <!-- Dispensed Number of Packages:-->
                            <span class="td_label">
                                <xsl:call-template name="show-eHDSIDisplayLabel">
                                    <xsl:with-param name="code" select="'115'"/>
                                </xsl:call-template>
                            </span>
                        </th>
                        <td>
                            <input type="text" size="50">
                                <xsl:attribute name="id">
                                    <xsl:text>dispensedNumberOfPackages_</xsl:text>
                                    <xsl:value-of select="position()-1"/>
                                </xsl:attribute>
                                <xsl:attribute name="name">
                                    <xsl:text>dispensedNumberOfPackages_</xsl:text>
                                    <xsl:value-of select="position()-1"/>
                                </xsl:attribute>
                                <xsl:attribute name="value">
                                    <xsl:call-template name="number-of-packages">
                                        <xsl:with-param name="supply" select="n1:entryRelationship[@typeCode='COMP']"/>
                                    </xsl:call-template>
                                </xsl:attribute>
                            </input>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="3">
                            <!-- HOTFIX - Has to be replaced with a value from the epSOSDisplayLabel value set -->
                            *  If substitution of brand name is marked as not allowed, pharmacists may still consider dispensing the national equivalent even though the brand name might be slightly different.<br/>
                            &#160;&#160;This is a known situation: the same pharmaceutical company is marketing the same medicinal product in different countries with slightly different names due to marketing reasons.<br/>
                            &#160;&#160;If the pharmacist is certain that this is the case, the systems allows the input of the new brand name.<br/>
                        </td>
                    </tr>
                    <tr>
                        <td style="text-align:center;vertical-align:middle;">
                            <!--  Dispense -->
                            <xsl:choose>
                                <xsl:when test="$allowDispense='true'">
                                    <input type="submit" class="button">
                                        <xsl:attribute name="value">
                                            <xsl:call-template name="show-eHDSIDisplayLabel">
                                                <xsl:with-param name="code" select="'22'"/>
                                            </xsl:call-template>
                                        </xsl:attribute>
                                    </input>
                                </xsl:when>
                            </xsl:choose>
                            <input type="hidden" name="prescriptionID" id="prescriptionID">
                                <xsl:attribute name="value">
                                    <xsl:value-of select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='1.3.6.1.4.1.12559.11.10.1.3.1.2.1']/n1:id/@extension"/>
                                </xsl:attribute>
                            </input>
                            <input type="hidden">
                                <xsl:call-template name="inputformID">
                                    <xsl:with-param name="txt" select="'dispensationid_'"/>
                                    <xsl:with-param name="root" select="n1:id/@root"/>
                                    <xsl:with-param name="extension" select="n1:id/@extension"/>
                                </xsl:call-template>
                            </input>
                            <xsl:variable name="currentPackageFormName">
                                <xsl:call-template name="show-formCode">
                                    <xsl:with-param name="parameter"
                                                    select="n1:consumable/n1:manufacturedProduct/n1:manufacturedMaterial/pharm:formCode"/>
                                </xsl:call-template>
                            </xsl:variable>
                            <input type="hidden">
                                <xsl:call-template name="inputform">
                                    <xsl:with-param name="txt" select="'packaging1_'"/>
                                    <xsl:with-param name="val" select="$currentPackageFormName"/>
                                </xsl:call-template>
                            </input>
                            <xsl:variable name="currentDoseFormName">
                                <xsl:call-template name="show-formCode">
                                    <xsl:with-param name="parameter"
                                                    select="n1:consumable/n1:manufacturedProduct/n1:manufacturedMaterial/pharm:asContent/pharm:containerPackagedProduct/pharm:formCode"/>
                                </xsl:call-template>
                            </xsl:variable>
                            <input type="hidden">
                                <xsl:call-template name="inputform">
                                    <xsl:with-param name="txt" select="'packaging2_'"/>
                                    <xsl:with-param name="val" select="$currentDoseFormName"/>
                                </xsl:call-template>
                            </input>
                            <input type="hidden">
                                <xsl:call-template name="inputform">
                                    <xsl:with-param name="txt" select="'packaging3_'"/>
                                    <xsl:with-param name="val"
                                                    select="n1:consumable/n1:manufacturedProduct/n1:manufacturedMaterial/pharm:asContent/pharm:quantity/pharm:numerator/@value"/>
                                </xsl:call-template>
                            </input>

                            <xsl:variable name="currentIngredient">
                                <xsl:call-template name="show-formCode">
                                    <xsl:with-param name="parameter"
                                                    select="n1:consumable/n1:manufacturedProduct/n1:manufacturedMaterial/pharm:ingredient[@classCode='ACTI']/pharm:ingredientSubstance/pharm:code"/>
                                </xsl:call-template>
                            </xsl:variable>
                            <input type="hidden">
                                <xsl:call-template name="inputform">
                                    <xsl:with-param name="txt" select="'ingredient_'"/>
                                    <xsl:with-param name="val" select="$currentIngredient"/>
                                </xsl:call-template>
                            </input>
                            <input type="hidden">
                                <xsl:attribute name="id">
                                    <xsl:text>name_</xsl:text>
                                    <xsl:value-of select="position()-1"/>
                                </xsl:attribute>
                                <xsl:attribute name="name">
                                    <xsl:text>name_</xsl:text>
                                    <xsl:value-of select="position()-1"/>
                                </xsl:attribute>
                                <xsl:attribute name="value">
                                    <xsl:call-template name="show-strength">
                                        <xsl:with-param name="node" select="n1:consumable/n1:manufacturedProduct/n1:manufacturedMaterial/pharm:ingredient[@classCode='ACTI'][position()-1]/pharm:quantity"/>
                                    </xsl:call-template>
                                </xsl:attribute>
                            </input>
                            <input type="hidden">
                                <xsl:attribute name="id">
                                    <xsl:text>nrOfPacks_</xsl:text>
                                    <xsl:value-of select="position()-1"/>
                                </xsl:attribute>
                                <xsl:attribute name="name">
                                    <xsl:text>nrOfPacks_</xsl:text>
                                    <xsl:value-of select="position()-1"/>
                                </xsl:attribute>
                                <xsl:attribute name="value">
                                    <xsl:call-template name="number-of-packages">
                                        <xsl:with-param name="supply" select="n1:entryRelationship[@typeCode='COMP']"/>
                                    </xsl:call-template>
                                </xsl:attribute>
                            </input>
                            <input type="hidden">
                                <xsl:attribute name="id">
                                    <xsl:text>country</xsl:text>
                                </xsl:attribute>
                                <xsl:attribute name="name">
                                    <xsl:text>country</xsl:text>
                                </xsl:attribute>
                                <xsl:attribute name="value">
                                    <xsl:value-of
                                            select=" /n1:ClinicalDocument/n1:recordTarget/n1:patientRole/n1:addr/n1:country"/>
                                </xsl:attribute>
                            </input>
                        </td>
                    </tr>
                </tbody>
            </table>
        </fieldset>
    </xsl:template>

    <xsl:template name="show-package-size-for-dispense">
        <xsl:param name="asContent_level1"/>
        <xsl:param name="asContent_level2"/>
        <xsl:param name="asContent_level3"/>
        <ul>
            <xsl:choose>
                <xsl:when test="$asContent_level3">
                    <li>
                        <xsl:call-template name="show-formCode">
                            <xsl:with-param name="parameter"
                                            select="$asContent_level3/pharm:containerPackagedProduct/pharm:formCode"/>
                        </xsl:call-template>
                        <ul>
                            <li>
                                <input type="text" size="5">
                                    <xsl:attribute name="id">
                                        <xsl:text>dispensedPackageSizeL3_</xsl:text>
                                        <xsl:value-of select="position()-1"/>
                                    </xsl:attribute>
                                    <xsl:attribute name="name">
                                        <xsl:text>dispensedPackageSizeL3_</xsl:text>
                                        <xsl:value-of select="position()-1"/>
                                    </xsl:attribute>
                                    <xsl:attribute name="value">
                                        <xsl:value-of
                                                select="$asContent_level3/pharm:quantity/@value"/>
                                    </xsl:attribute>
                                </input>
                                <xsl:call-template name="show_quantity">
                                    <xsl:with-param name="quantity" select="$asContent_level3/pharm:quantity"/>
                                    <xsl:with-param name="showValue" select="false"/>
                                </xsl:call-template>
                                <xsl:text> </xsl:text>
                                <xsl:call-template name="show-formCode">
                                    <xsl:with-param name="parameter"
                                                    select="$asContent_level2/pharm:containerPackagedProduct/pharm:formCode"/>
                                </xsl:call-template>
                                <ul>
                                    <li>
                                        <input type="text" size="5">
                                            <xsl:attribute name="id">
                                                <xsl:text>dispensedPackageSizeL2_</xsl:text>
                                                <xsl:value-of select="position()-1"/>
                                            </xsl:attribute>
                                            <xsl:attribute name="name">
                                                <xsl:text>dispensedPackageSizeL2_</xsl:text>
                                                <xsl:value-of select="position()-1"/>
                                            </xsl:attribute>
                                            <xsl:attribute name="value">
                                                <xsl:value-of
                                                        select="$asContent_level2/pharm:quantity/@value"/>
                                            </xsl:attribute>
                                        </input>
                                        <xsl:call-template name="show_quantity">
                                            <xsl:with-param name="quantity" select="$asContent_level2/pharm:quantity"/>
                                            <xsl:with-param name="showValue" select="false"/>
                                        </xsl:call-template>
                                        <xsl:text> </xsl:text>
                                        <xsl:call-template name="show-formCode">
                                            <xsl:with-param name="parameter"
                                                            select="$asContent_level1/pharm:containerPackagedProduct/pharm:formCode"/>
                                        </xsl:call-template>
                                        <ul>
                                            <li>
                                                <input type="text" size="5">
                                                    <xsl:attribute name="id">
                                                        <xsl:text>dispensedPackageSizeL1_</xsl:text>
                                                        <xsl:value-of select="position()-1"/>
                                                    </xsl:attribute>
                                                    <xsl:attribute name="name">
                                                        <xsl:text>dispensedPackageSizeL1_</xsl:text>
                                                        <xsl:value-of select="position()-1"/>
                                                    </xsl:attribute>
                                                    <xsl:attribute name="value">
                                                        <xsl:value-of
                                                                select="$asContent_level1/pharm:quantity/@value"/>
                                                    </xsl:attribute>
                                                </input>
                                                <xsl:call-template name="show_quantity">
                                                    <xsl:with-param name="quantity" select="$asContent_level1/pharm:quantity"/>
                                                    <xsl:with-param name="showValue" select="false"/>
                                                </xsl:call-template>
                                                <xsl:text> </xsl:text>
                                                <xsl:call-template name="show-formCode">
                                                    <xsl:with-param name="parameter"
                                                                    select="$asContent_level1/../pharm:formCode"/>
                                                </xsl:call-template>
                                            </li>
                                        </ul>
                                    </li>
                                </ul>
                            </li>
                        </ul>
                    </li>
                </xsl:when>
                <xsl:when test="$asContent_level2">
                    <li>
                        <xsl:call-template name="show-formCode">
                            <xsl:with-param name="parameter"
                                            select="$asContent_level2/pharm:containerPackagedProduct/pharm:formCode"/>
                        </xsl:call-template>
                        <ul>
                            <li>
                                <input type="text" size="5">
                                    <xsl:attribute name="id">
                                        <xsl:text>dispensedPackageSizeL2_</xsl:text>
                                        <xsl:value-of select="position()-1"/>
                                    </xsl:attribute>
                                    <xsl:attribute name="name">
                                        <xsl:text>dispensedPackageSizeL2_</xsl:text>
                                        <xsl:value-of select="position()-1"/>
                                    </xsl:attribute>
                                    <xsl:attribute name="value">
                                        <xsl:value-of
                                                select="$asContent_level2/pharm:quantity/@value"/>
                                    </xsl:attribute>
                                </input>
                                <xsl:call-template name="show_quantity">
                                    <xsl:with-param name="quantity" select="$asContent_level2/pharm:quantity"/>
                                    <xsl:with-param name="showValue" select="false"/>
                                </xsl:call-template>
                                <xsl:text> </xsl:text>
                                <xsl:call-template name="show-formCode">
                                    <xsl:with-param name="parameter"
                                                    select="$asContent_level1/pharm:containerPackagedProduct/pharm:formCode"/>
                                </xsl:call-template>
                                <ul>
                                    <li>
                                        <input type="text" size="5">
                                            <xsl:attribute name="id">
                                                <xsl:text>dispensedPackageSizeL1_</xsl:text>
                                                <xsl:value-of select="position()-1"/>
                                            </xsl:attribute>
                                            <xsl:attribute name="name">
                                                <xsl:text>dispensedPackageSizeL1_</xsl:text>
                                                <xsl:value-of select="position()-1"/>
                                            </xsl:attribute>
                                            <xsl:attribute name="value">
                                                <xsl:value-of
                                                        select="$asContent_level1/pharm:quantity/@value"/>
                                            </xsl:attribute>
                                        </input>
                                        <xsl:call-template name="show_quantity">
                                            <xsl:with-param name="quantity" select="$asContent_level1/pharm:quantity"/>
                                            <xsl:with-param name="showValue" select="false"/>
                                        </xsl:call-template>
                                        <xsl:text> </xsl:text>
                                        <xsl:call-template name="show-formCode">
                                            <xsl:with-param name="parameter"
                                                            select="$asContent_level1/../pharm:formCode"/>
                                        </xsl:call-template>
                                    </li>
                                </ul>
                            </li>
                        </ul>
                    </li>
                </xsl:when>
                <xsl:otherwise>
                    <ul>
                        <li>
                            <xsl:call-template name="show-formCode">
                                <xsl:with-param name="parameter"
                                                select="$asContent_level1/pharm:containerPackagedProduct/pharm:formCode"/>
                            </xsl:call-template>
                            <ul>
                                <li>
                                    <input type="text" size="5">
                                        <xsl:attribute name="id">
                                            <xsl:text>dispensedPackageSizeL1_</xsl:text>
                                            <xsl:value-of select="position()-1"/>
                                        </xsl:attribute>
                                        <xsl:attribute name="name">
                                            <xsl:text>dispensedPackageSizeL1_</xsl:text>
                                            <xsl:value-of select="position()-1"/>
                                        </xsl:attribute>
                                        <xsl:attribute name="value">
                                            <xsl:value-of
                                                    select="$asContent_level1/pharm:quantity/@value"/>
                                        </xsl:attribute>
                                    </input>
                                    <xsl:call-template name="show_quantity">
                                        <xsl:with-param name="quantity" select="$asContent_level1/pharm:quantity"/>
                                        <xsl:with-param name="showValue" select="false"/>
                                    </xsl:call-template>
                                    <xsl:text> </xsl:text>
                                    <xsl:call-template name="show-formCode">
                                        <xsl:with-param name="parameter"
                                                        select="$asContent_level1/../pharm:formCode"/>
                                    </xsl:call-template>
                                </li>
                            </ul>
                        </li>
                    </ul>
                </xsl:otherwise>
            </xsl:choose>
        </ul>
    </xsl:template>
</xsl:stylesheet>