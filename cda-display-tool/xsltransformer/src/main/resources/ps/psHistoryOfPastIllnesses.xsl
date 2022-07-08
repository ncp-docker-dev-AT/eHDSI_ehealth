<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:n1="urn:hl7-org:v3" version="2.0">

    <xsl:variable name="historyOfPastIllnessesSectionCode"
                  select="'11348-0'"/>

    <!-- history of past illnesses -->
    <xsl:template name="historyOfPastIllnesses" match="/">
        <xsl:apply-templates select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:code[@code=$historyOfPastIllnessesSectionCode]]"/>
    </xsl:template>

    <xsl:template match = "/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:code[@code=$historyOfPastIllnessesSectionCode]]">
        <div class="wrap-collapsible">
            <input id="collapsible-history-of-past-illnesses-section" class="toggle" type="checkbox" checked="true" />
            <label for="collapsible-history-of-past-illnesses-section" class="lbl-toggle-title">
                <!-- Section title -->
                <xsl:call-template name="show-eHDSISection">
                    <xsl:with-param name="code" select="$historyOfPastIllnessesSectionCode"/>
                </xsl:call-template>
            </label>
            <div class="collapsible-content-title">
                <div class="content-inner-title">
                    <div class="wrap-collapsible">
                        <input id="collapsible-history-of-past-illnesses-original" class="toggle" type="checkbox"/>
                        <label for="collapsible-history-of-past-illnesses-original" class="lbl-toggle">
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
                        <input id="collapsible-history-of-past-illnesses-translated" class="toggle" type="checkbox" checked="true"/>
                        <label for="collapsible-history-of-past-illnesses-translated" class="lbl-toggle">
                            <xsl:value-of select="$translatedCodedTableTitle"/>
                        </label>
                        <div class="collapsible-content">
                            <div class="content-inner">
                                <table class="translation_table">
                                    <tbody>
                                        <tr>
                                            <th>
                                                <!-- Closed Inactive Problem -->
                                                <xsl:call-template name="show-eHDSIDisplayLabel">
                                                    <xsl:with-param name="code" select="'11'"/>
                                                </xsl:call-template>
                                            </th>
                                            <th>
                                                <!-- OnSet Date -->
                                                <xsl:call-template name="show-eHDSIDisplayLabel">
                                                    <xsl:with-param name="code" select="'45'"/>
                                                </xsl:call-template>
                                            </th>
                                            <th>
                                                <!-- End Date -->
                                                <xsl:call-template name="show-eHDSIDisplayLabel">
                                                    <xsl:with-param name="code" select="'26'"/>
                                                </xsl:call-template>
                                            </th>
                                            <th>
                                                <!-- Problem status code -->
                                                <!-- TODO concept to be added to the eHDSIDisplayLabel value set -->
                                                Problem Status Code
                                            </th>
                                        </tr>
                                        <xsl:apply-templates select="n1:entry/n1:act" mode="historyOfPastIllnesses"/>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <br />
        <br />
    </xsl:template>

    <xsl:template match="n1:entry/n1:act" mode="historyOfPastIllnesses">
        <xsl:variable name="problemCondition"
                      select="n1:entryRelationship[@typeCode='SUBJ']/n1:observation/n1:templateId[@root='1.3.6.1.4.1.12559.11.10.1.3.1.3.7']/../n1:value"/>
        <xsl:variable name="problemOnSetDate"
                      select="n1:effectiveTime/n1:low"/>
        <xsl:variable name="problemEndDate"
                      select="n1:effectiveTime/n1:high"/>
        <xsl:variable name="problemStatusCode"
                      select="n1:entryRelationship[@typeCode='SUBJ']/n1:observation/n1:templateId[@root='1.3.6.1.4.1.12559.11.10.1.3.1.3.7']/../n1:entryRelationship[@typeCode='REFR']/n1:observation/n1:templateId[@root='1.3.6.1.4.1.12559.11.10.1.3.1.3.9']/../n1:value"/>

        <xsl:choose>
            <xsl:when test="not(@nullFlavor)">
                <xsl:choose>
                    <!-- known absence / no information scenario -->
                    <xsl:when test="($problemCondition/@code='no-known-problems' or $problemCondition/@code='no-problem-info')">
                        <tr>
                            <td colspan="3">
                                <xsl:call-template name="show-eHDSIAbsentOrUnknownProblem">
                                    <xsl:with-param name="node" select="$problemCondition"/>
                                </xsl:call-template>
                            </td>
                        </tr>
                    </xsl:when>
                    <xsl:otherwise>
                        <tr>
                            <td>
                                <!-- Closed Inactive Problem -->
                                <xsl:call-template name="show-eHDSIIllnessandDisorder">
                                    <xsl:with-param name="node" select="$problemCondition"/>
                                </xsl:call-template>
                                <xsl:if test="not($problemCondition/@nullFlavor)">
                                    <xsl:text> (</xsl:text>
                                    <xsl:value-of select="$problemCondition/@code"/>
                                    <xsl:text>)</xsl:text>
                                </xsl:if>
                            </td>
                            <td>
                                <!-- OnSet Date -->
                                <xsl:call-template name="show-TS">
                                    <xsl:with-param name="node" select="$problemOnSetDate"/>
                                </xsl:call-template>
                            </td>
                            <td>
                                <!-- End Date -->
                                <xsl:call-template name="show-TS">
                                    <xsl:with-param name="node" select="$problemEndDate"/>
                                </xsl:call-template>
                            </td>
                            <td>
                                <!-- Problem Status code -->
                                <xsl:call-template name="show-eHDSIStatusCode">
                                    <xsl:with-param name="node" select="$problemStatusCode"/>
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
                            <xsl:with-param name="code" select="./@nullFlavor"/>
                        </xsl:call-template>
                    </td>
                </tr>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>