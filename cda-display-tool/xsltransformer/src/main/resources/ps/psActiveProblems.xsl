<?xml version="1.0"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:n1="urn:hl7-org:v3"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

    <xsl:variable name="currentProblemsSectionCode"
                  select="'11450-4'"/>

    <!-- eHDSI Active Problems -->
    <xsl:template name="activeProblems" match="/">
        <xsl:choose>
            <xsl:when test="not(/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:code[@code=$currentProblemsSectionCode]])">
                <span class="sectionTitle">
                    <!-- The Active Problem section is missing ! -->
                    <xsl:call-template name="show-eHDSIDisplayLabel">
                        <xsl:with-param name="code" select="'72'"/>
                    </xsl:call-template>
                </span>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:code[@code=$currentProblemsSectionCode]]"/>
            </xsl:otherwise>
        </xsl:choose>
        <br/>
        <br/>
    </xsl:template>

    <xsl:template match = "/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:code[@code=$currentProblemsSectionCode]]">
        <xsl:variable name="problemCondition"
                      select="n1:entry/n1:act/n1:entryRelationship[@typeCode='SUBJ']/n1:observation/n1:templateId[@root='1.3.6.1.4.1.12559.11.10.1.3.1.3.7']/../n1:value"/>
        <div class="wrap-collapsible">
            <input id="collapsible-current-problems-section" class="toggle" type="checkbox" checked="true" />
            <label for="collapsible-current-problems-section" class="lbl-toggle-title">
                <!-- Section title -->
                <xsl:call-template name="show-eHDSISection">
                    <xsl:with-param name="code" select="$currentProblemsSectionCode"/>
                </xsl:call-template>
            </label>
            <div class="collapsible-content-title">
                <div class="content-inner-title">
                    <div class="wrap-collapsible">
                        <input id="collapsible-current-problems-original" class="toggle" type="checkbox"/>
                        <label for="collapsible-current-problems-original" class="lbl-toggle">
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
                        <input id="collapsible-current-problems-translated" class="toggle" type="checkbox" checked="true"/>
                        <label for="collapsible-current-problems-translated" class="lbl-toggle">
                            <xsl:value-of select="$translatedCodedTableTitle"/>
                        </label>
                        <div class="collapsible-content">
                            <div class="content-inner">
                                <table class="translation_table">
                                    <tbody>
                                        <xsl:choose>
                                            <xsl:when test="($problemCondition/@code='no-known-problems' or $problemCondition/@code='no-problem-info')">
                                                <tr>
                                                    <td colspan="2">
                                                        <xsl:call-template name="show-eHDSIAbsentOrUnknownProblem">
                                                            <xsl:with-param name="node" select="$problemCondition"/>
                                                        </xsl:call-template>
                                                    </td>
                                                </tr>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <tr>
                                                    <th>
                                                        <!-- Active Problem -->
                                                        <xsl:call-template name="show-eHDSIDisplayLabel">
                                                            <xsl:with-param name="code" select="'2'"/>
                                                        </xsl:call-template>
                                                    </th>
                                                    <th>
                                                        <!-- OnSet Date -->
                                                        <xsl:call-template name="show-eHDSIDisplayLabel">
                                                            <xsl:with-param name="code" select="'45'"/>
                                                        </xsl:call-template>
                                                    </th>
                                                </tr>
                                                <xsl:apply-templates select="n1:entry/n1:act" mode="currentProblems"/>
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
    </xsl:template>

    <xsl:template match="n1:entry/n1:act" mode="currentProblems">
        <xsl:variable name="problemCondition"
                      select="n1:entryRelationship[@typeCode='SUBJ']/n1:observation/n1:templateId[@root='1.3.6.1.4.1.12559.11.10.1.3.1.3.7']/../n1:value"/>
        <xsl:variable name="probOnSetDate"
                      select="n1:effectiveTime/n1:low"/>
        <xsl:choose>
            <xsl:when test="not(@nullFlavor)">
                <tr>
                    <td>
                        <!-- Active Problem -->
                        <xsl:call-template name="show-eHDSIIllnessandDisorder">
                            <xsl:with-param name="node" select="$problemCondition"/>
                        </xsl:call-template>
                        <xsl:if test="not($problemCondition/@nullFlavor)">
                            <xsl:text> (</xsl:text>
                            <xsl:value-of select="$problemCondition/@code"/>
                            <xsl:text>)</xsl:text>
                        </xsl:if>
                    </td>
                    <td>
                        <!-- OnSet Date -->
                        <xsl:call-template name="show-TS">
                            <xsl:with-param name="node" select="$probOnSetDate"/>
                        </xsl:call-template>
                    </td>
                </tr>
            </xsl:when>
            <xsl:otherwise>
                <tr>
                    <td colspan="2">
                        <xsl:call-template name="show-eHDSINullFlavor">
                            <xsl:with-param name="code" select="./@nullFlavor"/>
                        </xsl:call-template>
                    </td>
                </tr>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>