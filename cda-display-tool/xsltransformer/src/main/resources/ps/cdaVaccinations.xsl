<?xml version="1.0" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:n1="urn:hl7-org:v3" version="1.0">
    <xsl:output method="html" indent="yes" version="4.01"
                doctype-system="http://www.w3.org/TR/html4/strict.dtd" doctype-public="-//W3C//DTD HTML 4.01//EN"/>

    <!-- variable to check that at least one vaccination section exist -->
    <xsl:variable name="vaccinationsExist"
                  select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section/n1:code[@code='11369-6']"/>

    <!-- vaccinations -->
    <xsl:template name="vaccinations"
                  match="/n1:ClinicalDocument/n1:component/n1:structuredBody">

        <xsl:choose>
            <!-- if we have at least one vaccination section -->
            <xsl:when test="($vaccinationsExist)">
                <xsl:for-each
                        select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section">
                    <xsl:call-template name="vaccinationsSection"/>
                </xsl:for-each>
                <br/>
                <br/>
            </xsl:when>
            <!-- in the case the vaccination section is missing, nothing is displayed -->
        </xsl:choose>
    </xsl:template>

    <xsl:template name="vaccinationsSection">
        <!-- Defining all needed variables -->
        <xsl:variable name="vaccinationsSectionTitleCode" select="n1:code/@code"/>

        <xsl:variable name="vacAct" select="n1:entry/n1:substanceAdministration"/>

        <!-- End definition of variables -->
        <xsl:choose>
            <!-- if sectionTitle is not missing for alerts (Exception alerts section is missing) -->
            <xsl:when test=" ($vaccinationsSectionTitleCode='11369-6')">
                <div class="wrap-collabsible">
                    <input id="collapsible-vaccinations-section-original" class="toggle" type="checkbox" checked="true" />
                    <label for="collapsible-vaccinations-section-original" class="lbl-toggle-title">
                        <!-- Section title -->
                        <xsl:call-template name="show-eHDSISection">
                            <xsl:with-param name="code" select="'11369-6'"/>
                        </xsl:call-template>
                    </label>
                    <div class="collapsible-content-title">
                        <div class="content-inner-title">
                            <xsl:choose>
                                <xsl:when test="$shownarrative='true'">
                                    <div class="wrap-collabsible">
                                        <input id="collapsible-vaccinations-original" class="toggle" type="checkbox"/>
                                        <label for="collapsible-vaccinations-original" class="lbl-toggle">
                                            <xsl:value-of select="$originalNarrativeTableTitle"/>
                                        </label>
                                        <div class="collapsible-content">
                                            <div class="content-inner">
                                                <xsl:call-template name="show-narrative">
                                                    <xsl:with-param name="node" select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section/n1:code[@code='11369-6']/../n1:text"/>
                                                </xsl:call-template>
                                            </div>
                                        </div>
                                    </div>
                                </xsl:when>
                            </xsl:choose>
                            <br/>
                            <!-- nullflavored act -->
                            <div class="wrap-collabsible">
                                <input id="collapsible-vaccinations-translated" class="toggle" type="checkbox" checked="true"/>
                                <label for="collapsible-vaccinations-translated" class="lbl-toggle">
                                    <xsl:value-of select="$translatedCodedTableTitle"/>
                                </label>
                                <div class="collapsible-content">
                                    <div class="content-inner">
                                        <xsl:choose>
                                            <xsl:when test="not ($vacAct/@nullFlavor)">
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
                                                            <xsl:if test="$vacAct/n1:entryRelationship/n1:observation[@classCode='OBS'][@moodCode='EVN']/n1:code[@codeSystem='2.16.840.1.113883.6.1'][@code ='30973-2']">
                                                            <th>
                                                                <!-- Dose number in series header -->
                                                                <!-- TODO Add label to eHDSIDisplayLabel value set-->
                                                                Dose number in series
                                                            </th>
                                                            </xsl:if>
                                                            <th>
                                                                <!-- Administered header -->
                                                                <xsl:call-template name="show-eHDSIDisplayLabel">
                                                                    <xsl:with-param name="code" select="'122'"/>
                                                                </xsl:call-template>
                                                            </th>
                                                        </tr>
                                                        <xsl:for-each select="n1:entry">
                                                            <xsl:call-template name="vaccinationsSectionEntry">
                                                            </xsl:call-template>
                                                        </xsl:for-each>
                                                    </tbody>
                                                </table>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:call-template name="show-eHDSINullFlavor">
                                                    <xsl:with-param name="code" select="$vacAct/@nullFlavor"/>
                                                </xsl:call-template>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <!-- foreach entry of the section -->
    <xsl:template name="vaccinationsSectionEntry">
        <!-- Defining all needed variables -->
        <xsl:variable name="vaccination"
                      select="n1:substanceAdministration/n1:templateId[@root= '2.16.840.1.113883.10.20.1.24']/../n1:consumable/n1:manufacturedProduct/n1:manufacturedMaterial/n1:code"/>

        <xsl:variable name="vaccinationsBrandName"
                      select="n1:substanceAdministration/n1:templateId[@root= '2.16.840.1.113883.10.20.1.24']/../n1:consumable/n1:manufacturedProduct/n1:manufacturedMaterial/n1:name"/>

        <xsl:variable name="vaccinationsDate"
                      select="n1:substanceAdministration/n1:templateId[@root= '2.16.840.1.113883.10.20.1.24']/../n1:effectiveTime"/>

        <xsl:variable name="vaccinationsPosition"
                      select="n1:substanceAdministration/n1:templateId[@root= '2.16.840.1.113883.10.20.1.24']/../n1:entryRelationship/n1:observation[@classCode='OBS'][@moodCode='EVN']/n1:code[@codeSystem='2.16.840.1.113883.6.1'][@code ='30973-2']"/>

        <xsl:variable name="negationInd"
                      select="n1:substanceAdministration/n1:templateId[@root= '2.16.840.1.113883.10.20.1.24']/../@negationInd"/>
        <!-- End definition of variables -->

        <xsl:choose>
            <xsl:when test="not(n1:substanceAdministration/@nullFlavor)">
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
