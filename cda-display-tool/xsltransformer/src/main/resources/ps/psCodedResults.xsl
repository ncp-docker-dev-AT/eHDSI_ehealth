<?xml version="1.0"  ?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:n1="urn:hl7-org:v3">

    <xsl:variable name="codedResultsSectionCode"
                  select="'30954-2'"/>

    <!-- eHDSI Coded Results -->
    <xsl:template name="codedResults" match="/">
        <xsl:apply-templates select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:code[@code=$codedResultsSectionCode]]"/>
    </xsl:template>

    <xsl:template match = "/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:code[@code=$codedResultsSectionCode]]">
        <div class="wrap-collapsible">
            <input id="collapsible-coded-results-section" class="toggle" type="checkbox" checked="true" />
            <label for="collapsible-coded-results-section" class="lbl-toggle-title">
                <!-- Section title -->
                <xsl:call-template name="show-eHDSISection">
                    <xsl:with-param name="code" select="$codedResultsSectionCode"/>
                </xsl:call-template>
            </label>
            <div class="collapsible-content-title">
                <div class="content-inner-title">
                    <div class="wrap-collapsible">
                        <input id="collapsible-coded-results-original" class="toggle" type="checkbox"/>
                        <label for="collapsible-coded-results-original" class="lbl-toggle">
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
                        <input id="collapsible-coded-results-translated" class="toggle" type="checkbox" checked="true"/>
                        <label for="collapsible-coded-results-translated" class="lbl-toggle">
                            <xsl:value-of select="$translatedCodedTableTitle"/>
                        </label>
                        <div class="collapsible-content">
                            <div class="content-inner">
                                <table class="translation_table">
                                    <tbody>
                                        <tr>
                                            <th>
                                                <!-- Diagnostic Date -->
                                                <xsl:call-template name="show-eHDSIDisplayLabel">
                                                    <xsl:with-param name="code" select="'118'"/>
                                                </xsl:call-template>
                                            </th>
                                            <th>
                                                <!-- Result type Header -->
                                                <xsl:call-template name="show-eHDSIDisplayLabel">
                                                    <xsl:with-param name="code" select="'159'"/>
                                                </xsl:call-template>
                                            </th>
                                            <th>
                                                <!-- Result value Header -->
                                                <xsl:call-template name="show-eHDSIDisplayLabel">
                                                    <xsl:with-param name="code" select="'160'"/>
                                                </xsl:call-template>
                                            </th>
                                            <th>
                                                <!-- Performer Header -->
                                                <xsl:call-template name="show-eHDSIDisplayLabel">
                                                    <xsl:with-param name="code" select="'161'"/>
                                                </xsl:call-template>
                                            </th>
                                            <th>
                                                <!-- Reporter Header -->
                                                <xsl:call-template name="show-eHDSIDisplayLabel">
                                                    <xsl:with-param name="code" select="'162'"/>
                                                </xsl:call-template>
                                            </th>
                                        </tr>
                                        <xsl:choose>
                                            <xsl:when test="n1:entry/@nullFlavor">
                                                <tr>
                                                    <td colspan="2">
                                                        <xsl:call-template name="show-eHDSINullFlavor">
                                                            <xsl:with-param name="code" select="n1:entry/@nullFlavor"/>
                                                        </xsl:call-template>
                                                    </td>
                                                </tr>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:apply-templates select="n1:entry/n1:observation" mode="codedResultsBloodGroup"/>
                                                <xsl:apply-templates select="n1:entry/n1:organizer/n1:component/n1:observation" mode="codedResults"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
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

    <xsl:template match="n1:entry/n1:observation" mode="codedResultsBloodGroup">
        <tr>
            <td>
                <xsl:call-template name="show-TS">
                    <xsl:with-param name="node" select="n1:code[@code='34530-6']/../n1:effectiveTime"/>
                </xsl:call-template>
            </td>
            <td>
                <!-- TODO add value to value Set in MVC -->
                <xsl:value-of select="n1:code[@code='34530-6']/@displayName"/>
            </td>
            <td>
                <xsl:call-template name="show-eHDSIBloodGroup">
                    <xsl:with-param name="node" select="n1:code[@code='34530-6']/../n1:value"/>
                </xsl:call-template>
            </td>
            <td/>
            <td/>
        </tr>
    </xsl:template>

    <xsl:template match="n1:entry/n1:organizer/n1:component/n1:observation" mode="codedResults">
        <tr>
            <td>
                <xsl:call-template name="show-IVL_TS">
                    <xsl:with-param name="node" select="n1:effectiveTime"/>
                </xsl:call-template>
            </td>
            <td>
                <!-- TODO Value set to be added in MVC -->
                <xsl:value-of select="n1:code/@displayName"/>
            </td>
            <td>
                <xsl:choose>
                    <!-- TODO Value set to be added in MVC -->
                    <xsl:when test="n1:value/@xsi:type='CD'">
                       <xsl:value-of select="n1:value/@displayName"/>
                    </xsl:when>
                    <xsl:when test="n1:value/@xsi:type='CE'">
                        <xsl:value-of select="n1:value/@displayName"/>
                    </xsl:when>
                    <xsl:when test="n1:value/@xsi:type='PQ'">
                        <xsl:call-template name="show-PQ">
                            <xsl:with-param name="node" select="n1:value"/>
                        </xsl:call-template>
                    </xsl:when>
                </xsl:choose>
            </td>
            <td>
                <xsl:call-template name="show-performer">
                    <xsl:with-param name="node" select="../../n1:performer"/>
                </xsl:call-template>
            </td>
            <td>
                <xsl:call-template name="show-author">
                    <xsl:with-param name="node" select="../../n1:author"/>
                </xsl:call-template>
            </td>
        </tr>
    </xsl:template>
</xsl:stylesheet>