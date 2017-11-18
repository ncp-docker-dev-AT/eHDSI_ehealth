<?xml version="1.0"  ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
    <xsl:import href="cdaHeader.xsl"/>
    <xsl:import href="cdaExtendedHeader.xsl"/>
    <xsl:import href="cdaAlerts.xsl"/>
    <!--<xsl:import href="cdaDiagnosticTest.xsl"/>-->
    <!--<xsl:import href="cdaCurrentProblems.xsl"/>-->
    <!--<xsl:import href="cdaMedicationSummary.xsl"/>-->
    <!--<xsl:import href="cdaMedicalDevices.xsl"/>-->
    <xsl:import href="cdaSurgicalProcedures.xsl"/>
    <!--<xsl:import href="cdaHistoryIllness.xsl"/>-->
    <!--<xsl:import href="cdaVaccinations.xsl"/>-->
    <!--<xsl:import href="cdaTreatment.xsl"/>-->
    <!--<xsl:import href="cdaAutonomy.xsl"/>-->
    <!--<xsl:import href="cdaSocialHistory.xsl"/>-->
    <!--<xsl:import href="cdaPregnancyHistory.xsl"/>-->
    <!--<xsl:import href="cdaPhysicalFindings.xsl"/>-->
    <!--<xsl:import href="cdaOtherSection.xsl"/>-->

    <!-- xsl:output method="html" indent="yes" version="4.01"   doctype-system="http://www.w3.org/TR/html4/strict.dtd" doctype-public="-//W3C//DTD HTML 4.01//EN"/ -->
    <xsl:template name="psCda">
        <!-- Main -->
        <!-- produce browser rendered, human readable clinical document	-->
        <!-- START display top portion of clinical document -->
        <!--- BASIC HEADER INFORMATION -->
        <xsl:call-template name="basicCdaHeader"/>
        <br/>
        <br/>
        <!--- EXTENDED HEADER INFORMATION -->
        <fieldset>
            <legend>
                <h2>
                    <a href="javascript: showhide('extendedCdaHeader'); self.focus(); void(0);">
                        <xsl:call-template name="show-displayLabels">
                            <xsl:with-param name="code" select="'68'"/>
                        </xsl:call-template>
                    </a>
                </h2>
            </legend>
            <div id="extendedCdaHeader" style="display:none">
                <xsl:call-template name="extendedCdaHeader"/>
                <br/>
                <br/>
            </div>
        </fieldset>
        <br/>
        <!-- ALERTS -->
        <xsl:call-template name="alerts"/>
        <!--&lt;!&ndash;DIAGNOSTIC TEST &ndash;&gt;-->
        <!--<xsl:call-template name="diagnosticTests"/>-->
        <!--&lt;!&ndash;CurrenTProblems &ndash;&gt;-->
        <!--<xsl:call-template name="currentProblems"/>-->
        <!--&lt;!&ndash;Medication Summary &ndash;&gt;-->
        <!--<xsl:call-template name="medicationSummary"/>-->
        <!--&lt;!&ndash;Medical Devices Summary &ndash;&gt;-->
        <!--<xsl:call-template name="medicalDevices"/>-->
        <!--Surgical Procedures-->
        <xsl:call-template name="surgicalProcedures"/>
        <!--&lt;!&ndash;History Illness&ndash;&gt;-->
        <!--<xsl:call-template name="historyIllness"/>-->
        <!--&lt;!&ndash;Vaccination&ndash;&gt;-->
        <!--<xsl:call-template name="vaccinations"/>-->
        <!--&lt;!&ndash;treatments&ndash;&gt;-->
        <!--<xsl:call-template name="treatment"/>-->
        <!--&lt;!&ndash;autonomy&ndash;&gt;-->
        <!--<xsl:call-template name="autonomy"/>-->
        <!--&lt;!&ndash;social History&ndash;&gt;-->
        <!--<xsl:call-template name="socialHistory"/>-->
        <!--&lt;!&ndash;pregnancy History&ndash;&gt;-->
        <!--<xsl:call-template name="pregnancyHistory"/>-->
        <!--&lt;!&ndash;physical Findings&ndash;&gt;-->
        <!--<xsl:call-template name="physicalFindings"/>-->
        <!--&lt;!&ndash;all other sections&ndash;&gt;-->
        <!--<xsl:call-template name="otherSections"/>-->
    </xsl:template>
    <!-- generate table of contents -->
</xsl:stylesheet>
