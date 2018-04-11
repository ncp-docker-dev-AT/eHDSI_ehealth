<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:n1="urn:hl7-org:v3" version="1.0">

    <!-- global variable title -->
    <xsl:import href="common/cdaCommon.xsl"/>
    <xsl:import href="ps/ps.xsl"/>
    <xsl:import href="ep/ep.xsl"/>
    <!--<xsl:import href="mro/mro.xsl"/>-->
    <!--<xsl:import href="hcer/hcer.xsl"/>-->

    <xsl:variable name="documentCode" select="/n1:ClinicalDocument/n1:code/@code"/>

    <xsl:output method="html" indent="yes" version="4.01" doctype-system="http://www.w3.org/TR/html4/strict.dtd"
                doctype-public="-//W3C//DTD HTML 4.01//EN"/>

    <!-- global variable title -->
    <xsl:variable name="title">
        <xsl:choose>
            <xsl:when test="/n1:ClinicalDocument/n1:code/@displayName">
                <xsl:value-of select="/n1:ClinicalDocument/n1:code/@displayName"/>
            </xsl:when>
            <xsl:when test="string-length(/n1:ClinicalDocument/n1:title)  &gt;= 1">
                <xsl:value-of select="/n1:ClinicalDocument/n1:title"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text>Clinical Document</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:variable>

    <!-- Main -->
    <xsl:template match="/">
        <xsl:apply-templates select="n1:ClinicalDocument"/>
    </xsl:template>
    <!-- produce browser rendered, human readable clinical document	-->
    <xsl:template match="n1:ClinicalDocument">
        <html>
            <head>
                <link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.0.9/css/all.css" integrity="sha384-5SOiIsAziJl6AWe0HWRKTXlfcSHKmYV4RBF18PPJ173Kzn7jzMyFuTtk8JA7QQG1" crossorigin="anonymous"/>
                <xsl:comment>Do NOT edit this HTML directly: it was generated via an XSLT transformation from a CDA
                    Release 2 XML document.
                </xsl:comment>
                <title>
                    <xsl:value-of select="$title"/>
                </title>
                <style type="text/css">
					<xsl:text>
body {
/*	color: #003366;*/
	font-family: Arial;
	font-size: 12px;
}
div {}
table {
	/*line-height: 10pt;*/
	width: 100%;
}
tr {}
td {
	background-color: #F0F0F0;
	padding:5px;
	border-width:1px;
	border-color:#E1E1E1;
	border-style:solid;
	vertical-align: top;
	text-align: left;
	font-weight: normal;
}
th {
	background-color:#D8D8D8;
	color:#4A4A4A;
	font-size:13px;
	padding:6px;
	border-bottom-width:2px;
	border-bottom-color:#4A4A4A;
	border-bottom-style:solid;
	vertical-align: top;
	text-align: left;
}
.h1center {
	font-size: 2em;
	font-weight: bold;
	text-align: center;
}
.header_table{
	border-bottom:0px solid #666;
}
.td_creation_date{
	background-color:#FFF;
	border:0;
}
.header_table th {
	border:0;
	background-color:#000066;
	color:#FFF;
}
.header_table td {
	color:#039;
	font-size:12px;
    font-weight: bold;
}
.ingredients_table{
	border-bottom:0px solid #666;
}
.ingredients_table th {
	border:0;
	background-color:#B8B8B8;
}
.ingredients_table td {
	font-size:12px;
}
.tdtext {
	color:#9FBFDF;
}
.sectionTitle{
	color:#3871A9;
	font-weight: bold;
	font-size:16px;
}
.narr_table {
	width: 100%;
}
.narrow_table {
	width: 35%;
}

.table_caption {
	border-bottom:0px solid #666;
    width: 100%;
}

.table_caption th {
	background-color:#9FBFDF;
	color:#4A4A4A;
	font-size:13px;
	padding:6px;
	border-bottom-width:2px;
	border-bottom-color:#4A4A4A;
	border-bottom-style:solid;
    -moz-border-radius: 0px;
    -webkit-border-radius: 6px 6px 0px 0px;
    border-radius: 6px 6px 0px 0px;
	vertical-align: top;
	text-align:left;
    vertical-align:middle;
}

.translation_table{
}

.translation_table th {
	border:0;
    border-bottom-width:2px;
	border-bottom-color:#4A4A4A;
	border-bottom-style:solid;
	background-color:#C3D9EF;
}

.translation_table td {
	background-color:#E6F2FF;
	font-size:12px;
}

.nullFlavor {
  display: inline;
  text-align: center;
  vertical-align: middle;
  border-radius: 50%;
  background: yellow;
  font-weight: bold;
  padding:5px;
}

.wrap-collabsible {
  margin-bottom: 1.2rem 0;
  width: 100%;
}

input[type='checkbox'] {
  display: none;
}

<!-- collapsible panel -->
.lbl-toggle {
  display: block;

  font-weight: bold;
  font-size: 13px;
  text-align: left;

  padding: 0.5rem;

  background: #9FBFDF;

  cursor: pointer;

  border-radius: 3px;
  transition: all 0.25s ease-out;
}

.lbl-toggle:hover {
  color: #7C5A0B;
}

.lbl-toggle::before {
  content: ' ';
  display: inline-block;

  border-top: 5px solid transparent;
  border-bottom: 5px solid transparent;
  border-left: 5px solid currentColor;
  vertical-align: middle;
  margin-right: .7rem;
  transform: translateY(-2px);

  transition: transform .2s ease-out;
}

