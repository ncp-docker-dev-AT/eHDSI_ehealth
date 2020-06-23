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

    <!-- eHDSIAbsentOrUnknownAllergy -->
    <xsl:template name="show-eHDSIAbsentOrUnknownAllergy">
        <xsl:param name="node"/>
        <div class="tooltip-right">
            <xsl:choose>
                <xsl:when test="($node/@code='no-allergy-info')">
                    <!-- TODO: add label to eHDSIDisplayLabel value set -->
                    <span class="tooltiptext">There is no information available regarding the subject's allergy conditions.</span>
                </xsl:when>
                <xsl:when test="($node/@code='no-known-allergies')">
                    <!-- TODO: add label to eHDSIDisplayLabel value set -->
                    <span class="tooltiptext">The subject has no known allergy conditions.</span>
                </xsl:when>
                <xsl:when test="($node/@code='no-known-medication-allergies')">
                    <!-- TODO: add label to eHDSIDisplayLabel value set -->
                    <span class="tooltiptext">The subject has no known medication allergy conditions.</span>
                </xsl:when>
                <xsl:when test="($node/@code='no-known-environmental-allergies')">
                    <!-- TODO: add label to eHDSIDisplayLabel value set -->
                    <span class="tooltiptext">The subject has no known environmental allergy conditions.</span>
                </xsl:when>
                <xsl:when test="($node/@code='no-known-food-allergies')">
                    <!-- TODO: add label to eHDSIDisplayLabel value set -->
                    <span class="tooltiptext">The subject has no known food allergy conditions.</span>
                </xsl:when>
            </xsl:choose>
            <xsl:call-template name="show-codedElement">
                <xsl:with-param name="node" select="$node"/>
                <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.47.xml'"/>
                <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.5.1150.1'"/>
            </xsl:call-template>
        </div>
    </xsl:template>

    <!-- eHDSIAbsentOrUnknownDevice -->
    <xsl:template name="show-eHDSIAbsentOrUnknownDevice">
        <xsl:param name="node"/>
        <div class="tooltip-right">
            <xsl:choose>
                <xsl:when test="($node/@code='no-device-info')">
                    <!-- TODO: add label to eHDSIDisplayLabel value set -->
                    <span class="tooltiptext">There is no information available regarding implanted or external devices for the subject.</span>
                </xsl:when>
                <xsl:when test="($node/@code='no-known-devices')">
                    <!-- TODO: add label to eHDSIDisplayLabel value set -->
                    <span class="tooltiptext">There are no devices known to be implanted in or used by the subject that have to be reported in this record. This can mean either that there are none known, or that those known are not relevant for the purpose of this record.</span>
                </xsl:when>
            </xsl:choose>
            <xsl:call-template name="show-codedElement">
                <xsl:with-param name="node" select="$node"/>
                <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.48.xml'"/>
                <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.5.1150.1'"/>
            </xsl:call-template>
        </div>
    </xsl:template>

    <!-- eHDSIAbsentOrUnknownMedication -->
    <xsl:template name="show-eHDSIAbsentOrUnknownMedication">
        <xsl:param name="node"/>
        <div class="tooltip-right">
            <xsl:choose>
                <xsl:when test="($node/@code='no-medication-info')">
                    <!-- TODO: add label to eHDSIDisplayLabel value set -->
                    <span class="tooltiptext">There is no information available about the subject's medication use or administration.</span>
                </xsl:when>
                <xsl:when test="($node/@code='no-known-medications')">
                    <!-- TODO: add label to eHDSIDisplayLabel value set -->
                    <span class="tooltiptext">There are no medications for the subject that have to be reported in this record. This can mean either that there are none known, or that those known are not relevant for the purpose of this record.</span>
                </xsl:when>
            </xsl:choose>
            <xsl:call-template name="show-codedElement">
                <xsl:with-param name="node" select="$node"/>
                <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.49.xml'"/>
                <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.5.1150.1'"/>
            </xsl:call-template>
        </div>
    </xsl:template>

    <!-- eHDSIAbsentOrUnknownProblem -->
    <xsl:template name="show-eHDSIAbsentOrUnknownProblem">
        <xsl:param name="node"/>
        <div class="tooltip-right">
            <xsl:choose>
                <xsl:when test="($node/@code='no-problem-info')">
                    <!-- TODO: add label to eHDSIDisplayLabel value set -->
                    <span class="tooltiptext">There is no information available about the subject's health problems or disabilities.</span>
                </xsl:when>
                <xsl:when test="($node/@code='no-known-problems')">
                    <!-- TODO: add label to eHDSIDisplayLabel value set -->
                    <span class="tooltiptext">The subject is not known to have any health problems or disabilities that have to be reported in this record. This can mean either that there are none known, or that those known are not relevant for the purpose of this record.</span>
                </xsl:when>
            </xsl:choose>
            <xsl:call-template name="show-codedElement">
                <xsl:with-param name="node" select="$node"/>
                <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.50.xml'"/>
                <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.5.1150.1'"/>
            </xsl:call-template>
        </div>
    </xsl:template>

    <!-- eHDSIAbsentOrUnknownProcedure -->
    <xsl:template name="show-eHDSIAbsentOrUnknownProcedure">
        <xsl:param name="node"/>
        <div class="tooltip-right">
            <xsl:choose>
                <xsl:when test="($node/@code='no-procedure-info')">
                    <!-- TODO: add label to eHDSIDisplayLabel value set -->
                    <span class="tooltiptext">There is no information available about the subject's past history of procedures.</span>
                </xsl:when>
                <xsl:when test="($node/@code='no-known-procedures')">
                    <!-- TODO: add label to eHDSIDisplayLabel value set -->
                    <span class="tooltiptext">The subject has no history of procedures that have to be reported in this record. This can mean either that there are none known, or that those known are not relevant for the purpose of this record.</span>
                </xsl:when>
            </xsl:choose>
            <xsl:call-template name="show-codedElement">
                <xsl:with-param name="node" select="$node"/>
                <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.51.xml'"/>
                <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.5.1150.1'"/>
            </xsl:call-template>
        </div>
    </xsl:template>

    <!-- eHDSIActiveIngredient -->
    <xsl:template name="show-eHDSIActiveIngredient">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.24.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.6.73'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- eHDSIAdministrativeGender -->
    <xsl:template name="show-eHDSIAdministrativeGender">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.34.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.5.1'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- eHDSIAdverseEventType -->
    <xsl:template name="show-eHDSIAdverseEventType">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.18.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.6.96'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- eHDSIAllergenNoDrug -->
    <xsl:template name="show-eHDSIAllergenNoDrug">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.19.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.6.96'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- eHDSIBloodGroup -->
    <xsl:template name="show-eHDSIBloodGroup">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.20.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.6.96'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- eHDSIBloodPressure -->
    <xsl:template name="show-eHDSIBloodPressure">
        <xsl:param name="code"/>
        <xsl:call-template name="show-code-value">
            <xsl:with-param name="code" select="$code"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.21.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.6.1'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- eHDSICodeProb -->
    <xsl:template name="show-eHDSICodeProb">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.23.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.6.96'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- eHDSIConfidentiality -->
    <xsl:template name="show-eHDSIConfidentiality">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.31.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.5.25'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- eHDSICountry -->
    <xsl:template name="show-eHDSICountry">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.4.xml'"/>
            <xsl:with-param name="codeSystem" select="'1.0.3166.1'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- eHDSIDisplayLabel -->
    <xsl:template name="show-eHDSIDisplayLabel">
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

    <!-- eHDSIDocumentCode -->
    <xsl:template name="show-eHDSIDocumentCode">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.32.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.6.1'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- eHDSIDoseForm -->
    <xsl:template name="show-eHDSIDoseForm">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.2.xml'"/>
            <xsl:with-param name="codeSystem" select="'0.4.0.127.0.16.1.1.2.1'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- eHDSIHealthcareProfessionalRole -->
    <xsl:template name="show-eHDSIHealthcareProfessionalRole">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.1.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.2.9.6.2.7'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- eHDSIIllnessesandDisorder -->
    <xsl:template name="show-eHDSIIllnessandDisorder">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.5.xml'"/>
            <xsl:with-param name="codeSystem" select="'1.3.6.1.4.1.12559.11.10.1.3.1.44.2'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- eHDSILanguage -->
    <xsl:template name="show-eHDSILanguage">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.6.xml'"/>
            <xsl:with-param name="codeSystem" select="'1dd183a6-6d2b-4a9d-8f5d-be09d6bb5a6e'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- eHDSIMedicalDevice -->
    <xsl:template name="show-eHDSIMedicalDevice">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.8.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.6.96'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- eHDSINullFlavor -->
    <xsl:template name="show-eHDSINullFlavor">
        <xsl:param name="code"/>
        <div class="tooltip-right">
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

    <!-- eHDSIPackage -->
    <xsl:template name="show-eHDSIPackage">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.3.xml'"/>
            <xsl:with-param name="codeSystem" select="'0.4.0.127.0.16.1.1.2.1'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- eHDSIPersonalRelationship -->
    <xsl:template name="show-eHDSIPersonalRelationship">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.38.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.5.111'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- eHDSIPregnancyInformation -->
    <xsl:template name="show-eHDSIPregnancyInformation">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.9.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.6.1'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- eHDSIProcedure -->
    <xsl:template name="show-eHDSIProcedure">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.10.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.6.96'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- eHDSIReactionAllergy -->
    <xsl:template name="show-eHDSIReactionAllergy">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.11.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.6.96'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- eHDSIResolutionOutcome -->
    <xsl:template name="show-eHDSIResolutionOutcome">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.30.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.6.96'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- eHDSIRoleClass -->
    <xsl:template name="show-eHDSIRoleClass">
        <xsl:param name="code"/>
        <xsl:call-template name="show-code-value">
            <xsl:with-param name="code" select="$code"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.39.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.5.110'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- eHDSIRouteOfAdministration -->
    <xsl:template name="show-eHDSIRouteOfAdministration">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.12.xml'"/>
            <xsl:with-param name="codeSystem" select="'0.4.0.127.0.16.1.1.2.1'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- eHDSISection -->
    <xsl:template name="show-eHDSISection">
        <xsl:param name="code"/>
        <xsl:call-template name="show-code-value">
            <xsl:with-param name="code" select="$code"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.26.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.6.1'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- eHDSISeverity -->
    <xsl:template name="show-eHDSISeverity">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.13.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.6.96'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- eHDSISocialHistory -->
    <xsl:template name="show-eHDSISocialHistory">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.14.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.6.96'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- eHDSIStatusCode -->
    <xsl:template name="show-eHDSIStatusCode">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.15.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.6.96'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- eHDSISubstitutionCode -->
    <xsl:template name="show-eHDSISubstitutionCode">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.7.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.5.1070'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- eHDSITelecomAddress -->
    <xsl:template name="show-eHDSITelecomAddress">
        <xsl:param name="code"/>
        <xsl:call-template name="show-code-value">
            <xsl:with-param name="code" select="$code"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.40.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.5.1119'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- eHDSITimingEvent -->
    <xsl:template name="show-eHDSITimingEvent">
        <xsl:param name="node"/>
        <xsl:call-template name="show-codedElement">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.41.xml'"/>
            <xsl:with-param name="codeSystem" select="'2.16.840.1.113883.5.139'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- eHDSIUnit -->
    <xsl:template name="show-eHDSIUnit">
        <xsl:param name="code"/>
        <xsl:call-template name="show-code-value">
            <xsl:with-param name="code" select="$code"/>
            <xsl:with-param name="xmlFile" select="'1.3.6.1.4.1.12559.11.10.1.3.1.42.16.xml'"/>
            <xsl:with-param name="codeSystem" select="'1.3.6.1.4.1.12009.10.3.1'"/>
        </xsl:call-template>
    </xsl:template>

    <!-- eHDSIVaccine -->
    <xsl:template name="show-eHDSIVaccine">
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
                <xsl:call-template name="show-eHDSINullFlavor">
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