<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:n1="urn:hl7-org:v3"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:epsos="urn:epsos-org:ep:medication">
    <xsl:output method="html" indent="yes" version="4.01" doctype-system="http://www.w3.org/TR/html4/strict.dtd"
                doctype-public="-//W3C//DTD HTML 4.01//EN"/>


    <!-- variable to check that at least one social history section exist -->
    <xsl:variable name="socialHistoryExist"
                  select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section/n1:code[@code='29762-2']"/>

    <!--social histories -->
    <xsl:template name="socialHistory" match="/n1:ClinicalDocument/n1:component/n1:structuredBody">

        <xsl:choose>
            <!-- if we have at least one social history section -->
            <xsl:when test="($socialHistoryExist)">

                <xsl:for-each select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section">
                    <xsl:call-template name="socialHistorySection"/>
                </xsl:for-each>
                <br/>
                <br/>
            </xsl:when>
            <!-- in the case the social history section is missing, nothing is displayed -->
        </xsl:choose>
    </xsl:template>


    <xsl:template name="socialHistorySection">

        <!-- Defining all needed variables -->
        <xsl:variable
                name="socialHistorySectionTitleCode"
                select="n1:code/@code"/>
        <xsl:variable
                name="nullEntry"
                select="n1:entry"/>
        <xsl:variable name="socHistAct"
                      select="n1:entry/n1:observation"/>
        <!-- End definition of variables-->

        <xsl:choose>
            <!-- if sectionTitle is not missing for social history  (Exception social history section is missing)-->
            <xsl:when test=" ($socialHistorySectionTitleCode='29762-2')">
                <div class="wrap-collabsible">
                    <input id="collapsible-social-history-section-original" class="toggle" type="checkbox" checked="true" />
                    <label for="collapsible-social-history-section-original" class="lbl-toggle-title">
                        <!-- Section title -->
                        <xsl:call-template name="show-epSOSSections">
                            <xsl:with-param name="code" select="'29762-2'"/>
                        </xsl:call-template>
                    </label>
                    <div class="collapsible-content-title">
                        <div class="content-inner-title">
                            <xsl:choose>
                                <xsl:when test="$shownarrative='true'">
                                    <div class="wrap-collabsible">
                                        <input id="collapsible-social-history-original" class="toggle" type="checkbox"/>
                                        <label for="collapsible-social-history-original" class="lbl-toggle">
                                            <xsl:value-of select="$originalNarrativeTableTitle"/>
                                        </label>
                                        <div class="collapsible-content">
                                            <div class="content-inner">
                                                <xsl:call-template name="show-narrative">
                                                    <xsl:with-param name="node" select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section/n1:code[@code='29762-2']/../n1:text"/>
                                                </xsl:call-template>
                                            </div>
                                        </div>
                                    </div>
                                </xsl:when>
                            </xsl:choose>
                            <br/>
                            <div class="wrap-collabsible">
                                <input id="collapsible-social-history-translated" class="toggle" type="checkbox" checked="true"/>
                                <label for="collapsible-social-history-translated" class="lbl-toggle">
                                    <xsl:value-of select="$translatedCodedTableTitle"/>
                                </label>
                                <div class="collapsible-content">
                                    <div class="content-inner">
                                        <xsl:choose>
                                            <xsl:when test="not($socHistAct/@nullFlavor)">
                                                <table class="translation_table">
                                                    <tbody>
                                                        <tr>
                                                            <th>
                                                                <!-- Observation Type -->
                                                                <xsl:call-template name="show-epSOSDisplayLabels">
                                                                    <xsl:with-param name="code" select="'44'"/>
                                                                </xsl:call-template>
                                                            </th>
                                                            <th>
                                                                <!-- Date From  -->
                                                                <xsl:call-template name="show-epSOSDisplayLabels">
                                                                    <xsl:with-param name="code" select="'85'"/>
                                                                </xsl:call-template>
                                                            </th>
                                                            <th>
                                                                <!-- Date To -->
                                                                <xsl:call-template name="show-epSOSDisplayLabels">
                                                                    <xsl:with-param name="code" select="'18'"/>
                                                                </xsl:call-template>

                                                            </th>
                                                            <th>
                                                                <!--  Observation Value -->
                                                                <xsl:call-template name="show-epSOSDisplayLabels">
                                                                    <xsl:with-param name="code" select="'84'"/>
                                                                </xsl:call-template>
                                                            </th>
                                                        </tr>
                                                        <xsl:for-each select="n1:entry">
                                                            <xsl:call-template name="socialHistorySectionEntry">
                                                            </xsl:call-template>
                                                        </xsl:for-each>
                                                    </tbody>
                                                </table>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <tr>
                                                    <td colspan="3">
                                                        <xsl:call-template name="show-epSOSNullFlavor">
                                                            <xsl:with-param name="code" select="$socHistAct/@nullFlavor"/>
                                                        </xsl:call-template>
                                                    </td>
                                                </tr>
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

    <!--Social  History  Section Entry-->
    <xsl:template name="socialHistorySectionEntry">
        <!-- Defining all needed variables -->
        <xsl:variable name="socHistAct"
                      select="n1:observation"/>
        <xsl:variable
                name="socialHistoryObservationType"
                select="n1:observation/n1:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.4.13.4']/../n1:code"/>
        <xsl:variable
                name="socialHistoryDateFrom"
                select="n1:observation/n1:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.4.13.4']/../n1:effectiveTime/n1:low"/>
        <xsl:variable
                name="socialHistoryDateTo"
                select="n1:observation/n1:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.4.13.4']/../n1:effectiveTime/n1:high"/>
        <xsl:variable
                name="socialHistoryObservationValue"
                select="n1:observation/n1:templateId[@root= '1.3.6.1.4.1.19376.1.5.3.1.4.13.4']/../n1:value"/>
        <!-- End definition of variables-->

        <!-- nullflavored act -->
        <xsl:choose>
            <xsl:when test="not($socHistAct/@nullFlavor)">
                <tr>
                    <td>
                        <!-- Observation Type -->
                        <xsl:call-template name="show-epSOSSocialHistory">
                            <xsl:with-param name="node" select="$socialHistoryObservationType"/>
                        </xsl:call-template>
                    </td>
                    <td>
                        <!-- Date From -->
                        <xsl:call-template name="show-TS">
                            <xsl:with-param name="node" select="$socialHistoryDateFrom"/>
                        </xsl:call-template>
                    </td>
                    <td>
                        <!-- Date To -->
                        <xsl:call-template name="show-TS">
                            <xsl:with-param name="node" select="$socialHistoryDateTo"/>
                        </xsl:call-template>
                    </td>
                    <td>
                        <!-- Observation Value -->
                        <xsl:call-template name="show-PQ">
                            <xsl:with-param name="node" select="$socialHistoryObservationValue"/>
                        </xsl:call-template>
                    </td>
                </tr>
            </xsl:when>
            <xsl:otherwise>
                <tr>
                    <td colspan="3">
                        <xsl:call-template name="show-epSOSNullFlavor">
                            <xsl:with-param name="code" select="$socHistAct/@nullFlavor"/>
                        </xsl:call-template>
                    </td>
                </tr>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>