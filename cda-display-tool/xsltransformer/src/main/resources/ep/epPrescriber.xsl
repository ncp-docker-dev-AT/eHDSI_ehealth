<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:n1="urn:hl7-org:v3"
                version="2.0">

    <xsl:template match="/n1:ClinicalDocument/n1:author">
        <div class="wrap-collapsible">
            <input id="collapsible-prescriber-header" class="toggle" type="checkbox"/>
            <label for="collapsible-prescriber-header" class="lbl-toggle-main">
                <!-- Prescriber -->
                <xsl:call-template name="show-eHDSIDisplayLabel">
                    <xsl:with-param name="code" select="'56'"/>
                </xsl:call-template>
            </label>
            <div class="collapsible-content-main">
                <div class="content-inner-main">
                    <table class="header_table">
                        <xsl:attribute name="id">
                            <xsl:text>prescriberTable</xsl:text>
                        </xsl:attribute>
                        <tr>
                            <th>
                                <!--  Prescriber: -->
                                <xsl:call-template name="show-eHDSIDisplayLabel">
                                    <xsl:with-param name="code" select="'56'"/>
                                </xsl:call-template>
                            </th>
                            <th>
                                <!--  Profession: -->
                                <xsl:call-template name="show-eHDSIDisplayLabel">
                                    <xsl:with-param name="code" select="'64'"/>
                                </xsl:call-template>
                            </th>
                        </tr>
                        <tr>
                            <td>
                                <xsl:apply-templates select="n1:assignedAuthor/n1:assignedPerson/n1:name"/>
                            </td>
                            <td>
                                <xsl:call-template name="show-formCode">
                                    <xsl:with-param name="parameter"
                                                    select="n1:functionCode"/>
                                </xsl:call-template>
                            </td>
                        </tr>
                    </table>
                    <br/>
                    <div class="wrap-collapsible">
                        <input id="collapsible-extended-prescriber" class="toggle" type="checkbox"/>
                        <label for="collapsible-extended-prescriber" class="lbl-toggle">
                            <xsl:call-template name="show-eHDSIDisplayLabel">
                                <xsl:with-param name="code" select="'68'"/>
                            </xsl:call-template>
                        </label>
                        <div class="collapsible-content">
                            <div class="content-inner">
                                <div id="extendedCdaHeader">
                                    <table class="header_table" width="100%">
                                        <colgroup>
                                            <col span="1" style="width: 10%;"/>
                                            <col span="1" style="width: 20%;"/>
                                            <col span="1" style="width: 15%;"/>
                                            <col span="1" style="width: 20%;"/>
                                            <col span="1" style="width: 15%;"/>
                                            <col span="1" style="width: 20%;"/>
                                        </colgroup>

                                        <tbody>
                                            <tr>
                                                <th>
                                                    <!--  Specialty: -->
                                                    <xsl:call-template name="show-eHDSIDisplayLabel">
                                                        <xsl:with-param name="code" select="'69'"/>
                                                    </xsl:call-template>
                                                </th>
                                                <xsl:value-of select="/n1:ClinicalDocument/n1:author/n1:assignedAuthor/n1:code/@displayName"/>
                                                <td>
                                                </td>
                                                <th>
                                                    <!--  Contact Information: -->
                                                    <xsl:call-template name="show-eHDSIDisplayLabel">
                                                        <xsl:with-param name="code" select="'12'"/>
                                                    </xsl:call-template>
                                                </th>
                                                <td>
                                                    <xsl:for-each select="/n1:ClinicalDocument/n1:author/n1:assignedAuthor/n1:telecom">
                                                        <xsl:call-template name="show-telecom">
                                                            <xsl:with-param name="telecom" select="."/>
                                                        </xsl:call-template>
                                                    </xsl:for-each>
                                                </td>
                                                <th>
                                                    <!--  Organisation Name: -->
                                                    <xsl:call-template name="show-eHDSIDisplayLabel">
                                                        <xsl:with-param name="code" select="'47'"/>
                                                    </xsl:call-template>
                                                </th>
                                                <td>
                                                    <xsl:call-template name="organization">
                                                        <xsl:with-param name="name"
                                                                        select="//n1:entry/n1:substanceAdministration[n1:templateId[@root='1.3.6.1.4.1.12559.11.10.1.3.1.3.2']]/n1:participant[@typeCode='AUT']/n1:participantRole[@classCode='LIC']/n1:scopingEntity[@classCode='ORG']/n1:desc"/>
                                                    </xsl:call-template>
                                                </td>
                                            </tr>
                                            <tr>
                                                <th>
                                                    <!-- Facility ID -->
                                                    <xsl:call-template name="show-eHDSIDisplayLabel">
                                                        <xsl:with-param name="code" select="'28'"/>
                                                    </xsl:call-template>
                                                </th>
                                                <td>
                                                    <xsl:call-template name="showId">
                                                        <xsl:with-param name="id"
                                                                        select="/n1:ClinicalDocument/n1:author/n1:assignedAuthor/n1:representedOrganization/n1:id"/>
                                                    </xsl:call-template>
                                                </td>
                                                <th>
                                                    <!-- Facility Name:-->
                                                    <xsl:call-template name="show-eHDSIDisplayLabel">
                                                        <xsl:with-param name="code" select="'29'"/>
                                                    </xsl:call-template>
                                                </th>
                                                <td>
                                                    <xsl:call-template name="facilityName">
                                                        <xsl:with-param name="name"
                                                                        select="/n1:ClinicalDocument/n1:author/n1:assignedAuthor/n1:representedOrganization/n1:name"/>
                                                    </xsl:call-template>
                                                </td>
                                                <th>
                                                    <!--  Organisation Identifier: -->
                                                    <xsl:call-template name="show-eHDSIDisplayLabel">
                                                        <xsl:with-param name="code" select="'46'"/>
                                                    </xsl:call-template>
                                                </th>
                                                <td>
                                                    <xsl:call-template name="showId">
                                                        <xsl:with-param name="id"
                                                                        select="//n1:entry/n1:substanceAdministration[n1:templateId[@root='1.3.6.1.4.1.12559.11.10.1.3.1.3.2']]/n1:participant[@typeCode='AUT']/n1:participantRole[@classCode='LIC']/n1:scopingEntity[@classCode='ORG']/n1:id"/>
                                                    </xsl:call-template>
                                                </td>
                                            </tr>
                                            <tr>
                                                <th>
                                                    <!-- Country: -->
                                                    <xsl:call-template name="show-eHDSIDisplayLabel">
                                                        <xsl:with-param name="code" select="'13'"/>
                                                    </xsl:call-template>
                                                </th>
                                                <td>
                                                    <xsl:call-template name="country">
                                                        <xsl:with-param name="name"
                                                                        select="/n1:ClinicalDocument/n1:author/n1:assignedAuthor/n1:representedOrganization/n1:addr/n1:country"/>
                                                    </xsl:call-template>
                                                </td>
                                                <th>
                                                    <!--  Address: -->
                                                    <xsl:call-template name="show-eHDSIDisplayLabel">
                                                        <xsl:with-param name="code" select="'3'"/>
                                                    </xsl:call-template>
                                                </th>
                                                <td>
                                                    <xsl:call-template name="show-address">
                                                        <xsl:with-param name="address" select="/n1:ClinicalDocument/n1:author/n1:assignedAuthor/n1:representedOrganization/n1:addr"/>
                                                    </xsl:call-template>
                                                </td>
                                                <th>
                                                    <!--  Organization Contact Information -->
                                                    <xsl:call-template name="show-eHDSIDisplayLabel">
                                                        <xsl:with-param name="code" select="'182'"/>
                                                    </xsl:call-template>
                                                </th>
                                                <td>
                                                    <xsl:for-each select="//n1:entry/n1:substanceAdministration[n1:templateId[@root='1.3.6.1.4.1.12559.11.10.1.3.1.3.2']]/n1:participant[@typeCode='AUT']/n1:participantRole[@classCode='LIC']/n1:telecom">
                                                        <xsl:call-template name="show-telecom">
                                                            <xsl:with-param name="telecom" select="."/>
                                                        </xsl:call-template>
                                                    </xsl:for-each>
                                                </td>
                                            </tr>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <br/>
            <br/>
        </div>
    </xsl:template>

    <xsl:template match="n1:assignedAuthor/n1:assignedPerson/n1:name">
        <!--  Prefix : -->
        <xsl:apply-templates select="n1:prefix"/>
        <!--  Family : -->
        <xsl:apply-templates select="n1:given"/>
        <!--  Given : -->
        <xsl:apply-templates select="n1:family"/>
    </xsl:template>

    <xsl:template match="n1:prefix">
        <xsl:value-of select="."/>&#160;
    </xsl:template>

    <xsl:template match="n1:given">
        <xsl:value-of select="."/>&#160;
    </xsl:template>

    <xsl:template match="n1:family">
        <xsl:value-of select="."/>&#160;
    </xsl:template>

    <!-- Contact information -->
    <xsl:template match="n1:assignedAuthor/n1:telecom">
        <xsl:choose>
            <xsl:when test="./@nullFlavor">
                <xsl:call-template name="show-eHDSINullFlavor">
                    <xsl:with-param name="code" select="./@nullFlavor"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="./@value"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- Facility name -->
    <xsl:template match="n1:assignedAuthor/n1:representedOrganization/n1:name">
        <xsl:choose>
            <xsl:when test="./@nullFlavor">
                <xsl:call-template name="show-eHDSINullFlavor">
                    <xsl:with-param name="code" select="./@nullFlavor"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="."/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- Facility ID -->
    <xsl:template match="n1:assignedAuthor/n1:representedOrganization/n1:id">
        <xsl:choose>
            <xsl:when test="./@nullFlavor">
                <xsl:call-template name="show-eHDSINullFlavor">
                    <xsl:with-param name="code" select="./@nullFlavor"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="./@extension"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- Country -->
    <xsl:template match="n1:assignedAuthor/n1:representedOrganization/n1:addr/n1:country">
        <xsl:choose>
            <xsl:when test="./@nullFlavor">
                <xsl:call-template name="show-eHDSINullFlavor">
                    <xsl:with-param name="code" select="./@nullFlavor"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="."/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- Address -->
    <xsl:template match="n1:assignedAuthor/n1:representedOrganization/n1:addr">
        <xsl:call-template name="show-address">
            <xsl:with-param name="address" select="."/>
        </xsl:call-template>
    </xsl:template>

    <!-- Organization -->
    <xsl:template name="organization">
        <xsl:param name="name"/>
        <xsl:choose>
            <xsl:when test="$name/@nullFlavor">
                <xsl:call-template name="show-eHDSINullFlavor">
                    <xsl:with-param name="code" select="$name/@nullFlavor"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$name"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <xsl:template name="showId">
        <xsl:param name="id"/>
        <xsl:choose>
            <xsl:when test="$id/@nullFlavor">
                <xsl:call-template name="show-eHDSINullFlavor">
                    <xsl:with-param name="code" select="$id/@nullFlavor"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$id/@extension"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="facilityName">
        <xsl:param name="name"/>
        <xsl:choose>
            <xsl:when test="$name/@nullFlavor">
                <xsl:call-template name="show-eHDSINullFlavor">
                    <xsl:with-param name="code" select="$name/@nullFlavor"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$name"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="country">
        <xsl:param name="name"/>
        <xsl:choose>
            <xsl:when test="$name/@nullFlavor">
                <xsl:call-template name="show-eHDSINullFlavor">
                    <xsl:with-param name="code" select="$name/@nullFlavor"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$name"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
