<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:n1="urn:hl7-org:v3" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                version="1.0">

    <xsl:output method="html" indent="yes" version="4.01" doctype-system="http://www.w3.org/TR/html4/strict.dtd"
                doctype-public="-//W3C//DTD HTML 4.01//EN"/>

    <!-- PQ datatype -->
    <xsl:template name="show-PQ">
        <xsl:param name="node"/>
        <xsl:choose>
            <xsl:when test="not ($node/@nullFlavor)">
                <xsl:choose>
                    <xsl:when test="$node/@value">
                        <xsl:value-of select="$node/@value"/>
                        <xsl:text> </xsl:text>
                        <xsl:call-template name="show-epSOSUnits">
                            <xsl:with-param name="code" select="$node/@unit"/>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <!-- uncoded element -->
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

    <!-- IVL_TS datatype -->
    <xsl:template name="show-IVL_TS">
        <xsl:param name="node"/>
        <xsl:variable name="low" select="$node/n1:low"/>
        <xsl:variable name="high" select="$node/n1:high"/>
        <xsl:variable name="width" select="$node/n1:width"/>
        <xsl:variable name="center" select="$node/n1:center"/>
        <xsl:variable name="nullFlavor" select="$node/@nullFlavor"/>
        <xsl:choose>
            <xsl:when test="$width">
                <xsl:call-template name="show-PQ">
                    <xsl:with-param name="node" select="$width"/>
                </xsl:call-template>&#160;
            </xsl:when>
            <xsl:when test="$low and $high">
                <xsl:call-template name="show-TS">
                    <xsl:with-param name="node" select="$low"/>
                </xsl:call-template>-
                <xsl:call-template name="show-TS">
                    <xsl:with-param name="node" select="$high"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$low">
                <xsl:call-template name="show-TS">
                    <xsl:with-param name="node" select="$low"/>
                </xsl:call-template>&#160;
            </xsl:when>
            <xsl:when test="$high">
                <xsl:call-template name="show-TS">
                    <xsl:with-param name="node" select="$high"/>
                </xsl:call-template>&#160;
            </xsl:when>
            <xsl:when test="$nullFlavor">
                <xsl:call-template name="show-epSOSNullFlavor">
                    <xsl:with-param name="code" select="$nullFlavor"/>
                </xsl:call-template>&#160;
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="show-TS">
                    <xsl:with-param name="node" select="$node"/>
                </xsl:call-template>&#160;
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- TS Datatype -->
    <xsl:template name="show-TS">
        <xsl:param name="node"/>
        <xsl:choose>
            <xsl:when test="not($node)">
                <xsl:text> </xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:choose>
                    <xsl:when test="not($node/@nullFlavor)and $node/@value">
                        <xsl:call-template name="formatDateTime">
                            <xsl:with-param name="date" select="$node/@value"/>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:call-template name="show-epSOSNullFlavor">
                            <xsl:with-param name="code" select="$node/@nullFlavor"/>
                        </xsl:call-template>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>