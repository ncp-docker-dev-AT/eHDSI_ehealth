<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:n1="urn:hl7-org:v3"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <xsl:output method="html" indent="yes" version="4.01" doctype-system="http://www.w3.org/TR/html4/strict.dtd"
                doctype-public="-//W3C//DTD HTML 4.01//EN"/>

    <!-- variable to check that at least one problem section exists -->
    <xsl:variable name="problemExist"
                  select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section/n1:code[@code='11450-4']"/>

    <!--current problems -->
    <xsl:template name="currentProblems" match="/n1:ClinicalDocument/n1:component/n1:structuredBody">
        <xsl:choose>
            <!-- if we have at least one current problem section -->
            <xsl:when test="($problemExist)">
                <xsl:for-each select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section">
                    <xsl:call-template name="currentProblemsSection"/>
                </xsl:for-each>
                <br/>
                <br/>
            </xsl:when>
            <!-- else -->
            <xsl:otherwise>
                <span class="sectionTitle">
                    <!-- The Active Problem section is missing ! -->
                    <xsl:call-template name="show-epSOSDisplayLabels">
                        <xsl:with-param name="code" select="'72'"/>
                    </xsl:call-template>
                </span>
                <br/>
                <br/>
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>

    <!--  section -->
    <xsl:template name="currentProblemsSection">

        <!-- Defining all needed variables -->
        <xsl:variable
                name="probSectionTitleCode"
                select="n1:code/@code"/>
        <xsl:variable name="probAct"
                      select="n1:entry/n1:act"/>
        <xsl:variable name="obsValueCode"
                      select="n1:entry/n1:act/n1:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.4.5.2']/../n1:entryRelationship[@typeCode='SUBJ']/n1:observation/n1:templateId[@root='1.3.6.1.4.1.12559.11.10.1.3.1.3.7']/../n1:value/@code"/>
        <!-- End definition of variables-->


        <!--- table Display -->
        <xsl:choose>
            <!-- if sectionTitle is not missing for alerts  (Exception alerts section is missing)-->
            <xsl:when test=" ($probSectionTitleCode='11450-4')">
                <div class="wrap-collabsible">
                    <input id="collapsible-current-problems-section-original" class="toggle" type="checkbox" checked="true" />
                    <label for="collapsible-current-problems-section-original" class="lbl-toggle-title">
                        <!-- Section title -->
                        <xsl:call-template name="show-epSOSSections">
                            <xsl:with-param name="code" select="'11450-4'"/>
                        </xsl:call-template>
                    </label>
                    <div class="collapsible-content-title">
                        <div class="content-inner-title">
                            <xsl:choose>
                                <xsl:when test="$shownarrative='true'">
                                    <div class="wrap-collabsible">
                                        <input id="collapsible-current-problems-original" class="toggle" type="checkbox"/>
                                        <label for="collapsible-current-problems-original" class="lbl-toggle">
                                            <xsl:value-of select="$originalNarrativeTableTitle"/>
                                        </label>
                                        <div class="collapsible-content">
                                            <div class="content-inner">
                                                <xsl:call-template name="show-narrative">
                                                    <xsl:with-param name="node" select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section/n1:code[@code='11450-4']/../n1:text"/>
                                                </xsl:call-template>
                                            </div>
                                        </div>
                                    </div>
                                </xsl:when>
                            </xsl:choose>
                            <br/>
                            <!-- nullflavored act -->
                            <div class="wrap-collabsible">
                                <input id="collapsible-current-problems-translated" class="toggle" type="checkbox" checked="true"/>
                                <label for="collapsible-current-problems-translated" class="lbl-toggle">
                                    <xsl:value-of select="$translatedCodedTableTitle"/>
                                </label>
                                <div class="collapsible-content">
                                    <div class="content-inner">
                                        <xsl:choose>
                                            <xsl:when test="not($probAct/@nullFlavor)">
                                                <table class="translation_table">
                                                    <tbody>
                                                        <xsl:if test="not ($obsValueCode='no-known-problems' or $obsValueCode='no-problem-info')">
                                                            <tr>
                                                                <th>
                                                                    <!-- Active Problem -->
                                                                    <xsl:call-template name="show-epSOSDisplayLabels">
                                                                        <xsl:with-param name="code" select="'2'"/>
                                                                    </xsl:call-template>
                                                                </th>
                                                                <th>
                                                                    <!-- OnSet Date -->
                                                                    <xsl:call-template name="show-epSOSDisplayLabels">
                                                                        <xsl:with-param name="code" select="'45'"/>
                                                                    </xsl:call-template>
                                                                </th>
                                                            </tr>
                                                        </xsl:if>
                                                        <xsl:for-each select="n1:entry">
                                                            <xsl:call-template name="currentProblemsSectionEntry"/>
                                                        </xsl:for-each>
                                                    </tbody>
                                                </table>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:call-template name="show-epSOSNullFlavor">
                                                    <xsl:with-param name="data" select="$probAct/@nullFlavor"/>
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


    <!-- - FOR EACH ENTRY -->


    <xsl:template name="currentProblemsSectionEntry">

        <!-- Defining all needed variables -->
        <xsl:variable name="problemCondition"
                      select="n1:act/n1:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.4.5.2']/../n1:entryRelationship[@typeCode='SUBJ']/n1:observation/n1:templateId[@root='1.3.6.1.4.1.12559.11.10.1.3.1.3.7']/../n1:value"/>
        <xsl:variable name="probOnSetDate"
                      select="n1:act/n1:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.4.5.2']/../n1:effectiveTime/n1:low"/>
        <!-- End definition of variables-->

        <!-- nullflavored act -->
        <xsl:choose>
            <xsl:when test="not(n1:act/@nullFlavor)">
                <xsl:choose>
                    <!-- known absence / no information scenario -->
                    <xsl:when test="($problemCondition/@code='no-known-problems' or $problemCondition/@code='no-problem-info')">
                        <tr>
                            <td colspan="3">
                                <xsl:call-template name="show-eHDSI-AbsentOrUnknownProblems">
                                    <xsl:with-param name="node" select="$problemCondition"/>
                                </xsl:call-template>
                            </td>
                        </tr>
                    </xsl:when>
                    <xsl:otherwise>
                        <tr>
                            <td>
                                <!-- Active Problem -->
                                <xsl:call-template name="show-epSOSIllnessesandDisorders">
                                    <xsl:with-param name="node" select="$problemCondition"/>
                                </xsl:call-template>
                                <xsl:text> (</xsl:text>
                                    <xsl:value-of select="$problemCondition/@code"/>
                                <xsl:text>)</xsl:text>
                            </td>
                            <td>
                                <!-- OnSet Date -->
                                <xsl:call-template name="show-TS">
                                    <xsl:with-param name="node" select="$probOnSetDate"/>
                                </xsl:call-template>&#160;
                            </td>
                        </tr>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <tr>
                    <td colspan="3">
                        <xsl:call-template name="show-epSOSNullFlavor">
                            <xsl:with-param name="code" select="n1:act/@nullFlavor"/>
                        </xsl:call-template>
                    </td>
                </tr>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>