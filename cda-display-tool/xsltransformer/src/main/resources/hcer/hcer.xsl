<?xml version="1.0"  ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:import href="../common/cdaCommon.xsl"/>
    <xsl:import href="../ps/psHeader.xsl"/>
    <xsl:import href="../ps/psExtendedHeader.xsl"/>
    <xsl:import href="../ps/psAllergiesAndOtherAdverseReactions.xsl"/>
    <xsl:import href="../ps/psCodedResults.xsl"/>
    <xsl:import href="../ps/psActiveProblems.xsl"/>
    <xsl:import href="../ps/psMedicationSummary.xsl"/>
    <xsl:import href="../ps/psMedicalDevices.xsl"/>
    <xsl:import href="../ps/psListOfSurgeries.xsl"/>
    <xsl:import href="../ps/psHistoryOfPastIllnesses.xsl"/>
    <xsl:import href="../ps/psImmunizations.xsl"/>
    <xsl:import href="../ps/psHealthMaintenanceCarePlan.xsl"/>
    <xsl:import href="../ps/psFunctionalStatus.xsl"/>
    <xsl:import href="../ps/psSocialHistory.xsl"/>
    <xsl:import href="../ps/psPregnancyHistory.xsl"/>
    <xsl:import href="../ps/psVitalSigns.xsl"/>
    <xsl:import href="../ps/psOtherSection.xsl"/>

    <xsl:template name="hcerCda">
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
                <a href="javascript: showhide('extendedCdaHeader'); self.focus(); void(0);">
                    <xsl:call-template name="show-eHDSIDisplayLabel">
                        <xsl:with-param name="code" select="'68'"/>
                    </xsl:call-template>
                </a>
            </legend>
            <div id="extendedCdaHeader" style="display:none">
                <xsl:call-template name="extendedCdaHeader"/>
                <br/>
                <br/>
            </div>
        </fieldset>
        <br/>
        <!-- eHDSI Allergies and Other Adverse Reactions -->
        <xsl:call-template name="allergiesAndIntolerances"/>
        <!-- eHDSI Coded Results -->
        <xsl:call-template name="codedResults"/>
        <!-- eHDSI Active Problems -->
        <xsl:call-template name="activeProblems"/>
        <!-- eHDSI Medication Summary -->
        <xsl:call-template name="medicationSummary"/>
        <!-- eHDSI Medical Devices -->
        <xsl:call-template name="medicalDevices"/>
        <!-- eHDSI List of Surgeries -->
        <xsl:call-template name="surgicalProcedures"/>
        <!-- eHDSI History of Past Illnesses -->
        <xsl:call-template name="historyOfPastIllnesses"/>
        <!-- eHDSI Immunizations -->
        <xsl:call-template name="immunizations"/>
        <!-- eHDSI Health Maintenance Care Plan -->
        <xsl:call-template name="healthMaintenanceCarePlan"/>
        <!-- eHDSI Functional Status -->
        <xsl:call-template name="functionalStatus"/>
        <!-- eHDSI Social History -->
        <xsl:call-template name="socialHistory"/>
        <!-- eHDSI Pregnancy History -->
        <xsl:call-template name="pregnancyHistory"/>
        <!-- eHDSI Vital Signs -->
        <xsl:call-template name="vitalSigns"/>
        <!-- All other sections -->
        <xsl:call-template name="otherSections"/>
    </xsl:template>
    <!-- generate table of contents -->
</xsl:stylesheet>
