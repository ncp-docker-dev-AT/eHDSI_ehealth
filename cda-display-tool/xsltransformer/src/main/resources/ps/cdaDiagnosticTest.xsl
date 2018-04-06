<?xml version="1.0"  ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:n1="urn:hl7-org:v3">
    <xsl:output method="html" indent="yes" version="4.01" doctype-system="http://www.w3.org/TR/html4/strict.dtd"
                doctype-public="-//W3C//DTD HTML 4.01//EN"/>

    <!-- variable to check that at least one diagnostic section exists -->
    <xsl:variable name="diagnosticExist"
                  select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section/n1:code[@code='30954-2']"/>

    <!-- diagnostics -->
    <xsl:template name="diagnosticTests" match="/n1:ClinicalDocument/n1:component/n1:structuredBody">
        <!-- if we have at least one relevant diagnostic test section -->
        <xsl:if test="($diagnosticExist)">
            <xsl:for-each select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section">
                <xsl:call-template name="diagnosticSection"/>
            </xsl:for-each>
        </xsl:if>
    </xsl:template>

    <xsl:template name="diagnosticSection">

        <!-- Defining all needed variables -->
        <xsl:variable
                name="diagSectionTitleCode"
                select="n1:code/@code"/>

        <xsl:variable
                name="diagSectionTitle"
                select="n1:code[@code='30954-2']/@displayName"/>

        <xsl:variable
                name="nullEntry"
                select="n1:entry"/>
        <!-- End definition of variables-->

        <xsl:choose>
            <!-- if sectionTitle is not missing for relevant diagnostic test (Exception relevant diagnostic test section is missing) -->
            <xsl:when test=" ($diagSectionTitleCode='30954-2')">
                <span class="sectionTitle">
                    <xsl:value-of select="$diagSectionTitle"/>&#160;&#160;
                </span>
                <br/>
                <xsl:choose>
                    <xsl:when test="$shownarrative='true'">
                        <a href="javascript: showhide('diagTr'); self.focus(); void(0);">Show/Hide</a>
                        <div id="diagTr" style="display:block">
                            <xsl:apply-templates
                                    select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section/n1:code[@code='30954-2']/../n1:text"/>
                            <br/>
                        </div>
                    </xsl:when>
                </xsl:choose>
                <xsl:choose>
                    <xsl:when test="not($nullEntry/@nullFlavor)">
                        <table>
                            <tbody>
                                <tr>
                                    <!-- TODO These values have to be added to the epsosDisplayLabel value set -->
                                    <th>Diagnostic Date</th>
                                    <th>Blood Group</th>
                                </tr>
                                <xsl:for-each select="n1:entry">
                                    <xsl:call-template name="diagnosticSectionEntry"/>
                                </xsl:for-each>
                            </tbody>
                        </table>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:call-template name="show-nullFlavor">
                            <xsl:with-param name="code" select="$nullEntry/@nullFlavor"/>
                        </xsl:call-template>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <!-- FOR EACH ENTRY -->
    <xsl:template name="diagnosticSectionEntry">
        <!-- Defining all needed variables -->
        <!--  release constraint with no root templateid -->
        <xsl:variable
                name="diagnosticDate"
                select="n1:observation/n1:code[@code='34530-6']/../n1:effectiveTime"/>
        <xsl:variable
                name="bloodGroup"
                select="n1:observation/n1:code[@code='34530-6']/../n1:value/@displayName"/>
        <xsl:variable
                name="nullEntry"
                select="."/>
        <!-- End definition of variables-->

        <xsl:choose>
            <!-- if sectionTitle is not missing for diagnostics  (Exception diagnostics section is missing)-->
            <xsl:when test="not($nullEntry/@nullFlavor)">
                <tr>
                    <td>
                        <xsl:call-template name="show-time">
                            <xsl:with-param name="datetime" select="$diagnosticDate"/>
                        </xsl:call-template>
                    </td>
                    <td>
                        <xsl:value-of select="$bloodGroup"/>
                    </td>
                </tr>
            </xsl:when>
            <xsl:otherwise>
                <tr>
                    <td colspan="2">
                        <xsl:call-template name="show-nullFlavor">
                            <xsl:with-param name="code" select="$nullEntry/@nullFlavor"/>
                        </xsl:call-template>
                    </td>
                </tr>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>