.toggle:checked + .lbl-toggle::before {
  transform: rotate(90deg) translateX(-3px);
}

.collapsible-content {
  max-height: 0px;
  overflow: hidden;
  transition: max-height .25s ease-in-out;
}

.toggle:checked + .lbl-toggle + .collapsible-content {
  max-height: none;
}

.toggle:checked + .lbl-toggle {
  border-bottom-right-radius: 0;
  border-bottom-left-radius: 0;
}

.collapsible-content .content-inner {
  background: white;
  border-bottom: 2px solid #9FBFDF;
  border-left: 2px solid #9FBFDF;
  border-right: 2px solid #9FBFDF;
  border-bottom-left-radius: 7px;
  border-bottom-right-radius: 7px;
  padding: .5rem 1rem;
}

<!-- collapsible panel for section titles -->
.lbl-toggle-title {
  display: block;

  font-weight: bold;
  color: white;
  font-size: 15px;
  text-align: left;

  padding: 0.5rem;

  background: #000066;

  cursor: pointer;

  border-radius: 3px;
  transition: all 0.25s ease-out;
}

.lbl-toggle-title:hover {
  color: #9FBFDF;
}

.lbl-toggle-title::before {
  content: ' ';
  display: inline-block;

  border-top: 5px solid transparent;
  border-bottom: 5px solid transparent;
  border-left: 5px solid currentColor;
  vertical-align: middle;
  margin-right: .7rem;
  transform: translateY(-2px);

  transition: transform .2s ease-out;
}

.toggle:checked + .lbl-toggle-title::before {
  transform: rotate(90deg) translateX(-3px);
}

.collapsible-content-title {
  max-height: 0px;
  overflow: hidden;
  transition: max-height .25s ease-in-out;
}

.toggle:checked + .lbl-toggle-title + .collapsible-content-title {
  max-height: none;
}

.toggle:checked + .lbl-toggle-title {
  border-bottom-right-radius: 0;
  border-bottom-left-radius: 0;
}

.collapsible-content-title .content-inner-title {
  background: white;
  border-bottom: 2px solid #000066;
  border-left: 2px solid #000066;
  border-right: 2px solid #000066;
  border-bottom-left-radius: 7px;
  border-bottom-right-radius: 7px;
  padding: .5rem 1rem;
}

<!-- collapsible panel for patient block -->
.lbl-toggle-patient {
  display: block;

  font-weight: bold;
  color: #000066;
  font-size: 15px;
  text-align: left;

  padding: 0.5rem;

  background: #9FBFDF;

  cursor: pointer;

  border-radius: 3px;
  transition: all 0.25s ease-out;
}

.lbl-toggle-patient:hover {
  color: white;
}

.lbl-toggle-patient::before {
  content: ' ';
  display: inline-block;

  border-top: 5px solid transparent;
  border-bottom: 5px solid transparent;
  border-left: 5px solid currentColor;
  vertical-align: middle;
  margin-right: .7rem;
  transform: translateY(-2px);

  transition: transform .2s ease-out;
}

.toggle:checked + .lbl-toggle-patient::before {
  transform: rotate(90deg) translateX(-3px);
}

.collapsible-content-patient {
  max-height: 0px;
  overflow: hidden;
  transition: max-height .25s ease-in-out;
}

.toggle:checked + .lbl-toggle-patient + .collapsible-content-patient {
  max-height: none;
}

.toggle:checked + .lbl-toggle-patient {
  border-bottom-right-radius: 0;
  border-bottom-left-radius: 0;
}

.collapsible-content-patient .content-inner-patient {
  background: #80DFFF;
  border-bottom: 2px solid #9FBFDF;
  border-left: 2px solid #9FBFDF;
  border-right: 2px solid #9FBFDF;
  border-bottom-left-radius: 7px;
  border-bottom-right-radius: 7px;
  padding: .5rem 1rem;
}


.codeSystem {
    background-color:#9FBFDF;
    margin-top:20px;
    padding-left:10px;
    padding-right:10px;
    border-radius:10px 10px 10px 10px;
}
                    </xsl:text>
                </style>
                <script language="javascript">
                    function showhide(id){
                    if (document.getElementById){
                    obj = document.getElementById(id);
                    if (obj.style.display == "none"){
                    obj.style.display = "";
                    } else {
                    obj.style.display = "none";
                    }
                    }
                    }
                </script>
            </head>
            <body>
                <h1 class="h1center">
                    <xsl:value-of select="$title"/>
                </h1>
                <!-- START display top portion of clinical document -->
                <!--- BASIC HEADER INFORMATION -->
                <xsl:choose>
                    <xsl:when test="$documentCode='60591-5'">
                        <xsl:call-template name="psCda"/>
                        <br/>
                        <br/>
                    </xsl:when>
                    <xsl:when test="$documentCode='57833-6'">
                        <xsl:call-template name="epCda"/>
                        <br/>
                        <br/>
                    </xsl:when>
                    <!--<xsl:when test="$documentCode='56445-0'">-->
                        <!--<xsl:call-template name="mroCda"/>-->
                        <!--<br/>-->
                        <!--<br/>-->
                    <!--</xsl:when>-->
                    <!--<xsl:when test="$documentCode='34133-9'">-->
                        <!--<xsl:call-template name="hcerCda"/>-->
                        <!--<br/>-->
                        <!--<br/>-->
                    <!--</xsl:when>-->
                </xsl:choose>
                <br/>
                <br/>
            </body>
        </html>
    </xsl:template>
    <!-- generate table of contents -->
</xsl:stylesheet>