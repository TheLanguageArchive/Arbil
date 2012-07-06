<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xhtml="http://www.w3.org/1999/xhtml" exclude-result-prefixes="xhtml"
    version="1.0">
    <xsl:output method="xml" omit-xml-declaration="yes"/>
    <xsl:template match="/xhtml:html">
        <xsl:copy-of select="xhtml:body" />
    </xsl:template>
    <xsl:template match="*"/>
</xsl:stylesheet>