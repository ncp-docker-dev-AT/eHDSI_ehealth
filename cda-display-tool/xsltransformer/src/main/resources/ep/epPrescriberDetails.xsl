<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:n1="urn:hl7-org:v3"
                version="1.0">

    <xsl:output method="html"
                indent="yes"
                version="4.01"
                doctype-system="http://www.w3.org/TR/html4/strict.dtd"
                doctype-public="-//W3C//DTD HTML 4.01//EN"/>

    <xsl:template name="telecom">
        <xsl:param name="telecomParam"/>
        <xsl:choose>
            <xsl:when test="$telecomParam/@nullFlavor">
                <xsl:call-template name="show-epSOSNullFlavor">
                    <xsl:with-param name="code" select="$telecomParam/@nullFlavor"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$telecomParam/@value"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="facilityName">
        <xsl:param name="name"/>
        <xsl:choose>
            <xsl:when test="$name/@nullFlavor">
                <xsl:call-template name="show-epSOSNullFlavor">
                    <xsl:with-param name="code" select="$name/@nullFlavor"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$name"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="facilityId">
        <xsl:param name="id"/>
        <xsl:choose>
            <xsl:when test="$id/@nullFlavor">
                <xsl:call-template name="show-epSOSNullFlavor">
                    <xsl:with-param name="code" select="$id/@nullFlavor"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$id/@extension"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="country">
        <xsl:param name="name"/>
        <xsl:choose>
            <xsl:when test="$name/@nullFlavor">
                <xsl:call-template name="show-epSOSNullFlavor">
                    <xsl:with-param name="code" select="$name/@nullFlavor"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$name"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="organization">
        <xsl:param name="name"/>
        <xsl:choose>
            <xsl:when test="$name/@nullFlavor">
                <xsl:call-template name="show-epSOSNullFlavor">
                    <xsl:with-param name="code" select="$name/@nullFlavor"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$name"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="epPrescriberDetails">
        <div class="wrap-collabsible">
            <input id="collapsible-prescriber-header" class="toggle" type="checkbox"/>
            <label for="collapsible-prescriber-header" class="lbl-toggle-main">
                <!-- Prescriber -->
                <xsl:call-template name="show-displayLabels">
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
                                <xsl:call-template name="show-epSOSDisplayLabels">
                                    <xsl:with-param name="code" select="'56'"/>
                                </xsl:call-template>
                            </th>
                            <th>
                                <!--  Profession: -->
                                <xsl:call-template name="show-epSOSDisplayLabels">
                                    <xsl:with-param name="code" select="'64'"/>
                                </xsl:call-template>
                            </th>
                        </tr>
                        <tr>
                            <td>
                                <xsl:call-template name="authorName">
                                    <xsl:with-param name="authorLocation"
                                                    select="/n1:ClinicalDocument/n1:author"/>
                                </xsl:call-template>
                            </td>
                            <td>
                                <xsl:call-template name="show-formCode">
                                    <xsl:with-param name="parameter"
                                                    select="/n1:ClinicalDocument/n1:author/n1:functionCode"/>
                                </xsl:call-template>
                            </td>
                        </tr>
                    </table>
                    <br/>
                    <div class="wrap-collabsible">
                        <input id="collapsible-extended-prescriber" class="toggle" type="checkbox"/>
                        <label for="collapsible-extended-prescriber" class="lbl-toggle">
                            <xsl:call-template name="show-epSOSDisplayLabels">
                                <xsl:with-param name="code" select="'68'"/>
                            </xsl:call-template>
                        </label>
                        <div class="collapsible-content">
                            <div class="content-inner">
                                <div id="extendedCdaHeader">
                                    <table class="header_table" width="100%">
                                        <colgroup>
                                            <col span="1" style="width: 10%;"/>
                                            <col span="1" style="width: 25%;"/>
                                            <col span="1" style="width: 10%;"/>
                                            <col span="1" style="width: 25%;"/>
                                            <col span="1" style="width: 10%;"/>
                                            <col span="1" style="width: 20%;"/>
                                        </colgroup>

                                        <tbody>
                                            <tr>
                                                <th>
                                                    <!--  Specialty: -->
                                                    <xsl:call-template name="show-epSOSDisplayLabels">
                                                        <xsl:with-param name="code" select="'69'"/>
                                                    </xsl:call-template>
                                                </th>
                                                    <xsl:value-of select="/ClinicalDocument/author/assignedAuthor/code/@displayName"/>
                                                <td>
                                                </td>
                                                <th>
                                                    <!--  Contact Information: -->
                                                    <xsl:call-template name="show-epSOSDisplayLabels">
                                                        <xsl:with-param name="code" select="'12'"/>
                                                    </xsl:call-template>
                                                </th>
                                                <td>
                                                    <xsl:call-template name="telecom">
                                                        <xsl:with-param name="telecomParam"
                                                                        select="/n1:ClinicalDocument/n1:recordTarget/n1:patientRole/n1:telecom"/>
                                                    </xsl:call-template>
                                                </td>
                                            </tr>
                                            <tr>
                                                <th>
                                                    <!-- Facility ID -->
                                                    <xsl:call-template name="show-epSOSDisplayLabels">
                                                        <xsl:with-param name="code" select="'28'"/>
                                                    </xsl:call-template>
                                                </th>
                                                <td>
                                                    <xsl:call-template name="facilityId">
                                                        <xsl:with-param name="id"
                                                                        select="/n1:ClinicalDocument/n1:author/n1:assignedAuthor/n1:representedOrganization/n1:id"/>
                                                    </xsl:call-template>
                                                </td>
                                                <th>
                                                    <!-- Facility Name:-->
                                                    <xsl:call-template name="show-epSOSDisplayLabels">
                                                        <xsl:with-param name="code" select="'29'"/>
                                                    </xsl:call-template>
                                                </th>
                                                <td>
                                                    <xsl:call-template name="facilityName">
                                                        <xsl:with-param name="name"
                                                                        select="/n1:ClinicalDocument/n1:author/n1:assignedAuthor/n1:representedOrganization/n1:name"/>
                                                    </xsl:call-template>
                                                </td>
                                            </tr>
                                            <tr>
                                                <th>
                                                    <!-- Country: -->
                                                    <xsl:call-template name="show-epSOSDisplayLabels">
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
                                                    <xsl:call-template name="show-epSOSDisplayLabels">
                                                        <xsl:with-param name="code" select="'3'"/>
                                                    </xsl:call-template>
                                                </th>
                                                <td>
                                                    <xsl:call-template name="show-address">
                                                        <xsl:with-param name="address" select="/n1:ClinicalDocument/n1:author/n1:assignedAuthor/n1:representedOrganization/n1:addr"/>
                                                    </xsl:call-template>
                                                </td>
                                            </tr>
                                            <tr>
                                                <th>
                                                    <!--  Organisation Name: -->
                                                    <xsl:call-template name="show-epSOSDisplayLabels">
                                                        <xsl:with-param name="code" select="'47'"/>
                                                    </xsl:call-template>
                                                </th>
                                                <td>
                                                    <xsl:call-template name="organization">
                                                        <xsl:with-param name="name"
                                                                        select="//n1:entry/n1:substanceAdministration[n1:templateId[@root='1.3.6.1.4.1.12559.11.10.1.3.1.3.2']]/n1:participant[@typeCode='AUT']/n1:participantRole[@classCode='LIC']/n1:scopingEntity[@classCode='ORG']/n1:desc"/>
                                                    </xsl:call-template>
                                                </td>
                                                <th>
                                                    <!--  Organisation Identifier: -->
                                                    <xsl:call-template name="show-epSOSDisplayLabels">
                                                        <xsl:with-param name="code" select="'46'"/>
                                                    </xsl:call-template>
                                                </th>
                                                <td>
                                                    <xsl:call-template name="organization">
                                                        <xsl:with-param name="name"
                                                                        select="//n1:entry/n1:substanceAdministration[n1:templateId[@root='1.3.6.1.4.1.12559.11.10.1.3.1.3.2']]/n1:participant[@typeCode='AUT']/n1:participantRole[@classCode='LIC']/n1:scopingEntity[@classCode='ORG']/n1:id"/>
                                                    </xsl:call-template>
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
        </div>
    </xsl:template>
</xsl:stylesheet>
