<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:n1="urn:hl7-org:v3" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                version="1.0">

    <xsl:output method="html" indent="yes" version="4.01" doctype-system="http://www.w3.org/TR/html4/strict.dtd"
                doctype-public="-//W3C//DTD HTML 4.01//EN"/>

    <!-- Global variables -->
    <xsl:param name="userLang" select="'en-GB'"/>
    <xsl:param name="epsosLangDir" select="'../EpsosRepository'"/>
    <xsl:param name="defaultUserLang" select="'en-GB'"/>

    <!-- eHDSI-AbsentOrUnknownAllergies -->
    <xsl:template name="show-eHDSI-AbsentOrUnknownAllergies">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.47.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.5.1150.1'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- eHDSI-AbsentOrUnknownDevices -->
    <xsl:template name="show-eHDSI-AbsentOrUnknownDevices">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.48.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.5.1150.1'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- eHDSI-AbsentOrUnknownMedication -->
    <xsl:template name="show-eHDSI-AbsentOrUnknownMedication">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.49.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.5.1150.1'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- eHDSI-AbsentOrUnknownProblems -->
    <xsl:template name="show-eHDSI-AbsentOrUnknownProblems">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.50.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.5.1150.1'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- eHDSI-AbsentOrUnknownProcedures -->
    <xsl:template name="show-eHDSI-AbsentOrUnknownProcedures">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.51.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.5.1150.1'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- epSOSActiveIngredient -->
    <xsl:template name="show-epSOSActiveIngredient">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.24.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.6.73'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- epSOSAdministrativeGender -->
    <xsl:template name="show-epSOSAdministrativeGender">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.34.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.5.1'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- epSOSAdverseEventType -->
    <xsl:template name="show-epSOSAdverseEventType">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.18.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.6.96'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- epSOSAllergenNoDrugs -->
    <xsl:template name="show-epSOSAllergenNoDrugs">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.19.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.6.96'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- epSOSBloodGroup -->
    <xsl:template name="show-epSOSBloodGroup">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.20.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.6.96'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- epSOSBloodPressure -->
    <xsl:template name="show-epSOSBloodPressure">
        <xsl:param name="code"/>
        <xsl:call-template name="show-code-value">
            <xsl:with-param name="code" select="$code"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.21.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.6.1'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- epSOSCodeProb -->
    <xsl:template name="show-epSOSCodeProb">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.23.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.6.96'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- epSOSConfidentiality -->
    <xsl:template name="show-epSOSConfidentiality">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.31.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.5.25'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- epSOSCountry -->
    <xsl:template name="show-epSOSCountry">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.4.xml'"/>
            <xsl:with-param name="codeSystem" select="'1.0.3166.1'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- epSOSDisplayLabels -->
    <xsl:template name="show-epSOSDisplayLabels">
        <xsl:param name="code"/>
        <xsl:variable name="dirFile"
                      select="concat($epsosLangDir,'/1.3.6.1.4.1.12559.11.10.1.3.1.42.46.xml')"/>
        <xsl:variable name="foundKey"
                      select="document(concat('file://', $dirFile))/ValueSet/concept[@code=$code]"/>
        <!-- Windows format -->
        <!-- <xsl:variable name="foundKey"
                      select="document(concat('file:///', translate($dirFile,'\','/')))/ValueSet/concept[@code=$code]"/> -->
        <xsl:variable name="foundKeyLang"
                      select="$foundKey/designation[@lang=$userLang]"/>
        <xsl:variable name="defFoundKeyLang"
                      select="$foundKey/designation[@lang=$defaultUserLang]"/>
        <xsl:choose>
            <xsl:when test="not ($foundKey)">
                <xsl:value-of select="$code"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:choose>
                    <xsl:when test="not ($foundKeyLang)">
                        <xsl:value-of select="$defFoundKeyLang"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$foundKeyLang"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- epSOSDocumentCode -->
    <xsl:template name="show-epSOSDocumentCode">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.32.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.6.1'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- epSOSDoseForm -->
    <xsl:template name="show-epSOSDoseForm">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.2.xml'"/>
            <xsl:with-param name="codeSystem" select="'0.4.0.127.0.16.1.1.2.1'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- epSOSHealthcareProfessionalRoles -->
    <xsl:template name="show-epSOSHealthcareProfessionalRoles">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.1.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.2.9.6.2.7'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- epSOSIllnessesandDisorders -->
    <xsl:template name="show-epSOSIllnessesandDisorders">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.5.xml'"/>
            <xsl:with-param name="codeSystem" select="'1.3.6.1.4.1.12559.11.10.1.3.1.44.2'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- epSOSLanguage -->
    <xsl:template name="show-epSOSLanguage">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.6.xml'"/>
            <xsl:with-param name="codeSystem" select="'1dd183a6-6d2b-4a9d-8f5d-be09d6bb5a6e'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- epSOSMedicalDevices -->
    <xsl:template name="show-epSOSMedicalDevices">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.8.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.6.96'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- epSOSNullFlavor -->
    <xsl:template name="show-epSOSNullFlavor">
        <xsl:param name="code"/>
        <div class="tooltip">
            <i class="fas fa-exclamation-circle" style="color:#085a9f" aria-hidden="true"/>
            <span class="tooltiptext">Missing information</span>
        </div>
        <xsl:text> </xsl:text>
        <xsl:call-template name="show-code-value">
            <xsl:with-param name="code" select="$code"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.37.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.5.1008'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- epSOSPackage -->
    <xsl:template name="show-epSOSPackage">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.3.xml'"/>
            <xsl:with-param name="codeSystem" select="'0.4.0.127.0.16.1.1.2.1'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- epSOSPersonalRelationship -->
    <xsl:template name="show-epSOSPersonalRelationship">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.38.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.5.111'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- epSOSPregnancyInformation -->
    <xsl:template name="show-epSOSPregnancyInformation">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.9.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.6.1'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- epSOSProcedures -->
    <xsl:template name="show-epSOSProcedures">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.10.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.6.96'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- epSOSReactionAllergy -->
    <xsl:template name="show-epSOSReactionAllergy">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.11.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.6.96'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- epSOSResolutionOutcome -->
    <xsl:template name="show-epSOSResolutionOutcome">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.30.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.6.96'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- epSOSRoleClass -->
    <xsl:template name="show-epSOSRoleClass">
        <xsl:param name="code"/>
        <xsl:call-template name="show-code-value">
            <xsl:with-param name="code" select="$code"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.39.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.5.110'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- epSOSRouteOfAdministration -->
    <xsl:template name="show-epSOSRouteOfAdministration">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.12.xml'"/>
            <xsl:with-param name="codeSystem" select="'0.4.0.127.0.16.1.1.2.1'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- epSOSSections -->
    <xsl:template name="show-epSOSSections">
        <xsl:param name="code"/>
        <xsl:call-template name="show-code-value">
            <xsl:with-param name="code" select="$code"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.26.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.6.1'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- epSOSSeverity -->
    <xsl:template name="show-epSOSSeverity">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.13.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.6.96'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- epSOSSocialHistory -->
    <xsl:template name="show-epSOSSocialHistory">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.14.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.6.96'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- epSOSStatusnode -->
    <xsl:template name="show-epSOSStatusCode">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.15.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.6.96'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- epSOSSubstitutionCode -->
    <xsl:template name="show-epSOSSubstitutionCode">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.7.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.5.1070'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- epSOSTelecomAddress -->
    <xsl:template name="show-epSOSTelecomAddress">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.40.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.5.1119'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- epSOSTimingEvent -->
    <xsl:template name="show-epSOSTimingEvent">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.41.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.5.139'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- epSOSUnits -->
    <xsl:template name="show-epSOSUnits">
        <xsl:param name="code"/>
        <xsl:call-template name="show-code-value">
            <xsl:with-param name="code" select="$code"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.16.xml'"/>
            <xsl:with-param name="codeSystem" select="'1.3.6.1.4.1.12009.10.3.1'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- epSOSVaccine -->
    <xsl:template name="show-epSOSVaccine">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.28.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.6.96'"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="show-codedElement">
        <xsl:param name="node"/>
        <xsl:param name="xmlFile"/>
        <xsl:param name="codeSystem"/>

        <xsl:choose>
        <xsl:when test="not($node/@nullFlavor)">
            <xsl:choose>
                <xsl:when test="$node/@code">
                    <xsl:call-template name="show-code-value">
                        <xsl:with-param name="code" select="$node/@code"/>
                        <xsl:with-param name="xmlFile" select="$xmlFile"/>
                        <xsl:with-param name="codeSystem" select="$codeSystem"/>
                    </xsl:call-template>
                </xsl:when>
                <xsl:otherwise>
                    <!-- narrative -->
                    <xsl:if test="$node/n1:originalText/n1:reference/@value">
                        <xsl:call-template name="show-uncodedElement">
                            <xsl:with-param name="code"
                                            select="$node/n1:originalText/n1:reference/@value"/>
                        </xsl:call-template>
                    </xsl:if>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
            <xsl:call-template name="show-epSOSNullFlavor">
                <xsl:with-param name="code" select="$node/@nullFlavor"/>
            </xsl:call-template>
        </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- display translated code value -->
    <xsl:template name="show-code-value" match="/n1:ValueSet/n1:concept">
        <xsl:param name="code"/>
        <xsl:param name="xmlFile"/>
        <xsl:param name="codeSystem"/>
        <xsl:variable name="dirFile" select="concat($epsosLangDir,'/',$xmlFile)"/>
        <xsl:variable name="foundKey"
                      select="document(concat('file://', $dirFile))/ValueSet/concept[@code=$code and @codeSystem=$codeSystem]"/>
        <!-- Windows format -->
        <!--<xsl:variable name="foundKey"
                      select="document(concat('file:///', translate($dirFile,'\','/')))/ValueSet/concept[@code=$code and @codeSystem=$codeSystem]"/> -->
        <xsl:variable name="foundKeyLang" select="$foundKey/designation[@lang=$userLang]"/>
        <xsl:variable name="defFoundKeyLang" select="$foundKey/designation[@lang=$defaultUserLang]"/>
        <xsl:choose>
            <xsl:when test="not ($foundKeyLang)">
                <xsl:value-of select="$defFoundKeyLang"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$foundKeyLang"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>