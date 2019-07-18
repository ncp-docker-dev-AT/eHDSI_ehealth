<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:n1="urn:hl7-org:v3">
    <xsl:output method="html" indent="yes" version="4.01" doctype-system="http://www.w3.org/TR/html4/strict.dtd"
                doctype-public="-//W3C//DTD HTML 4.01//EN"/>

    <!-- variable to check that at least one autonomy section exist -->
    <xsl:variable name="autonomyExist"
                  select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section/n1:code[@code='47420-5']"/>


    <!-- autonomy/invalidity -->
    <xsl:template name="autonomy" match="/n1:ClinicalDocument/n1:component/n1:structuredBody">

        <xsl:choose>
            <!-- if we have at least one autonomy section -->
            <xsl:when test="($autonomyExist)">
                <xsl:for-each select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section">
                    <xsl:call-template name="autonomySection"/>
                </xsl:for-each>
                <br/>
                <br/>
            </xsl:when>
        </xsl:choose>
    </xsl:template>


    <xsl:template name="autonomySection">

        <!-- Defining all needed variables -->
        <xsl:variable
                name="autonomySectionTitleCode"
                select="n1:code/@code"/>
        <xsl:variable
                name="autonomySectionTitle"
                select="n1:code[@code='47420-5']/@displayName"/>
        <xsl:variable
                name="autonomySectionText"
                select="n1:code[@code='47420-5']/../n1:text"/>
        <!-- End definition of variables-->

        <xsl:choose>
            <!-- if sectionTitle is not missing for alerts  (Exception alerts section is missing)-->
            <xsl:when test=" ($autonomySectionTitleCode='47420-5')">

                <div class="wrap-collabsible">
                    <input id="collapsible-social-history-section-original" class="toggle" type="checkbox" checked="true" />
                    <label for="collapsible-social-history-section-original" class="lbl-toggle-title">
                        <xsl:value-of select="$autonomySectionTitle"/>
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
                                                    <xsl:with-param name="node" select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section/n1:code[@code='47420-5']/../n1:text"/>
                                                </xsl:call-template>
                                            </div>
                                        </div>
                                    </div>
                                </xsl:when>
                            </xsl:choose>
                            <br/>
                        </div>
                    </div>
                </div>
            </xsl:when>
        </xsl:choose>

        <xsl:for-each select="n1:component/n1:section">
            <xsl:call-template name="autonomySection">
            </xsl:call-template>
        </xsl:for-each>

    </xsl:template>
</xsl:stylesheet>