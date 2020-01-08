<?xml version="1.0"  ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:n1="urn:hl7-org:v3"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:epsos="urn:epsos-org:ep:medication" version="1.0">

    <xsl:output method="html" indent="yes" version="4.01" doctype-system="http://www.w3.org/TR/html4/strict.dtd"
                doctype-public="-//W3C//DTD HTML 4.01//EN"/>

    <!-- variable to check that at least one medical device section exists -->
    <xsl:variable name="medicalDevicesExist"
                  select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section/n1:code[@code='46264-8']"/>

    <!--medical devices -->
    <xsl:template name="medicalDevices" match="/n1:ClinicalDocument/n1:component/n1:structuredBody">
        <xsl:choose>
            <!-- if we have at least one medical device section -->
            <xsl:when test="($medicalDevicesExist)">
                <xsl:for-each select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section">
                    <xsl:call-template name="medicalDevicesSection"/>
                </xsl:for-each>
                <br/>
                <br/>
            </xsl:when>
            <!-- else -->
            <xsl:otherwise>
                <span class="sectionTitle">
                    <!-- The Medical devices and implants section is missing -->
                    <xsl:choose>
                        <xsl:when test=" ($documentCode='60591-5')">
                            <xsl:call-template name="show-epSOSDisplayLabels">
                                <xsl:with-param name="code" select="'74'"/>
                            </xsl:call-template>
                        </xsl:when>
                    </xsl:choose>
                </span>
                <br/>
                <br/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="medicalDevicesSection">
        <!-- Define all needed variables -->
        <xsl:variable name="medicalDevicesSectionTitleCode" select="n1:code/@code"/>
        <xsl:variable name="medDevAct" select="n1:entry/n1:supply"/>
        <xsl:variable name="playingDeviceCode" select="$medDevAct/n1:participant/n1:participantRole/n1:playingDevice/n1:code/@code"/>

        <!-- End definition of variables-->
        <xsl:choose>
            <!-- if sectionTitle is not missing for alerts  (Exception alerts section is missing)-->
            <xsl:when test=" ($medicalDevicesSectionTitleCode='46264-8')">
                <div class="wrap-collabsible">
                    <input id="collapsible-medication-medical-devices-original" class="toggle" type="checkbox" checked="true" />
                    <label for="collapsible-medication-medical-devices-original" class="lbl-toggle-title">
                        <!-- Section title -->
                        <xsl:call-template name="show-epSOSSections">
                            <xsl:with-param name="code" select="'46264-8'"/>
                        </xsl:call-template>
                    </label>
                    <div class="collapsible-content-title">
                        <div class="content-inner-title">
                            <xsl:choose>
                                <xsl:when test="$shownarrative='true'">
                                    <div class="wrap-collabsible">
                                        <input id="collapsible-medical-devices-original" class="toggle" type="checkbox"/>
                                        <label for="collapsible-medical-devices-original" class="lbl-toggle">
                                            <xsl:value-of select="$originalNarrativeTableTitle"/>
                                        </label>
                                        <div class="collapsible-content">
                                            <div class="content-inner">
                                                <xsl:call-template name="show-narrative">
                                                    <xsl:with-param name="node" select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section/n1:code[@code='46264-8']/../n1:text"/>
                                                </xsl:call-template>
                                            </div>
                                        </div>
                                    </div>
                                </xsl:when>
                            </xsl:choose>
                            <br/>
                            <div class="wrap-collabsible">
                                <input id="collapsible-medical-devices-translated" class="toggle" type="checkbox" checked="true"/>
                                <label for="collapsible-medical-devices-translated" class="lbl-toggle">
                                    <xsl:value-of select="$translatedCodedTableTitle"/>
                                </label>
                                <div class="collapsible-content">
                                    <div class="content-inner">
                                        <xsl:choose>
                                            <xsl:when test="not($medDevAct/@nullFlavor)">
                                                <table class="translation_table">
                                                    <tbody>
                                                        <xsl:if test="not ($playingDeviceCode='no-known-devices' or $playingDeviceCode='no-device-info')">
                                                            <tr>
                                                                <th>
                                                                    <!-- Device Implant -->
                                                                    <xsl:call-template name="show-epSOSDisplayLabels">
                                                                        <xsl:with-param name="code" select="'21'"/>
                                                                    </xsl:call-template>
                                                                </th>
                                                                <th>
                                                                    <!-- Implant Date -->
                                                                    <xsl:call-template name="show-epSOSDisplayLabels">
                                                                        <xsl:with-param name="code" select="'36'"/>
                                                                    </xsl:call-template>
                                                                </th>
                                                            </tr>
                                                        </xsl:if>
                                                        <xsl:for-each select="n1:entry">
                                                            <xsl:call-template name="medicalDevicesSectionEntry">
                                                            </xsl:call-template>
                                                        </xsl:for-each>
                                                    </tbody>
                                                </table>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <br/>
                                                <xsl:call-template name="show-epSOSNullFlavor">
                                                    <xsl:with-param name="code" select="$medDevAct/@nullFlavor"/>
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

    <!--  FOR EACH ENTRY -->
    <xsl:template name="medicalDevicesSectionEntry">
        <!-- Defining all needed variables -->
        <xsl:variable name="medDevAct" select="n1:supply"/>
        <xsl:variable name="medDeviceImplantDescription"
                      select="n1:supply/n1:templateId[@root= '1.3.6.1.4.1.12559.11.10.1.3.1.3.5']/../n1:participant[@typeCode='DEV']/n1:participantRole/n1:playingDevice/n1:code"/>
        <xsl:variable name="medDeviceImplantDate"
                      select="n1:supply/n1:templateId[@root= '1.3.6.1.4.1.12559.11.10.1.3.1.3.5']/../n1:effectiveTime"/>
        <!-- End definition of variables-->

        <!-- null flavor act -->
        <xsl:choose>
            <xsl:when test="not($medDevAct/@nullFlavor)">
                <xsl:choose>
                    <xsl:when test="($medDeviceImplantDescription/@code='no-known-devices' or $medDeviceImplantDescription/@code='no-device-info')">
                        <tr>
                            <td colspan="2">
                                <xsl:call-template name="show-eHDSI-AbsentOrUnknownDevices">
                                    <xsl:with-param name="node" select="$medDeviceImplantDescription"/>
                                </xsl:call-template>
                            </td>
                        </tr>
                    </xsl:when>
                    <xsl:otherwise>
                        <tr>
                            <td>
                                <!-- Device Implant -->
                                <xsl:call-template name="show-epSOSMedicalDevices">
                                    <xsl:with-param name="node" select="$medDeviceImplantDescription"/>
                                </xsl:call-template>
                            </td>
                            <td>
                                <!-- Implant Date -->
                                <xsl:call-template name="show-TS">
                                    <xsl:with-param name="node" select="$medDeviceImplantDate"/>
                                </xsl:call-template>
                            </td>
                        </tr>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <tr>
                    <td colspan="3">
                        <xsl:call-template name="show-epSOSNullFlavor">
                            <xsl:with-param name="code" select="$medDevAct/@nullFlavor"/>
                        </xsl:call-template>
                    </td>
                </tr>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
