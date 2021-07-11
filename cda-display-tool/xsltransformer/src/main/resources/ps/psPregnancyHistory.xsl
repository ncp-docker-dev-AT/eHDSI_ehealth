<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:n1="urn:hl7-org:v3">

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
                                        <xsl:when test="not(n1:entry/n1:observation/@nullFlavor)">
                                            <tbody>
                                                <tr>
                                                    <th>
                                                        <xsl:call-template name="show-eHDSIPregnancyInformation">
                                                            <xsl:with-param name="node" select="n1:entry/n1:observation/n1:code"/>
                                                        </xsl:call-template>
                                                    </th>
                                                    <xsl:apply-templates select="n1:entry/n1:observation" mode="pregnancyhistory" />
                                                </tr>
                                            </tbody>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <tr>
                                                <td>
                                                    <xsl:call-template name="show-eHDSINullFlavor">
                                                        <xsl:with-param name="code" select="n1:entry/n1:observation/@nullFlavor"/>
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

    <!-- Pregnancy History Section Entry-->
    <xsl:template match="n1:entry/n1:observation" mode="pregnancyhistory">
        <td>
            <xsl:apply-templates select="n1:value"/>
        </td>
    </xsl:template>

    <xsl:template match="n1:value">
    <xsl:call-template name="show-TS">
        <xsl:with-param name="node" select="."/>
    </xsl:call-template>
    </xsl:template>
</xsl:stylesheet>