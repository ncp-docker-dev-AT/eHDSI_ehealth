<?xml version="1.0"  ?>


<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:n1="urn:hl7-org:v3" version="1.0">
    <xsl:output method="html" indent="yes" version="4.01" doctype-system="http://www.w3.org/TR/html4/strict.dtd"
                doctype-public="-//W3C//DTD HTML 4.01//EN"/>


    <!--- Extended HEADER ELEMENT -->

    <!-- patient address -->
    <xsl:variable
            name="patientRole"
            select="/n1:ClinicalDocument/n1:recordTarget/n1:patientRole"/>

    <xsl:variable
            name="patientPreferLang"
            select="$patientRole/n1:patient/n1:languageCommunication/n1:languageCode"/>

    <!-- guardian -->
    <xsl:variable
            name="patientGuardian"
            select="$patientRole/n1:patient/n1:guardian"/>

    <!--Prefered HCP/ Legal Organization -->
    <xsl:variable
            name="preferredHCPPRSScopingOrganisation"
            select="/n1:ClinicalDocument/n1:participant/n1:functionCode[@code='PCP'][@codeSystem='2.16.840.1.113883.5.88']/../n1:associatedEntity[@classCode='PRS']/n1:scopingOrganization"/>
    <xsl:variable
            name="preferredHCPPRSScopingOrganisationName"
            select="$preferredHCPPRSScopingOrganisation/n1:name"/>

    <xsl:variable
            name="preferredHCPAssociatedPerson"
            select="/n1:ClinicalDocument/n1:participant/n1:functionCode[@code='PCP'][@codeSystem='2.16.840.1.113883.5.88']/../n1:associatedEntity[@classCode='PRS']/n1:associatedPerson"/>
    <xsl:variable
            name="preferredHCPAssociatedPersonName"
            select="$preferredHCPAssociatedPerson/n1:name"/>


    <!--Prefered HCP/ Legal Organization Address-->
    <xsl:variable
            name="preferredHCPAssociatedEntity"
            select="/n1:ClinicalDocument/n1:participant/n1:functionCode[@code='PCP'][@codeSystem='2.16.840.1.113883.5.88']/../n1:associatedEntity"/>

    <xsl:variable
            name="preferredHCPScopingOrganisation"
            select="$preferredHCPAssociatedEntity/n1:scopingOrganization"/>

    <!-- Authoring device -->
    <xsl:variable
            name="AuthoringDeviceName"
            select="/n1:ClinicalDocument/n1:author/n1:assignedAuthor/n1:assignedAuthoringDevice"/>


    <!-- Legal Othenticator -->
    <xsl:variable
            name="LegalAuthenticator"
            select="/n1:ClinicalDocument/n1:legalAuthenticator/n1:assignedEntity/n1:assignedPerson"/>

    <xsl:variable
            name="LegalAuthenticator2"
            select="/n1:ClinicalDocument/n1:legalAuthenticator/n1:assignedEntity/n1:representedOrganization"/>

    <!--Custodian-->
    <xsl:variable
            name="patientCustodian"
            select="/n1:ClinicalDocument/n1:custodian/n1:assignedCustodian/n1:representedCustodianOrganization"/>


    <xsl:template name="extendedCdaHeader">
        <div class="container">
            <div class="item"><xsl:call-template name="displayPatientContactInformation"/></div>
            <div class="item"><xsl:call-template name="displayPreferredHCPAndLegalOrganization"/></div>
            <div class="item"><xsl:call-template name="displayAuthors"/></div>
            <div class="item"><xsl:call-template name="displayAuthoringDevice"/></div>
            <div class="item"><xsl:call-template name="displayLegalAuthenticator"/></div>
            <div class="item"><xsl:call-template name="displayOtherContacts"/></div>
            <div class="item"><xsl:call-template name="displayGuardian"/></div>
            <div class="item"><xsl:call-template name="displayCustodian"/></div>
        </div>
    </xsl:template>

    <xsl:template name="displayPatientContactInformation">
        <div class="extended_header_block">
            <table class="extended_header_table">
                <tbody>
                    <!-- Patient Contact Info-->
                    <tr>
                        <th colspan="2">
                            <xsl:call-template name="show-epSOSDisplayLabels">
                                <xsl:with-param name="code" select="'51'"/>
                            </xsl:call-template>
                        </th>
                    </tr>
                    <tr>
                        <td>
                        </td>
                        <td>
                            <table class="contact_information_table">
                                <tr>
                                    <th>
                                        <xsl:call-template name="show-epSOSDisplayLabels">
                                            <xsl:with-param name="code" select="'12'"/>
                                        </xsl:call-template>
                                    </th>
                                </tr>
                                <tr>
                                    <td>
                                        <xsl:call-template name="show-contactInfo">
                                            <xsl:with-param name="contact" select="$patientRole"/>
                                        </xsl:call-template>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </xsl:template>

    <xsl:template name="displayPreferredHCPAndLegalOrganization">
        <div class="extended_header_block">
        <table class="extended_header_table">
            <tbody>
                <tr>
                    <th colspan="2">
                        <!-- Preferred HCP/Legal organization to contact-->
                        <xsl:call-template name="show-epSOSDisplayLabels">
                            <xsl:with-param name="code" select="'54'"/>
                        </xsl:call-template>
                    </th>
                </tr>
                <tr>
                    <td>
                        <!-- show person's name and if exists organization name -->
                        <xsl:if test="not($preferredHCPAssociatedPersonName/@nullFlavor)">
                            <xsl:value-of select="$preferredHCPAssociatedPersonName/n1:given"/>&#160;
                            <xsl:value-of select="$preferredHCPAssociatedPersonName/n1:family"/>
                        </xsl:if>
                        <xsl:if test="$preferredHCPPRSScopingOrganisation">
                            <xsl:if test="($preferredHCPAssociatedPersonName/n1:given or $preferredHCPAssociatedPersonName/n1:family) and $preferredHCPPRSScopingOrganisation/n1:name">
                                ,&#160;
                            </xsl:if>
                            <xsl:value-of select="$preferredHCPPRSScopingOrganisation/n1:name"/>
                        </xsl:if>
                    </td>
                    <td>
                        <table class="contact_information_table">
                            <tr>
                                <th>
                                    <xsl:call-template name="show-epSOSDisplayLabels">
                                        <xsl:with-param name="code" select="'12'"/>
                                    </xsl:call-template>
                                </th>
                            </tr>
                            <tr>
                                <td>
                                    <xsl:choose>
                                        <!-- first person's address.. then if not exist show org address -->
                                        <xsl:when test="$preferredHCPAssociatedEntity">
                                            <xsl:call-template name="show-contactInfo">
                                                <xsl:with-param name="contact" select="$preferredHCPAssociatedEntity"/>
                                            </xsl:call-template>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:call-template name="show-contactInfo">
                                                <xsl:with-param name="contact" select="$preferredHCPScopingOrganisation"/>
                                            </xsl:call-template>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </tbody>
        </table>
        </div>
    </xsl:template>

    <xsl:template name="displayAuthors">
        <xsl:for-each select="/n1:ClinicalDocument/n1:author">
            <xsl:variable
                    name="hcpCounter"
                    select="position()"/>
            <xsl:variable
                    name="HCPIdentificationAuthor"
                    select="/n1:ClinicalDocument/n1:author[$hcpCounter]/n1:assignedAuthor"/>
            <xsl:variable
                    name="HCPIdentificationPerformer"
                    select="/n1:ClinicalDocument/n1:documentationOf[$hcpCounter]/n1:serviceEvent/n1:performer/n1:assignedEntity"/>
            <xsl:variable
                    name="HCPName"
                    select="/n1:ClinicalDocument/n1:author[$hcpCounter]/n1:assignedAuthor/n1:assignedPerson/n1:name"/>
            <xsl:variable
                    name="HCPOrgName"
                    select="/n1:ClinicalDocument/n1:author[$hcpCounter]/n1:assignedAuthor/n1:representedOrganization/n1:name"/>
            <xsl:variable
                    name="HCPName2"
                    select="/n1:ClinicalDocument/n1:documentationOf[$hcpCounter]/n1:serviceEvent/n1:performer/n1:assignedEntity/n1:assignedPerson/n1:name"/>
            <xsl:variable
                    name="HCPName2Org"
                    select="/n1:ClinicalDocument/n1:documentationOf[$hcpCounter]/n1:serviceEvent/n1:performer/n1:assignedEntity/n1:representedOrganization/n1:name"/>
            <div class="extended_header_block">
                <table class="extended_header_table">
                    <tbody>
                        <tr>
                            <th colspan="2">
                                <!-- Author (HCP) -->
                                <xsl:call-template name="show-epSOSDisplayLabels">
                                    <xsl:with-param name="code" select="'7'"/>
                                </xsl:call-template>
                            </th>
                        </tr>
                        <tr>
                            <td>
                                <xsl:choose>
                                    <xsl:when test="$HCPName">
                                        <!-- show person's name and if exists organization name -->
                                        <xsl:value-of select="$HCPName/n1:given"/>&#160;
                                        <xsl:value-of select="$HCPName/n1:family"/>
                                        <xsl:if test="$HCPOrgName">
                                            ,&#160;<xsl:value-of select="$HCPOrgName"/>&#160;
                                        </xsl:if>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:if test="$HCPName2">
                                            <xsl:value-of select="$HCPName2/n1:given"/>&#160;
                                            <xsl:value-of select="$HCPName2/n1:family"/>
                                            <xsl:value-of select="$HCPOrgName"/>&#160;
                                            <xsl:value-of select="$HCPName2Org"/>&#160;
                                        </xsl:if>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                            <td>
                                <table class="contact_information_table">
                                    <tr>
                                        <th>
                                            <xsl:call-template name="show-epSOSDisplayLabels">
                                                <xsl:with-param name="code" select="'12'"/>
                                            </xsl:call-template>
                                        </th>
                                    </tr>
                                    <tr>
                                        <td>
                                            <xsl:choose>
                                                <xsl:when test="$HCPIdentificationAuthor/n1:addr">
                                                    <xsl:call-template name="show-contactInfo">
                                                        <xsl:with-param name="contact" select="$HCPIdentificationAuthor"/>
                                                    </xsl:call-template>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <xsl:call-template name="show-contactInfo">
                                                        <xsl:with-param name="contact" select="$HCPIdentificationPerformer"/>
                                                    </xsl:call-template>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="displayAuthoringDevice">
        <div class="extended_header_block">
            <table class="extended_header_table">
                <tbody>
                    <tr>
                        <th colspan="2">
                            <!-- Authoring Device -->
                            <xsl:call-template name="show-epSOSDisplayLabels">
                                <xsl:with-param name="code" select="'8'"/>
                            </xsl:call-template>
                        </th>
                    </tr>
                    <tr>
                        <td>
                            <!-- show person's name and if exists organization name -->
                            <xsl:value-of select="$AuthoringDeviceName/n1:manufacturerModelName"/>&#160;
                            <xsl:value-of select="$AuthoringDeviceName/n1:softwareName"/>&#160;
                        </td>
                        <td>
                            <table class="contact_information_table">
                                <tr>
                                    <th>
                                        <xsl:call-template name="show-epSOSDisplayLabels">
                                            <xsl:with-param name="code" select="'12'"/>
                                        </xsl:call-template>
                                    </th>
                                </tr>
                                <tr>
                                    <td>
                                        <xsl:choose>
                                            <xsl:when test="$AuthoringDeviceName/n1:addr">
                                                <xsl:call-template name="show-contactInfo">
                                                    <xsl:with-param name="contact" select="$AuthoringDeviceName"/>
                                                </xsl:call-template>
                                            </xsl:when>
                                        </xsl:choose>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </xsl:template>

    <xsl:template name="displayLegalAuthenticator">
        <div class="extended_header_block">
            <table class="extended_header_table">
                <tbody>
                    <tr>
                        <th colspan="2">
                            <!-- Legal Authenticator-->
                            <xsl:call-template name="show-epSOSDisplayLabels">
                                <xsl:with-param name="code" select="'40'"/>
                            </xsl:call-template>
                        </th>
                    </tr>
                    <tr>
                        <td>
                            <xsl:value-of select="$LegalAuthenticator/n1:name/n1:given"/>&#160;
                            <xsl:value-of select="$LegalAuthenticator/n1:name/n1:family"/>
                            <xsl:if test="$LegalAuthenticator2/n1:name and not($LegalAuthenticator2/n1:name/@nullFlavor)">
                                ,&#160;<xsl:value-of select="$LegalAuthenticator2/n1:name"/>&#160;
                            </xsl:if>
                        </td>
                        <td>
                            <table class="contact_information_table">
                                <tr>
                                    <th>
                                        <xsl:call-template name="show-epSOSDisplayLabels">
                                            <xsl:with-param name="code" select="'12'"/>
                                        </xsl:call-template>
                                    </th>
                                </tr>
                                <tr>
                                    <td>
                                        <xsl:choose>
                                            <xsl:when
                                                    test="n1:legalAuthenticator/n1:assignedEntity/n1:addr | n1:legalAuthenticator/n1:assignedEntity/n1:telecom">
                                                <xsl:call-template name="show-contactInfo">
                                                    <xsl:with-param name="contact" select="n1:legalAuthenticator/n1:assignedEntity"/>
                                                </xsl:call-template>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:call-template name="show-contactInfo">
                                                    <xsl:with-param name="contact" select="$LegalAuthenticator2"/>
                                                </xsl:call-template>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </xsl:template>

    <xsl:template name="displayOtherContacts">
        <div class="extended_header_block">
            <table class="extended_header_table">
                <tbody>
                    <tr>
                        <th colspan="2">
                            <!-- Other Contacts-->
                            <xsl:call-template name="show-epSOSDisplayLabels">
                                <xsl:with-param name="code" select="'49'"/>
                            </xsl:call-template>
                        </th>
                    </tr>
                    <tr>
                        <td>
                            <ul>
                                <xsl:for-each select="/n1:ClinicalDocument/n1:participant/n1:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.2.4']/../n1:associatedEntity">
                                    <xsl:if test="n1:associatedPerson/n1:name/* or n1:scopingOrganization">
                                        <li><xsl:value-of select="n1:associatedPerson/n1:name/n1:given"/>&#160;
                                            <xsl:value-of select="n1:associatedPerson/n1:name/n1:family"/>&#160;
                                            <xsl:value-of select="n1:scopingOrganization/n1:name"/>&#160;
                                            <span class="label otherContacts-roleClass">
                                                <xsl:call-template name="show-epSOSRoleClass">
                                                    <xsl:with-param name="code" select="@classCode"/>
                                                </xsl:call-template>
                                            </span>&#160;
                                            <xsl:if test="../n1:functionCode and not(../n1:functionCode/@nullFlavor)">
                                                <span class="label otherContacts-personalRelationship">
                                                    <xsl:call-template name="show-epSOSPersonalRelationship">
                                                        <xsl:with-param name="node" select="../n1:functionCode"/>
                                                    </xsl:call-template>
                                                </span>
                                            </xsl:if>
                                        </li>
                                    </xsl:if>
                                </xsl:for-each>
                            </ul>
                        </td>
                        <td>
                            <table class="contact_information_table">
                                <tr>
                                    <th>
                                        <xsl:call-template name="show-epSOSDisplayLabels">
                                            <xsl:with-param name="code" select="'12'"/>
                                        </xsl:call-template>
                                    </th>
                                </tr>
                                <tr>
                                    <td>
                                        <ul>
                                            <xsl:for-each
                                                    select="/n1:ClinicalDocument/n1:participant/n1:associatedEntity">
                                                <xsl:if test="not(../n1:functionCode) or not(../n1:functionCode/@code='PCP')">
                                                    <xsl:if test="n1:associatedPerson/n1:name/* and not(n1:associatedPerson/n1:name/n1:given/@nullFlavor) and not(n1:associatedPerson/n1:name/n1:family/@nullFlavor)">
                                                        <li><xsl:value-of select="n1:associatedPerson/n1:name/n1:given"/>&#160;
                                                            <xsl:value-of select="n1:associatedPerson/n1:name/n1:family"/>
                                                            <br/>
                                                            <xsl:call-template name="show-contactInfo">
                                                                <xsl:with-param name="contact" select="."/>
                                                            </xsl:call-template>
                                                            <br/>
                                                        </li>
                                                    </xsl:if>
                                                </xsl:if>
                                            </xsl:for-each>
                                        </ul>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </xsl:template>

    <xsl:template name="displayGuardian">
        <div class="extended_header_block">
            <table class="extended_header_table">
                <tbody>
                    <tr>
                        <th colspan="2">
                            <!-- Guardian-->
                            <xsl:call-template name="show-epSOSDisplayLabels">
                                <xsl:with-param name="code" select="'35'"/>
                            </xsl:call-template>
                        </th>
                    </tr>
                    <tr>
                        <td>
                            <xsl:value-of select="$patientGuardian/n1:guardianPerson/n1:name/n1:given"/>&#160;
                            <xsl:value-of select="$patientGuardian/n1:guardianPerson/n1:name/n1:family"/>&#160;
                        </td>
                        <td>
                            <table class="contact_information_table">
                                <tr>
                                    <th>
                                        <xsl:call-template name="show-epSOSDisplayLabels">
                                            <xsl:with-param name="code" select="'12'"/>
                                        </xsl:call-template>
                                    </th>
                                </tr>
                                <tr>
                                    <td>
                                        <xsl:call-template name="show-contactInfo">
                                            <xsl:with-param name="contact" select="$patientGuardian"/>
                                        </xsl:call-template>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </xsl:template>

    <xsl:template name="displayCustodian">
        <div class="extended_header_block">
            <table class="extended_header_table">
                <tbody>
                    <tr>
                        <th colspan="2">
                            <!-- Custodian-->
                            <xsl:call-template name="show-epSOSDisplayLabels">
                                <xsl:with-param name="code" select="'16'"/>
                            </xsl:call-template>
                        </th>
                    </tr>
                    <tr>
                        <td>
                            <xsl:value-of select="$patientCustodian/n1:name"/>
                        </td>
                        <td>
                            <table class="contact_information_table">
                                <tr>
                                    <th>
                                        <!-- Contact Information -->
                                        <xsl:call-template name="show-epSOSDisplayLabels">
                                            <xsl:with-param name="code" select="'12'"/>
                                        </xsl:call-template>
                                    </th>
                                </tr>
                                <tr>
                                    <td>
                                        <xsl:call-template name="show-contactInfo">
                                            <xsl:with-param name="contact" select="$patientCustodian"/>
                                        </xsl:call-template>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </xsl:template>
</xsl:stylesheet>