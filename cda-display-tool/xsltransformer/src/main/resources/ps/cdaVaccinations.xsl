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

        <xsl:variable name="vaccinationsSectionTitle"
                      select="n1:code[@code='11369-6']/@displayName"/>

        <xsl:variable name="nullEntry" select="n1:entry"/>

        <xsl:variable name="vacAct" select="n1:entry/n1:substanceAdministration"/>

        <!-- End definition of variables -->
        <xsl:choose>
            <!-- if sectionTitle is not missing for alerts (Exception alerts section is missing) -->
            <xsl:when test=" ($vaccinationsSectionTitleCode='11369-6')">
                <div class="wrap-collabsible">
                    <input id="collapsible-vaccinations-section-original" class="toggle" type="checkbox" checked="true" />
                    <label for="collapsible-vaccinations-section-original" class="lbl-toggle-title">
                        <xsl:value-of select="$vaccinationsSectionTitle"/>
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
                                                <xsl:apply-templates
                                                        select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section/n1:code[@code='11369-6']/../n1:text/*"/>
                                                <br/>
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
                                                                <!-- Vaccination -->
                                                                <xsl:call-template name="show-displayLabels">
                                                                    <xsl:with-param name="code" select="'79'"/>
                                                                </xsl:call-template>
                                                            </th>
                                                            <th>
                                                                <!-- BrandName -->
                                                                <xsl:call-template name="show-displayLabels">
                                                                    <xsl:with-param name="code" select="'9'"/>
                                                                </xsl:call-template>
                                                            </th>
                                                            <th>
                                                                <!-- Vaccination Date -->
                                                                <xsl:call-template name="show-displayLabels">
                                                                    <xsl:with-param name="code" select="'80'"/>
                                                                </xsl:call-template>
                                                            </th>
                                                            <th>
                                                                <!-- Received -->
                                                                <!-- TODO Make this header part of the epsosDisplayLabel value set -->
                                                                Administered
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
                                                <xsl:call-template name="show-nullFlavor">
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

        <xsl:variable name="negationInd"
                      select="n1:substanceAdministration/n1:templateId[@root= '2.16.840.1.113883.10.20.1.24']/../@negationInd"/>

        <xsl:variable name="vacAct" select="n1:substanceAdministration"/>
        <!-- End definition of variables -->

        <xsl:choose>
            <xsl:when test="not($vacAct/@nullFlavor)">
                <tr>
                    <td>
                        <!-- Display vaccination -->
                        <xsl:call-template name="show-element">
                            <xsl:with-param name="node" select="$vaccination"/>
                        </xsl:call-template>
                    </td>
                    <td>
                        <xsl:value-of select="$vaccinationsBrandName"/>
                    </td>
                    <td>
                        <xsl:call-template name="show-time">
                            <xsl:with-param name="datetime" select="$vaccinationsDate"/>
                        </xsl:call-template>
                        &#160;
                    </td>
                    <td>
                        <xsl:choose>
                            <xsl:when test="(not($negationInd) or $negationInd='false')">
                                    <i class="fa fa-check" style="color:green" aria-hidden="true"/>
                            </xsl:when>
                            <xsl:otherwise>
                                    <i class="fa fa-ban" style="color:red" aria-hidden="true"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </td>
                </tr>
            </xsl:when>
            <xsl:otherwise>
                <tr>
                    <td colspan="3">
                        <xsl:call-template name="show-nullFlavor">
                            <xsl:with-param name="code" select="$vacAct/@nullFlavor"/>
                        </xsl:call-template>
                    </td>
                </tr>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
