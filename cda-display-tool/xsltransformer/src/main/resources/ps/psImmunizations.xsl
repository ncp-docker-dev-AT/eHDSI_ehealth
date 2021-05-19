<?xml version="1.0" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:n1="urn:hl7-org:v3" version="1.0">

    <xsl:variable name="immunizationsSectionCode"
                  select="'11369-6'"/>

    <!-- immunizations -->
    <xsl:template name="immunizations" match="/">
        <xsl:apply-templates select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:code[@code=$immunizationsSectionCode]]"/>
    </xsl:template>

    <xsl:template match = "/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:code[@code=$immunizationsSectionCode]]">
        <div class="wrap-collapsible">
            <input id="collapsible-immunizations-section" class="toggle" type="checkbox" checked="true" />
            <label for="collapsible-immunizations-section" class="lbl-toggle-title">
                <!-- Section title -->
                <xsl:call-template name="show-eHDSISection">
                    <xsl:with-param name="code" select="$immunizationsSectionCode"/>
                </xsl:call-template>
            </label>
            <div class="collapsible-content-title">
                <div class="content-inner-title">
                    <div class="wrap-collapsible">
                        <input id="collapsible-immunizations-original" class="toggle" type="checkbox"/>
                        <label for="collapsible-immunizations-original" class="lbl-toggle">
                            <xsl:value-of select="$originalNarrativeTableTitle"/>
                        </label>
                        <div class="collapsible-content">
                            <div class="content-inner">
                                <xsl:apply-templates select="n1:text"/>
                            </div>
                        </div>
                    </div>
                    <br/>
                    <div class="wrap-collapsible">
                        <input id="collapsible-immunizationsSectionCode-translated" class="toggle" type="checkbox" checked="true"/>
                        <label for="collapsible-immunizationsSectionCode-translated" class="lbl-toggle">
                            <xsl:value-of select="$translatedCodedTableTitle"/>
                        </label>
                        <div class="collapsible-content">
                            <div class="content-inner">
                                <xsl:choose>
                                <xsl:when test="not (n1:entry/n1:substanceAdministration/@nullFlavor)">
                                <table class="translation_table">
                                    <tbody>
                                        <tr>
                                            <th>
                                                <!-- Vaccination header -->
                                                <xsl:call-template name="show-eHDSIDisplayLabel">
                                                    <xsl:with-param name="code" select="'79'"/>
                                                </xsl:call-template>
                                            </th>
                                            <th>
                                                <!-- Brand name header -->
                                                <xsl:call-template name="show-eHDSIDisplayLabel">
                                                    <xsl:with-param name="code" select="'9'"/>
                                                </xsl:call-template>
                                            </th>
                                            <th>
                                                <!-- Vaccination Date header -->
                                                <xsl:call-template name="show-eHDSIDisplayLabel">
                                                    <xsl:with-param name="code" select="'80'"/>
                                                </xsl:call-template>
                                            </th>
                                            <xsl:if test="n1:entry/n1:substanceAdministration/n1:entryRelationship/n1:observation[@classCode='OBS'][@moodCode='EVN']/n1:code[@codeSystem='2.16.840.1.113883.6.1'][@code ='30973-2']">
                                                <th>
                                                    <!-- Dose number in series -->
                                                    <xsl:call-template name="show-eHDSIDisplayLabel">
                                                        <xsl:with-param name="code" select="'152'"/>
                                                    </xsl:call-template>
                                                </th>
                                            </xsl:if>
                                            <th>
                                                <!-- Administered header -->
                                                <xsl:call-template name="show-eHDSIDisplayLabel">
                                                    <xsl:with-param name="code" select="'122'"/>
                                                </xsl:call-template>
                                            </th>
                                        </tr>
                                        <xsl:apply-templates select="n1:entry/n1:substanceAdministration" mode="immunizations"/>
                                    </tbody>
                                </table>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:call-template name="show-eHDSINullFlavor">
                                        <xsl:with-param name="code" select="n1:entry/n1:substanceAdministration/@nullFlavor"/>
                                    </xsl:call-template>
                                </xsl:otherwise>
                            </xsl:choose>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <br />
        <br />
    </xsl:template>

    <xsl:template match="n1:entry/n1:substanceAdministration" mode="immunizations">
        <!-- Defining all needed variables -->
        <xsl:variable name="vaccination"
                      select="n1:consumable/n1:manufacturedProduct/n1:manufacturedMaterial/n1:code"/>

        <xsl:variable name="vaccinationsBrandName"
                      select="n1:consumable/n1:manufacturedProduct/n1:manufacturedMaterial/n1:name"/>

        <xsl:variable name="vaccinationsDate"
                      select="n1:effectiveTime"/>

        <xsl:variable name="vaccinationsPosition"
                      select="n1:entryRelationship/n1:observation[@classCode='OBS'][@moodCode='EVN']/n1:code[@codeSystem='2.16.840.1.113883.6.1'][@code ='30973-2']"/>

        <xsl:variable name="negationInd"
                      select="@negationInd"/>
        <!-- End definition of variables -->

        <xsl:choose>
            <xsl:when test="not(./@nullFlavor)">
                <tr>
                    <td>
                        <!-- Vaccination -->
                        <xsl:call-template name="show-eHDSIVaccine">
                            <xsl:with-param name="node" select="$vaccination"/>
                        </xsl:call-template>
                    </td>
                    <td>
                        <!-- Brand Name -->
                        <xsl:choose>
                            <xsl:when test="$vaccinationsBrandName/@nullFlavor">
                                <xsl:call-template name="show-eHDSINullFlavor">
                                    <xsl:with-param name="code"
                                                    select="$vaccinationsBrandName/@nullFlavor"/>
                                </xsl:call-template>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="$vaccinationsBrandName"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </td>
                    <td>
                        <!-- Vaccination Date -->
                        <xsl:call-template name="show-TS">
                            <xsl:with-param name="node" select="$vaccinationsDate"/>
                        </xsl:call-template>
                        &#160;
                    </td>
                    <xsl:if test="$vaccinationsPosition">
                        <td>
                            <!-- Dose number in series -->
                            <xsl:choose>
                                <xsl:when test="$vaccinationsPosition/@nullFlavor">
                                    <xsl:call-template name="show-eHDSINullFlavor">
                                        <xsl:with-param name="code" select="$vaccinationsPosition/@nullFlavor"/>
                                    </xsl:call-template>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="$vaccinationsPosition/../n1:value/@value"/>
                                </xsl:otherwise>
                            </xsl:choose>

                        </td>
                    </xsl:if>
                    <td>
                        <!-- Administered -->
                        <xsl:choose>
                            <xsl:when test="(not($negationInd) or $negationInd='false')">
                                <font color="green">&#10004;</font>
                            </xsl:when>
                            <xsl:otherwise>
                                <font color="red">&#10006;</font>
                            </xsl:otherwise>
                        </xsl:choose>
                    </td>
                </tr>
            </xsl:when>
            <xsl:otherwise>
                <tr>
                    <td colspan="3">
                        <xsl:call-template name="show-eHDSINullFlavor">
                            <xsl:with-param name="code" select="n1:substanceAdministration/@nullFlavor"/>
                        </xsl:call-template>
                    </td>
                </tr>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
