<?xml version="1.0"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:n1="urn:hl7-org:v3">

    <xsl:variable name="functionalStatusSectionCode"
                  select="'47420-5'"/>

    <!--functional status -->
    <xsl:template name="functionalStatus" match="/">
        <xsl:apply-templates select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:code[@code=$functionalStatusSectionCode]]"/>
        <br/>
        <br/>
    </xsl:template>

    <xsl:template match = "/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:code[@code=$functionalStatusSectionCode]]">
        <div class="wrap-collapsible">
            <input id="collapsible-functional-status-section" class="toggle" type="checkbox" checked="true" />
            <label for="collapsible-functional-status-section" class="lbl-toggle-title">
                <!-- Section title -->
                <xsl:call-template name="show-eHDSISection">
                    <xsl:with-param name="code" select="$functionalStatusSectionCode"/>
                </xsl:call-template>
            </label>
            <div class="collapsible-content-title">
                <div class="content-inner-title">
                    <div class="wrap-collapsible">
                        <input id="collapsible-functional-status-original" class="toggle" type="checkbox"/>
                        <label for="collapsible-functional-status-original" class="lbl-toggle">
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
                        <input id="collapsible-functional-status-translated" class="toggle" type="checkbox" checked="true"/>
                        <label for="collapsible-functional-status-translated" class="lbl-toggle">
                            <xsl:value-of select="$translatedCodedTableTitle"/>
                        </label>
                        <div class="collapsible-content">
                            <div class="content-inner">

                                <table class="translation_table">
                                    <tbody>
                                        <tr>
                                            <th>
                                                <!-- Functional Assessment Date-->
                                                <!-- TODO Needs to be added to eHDSIDisplayLabel value set -->
                                                Functional Assessment Date
                                            </th>
                                            <th>
                                                <!-- Functional Assessment -->
                                                <!-- TODO Needs to be added to eHDSIDisplayLabel value set -->
                                                Functional Assessment
                                            </th>
                                            <th>
                                                <!-- Onset Date -->
                                                <!-- TODO Needs to be added to eHDSIDisplayLabel value set -->
                                                Onset Date
                                            </th>
                                            <th>
                                                <!-- Functional assessment result -->
                                                <!-- TODO Needs to be added to eHDSIDisplayLabel value set -->
                                                Functional Assessment Result
                                            </th>
                                        </tr>
                                        <xsl:apply-templates select="n1:entry/n1:organizer/n1:component/n1:observation" mode="functionalStatus"/>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </xsl:template>

    <xsl:template match="n1:entry/n1:organizer/n1:component/n1:observation" mode="functionalStatus">
        <tr>
            <td>
                <xsl:call-template name="show-IVL_TS">
                    <xsl:with-param name="node" select="../../n1:effectiveTime"/>
                </xsl:call-template>
            </td>
            <td>
                <!-- TODO Needs to be bound with a value set in the MVC -->
                <xsl:value-of select="n1:code/@displayName"/>
            </td>
            <td>
                <xsl:call-template name="show-IVL_TS">
                    <xsl:with-param name="node" select="n1:effectiveTime"/>
                </xsl:call-template>
            </td>
            <td>
                <!-- TODO Needs to be bound with a value set in the MVC -->
                <xsl:value-of select="n1:value/@displayName"/>
            </td>
        </tr>
    </xsl:template>
</xsl:stylesheet>