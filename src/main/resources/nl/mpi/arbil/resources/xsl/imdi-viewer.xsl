<?xml version="1.0" encoding="UTF-8"?>
<!--
 View IMDI 3.0 as HTML, Version 1.1
 File  : imdi_viewer.xsl
 Author: fredof
 Date  : August 1, 2006
  
 Copyright (C) 2006  Freddy Offenga <freddy.offenga@mpi.nl>
 Max Planck Institute for Psycholinguistics
 Wundtlaan 1, 6525 XD Nijmegen, The Netherlands
 
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 
 This is a modified version which is under construction by Evelyn Richter, July 2009
 Last modification by Evelyn Richter, 13 Aug 2009, adjusted Catalogue: DocumentLanguages, SubjectLanguages, Quality, Format   
 -->
<xsl:stylesheet version="1.0" xmlns:imdi="http://www.mpi.nl/IMDI/Schema/IMDI" xmlns:saxon="http://saxon.sf.net/" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="saxon imdi">
    <xsl:output indent="yes" method="html" encoding="UTF-8" doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"/>
    <!--
        Display external descriptions as html links (true|false)
    -->
    <xsl:param name="INFO_LINKING">false</xsl:param>
    <!--
         Display corpus links as html links (true|false)
    -->
    <xsl:param name="CORPUS_LINKING">true</xsl:param>
    <!--
         Display media/written resource links as html links (true|false)
    -->
    <xsl:param name="RESOURCE_LINKING">false</xsl:param>
    <!--
         Servlet session id
    -->
    <xsl:param name="SESSION_ID"></xsl:param>
    <!--
         File used for javascript functions
    -->
    <xsl:param name="JAVASCRIPT_INCLUDE_FILE">imdi-viewer.js</xsl:param>
    <!-- 
        Javascript function for initialisation
    -->
    <xsl:param name="JAVASCRIPT_INITIALISE">init_viewer</xsl:param>
    <!-- 
            document if; e.g. imdi node id or URID  
        -->
    <xsl:param name="DOCUMENT_ID"></xsl:param>
    <!-- 
        Javascript function to change status of open/close group 
    -->
    <xsl:param name="JAVASCRIPT_CHANGE_STATUS">change_status</xsl:param>
    <!-- 
        File used for css classes
    -->
    <xsl:param name="CSS_FILE">imdi-viewer.css</xsl:param>
    <!-- 
        File used as tree-open icon
    -->
    <xsl:param name="IMAGE_OPEN">imdi-viewer-open.gif</xsl:param>
    <!-- 
        File used as tree-closed icon
    -->
    <xsl:param name="IMAGE_CLOSED">imdi-viewer-closed.gif</xsl:param>
    <!-- 
        Number of chars limit for small text blocks 
    -->
    <xsl:param name="SMALL_BLOCK_LIMIT">300</xsl:param>
    <!-- 
         Separator for tokens in the SEARCH_STRING parameter
         this character should not appear in the content!
    -->
    <xsl:param name="TOKEN_SEPARATOR" xml:space="preserve"> </xsl:param>
    <!--
        Search tokens as string parameter
    -->
    <xsl:param name="SEARCH_STRING"></xsl:param>
    <xsl:param name="DISPLAY_ONLY_BODY">false</xsl:param>
    <!-- 
        SEARCH_TOKENS is the SEARCH_STRING enclosed by TOKEN_SEPARATOR
        because the token in the XML content is also enclosed by TOKEN_SEPARATOR
        so that it can be used in the 'contains' function
    -->
    <xsl:variable name="SEARCH_TOKENS" select="concat($TOKEN_SEPARATOR, $SEARCH_STRING, $TOKEN_SEPARATOR)"/>
    <!--
        Root match
    -->
    <xsl:template match="/imdi:*">
        <xsl:choose>
          <xsl:when test="$DISPLAY_ONLY_BODY = 'true'">
                <xsl:apply-templates select="imdi:*">
                    <xsl:with-param name="level" select="0"/>
                </xsl:apply-templates>
          </xsl:when>
        <xsl:otherwise>
        <html>
            <head>
                <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
                <title>
                    <xsl:value-of select="name(imdi:*|*)"/> : <xsl:value-of select="imdi:*/imdi:Name/text()|*/Name/text()"/> - <xsl:value-of select="imdi:Session/imdi:Title/text()"/> [IMDI 3.0] </title>
                <script language="javascript">
                    <xsl:attribute name="src">
                        <xsl:value-of select="$JAVASCRIPT_INCLUDE_FILE"/>
                    </xsl:attribute>
                </script>
                <xsl:element name="link">
                    <xsl:attribute name="type">text/css</xsl:attribute>
                    <xsl:attribute name="rel">stylesheet</xsl:attribute>
                    <xsl:attribute name="href">
                        <xsl:value-of select="$CSS_FILE"/>
                    </xsl:attribute>
                </xsl:element>
            </head>
            <body>
                <xsl:attribute name="onload">
                    <xsl:value-of select="$JAVASCRIPT_INITIALISE"/>
                    <xsl:text>('</xsl:text>
                    <xsl:value-of select="$IMAGE_OPEN"/>
                    <xsl:text>','</xsl:text>
                    <xsl:value-of select="$IMAGE_CLOSED"/>
                    <xsl:text>');</xsl:text>
                </xsl:attribute>
                <div class="IMDI_header">
                    <span class="IMDI_logo"/>ISLE Metadata Initiative</div>
                <xsl:apply-templates select="imdi:*">
                    <xsl:with-param name="level" select="0"/>
                </xsl:apply-templates>
                <!--                <p>
                    <a href="http://validator.w3.org/check/referer">
                        <img src="http://www.w3.org/Icons/valid-xhtml10" alt="Valid XHTML 1.0!"
                            border="0" height="31" width="88"/>
                    </a>
                </p> -->
            </body>
        </html>
    </xsl:otherwise>
    </xsl:choose>
    </xsl:template>
    <!-- 
        Skip elements
    -->
    <xsl:template match="imdi:History">
        <!-- skip -->
    </xsl:template>
    <!-- 
        Forward elements
    -->
   <xsl:template match="imdi:MDGroup|imdi:Resources|imdi:CommunicationContext">
        <xsl:param name="level"/>
        <xsl:param name="parent_content_matched"/>
        <!--DEBUG-->
        <!--<xsl:message><xsl:value-of select="name(.)"/> level <xsl:value-of select="$level"/></xsl:message> -->
        <xsl:apply-templates select="imdi:*">
            <xsl:with-param name="level" select="$level"/>
            <xsl:with-param name="parent_content_matched" select="$parent_content_matched"/>
        </xsl:apply-templates>
    </xsl:template>
    <!-- 
        content matches with tokens in SEARCH STRING
    -->
    <xsl:template match="imdi:*|*" mode="content_matches">
        <xsl:variable name="plain_content">
            <xsl:apply-templates select="." mode="plain_text"/>
        </xsl:variable>
        <xsl:variable name="content_match_count">
            <xsl:call-template name="count_matches">
                <xsl:with-param name="string" select="$plain_content"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="string-length($content_match_count) != 0">true</xsl:when>
            <xsl:otherwise>false</xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <!--
        Elements
    -->
    <xsl:template match="imdi:*|*">
        <xsl:param name="level"/>
        <xsl:param name="parent_content_matched"/>
        <xsl:variable name="node_count" select="count(imdi:*)"/>
        <xsl:variable name="content">
            <xsl:apply-templates mode="highlight"/>
        </xsl:variable>
        <xsl:variable name="this_content_matched">
            <xsl:apply-templates select="." mode="content_matches"/>
        </xsl:variable>
        <!--       <xsl:variable name="plain_content">
            <xsl:apply-templates select="." mode="plain_text"/>
        </xsl:variable>
        <xsl:variable name="content_match_count">
            <xsl:call-template name="count_matches">
                <xsl:with-param name="string" select="$plain_content"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="this_content_matched">
            <xsl:choose>
                <xsl:when test="string-length($content_match_count) != 0">true</xsl:when>
                <xsl:otherwise>false</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>-->
        <!--<xsl:variable name="new_parent_content_matched">
            <xsl:choose>
                <xsl:when test="$parent_content_matched = 'false'">false</xsl:when>
                <xsl:otherwise>
                    <xsl:choose>
                        <xsl:when test="$this_content_matched = 'false'">false</xsl:when>
                        <xsl:otherwise/>
                    </xsl:choose>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>-->
        <!--
            the group type is static when:
            1. parent_content_matched = 'true'
            2. parent_content_matched = 'false' AND content_match_count > 0
        -->
        <xsl:variable name="current_group_type">
            <!--    <xsl:choose>
                <xsl:when test="$parent_content_matched = 'true'">static</xsl:when>
                <xsl:otherwise>-->
            <xsl:choose>
                <xsl:when test="$this_content_matched = 'true'">static</xsl:when>
                <xsl:otherwise>dynamic</xsl:otherwise>
            </xsl:choose>
            <!--</xsl:otherwise>
            </xsl:choose>-->
        </xsl:variable>
        <!--DEBUG-->
        <!--<xsl:message><xsl:value-of select="name(.)"/><xsl:copy-of select="$plain_content"/></xsl:message>-->
        <!--DEBUG-->
        <!--<xsl:message><xsl:value-of select="name(.)"/> level:<xsl:value-of select="$level"/> nodes:<xsl:value-of select="$node_count"/> parent_content_matched: <xsl:value-of select="$parent_content_matched"/></xsl:message>-->
        <xsl:choose>
            <xsl:when test="$node_count = 0">
                <xsl:call-template name="print_name_value">
                    <xsl:with-param name="level" select="$level"/>
                    <xsl:with-param name="name">
                        <xsl:value-of select="name(.)"/>
                    </xsl:with-param>
                    <xsl:with-param name="value">
                        <!--<xsl:value-of select="."/>-->
                        <!--<xsl:apply-templates/>-->
                        <xsl:copy-of select="$content"/>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$node_count > 0">
                <xsl:call-template name="content_group">
                    <xsl:with-param name="level" select="$level"/>
                    <!-- <xsl:with-param name="header">
                        <xsl:value-of select="name(.)"/>: <xsl:value-of select="imdi:Name|Name"/>
                    </xsl:with-param> -->
                    <xsl:with-param name="group-name">
                        <xsl:value-of select="name(.)"/>
                    </xsl:with-param>
                    <xsl:with-param name="group-type">
                        <xsl:value-of select="$current_group_type"/>
                    </xsl:with-param>
                    <xsl:with-param name="group-info">
                        <xsl:value-of select="imdi:Name"/>
                    </xsl:with-param>
                    <xsl:with-param name="content">
                        <!--DEBUG-->
                        <!--<xsl:text>%CUR:</xsl:text>
                        <xsl:value-of select="$current_group_type"/>
                        <xsl:text>%THIS:</xsl:text>
                        <xsl:value-of select="$this_content_matched"/>                        
                        <xsl:text>%PAR:</xsl:text>
                        <xsl:value-of select="$parent_content_matched"/>
                        <xsl:text>%NPAR:</xsl:text>
                        <xsl:value-of select="$new_parent_content_matched"/>-->
                        <xsl:apply-templates select="imdi:*">
                            <xsl:with-param name="level" select="$level+1"/>
                            <xsl:with-param name="parent_content_matched" select="$parent_content_matched"/>
                        </xsl:apply-templates>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:when>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="imdi:*|*" mode="plain_text">
        <xsl:apply-templates select="imdi:*|text()" mode="plain_text"/>
    </xsl:template>
    <xsl:template match="text()" mode="plain_text">
        <xsl:if test="normalize-space(.) != ''">
            <xsl:value-of select="$TOKEN_SEPARATOR"/>
            <xsl:value-of select="translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')"/>
            <xsl:value-of select="$TOKEN_SEPARATOR"/>
        </xsl:if>
    </xsl:template>
    <!--
        CorpusLink
    -->
    <xsl:template match="imdi:CorpusLink">
        <xsl:param name="level"/>
        <xsl:call-template name="print_link">
            <xsl:with-param name="level" select="$level"/>
            <xsl:with-param name="link-name" select="@Name"/>
            <xsl:with-param name="link-value" select="text()"/>
            <xsl:with-param name="link-info" select="text()"/>
            <xsl:with-param name="link-active" select="$CORPUS_LINKING"/>
        </xsl:call-template>
    </xsl:template>
    <!--
        ResourceLink
    -->
    <xsl:template match="imdi:ResourceLink|imdi:MediaResourceLink">
        <xsl:param name="level"/>
        <xsl:call-template name="print_link">
            <xsl:with-param name="level" select="$level"/>
            <xsl:with-param name="link-name" select="text()"/>
            <xsl:with-param name="link-value" select="text()"/>
            <xsl:with-param name="link-info" select="text()"/>
            <xsl:with-param name="link-active" select="$RESOURCE_LINKING"/>
        </xsl:call-template>
    </xsl:template>
    <!--
        Main groups (static content)
    -->
    <xsl:template match="imdi:Session|Session|imdi:Corpus|Corpus|imdi:Catalogue|Catalogue|imdi:Lexicon|Lexicon">
        <xsl:param name="level"/>
        <xsl:param name="parent_content_matched"/>
        <xsl:call-template name="content_group">
            <xsl:with-param name="level" select="$level"/>
            <xsl:with-param name="group-name">
                <xsl:value-of select="name(.)"/>
            </xsl:with-param>
            <xsl:with-param name="group-type">static</xsl:with-param>
            <xsl:with-param name="content">
                <xsl:apply-templates select="imdi:*">
                    <xsl:with-param name="level" select="$level"/>
                    <xsl:with-param name="parent_content_matched" select="$parent_content_matched"/>
                </xsl:apply-templates>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!--
        Static content groups
        Comment: DocumentLanguages & SubjectLanguages put back in, Evelyn Richter, 13 Aug 2009
    -->
    <xsl:template match="imdi:Languages|imdi:Actors|imdi:References|imdi:DocumentLanguages|imdi:SubjectLanguages">
        <xsl:param name="level"/>
        <xsl:param name="parent_content_matched"/>
        <xsl:call-template name="content_group">
            <xsl:with-param name="level" select="$level"/>
            <xsl:with-param name="group-name">
                <xsl:value-of select="name(.)"/>
            </xsl:with-param>
            <xsl:with-param name="group-type">static</xsl:with-param>
            <xsl:with-param name="content">
                <xsl:apply-templates select="imdi:*">
                    <xsl:with-param name="level" select="$level"/>
                    <xsl:with-param name="parent_content_matched" select="$parent_content_matched"/>
                </xsl:apply-templates>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!--
        TimePosition, CounterPosition  
    -->
    <xsl:template match="imdi:TimePosition|imdi:CounterPosition">
        <xsl:param name="level"/>
        <xsl:param name="parent_content_matched"/>
        <xsl:call-template name="content_group">
            <xsl:with-param name="level" select="$level"/>
            <xsl:with-param name="group-name">
                <xsl:value-of select="name(.)"/>
            </xsl:with-param>
            <xsl:with-param name="group-type">static</xsl:with-param>
            <xsl:with-param name="content">
                <xsl:apply-templates select="imdi:*">
                    <xsl:with-param name="level" select="$level"/>
                    <xsl:with-param name="parent_content_matched" select="$parent_content_matched"/>
                </xsl:apply-templates>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!--
        Description, ContentType, Applications, Author, SmallestAnnotationUnit, DistributionForm, Pricing, ContactPerson, ReferenceLink, MetadataLink, Publications
        Comment: was only Description, rest added by Evelyn, only works for elements that are unique in Catalogue schema
    -->
    <xsl:template match="imdi:Description|imdi:ContentType|imdi:Applications|imdi:Author|imdi:SmallestAnnotationUnit|imdi:DistributionForm|imdi:Pricing|imdi:ContactPerson|imdi:ReferenceLink|imdi:MetadataLink|imdi:Publications">
        <xsl:param name="level"/>
        <xsl:param name="parent_content_matched"/>
        <xsl:variable name="text">
            <xsl:choose>
                <xsl:when test="name(.) = 'ReferenceLink' or name(.) = 'MetadataLink'">
                    <a>
                        <xsl:attribute name="href">
                            <xsl:apply-templates mode="highlight"/>
                        </xsl:attribute>
                        <xsl:apply-templates mode="highlight"/>
                    </a>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates mode="highlight"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="this_content_matched">
            <xsl:apply-templates select="." mode="content_matches"/>
        </xsl:variable>
        <xsl:if test="normalize-space($text) != ''">
            <xsl:call-template name="content_group">
                <xsl:with-param name="level" select="$level"/>
                <xsl:with-param name="group-name">
                    <xsl:value-of select="name(.)"/>
                </xsl:with-param>
                <xsl:with-param name="group-type">static</xsl:with-param>
                <xsl:with-param name="content">
                    <xsl:if test="normalize-space(@Link) != ''">
                        <xsl:call-template name="print_link">
                            <xsl:with-param name="link-name">Read more</xsl:with-param>
                            <xsl:with-param name="link-value" select="@Link"/>
                            <xsl:with-param name="link-info" select="@Link"/>
                            <xsl:with-param name="link-active" select="$INFO_LINKING"/>
                        </xsl:call-template>
                    </xsl:if>
                    <pre>
                        <xsl:attribute name="class">
                            <xsl:choose>
                                <!-- no scrolling window for description with matches -->
                                <xsl:when test="$this_content_matched = 'true'">
                                    <xsl:text>IMDI_small_text_block</xsl:text>                                    
                                </xsl:when>
                                <!-- no scrolling window for small descriptions -->
                                <xsl:when test="string-length($text) &lt; $SMALL_BLOCK_LIMIT">
                                    <xsl:text>IMDI_small_text_block</xsl:text>
                                </xsl:when>
                                <!-- large descriptions, minimal size is SMALL_BLOCK_LIMIT -->
                                <xsl:otherwise>
                                    <xsl:text>IMDI_large_text_block</xsl:text>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:attribute>
                        <xsl:copy-of select="$text"/>
                    </pre>
                </xsl:with-param>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>
    <!--
        Catalogue: Size, Date, Publisher
        Comment: Size, Date & Publisher elements exist elsewhere, therefore require separate handling
    -->
    <xsl:template match="imdi:Size|imdi:Date|imdi:Publisher">
        <xsl:param name="level"/>
        <xsl:param name="parent_content_matched"/>
        <xsl:choose>
            <xsl:when test="name(..)='Catalogue'">
                <xsl:variable name="text">
                    <xsl:apply-templates mode="highlight"/>
                </xsl:variable>
                <xsl:variable name="this_content_matched">
                    <xsl:apply-templates select="." mode="content_matches"/>
                </xsl:variable>
                <xsl:if test="normalize-space($text) != ''">
                    <xsl:call-template name="content_group">
                        <xsl:with-param name="level" select="$level"/>
                        <xsl:with-param name="group-name">
                            <xsl:value-of select="name(.)"/>
                        </xsl:with-param>
                        <xsl:with-param name="group-type">static</xsl:with-param>
                        <xsl:with-param name="content">
                            <xsl:if test="normalize-space(@Link) != ''">
                                <xsl:call-template name="print_link">
                                    <xsl:with-param name="link-name">Read more</xsl:with-param>
                                    <xsl:with-param name="link-value" select="@Link"/>
                                    <xsl:with-param name="link-info" select="@Link"/>
                                    <xsl:with-param name="link-active" select="$INFO_LINKING"/>
                                </xsl:call-template>
                            </xsl:if>
                            <pre>
                                <xsl:attribute name="class">
                                    <xsl:choose>
                                        <!-- no scrolling window for description with matches -->
                                        <xsl:when test="$this_content_matched = 'true'">
                                            <xsl:text>IMDI_small_text_block</xsl:text>                                    
                                            </xsl:when>
                                        <!-- no scrolling window for small descriptions -->
                                        <xsl:when test="string-length($text) &lt; $SMALL_BLOCK_LIMIT">
                                            <xsl:text>IMDI_small_text_block</xsl:text>
                                            </xsl:when>
                                        <!-- large descriptions, minimal size is SMALL_BLOCK_LIMIT -->
                                        <xsl:otherwise>
                                            <xsl:text>IMDI_large_text_block</xsl:text>
                                        </xsl:otherwise>
                                      </xsl:choose>
                                    </xsl:attribute>
                                <xsl:copy-of select="$text"/>
                            </pre>
                        </xsl:with-param>
                    </xsl:call-template>
                </xsl:if>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="content">
                    <xsl:apply-templates select="." mode="highlight"/>
                </xsl:variable>
                <xsl:call-template name="print_name_value">
                    <xsl:with-param name="level" select="$level"/>
                    <xsl:with-param name="name">
                        <xsl:value-of select="name(.)"/>
                    </xsl:with-param>
                    <xsl:with-param name="value">
                        <!-- <xsl:value-of select="text()"/> -->
                        <!--<xsl:apply-templates/>-->
                        <xsl:copy-of select="$content"/>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <!-- 
       Catalogue: Format, Quality
        Format and Quality are dynamic in Catalogue, but labels in Sessions
    -->
    <xsl:template match="imdi:Format|imdi:Quality">
        <xsl:param name="level"/>
        <xsl:param name="parent_content_matched"/>
        <xsl:choose>
            <xsl:when test="name(..)='Catalogue'">
                <!-- content needs to be dynamic -->
                <xsl:call-template name="content_group">
                    <xsl:with-param name="level" select="$level"/>
                    <xsl:with-param name="group-name">
                        <xsl:value-of select="name(.)"/>
                    </xsl:with-param>
                    <xsl:with-param name="group-type">
                        <xsl:choose>
                            <xsl:when test="exists(imdi:Text) or exists(imdi:Audio) or exists(imdi:Video) or exists(imdi:Image)">dynamic</xsl:when>
                            <xsl:otherwise>static</xsl:otherwise>
                        </xsl:choose>
                    </xsl:with-param>
                    <xsl:with-param name="content">
                        <xsl:apply-templates select="imdi:*">
                            <xsl:with-param name="level" select="$level"/>
                            <xsl:with-param name="parent_content_matched" select="$parent_content_matched"/>
                        </xsl:apply-templates>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="content">
                    <xsl:apply-templates select="." mode="highlight"/>
                </xsl:variable>
                <xsl:call-template name="print_name_value">
                    <xsl:with-param name="level" select="$level"/>
                    <xsl:with-param name="name">
                        <xsl:value-of select="name(.)"/>
                    </xsl:with-param>
                    <xsl:with-param name="value">
                        <!-- <xsl:value-of select="text()"/> -->
                        <!--<xsl:apply-templates/>-->
                        <xsl:copy-of select="$content"/>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <!-- 
        Content highlighting, uses string tokenizing from XSLT Cookbook
    -->
    <xsl:template match="text()" mode="highlight">
        <!--<xsl:param name="string" select="''"/>-->
        <xsl:param name="string" select="."/>
        <xsl:param name="delimiters" select="' &#x9;&#xA;'"/>
        <xsl:choose>
            <!-- nothing to do for empty string -->
            <xsl:when test="not($string)"/>
            <xsl:otherwise>
                <xsl:call-template name="_tokenize_delimiters">
                    <xsl:with-param name="string" select="$string"/>
                    <xsl:with-param name="delimiters" select="$delimiters"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <!-- 
        Highlight tokens using delimiters        
    -->
    <xsl:template name="_tokenize_delimiters">
        <xsl:param name="string"/>
        <xsl:param name="delimiters"/>
        <xsl:param name="last-delimit"/>
        <!-- extract a delimiter -->
        <xsl:variable name="delimiter" select="substring($delimiters, 1, 1)"/>
        <xsl:variable name="token_string">
            <xsl:value-of select="$TOKEN_SEPARATOR"/>
            <!--<xsl:value-of select="$string"/>-->
            <xsl:value-of select="translate($string,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')"/>
            <xsl:value-of select="$TOKEN_SEPARATOR"/>
        </xsl:variable>
        <xsl:choose>
            <!-- if delimiter is empty we have a token -->
            <xsl:when test="not($delimiter)">
                <xsl:choose>
                    <xsl:when test="contains($SEARCH_TOKENS,$token_string)">
                        <b class="marker">
                            <xsl:value-of select="$string"/>
                        </b>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$string"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <!-- if the string contains at least one delimiter we must split it -->
            <xsl:when test="contains($string, $delimiter)">
                <xsl:if test="not(starts-with($string,$delimiter))">
                    <!-- handle the part that comes before the current delimiter -->
                    <!-- with the next delimiter. If there is no next the first test -->
                    <!-- in this template will detect the token -->
                    <xsl:call-template name="_tokenize_delimiters">
                        <xsl:with-param name="string" select="substring-before($string, $delimiter)"/>
                        <xsl:with-param name="delimiters" select="substring($delimiters, 2)"/>
                    </xsl:call-template>
                </xsl:if>
                <!-- FRD place delimiter here -->
                <xsl:value-of select="$delimiter"/>
                <!-- handle the part that comes after the delimiter using the -->
                <!-- current delimiter -->
                <xsl:call-template name="_tokenize_delimiters">
                    <xsl:with-param name="string" select="substring-after($string, $delimiter)"/>
                    <xsl:with-param name="delimiters" select="$delimiters"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <!-- No occurances of current delimiter so move on to next -->
                <xsl:call-template name="_tokenize_delimiters">
                    <xsl:with-param name="string" select="$string"/>
                    <xsl:with-param name="delimiters" select="substring($delimiters, 2)"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <!-- 
        Count token matches (based on XSLT Cookbook tokenizer example)
    -->
    <xsl:template name="count_matches">
        <xsl:param name="string" select="''"/>
        <xsl:param name="delimiters" select="' &#x9;&#xA;'"/>
        <xsl:choose>
            <!-- nothing to do when we want to search _in_ an empty string
                    or when we want to search _for_ an empty string -->
            <xsl:when test="not($string) or not($SEARCH_STRING)"/>
            <xsl:otherwise>
                <xsl:call-template name="_count_token_matches">
                    <xsl:with-param name="string" select="$string"/>
                    <xsl:with-param name="delimiters" select="$delimiters"/>
                    <xsl:with-param name="current_count" select="''"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <!-- 
        Recursive routine to count tokens that match SEARCH_STRING
    -->
    <xsl:template name="_count_token_matches">
        <xsl:param name="string"/>
        <xsl:param name="delimiters"/>
        <xsl:param name="current_count"/>
        <!-- extract a delimiter -->
        <xsl:variable name="delimiter" select="substring($delimiters, 1, 1)"/>
        <xsl:variable name="token_string">
            <xsl:value-of select="$TOKEN_SEPARATOR"/>
            <xsl:value-of select="$string"/>
            <xsl:value-of select="$TOKEN_SEPARATOR"/>
        </xsl:variable>
        <xsl:choose>
            <!-- if delimiter is empty we have a token -->
            <xsl:when test="not($delimiter)">
                <xsl:if test="contains($SEARCH_TOKENS,$token_string)">
                    <xsl:text>*</xsl:text>
                </xsl:if>
            </xsl:when>
            <!-- if the string contains at least one delimiter we must split it -->
            <xsl:when test="contains($string, $delimiter)">
                <xsl:choose>
                    <xsl:when test="not(starts-with($string,$delimiter))">
                        <xsl:value-of select="$current_count"/>
                        <xsl:call-template name="_count_token_matches">
                            <xsl:with-param name="string" select="substring-before($string, $delimiter)"/>
                            <xsl:with-param name="delimiters" select="substring($delimiters, 2)"/>
                            <xsl:with-param name="current_count" select="$current_count"/>
                        </xsl:call-template>
                        <xsl:call-template name="_count_token_matches">
                            <xsl:with-param name="string" select="substring-after($string, $delimiter)"/>
                            <xsl:with-param name="delimiters" select="$delimiters"/>
                            <xsl:with-param name="current_count" select="$current_count"/>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:call-template name="_count_token_matches">
                            <xsl:with-param name="string" select="substring-after($string, $delimiter)"/>
                            <xsl:with-param name="delimiters" select="$delimiters"/>
                            <xsl:with-param name="current_count" select="$current_count"/>
                        </xsl:call-template>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <!-- No occurances of current delimiter so move on to next -->
                <xsl:call-template name="_count_token_matches">
                    <xsl:with-param name="string" select="$string"/>
                    <xsl:with-param name="delimiters" select="substring($delimiters, 2)"/>
                    <xsl:with-param name="current_count" select="$current_count"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <!--
        Keys    
    -->
    <xsl:template match="imdi:Keys">
        <xsl:param name="level"/>
        <xsl:if test="count(imdi:Key)&gt;0">
            <xsl:call-template name="content_group">
                <xsl:with-param name="level" select="$level"/>
                <xsl:with-param name="group-name">
                    <xsl:value-of select="name(.)"/>
                </xsl:with-param>
                <xsl:with-param name="group-type">static</xsl:with-param>
                <xsl:with-param name="content">
                    <xsl:apply-templates select="imdi:Key"/>
                </xsl:with-param>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>
    <!-- 
        Key
    -->
    <xsl:template match="imdi:Key">
        <xsl:param name="level"/>
        <xsl:variable name="content">
            <xsl:apply-templates select="." mode="highlight"/>
        </xsl:variable>
        <xsl:call-template name="print_keys_name_value">
            <xsl:with-param name="level" select="$level"/>
            <xsl:with-param name="name">
                <xsl:value-of select="@Name"/>
            </xsl:with-param>
            <xsl:with-param name="value">
                <!-- <xsl:value-of select="text()"/> -->
                <!--<xsl:apply-templates/>-->
                <xsl:copy-of select="$content"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    
    <!--
        Create a grouping of content
        css classname gets the same name as the imdi element name
    -->
    <xsl:template name="content_group">
        <xsl:param name="level"/>
        <xsl:param name="group-name"/>
        <xsl:param name="group-type"/>
        <xsl:param name="group-info"/>
        <xsl:param name="content"/>
        <xsl:param name="parent_content_matched"/>
        <!--<xsl:variable name="content_match_count">
            <xsl:call-template name="count_matches">
                <xsl:with-param name="string" select="$content"/>
            </xsl:call-template>
        </xsl:variable>-->
         <xsl:variable name="tid" select="generate-id(.)"/>
        <!--DEBUG-->
        <!--<xsl:message> Count:<xsl:copy-of select="$content_match_count"/> ON:<xsl:copy-of select="$content"/>
            </xsl:message>-->
        <div>
            <xsl:attribute name="class">
                <xsl:text>IMDI_group</xsl:text>
            </xsl:attribute>
            <xsl:attribute name="style">
                <xsl:text>margin-left: </xsl:text>
                <xsl:value-of select="$level * 20"/>
                <xsl:text>px</xsl:text>
            </xsl:attribute>
            <xsl:choose>
                <xsl:when test="$group-type = 'static'">
                    <xsl:call-template name="group_header_static">
                        <xsl:with-param name="level" select="$level"/>
                        <xsl:with-param name="group-name" select="$group-name"/>
                    </xsl:call-template>
                </xsl:when>
                <xsl:when test="$group-type = 'dynamic'">
                    <xsl:call-template name="group_header_dynamic">
                        <xsl:with-param name="level" select="$level"/>
                        <xsl:with-param name="group-name" select="$group-name"/>
                        <xsl:with-param name="group-type" select="$group-type"/>
                        <xsl:with-param name="group-info" select="$group-info"/>
                        <xsl:with-param name="id" select="$tid"/>
                    </xsl:call-template>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:message>ERROR: wrong group type for </xsl:message>
                    <xsl:value-of select="$group-name"/>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:call-template name="group_body">
                <xsl:with-param name="level" select="$level"/>
                <xsl:with-param name="group-name" select="$group-name"/>
                <xsl:with-param name="group-type" select="$group-type"/>
                <xsl:with-param name="id" select="$tid"/>
                <xsl:with-param name="content" select="$content"/>
            </xsl:call-template>
        </div>
    </xsl:template>
    <!--
        Make table with specified content and id
    -->
    <xsl:template name="group_body">
        <xsl:param name="level"/>
        <xsl:param name="group-type"/>
        <xsl:param name="id"/>
        <xsl:param name="content"/>
        <div>
            <xsl:attribute name="class">
                <xsl:text>IMDI_group_</xsl:text>
                <xsl:value-of select="$group-type"/>
            </xsl:attribute>
            <xsl:attribute name="id">
                <xsl:value-of select="$id"/>
            </xsl:attribute>
            <xsl:copy-of select="$content"/>
        </div>
    </xsl:template>
    <!--
       Dynamic content header for metadata group
    -->
    <xsl:template name="group_header_dynamic">
        <xsl:param name="level"/>
        <xsl:param name="group-name"/>
        <xsl:param name="group-info"/>
        <xsl:param name="id"/>
        <xsl:variable name="xpath-id">
                <xsl:value-of select="saxon:path()"/>
        </xsl:variable>
        <div>
            <xsl:attribute name="class">IMDI_group_header_dynamic</xsl:attribute>
             <xsl:if test="$IMAGE_CLOSED">
                <img class="IMDI_click_image">
                    <xsl:attribute name="src">
                        <xsl:value-of select="$IMAGE_CLOSED"/>
                    </xsl:attribute>
                    <xsl:attribute name="onclick">javascript:change_status("<xsl:value-of select="$id"/>")</xsl:attribute>
                    <xsl:attribute name="id">
                        <xsl:text>img_</xsl:text>
                        <xsl:value-of select="$id"/>
                    </xsl:attribute>
                </img>
            </xsl:if>
            <span class="IMDI_group_header_link">
                <xsl:attribute name="id">
                        <xsl:value-of select="$xpath-id"/>
                    </xsl:attribute>        
                <xsl:element name="a">
                        <xsl:attribute name="href">
                            <xsl:text>javascript:</xsl:text>
                        <xsl:value-of select="$JAVASCRIPT_CHANGE_STATUS"/>
                        <xsl:text>('</xsl:text>
                        <xsl:value-of select="$id"/>
                        <xsl:text>')</xsl:text>
                    </xsl:attribute>
                        <xsl:value-of select="$group-name"/>
                    </xsl:element>
            </span>
            <span class="IMDI_header_info">
                <xsl:value-of select="$group-info"/>
            </span>
        </div>
    </xsl:template>
    <!--
       Static content header for metadata group
    -->
    <xsl:template name="group_header_static">
        <xsl:param name="level"/>
        <xsl:param name="group-name"/>
        <div>
            <xsl:attribute name="class">
                    <xsl:text>IMDI_group_header_static</xsl:text>
            </xsl:attribute>
            <xsl:attribute name="id"><xsl:value-of select="saxon:path()"/></xsl:attribute>
            <xsl:value-of select="$group-name"/>
        </div>
    </xsl:template>
    <!--
        Output name,value content
    -->
    <xsl:template name="print_name_value">
        <xsl:param name="level"/>
        <xsl:param name="name"/>
        <xsl:param name="value"/>
        <xsl:variable name="xpath-id">
                <xsl:value-of select="saxon:path()"/>
        </xsl:variable>
        <div class="IMDI_name_value">
            <span>
                <xsl:attribute name ="class">
                    <xsl:text>IMDI_label</xsl:text>
                </xsl:attribute>
     <xsl:attribute name="id">
                    <xsl:value-of select="$xpath-id"/>
                 </xsl:attribute>
                <xsl:copy-of select="$name"/>
            </span>
            <span class="IMDI_value">
               <xsl:copy-of select="$value"/>
            </span>
        </div>
        <br/>
    </xsl:template>
    <!--
        Output name,value content for keys, need to have separate class, because they need slightly different layout then the other key value pairs
    -->    
     <xsl:template name="print_keys_name_value">
        <xsl:param name="level"/>
        <xsl:param name="name"/>
        <xsl:param name="value"/>
        <xsl:variable name="xpath-id">
                <xsl:value-of select="saxon:path()"/>
        </xsl:variable>
        <div class="IMDI_key_name_value">
            <span>
                <xsl:attribute name ="class">
                    <xsl:text>IMDI_key_label</xsl:text>
                </xsl:attribute>
     <xsl:attribute name="id">
                    <xsl:value-of select="$xpath-id"/>
                 </xsl:attribute>
                <xsl:copy-of select="$name"/>
            </span>
            <span class="IMDI_key_value">
               <xsl:copy-of select="$value"/>
            </span>
        </div>
        <br/>
    </xsl:template>
    <!--
        Output name,value content
    -->
    <xsl:template name="print_link">
        <xsl:param name="level"/>
        <xsl:param name="link-name"/>
        <xsl:param name="link-value"/>
        <xsl:param name="link-info"/>
        <xsl:param name="link-active"/>
        <div class="IMDI_link">
            <xsl:choose>
                <xsl:when test="$link-active = 'true'">
                    <a>
                        <xsl:attribute name="href">
                            <xsl:value-of select="$link-value"/>
                        </xsl:attribute>
                        <xsl:attribute name="title">
                            <xsl:value-of select="$link-info"/>
                        </xsl:attribute>
                        <xsl:value-of select="$link-name"/>
                    </a>
                </xsl:when>
                <xsl:otherwise>
                    <!--<xsl:value-of select="$link-name"/>-->
                </xsl:otherwise>
            </xsl:choose>
        </div>
     </xsl:template>
</xsl:stylesheet>
