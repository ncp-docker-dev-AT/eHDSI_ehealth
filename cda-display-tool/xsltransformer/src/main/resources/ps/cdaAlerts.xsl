<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:n1="urn:hl7-org:v3" version="1.0">

    <xsl:output method="html" indent="yes" version="4.01" doctype-system="http://www.w3.org/TR/html4/strict.dtd"
                doctype-public="-//W3C//DTD HTML 4.01//EN"/>

    <!-- variable to check that at least one alert section exist -->
    <xsl:variable name="alertsExist"
                  select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section/n1:code[@code='48765-2']"/>
    <xsl:variable
            name="reactionTypeNarrative"
            select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section/n1:code[@code='48765-2']/../n1:text"/>

    <!--alerts -->
    <xsl:template name="alerts" match="/n1:ClinicalDocument/n1:component/n1:structuredBody">
        <xsl:choose>
            <!-- if we have at least one alert section -->
            <xsl:when test="($alertsExist)">
                <xsl:for-each select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section">
                    <xsl:call-template name="alertSection"/>
                </xsl:for-each>
                <br/>
                <br/>
            </xsl:when>
            <!-- else -->
            <xsl:otherwise>
                <span class="sectionTitle">
                    <!-- The Allergies, adverse reactions, alerts section is missing ! -->
                    <xsl:choose>
                        <xsl:when test=" ($documentCode='60591-5')">
                            <xsl:call-template name="show-eHDSIDisplayLabel">
                                <xsl:with-param name="code" select="'73'"/>
                            </xsl:call-template>
                        </xsl:when>
                    </xsl:choose>
                </span>
                <br/>
                <br/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="alertSection">
        <!-- Defining all needed variables -->
        <xsl:variable name="sectionCode"
                      select="n1:code"/>
        <xsl:variable name="act"
                      select="n1:entry/n1:act"/>
        <xsl:variable name="observation"
                      select="$act/n1:templateId[@root='2.16.840.1.113883.10.20.1.27']/../n1:entryRelationship[@typeCode='SUBJ']/n1:observation/n1:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.4.6']/.."/>
        <xsl:variable name="observationValue"
                      select="$observation/n1:value"/>
        <!-- End definition of variables-->

        <!--- table Display -->
        <xsl:choose>
            <!-- If sectionTitle is not missing for alerts  (Exception alerts section is missing)-->
            <xsl:when test="($sectionCode/@code='48765-2')">
                <div class="wrap-collabsible">
                    <input id="collapsible-alerts-section-original" class="toggle" type="checkbox" checked="true" />
                    <label for="collapsible-alerts-section-original" class="lbl-toggle-title">
                        <!-- Section title -->
                        <xsl:call-template name="show-eHDSISection">
                            <xsl:with-param name="code" select="'48765-2'"/>
                        </xsl:call-template>
                    </label>
                    <div class="collapsible-content-title">
                        <div class="content-inner-title">
                            <xsl:choose>
                                <xsl:when test="$shownarrative='true'">
                                    <div class="wrap-collabsible">
                                        <input id="collapsible-alerts-original" class="toggle" type="checkbox"/>
                                        <label for="collapsible-alerts-original" class="lbl-toggle">
                                            <xsl:value-of select="$originalNarrativeTableTitle"/>
                                        </label>
                                        <div class="collapsible-content">
                                            <div class="content-inner">
                                                <xsl:call-template name="show-narrative">
                                                    <xsl:with-param name="node" select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section/n1:code[@code='48765-2']/../n1:text"/>
                                                </xsl:call-template>
                                            </div>
                                        </div>
                                    </div>
                                </xsl:when>
                            </xsl:choose>
                            <br/>
                            <!-- nullflavored act -->
                            <div class="wrap-collabsible">
                                <input id="collapsible-alerts-translated" class="toggle" type="checkbox" checked="true"/>
                                <label for="collapsible-alerts-translated" class="lbl-toggle">
                                    <xsl:value-of select="$translatedCodedTableTitle"/>
                                </label>
                                <div class="collapsible-content">
                                    <div class="content-inner">
                                        <xsl:choose>
                                            <xsl:when test="not($act/@nullFlavor)">
                                                <table class="translation_table">
                                                    <tbody>
                                                        <xsl:if test="not ($observationValue/@code='no-known-allergies' or $observationValue/@code='no-allergy-info')">
                                                            <tr>
                                                                <th>
                                                                    <!-- Reaction Type header -->
                                                                    <xsl:call-template name="show-eHDSIDisplayLabel">
                                                                        <xsl:with-param name="code" select="'65'"/>
                                                                    </xsl:call-template>
                                                                </th>
                                                                <th>
                                                                    <!-- Clinical Manifestation header -->
                                                                    <xsl:call-template name="show-eHDSIDisplayLabel">
                                                                        <xsl:with-param name="code" select="'10'"/>
                                                                    </xsl:call-template>
                                                                </th>
                                                                <th>
                                                                    <!-- Agent header -->
                                                                    <xsl:call-template name="show-eHDSIDisplayLabel">
                                                                        <xsl:with-param name="code" select="'5'"/>
                                                                    </xsl:call-template>
                                                                </th>
                                                                <th>
                                                                    <!-- OnSet Date header -->
                                                                    <xsl:call-template name="show-eHDSIDisplayLabel">
                                                                        <xsl:with-param name="code" select="'45'"/>
                                                                    </xsl:call-template>
                                                                </th>
                                                                <th>
                                                                    <!-- Severity header -->
                                                                    <xsl:call-template name="show-eHDSIDisplayLabel">
                                                                        <xsl:with-param name="code" select="'123'"/>
                                                                    </xsl:call-template>
                                                                </th>
                                                            </tr>
                                                        </xsl:if>
                                                        <xsl:for-each select="n1:entry">
                                                            <xsl:call-template name="alertSectionEntry">
                                                            </xsl:call-template>
                                                        </xsl:for-each>
                                                    </tbody>
                                                </table>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:call-template name="show-eHDSINullFlavor">
                                                    <xsl:with-param name="code" select="$act/@nullFlavor"/>
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
    <!-- alert section entry -->

    <xsl:template name="alertSectionEntry">
        <!-- Defining all needed variables -->
        <xsl:variable name="observation"
                      select="n1:act/n1:templateId[@root= '2.16.840.1.113883.10.20.1.27']/../n1:entryRelationship[@typeCode='SUBJ']/n1:observation/n1:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.4.6']/.."/>
        <xsl:variable name="reactionType"
                      select="$observation/n1:code"/>
        <xsl:variable name="clinicalManifestation"
                      select="$observation/n1:entryRelationship[@typeCode='MFST']/n1:observation/n1:templateId[@root='1.3.6.1.4.1.12559.11.10.1.3.1.3.7']/../n1:value"/>
        <xsl:variable name="agentCode"
                      select="$observation/n1:participant[@typeCode='CSM']/n1:participantRole[@classCode='MANU']/n1:playingEntity[@classCode='MMAT']/n1:code"/>
        <xsl:variable name="onSetDate"
                      select="$observation/n1:effectiveTime/n1:low"/>
        <xsl:variable name="obsValue"
                      select="$observation/n1:value"/>
        <xsl:variable name="severity"
                      select="n1:act/n1:templateId[@root='2.16.840.1.113883.10.20.1.27']/../n1:entryRelationship[@typeCode='SUBJ']/n1:observation/n1:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.4.6']/../n1:entryRelationship/n1:observation/n1:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.4.1']/../n1:value"/>
        <!-- End definition of variables-->

        <xsl:choose>
            <xsl:when test="not(n1:act/@nullFlavor)">
                <!-- No info Scenario... observation.value@code is one of the two values -->
                <xsl:choose>
                    <xsl:when test="($obsValue/@code='no-known-allergies' or $obsValue/@code='no-allergy-info')">
                        <tr>
                            <td colspan="4">
                                <xsl:call-template name="show-eHDSIAbsentOrUnknownAllergy">
                                    <xsl:with-param name="node" select="$obsValue"/>
                                </xsl:call-template>
                            </td>
                        </tr>
                    </xsl:when>
                    <xsl:otherwise>
                        <tr>
                            <td>
                                <!-- Reaction Type -->
                                <xsl:call-template name="show-eHDSIAdverseEventType">
                                    <xsl:with-param name="node" select="$reactionType"/>
                                </xsl:call-template>
                            </td>
                            <td>
                                <!-- Clinical Manifestation -->
                                <xsl:choose>
                                    <xsl:when test="not($clinicalManifestation/@nullFlavor)">
                                        <xsl:call-template name="show-eHDSIReactionAllergy">
                                            <xsl:with-param name="node" select="$clinicalManifestation"/>
                                        </xsl:call-template>
                                        <xsl:call-template name="show-eHDSIIllnessandDisorder">
                                            <xsl:with-param name="node" select="$clinicalManifestation"/>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:call-template name="show-eHDSINullFlavor">
                                            <xsl:with-param name="code" select="$clinicalManifestation/@nullFlavor"/>
                                        </xsl:call-template>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                            <td>
                                <!-- Agent -->
                                <xsl:choose>
                                    <xsl:when test="not($agentCode/@nullFlavor)">
                                        <xsl:call-template name="show-eHDSIAllergenNoDrug">
                                            <xsl:with-param name="node" select="$agentCode"/>
                                        </xsl:call-template>
                                        <xsl:call-template name="show-eHDSIActiveIngredient">
                                            <xsl:with-param name="node" select="$agentCode"/>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:call-template name="show-eHDSINullFlavor">
                                            <xsl:with-param name="code" select="$agentCode/@nullFlavor"/>
                                        </xsl:call-template>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                            <td>
                                <!-- OnSet Date -->
                                <xsl:call-template name="show-TS">
                                    <xsl:with-param name="node" select="$onSetDate"/>
                                </xsl:call-template>
                            </td>
                            <td>
                                <!-- Severity -->
                                <xsl:call-template name="show-eHDSISeverity">
                                    <xsl:with-param name="node" select="$severity"/>
                                </xsl:call-template>
                            </td>
                        </tr>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <tr>
                    <td colspan="3">
                        <xsl:call-template name="show-eHDSINullFlavor">
                            <xsl:with-param name="code" select="n1:act/@nullFlavor"/>
                        </xsl:call-template>
                    </td>
                </tr>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>