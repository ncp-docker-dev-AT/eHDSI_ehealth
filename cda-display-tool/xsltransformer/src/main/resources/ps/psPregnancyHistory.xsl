<?xml version="1.0"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:n1="urn:hl7-org:v3">

    <xsl:variable name="pregnancyHistorySectionCode"
                  select="'10162-6'"/>

    <!-- pregnancy history -->
    <xsl:template name="pregnancyHistory" match="/">
        <xsl:apply-templates select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:code[@code=$pregnancyHistorySectionCode]]"/>
    </xsl:template>

    <xsl:template match = "/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:code[@code=$pregnancyHistorySectionCode]]">
        <div class="wrap-collapsible">
            <input id="collapsible-pregnancy-history-section-original" class="toggle" type="checkbox" checked="true" />
            <label for="collapsible-pregnancy-history-section-original" class="lbl-toggle-title">
                <!-- Section title -->
                <xsl:call-template name="show-eHDSISection">
                    <xsl:with-param name="code" select="$pregnancyHistorySectionCode"/>
                </xsl:call-template>
            </label>
            <div class="collapsible-content-title">
                <div class="content-inner-title">
                    <div class="wrap-collapsible">
                        <input id="collapsible-pregnancy-history-original" class="toggle" type="checkbox"/>
                        <label for="collapsible-pregnancy-history-original" class="lbl-toggle">
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
                        <input id="collapsible-pregnancy-history-translated" class="toggle" type="checkbox" checked="true"/>
                        <label for="collapsible-pregnancy-history-translated" class="lbl-toggle">
                            <xsl:value-of select="$translatedCodedTableTitle"/>
                        </label>
                        <div class="collapsible-content">
                            <div class="content-inner">
                                <table class="translation_table">
                                    <xsl:choose>
                                        <xsl:when test="not(n1:entry/n1:observation/n1:code[@code='82810-3']/../@nullFlavor)">
                                            <tbody>
                                                <tr>
                                                    <th>
                                                        <!-- Current Pregnancy Status -->
                                                        <!-- TODO Add concept to eHDSIDisplayLabel value set -->
                                                        Current Pregnancy Status
                                                    </th>
                                                    <th>
                                                        <!-- Observation Date -->
                                                        <!-- TODO Add concept to eHDSIDisplayLabel value set -->
                                                        Observation Date
                                                    </th>
                                                    <th>
                                                        <!-- Status -->
                                                        <!-- TODO Add concept to eHDSIDisplayLabel value set -->
                                                        Status
                                                    </th>
                                                    <th>
                                                        <!-- Pregnancy observation code -->
                                                        <xsl:call-template name="show-eHDSIPregnancyInformation">
                                                            <xsl:with-param name="node" select="n1:entry/n1:observation/n1:code[@code='82810-3']/../n1:entryRelationship[@typeCode='COMP']/n1:observation/n1:code"/>
                                                        </xsl:call-template>
                                                    </th>
                                                    <xsl:apply-templates select="n1:entry/n1:observation/n1:code[@code='82810-3']" mode="currentPregnancies"/>
                                                </tr>
                                            </tbody>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <tr>
                                                <td>
                                                    <xsl:call-template name="show-eHDSINullFlavor">
                                                        <xsl:with-param name="code" select="n1:entry/n1:observation/n1:code[@code='82810-3']/../@nullFlavor"/>
                                                    </xsl:call-template>
                                                </td>
                                            </tr>
                                        </xsl:otherwise>
                                    </xsl:choose>
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

    <xsl:template match="n1:entry/n1:observation/n1:code[@code='82810-3']" mode="currentPregnancies">
        <tr>
            <td/>
            <!-- Observation Date -->
            <td>
                <xsl:call-template name="show-IVL_TS">
                    <xsl:with-param name="node" select="../n1:effectiveTime"/>
                </xsl:call-template>
            </td>
            <td>
                <!-- Status -->
                <xsl:call-template name="show-eHDSICurrentPregnancyStatus">
                    <xsl:with-param name="node" select="../n1:value"/>
                </xsl:call-template>
            </td>
            <td>
                <!-- Delivery date estimated -->
                <xsl:call-template name="show-TS">
                    <xsl:with-param name="node" select="../n1:entryRelationship[@typeCode='COMP']/n1:observation/n1:value"/>
                </xsl:call-template>
            </td>
        </tr>
    </xsl:template>
</xsl:stylesheet>