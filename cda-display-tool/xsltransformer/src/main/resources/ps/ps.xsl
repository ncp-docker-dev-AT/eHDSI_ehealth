<?xml version="1.0"  ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
    <xsl:import href="cdaHeader.xsl"/>
    <xsl:import href="cdaExtendedHeader.xsl"/>
    <xsl:import href="cdaAlerts.xsl"/>
    <xsl:import href="cdaDiagnosticTest.xsl"/>
    <xsl:import href="cdaCurrentProblems.xsl"/>
    <xsl:import href="cdaMedicationSummary.xsl"/>
    <xsl:import href="cdaMedicalDevices.xsl"/>
    <xsl:import href="cdaSurgicalProcedures.xsl"/>
    <xsl:import href="cdaHistoryIllness.xsl"/>
    <xsl:import href="cdaVaccinations.xsl"/>
    <xsl:import href="cdaTreatment.xsl"/>
    <xsl:import href="cdaAutonomy.xsl"/>
    <xsl:import href="cdaSocialHistory.xsl"/>
    <xsl:import href="cdaPregnancyHistory.xsl"/>
    <xsl:import href="cdaPhysicalFindings.xsl"/>
    <xsl:import href="cdaOtherSection.xsl"/>

    <xsl:variable name="originalNarrativeTableTitle" select="'Original narrative'"/>
    <xsl:variable name="translatedCodedTableTitle" select="'Translated coded'"/>

    <!-- xsl:output method="html" indent="yes" version="4.01"   doctype-system="http://www.w3.org/TR/html4/strict.dtd" doctype-public="-//W3C//DTD HTML 4.01//EN"/ -->
    <xsl:template name="psCda">
        <!-- Main -->
        <!-- produce browser rendered, human readable clinical document	-->
        <!-- START display top portion of clinical document -->
        <!--- PATIENT INFORMATION -->
        <div class="patient">
            <div class="wrap-collabsible">
                <input id="collapsible-patient-header" class="toggle" type="checkbox" checked="true"/>
                <label for="collapsible-patient-header" class="lbl-toggle-patient">
                    <!-- Patient-->
                    <xsl:call-template name="show-displayLabels">
                        <xsl:with-param name="code" select="'51'"/>
                    </xsl:call-template>
                </label>
                <div class="collapsible-content-patient">
                    <div class="content-inner-patient">
                        <div>
                            <xsl:call-template name="patientBlock"/>
                            <br/>
                            <div class="wrap-collabsible">
                                <input id="collapsible-extended-header" class="toggle" type="checkbox"/>
                                <label for="collapsible-extended-header" class="lbl-toggle">
                                    <xsl:call-template name="show-displayLabels">
                                        <xsl:with-param name="code" select="'68'"/>
                                    </xsl:call-template>
                                </label>
                                <div class="collapsible-content">
                                    <div class="content-inner">
                                        <div id="extendedCdaHeader">
                                            <xsl:call-template name="extendedCdaHeader"/>
                                            <br/>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <br/>
        <div class="content">
            <!--- BASIC HEADER INFORMATION -->
            <xsl:call-template name="basicCdaHeader"/>
            <br/>
            <div class="wrap-collabsible">
                <input id="collapsible-clinical-sections" class="toggle" type="checkbox" checked="true"/>
                <label for="collapsible-clinical-sections" class="lbl-toggle-patient">
                    Clinical sections
                </label>
                <div class="collapsible-content-patient">
                    <div class="content-inner-patient">
                        <!-- Alerts -->
                        <xsl:call-template name="alerts"/>
                        <!-- Diagnostic Tests -->
                        <xsl:call-template name="diagnosticTests"/>
                        <!-- Current Problems -->
                        <xsl:call-template name="currentProblems"/>
                        <!-- Medication Summary -->
                        <xsl:call-template name="medicationSummary"/>
                        <!-- Medical Devices Summary -->
                        <xsl:call-template name="medicalDevices"/>
                        <!-- Surgical Procedures-->
                        <xsl:call-template name="surgicalProcedures"/>
                        <!-- History Illness-->
                        <xsl:call-template name="historyIllness"/>
                        <!-- Vaccination-->
                        <xsl:call-template name="vaccinations"/>
                        <!-- Treatments-->
                        <xsl:call-template name="treatment"/>
                        <!-- Autonomy-->
                        <xsl:call-template name="autonomy"/>
                        <!-- Social History-->
                        <xsl:call-template name="socialHistory"/>
                        <!-- Pregnancy History-->
                        <xsl:call-template name="pregnancyHistory"/>
                        <!-- Physical Findings-->
                        <xsl:call-template name="physicalFindings"/>
                        <!-- All other sections-->
                        <xsl:call-template name="otherSections"/>
                    </div>
                </div>
            </div>
        </div>
    </xsl:template>
    <!-- generate table of contents -->
</xsl:stylesheet>
