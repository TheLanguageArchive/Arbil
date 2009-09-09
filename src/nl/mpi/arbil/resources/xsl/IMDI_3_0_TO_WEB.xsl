<?xml version="1.0" encoding="UTF-8"?>
<!--
    Transform IMDI 3.0 to XHTML 1.0
    
    Version 0.5
    
    Freddy Offenga, 2006-03-16
-->
<xsl:stylesheet version="1.0" xmlns:imdi="http://www.mpi.nl/IMDI/Schema/IMDI" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="imdi">
    <xsl:output indent="yes" method="html" encoding="UTF-8" doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"/>
    <!--
        Parameter - Display external descriptions as html refs
    -->
    <xsl:param name="info-linking">true</xsl:param>
    <!--
        Parameter - Display corpus links as html refs
    -->
    <xsl:param name="corpus-linking">false</xsl:param>
    <!--
        Parameter - File with javascript functions
    -->
    <xsl:param name="javascript">google.js</xsl:param>
    <!-- 
        Parameter -  File used for style information
    -->
    <xsl:param name="css">imdi.css</xsl:param>
    <!-- 
        Parameter -  File used as tree-open icon
    -->
    <xsl:param name="icon-open"/>
    <!-- 
        Parameter -  File used as tree-closed icon
    -->
    <xsl:param name="icon-closed"/>
    <!--
        Top elements
    -->
    <xsl:template match="/*|/imdi:*">
        <html>
            <head>
                <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
                <title>
                    <xsl:value-of select="name(imdi:*|*)"/> : <xsl:value-of select="imdi:*/imdi:Name/text()|*/Name/text()"/> - <xsl:value-of select="imdi:Session/imdi:Title/text()"/> [IMDI 3.0] </title>
                <script language="javascript">
                    <xsl:attribute name="src">
                        <xsl:value-of select="$javascript"/>
                    </xsl:attribute>
                </script>
                <xsl:element name="link">
                    <xsl:attribute name="type">text/css</xsl:attribute>
                    <xsl:attribute name="rel">stylesheet</xsl:attribute>
                    <xsl:attribute name="href">
                        <xsl:value-of select="$css"/>
                    </xsl:attribute>
                </xsl:element>
            </head>
            <body onload="init_google();">
                <div class="IMDI_header">
                    <span class="IMDI_logo"/>ISLE Metadata Initiative</div>
                <xsl:apply-templates select="imdi:Session|Session|imdi:Corpus|Corpus|imdi:Catalogue|Catalogue|imdi:Lexicon|Lexicon"/>
                <!--                <p>
                    <a href="http://validator.w3.org/check/referer">
                        <img src="http://www.w3.org/Icons/valid-xhtml10" alt="Valid XHTML 1.0!"
                            border="0" height="31" width="88"/>
                    </a>
                </p> -->
            </body>
        </html>
    </xsl:template>
    <!--
        Session
    -->
    <xsl:template match="imdi:Session|Session">
        <xsl:call-template name="make_table_content">
            <xsl:with-param name="title">
                <xsl:value-of select="name(.)"/>: <xsl:value-of select="imdi:Name|Name"/>
            </xsl:with-param>
            <xsl:with-param name="style">
                <xsl:value-of select="name(.)"/>
            </xsl:with-param>
            <xsl:with-param name="content">
                <xsl:call-template name="make_row_table">
                    <xsl:with-param name="content">
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Name|Name"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Title|Title"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Date|Date"/>
                        </xsl:call-template>
                    </xsl:with-param>
                </xsl:call-template>
                <xsl:apply-templates select="imdi:Description|Description"/>
                <xsl:apply-templates select="imdi:MDGroup/imdi:Location|MDGroup/Location"/>
                <xsl:apply-templates select="imdi:MDGroup/imdi:Project|MDGroup/Project"/>
                <xsl:apply-templates select="imdi:MDGroup/imdi:Content|MDGroup/Content"/>
                <xsl:apply-templates select="imdi:MDGroup/imdi:Actors|MDGroup/Actors"/>
                <xsl:apply-templates select="imdi:Resources|Resources"/>
                <xsl:apply-templates select="imdi:References|References"/>
                <xsl:apply-templates select="imdi:MDGroup/imdi:Keys|MDGroup/Keys"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!--
        Corpus
    -->
    <xsl:template match="imdi:Corpus|Corpus">
        <xsl:call-template name="make_table_content">
            <xsl:with-param name="title">
                <xsl:value-of select="name(.)"/>: <xsl:value-of select="imdi:Name|Name"/>
            </xsl:with-param>
            <xsl:with-param name="style">
                <xsl:value-of select="name(.)"/>
            </xsl:with-param>
            <xsl:with-param name="content">
                <xsl:call-template name="make_row_table">
                    <xsl:with-param name="content">
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Name|Name"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Title|Title"/>
                        </xsl:call-template>
                    </xsl:with-param>
                </xsl:call-template>
                <xsl:apply-templates select="imdi:Description|Description" mode="make_row"/>
                <xsl:if test="count(imdi:MDGroup)&gt;0 or count(MDGroup)&gt;0">
                    <xsl:apply-templates select="imdi:MDGroup/imdi:Location|MDGroup/Location" mode="make_row"/>
                    <xsl:apply-templates select="imdi:MDGroup/imdi:Project|MDGroup/Project" mode="make_row"/>
                    <xsl:apply-templates select="imdi:MDGroup/imdi:Content|MDGroup/Content" mode="make_row"/>
                    <xsl:apply-templates select="imdi:MDGroup/imdi:Actors|MDGroup/Actors" mode="make_row"/>
                    <xsl:apply-templates select="imdi:MDGroup/imdi:Keys|MDGroup/Keys" mode="make_row"/>
                </xsl:if>
                <xsl:if test="$corpus-linking = 'true'">
                    <xsl:call-template name="make_table_content">
                        <xsl:with-param name="title">Links</xsl:with-param>
                        <xsl:with-param name="style">Links</xsl:with-param>
                        <xsl:with-param name="content">
                            <xsl:apply-templates select="imdi:CorpusLink|CorpusLink" mode="make_row"/>
                        </xsl:with-param>
                    </xsl:call-template>
                </xsl:if>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!--
        CorpusLink  
     -->
    <xsl:template match="imdi:CorpusLink|CorpusLink" mode="make_row">
        <xsl:call-template name="named_link_row">
            <xsl:with-param name="name" select="@Name"/>
            <xsl:with-param name="link" select="text()"/>
        </xsl:call-template>
    </xsl:template>
    <!--
        Catalogue
    -->
    <xsl:template match="imdi:Catalogue|Catalogue">
        <xsl:call-template name="make_table_content">
            <xsl:with-param name="title">
                <xsl:value-of select="name(.)"/>: <xsl:value-of select="imdi:Name|Name"/>
            </xsl:with-param>
            <xsl:with-param name="style">
                <xsl:value-of select="name(.)"/>
            </xsl:with-param>
            <xsl:with-param name="content">
                <xsl:call-template name="make_row_table">
                    <xsl:with-param name="content">
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Name|Name"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Title|Title"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Id|Id"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Date|Date"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:ContentType|ContentType"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:SmallestAnnotationUnit|SmallestAnnotationUnit"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Publisher|Publisher"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Size|Size"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Publisher|Publisher"/>
                            <!-- ??? is this correct ??? FRD -->
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Pricing|Pricing"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:DistributionForm|DistributionForm"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Applications|Applications"/>
                        </xsl:call-template>
                    </xsl:with-param>
                </xsl:call-template>
                <xsl:apply-templates select="imdi:Description|Description" mode="make_row"/>
                <xsl:apply-templates select="imdi:Location|Location" mode="make_row"/>
                <xsl:apply-templates select="imdi:Format|Format" mode="make_row"/>
                <xsl:apply-templates select="imdi:Quality|Quality" mode="make_row"/>
                <xsl:apply-templates select="imdi:Project|Project" mode="make_row"/>
                <xsl:apply-templates select="imdi:DocumentLanguages|DocumentLanguages" mode="make_row"/>
                <xsl:apply-templates select="imdi:SubjectLanguages|SubjectLanguages" mode="make_row"/>
                <xsl:apply-templates select="imdi:Access|Access" mode="make_row"/>
                <xsl:apply-templates select="imdi:Keys|Keys" mode="make_row"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!--
        Catalogue Format
    -->
    <xsl:template match="imdi:Catalogue/imdi:Format|Catalogue/Format">
        <xsl:call-template name="make_table_content">
            <xsl:with-param name="title">
                <xsl:value-of select="name(.)"/>: <xsl:value-of select="imdi:Name|Name"/>
            </xsl:with-param>
            <xsl:with-param name="style">
                <xsl:value-of select="name(.)"/>
            </xsl:with-param>
            <xsl:with-param name="content">
                <xsl:call-template name="make_row_table">
                    <xsl:with-param name="content">
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Audio|Audio"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Video|Video"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Text|Text"/>
                        </xsl:call-template>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!--
        Catalogue Quality    
    -->
    <xsl:template match="imdi:Catalogue/imdi:Quality|Catalogue/Quality">
        <xsl:call-template name="make_table_content">
            <xsl:with-param name="title">
                <xsl:value-of select="name(.)"/>: <xsl:value-of select="imdi:Name|Name"/>
            </xsl:with-param>
            <xsl:with-param name="style">
                <xsl:value-of select="name(.)"/>
            </xsl:with-param>
            <xsl:with-param name="content">
                <xsl:call-template name="make_row_table">
                    <xsl:with-param name="content">
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Audio|Audio"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Video|Video"/>
                        </xsl:call-template>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!--
        Location
    -->
    <xsl:template match="imdi:Location|Location">
        <xsl:call-template name="make_table_content_click">
            <xsl:with-param name="title">
                <xsl:value-of select="name(.)"/>: <xsl:value-of select="imdi:Continent|Continent"/>,<xsl:value-of select="imdi:Country|Country"/>
            </xsl:with-param>
            <xsl:with-param name="style">
                <xsl:value-of select="name(.)"/>
            </xsl:with-param>
            <xsl:with-param name="content">
                <xsl:call-template name="table_row">
                    <xsl:with-param name="value" select="imdi:Continent|Continent"/>
                </xsl:call-template>
                <xsl:call-template name="table_row">
                    <xsl:with-param name="value" select="imdi:Country|Country"/>
                </xsl:call-template>
                <tr>
                    <td align="right" width="150">
                        <div class="IMDI_element_name">Region:</div>
                    </td>
                    <td>
                        <div class="IMDI_element_value">
                            <xsl:for-each select="imdi:Region|Region">
                                <xsl:value-of select="text()"/>
                                <xsl:if test="position()&lt;last()">, </xsl:if>
                            </xsl:for-each>
                        </div>
                    </td>
                </tr>
                <xsl:call-template name="table_row">
                    <xsl:with-param name="value" select="imdi:Address|Address"/>
                </xsl:call-template>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!--
        Project
    -->
    <xsl:template match="imdi:Project|Project">
        <xsl:call-template name="make_table_content_click">
            <xsl:with-param name="title">
                <xsl:value-of select="name(.)"/>: <xsl:value-of select="imdi:Name|Name"/>
            </xsl:with-param>
            <xsl:with-param name="style">
                <xsl:value-of select="name(.)"/>
            </xsl:with-param>
            <xsl:with-param name="content">
                <xsl:call-template name="make_row_table">
                    <xsl:with-param name="content">
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Name|Name"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Id|Id"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Title|Title"/>
                        </xsl:call-template>
                    </xsl:with-param>
                </xsl:call-template>
                <xsl:apply-templates select="imdi:Description|Description" mode="make_row"/>
                <xsl:apply-templates select="imdi:Contact|Contact" mode="make_row"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!--
        Content
    -->
    <xsl:template match="imdi:Content|Content">
        <xsl:call-template name="make_table_content_click">
            <xsl:with-param name="title">
                <xsl:value-of select="name(.)"/>
            </xsl:with-param>
            <xsl:with-param name="style">
                <xsl:value-of select="name(.)"/>
            </xsl:with-param>
            <xsl:with-param name="content">
                <xsl:call-template name="make_row_table">
                    <xsl:with-param name="content">
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Genre|Genre"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:SubGenre|SubGenre"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Task|Task"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Modalities|Modalities"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Subject|Subject"/>
                        </xsl:call-template>
                    </xsl:with-param>
                </xsl:call-template>
                <xsl:apply-templates select="imdi:Description|Description" mode="make_row"/>
                <xsl:apply-templates select="imdi:CommunicationContext|CommunicationContext" mode="make_row"/>
                <xsl:apply-templates select="imdi:Languages|Languages" mode="make_row"/>
                <xsl:apply-templates select="imdi:Keys|Keys" mode="make_row"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!--
        Languages, DocumentLanguages, SubjectLanguages
    -->
    <xsl:template match="imdi:Languages|Languages|imdi:DocumentLanguages|DocumentLanguages|imdi:SubjectLanguages|SubjectLanguages">
        <xsl:call-template name="make_table_content">
            <xsl:with-param name="title">
                <xsl:value-of select="name(.)"/>
            </xsl:with-param>
            <xsl:with-param name="style">
                <xsl:value-of select="name(.)"/>
            </xsl:with-param>
            <xsl:with-param name="content">
                <xsl:apply-templates select="imdi:Description|Description" mode="make_row"/>
                <xsl:apply-templates select="imdi:Language|Language" mode="make_row"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!--
        Language    
    -->
    <xsl:template match="imdi:Language|Language">
        <xsl:call-template name="make_table_content_click">
            <xsl:with-param name="title">
                <xsl:value-of select="name(.)"/>: <xsl:value-of select="imdi:Name|Name"/>
            </xsl:with-param>
            <xsl:with-param name="style">
                <xsl:value-of select="name(.)"/>
            </xsl:with-param>
            <xsl:with-param name="content">
                <xsl:call-template name="make_row_table">
                    <xsl:with-param name="content">
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Id|Id"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Name|Name"/>
                        </xsl:call-template>
                        <xsl:if test="count(imdi:Dominant) &gt; 0 or count(imdi:SourceLanguage) &gt; 0 or count(imdi:TargetLanguage) &gt; 0 or count(Dominant) &gt; 0 or count(SourceLanguage) &gt; 0 or count(TargetLanguage) &gt; 0">
                            <xsl:call-template name="table_row">
                                <xsl:with-param name="value" select="imdi:SourceLanguage|TargetLanguage"/>
                            </xsl:call-template>
                            <xsl:call-template name="table_row">
                                <xsl:with-param name="value" select="imdi:TargetLanguage|TargetLanguage"/>
                            </xsl:call-template>
                            <xsl:call-template name="table_row">
                                <xsl:with-param name="value" select="imdi:Dominant|Dominant"/>
                            </xsl:call-template>
                            <xsl:if test="count(@ResourceRef) &gt; 0">
                                <xsl:call-template name="table_tag_value_row">
                                    <xsl:with-param name="tag">Resource References</xsl:with-param>
                                    <xsl:with-param name="value" select="@ResourceRef"/>
                                </xsl:call-template>
                            </xsl:if>
                        </xsl:if>
                        <xsl:if test="count(imdi:MotherTongue) &gt; 0 or count(imdi:PrimaryLanguage) &gt; 0 or count(MotherTongue) &gt; 0 or count(PrimaryLanguage) &gt; 0">
                            <xsl:call-template name="table_tag_value_row">
                                <xsl:with-param name="tag">Mother Tongue</xsl:with-param>
                                <xsl:with-param name="value" select="imdi:MotherTongue|MotherTongue"/>
                            </xsl:call-template>
                            <xsl:call-template name="table_tag_value_row">
                                <xsl:with-param name="tag">Primary Language</xsl:with-param>
                                <xsl:with-param name="value" select="imdi:PrimaryLanguage|PrimaryLanguage"/>
                            </xsl:call-template>
                        </xsl:if>
                    </xsl:with-param>
                </xsl:call-template>
                <xsl:apply-templates select="imdi:Description|Description" mode="make_row"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!--
        Actors    
    -->
    <xsl:template match="imdi:Actors|Actors">
        <xsl:call-template name="make_table_content_click">
            <xsl:with-param name="title">
                <xsl:value-of select="name(.)"/>
            </xsl:with-param>
            <xsl:with-param name="style">
                <xsl:value-of select="name(.)"/>
            </xsl:with-param>
            <xsl:with-param name="content">
                <xsl:apply-templates select="imdi:Actor|Actor" mode="make_row"/>
                <xsl:apply-templates select="imdi:Description|Description" mode="make_row"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!--
        Actor
    -->
    <xsl:template match="imdi:Actor|Actor">
        <xsl:call-template name="make_table_content_click">
            <xsl:with-param name="title">
                <xsl:value-of select="name(.)"/>: <xsl:value-of select="imdi:Name|Name"/>
            </xsl:with-param>
            <xsl:with-param name="style">
                <xsl:value-of select="name(.)"/>
            </xsl:with-param>
            <xsl:with-param name="content">
                <xsl:call-template name="make_row_table">
                    <xsl:with-param name="content">
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Role|Role"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Code|Code"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Name|Name"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:FullName|FullName"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_tag_value_row">
                            <xsl:with-param name="tag">Social/Family Role</xsl:with-param>
                            <xsl:with-param name="value" select="imdi:FamilySocialRole|FamilySocialRole"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_tag_value_row">
                            <xsl:with-param name="tag">Ethnic group</xsl:with-param>
                            <xsl:with-param name="value" select="imdi:EthicGroup|EthicGroup"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Age|Age"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Anonymized|Anonymized"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Sex|Sex"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Education|Education"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:BirthDate|BirthDate"/>
                        </xsl:call-template>
                    </xsl:with-param>
                </xsl:call-template>
                <xsl:apply-templates select="imdi:Description|Description" mode="make_row"/>
                <xsl:apply-templates select="imdi:Keys|Keys" mode="make_row"/>
                <xsl:apply-templates select="imdi:Languages|Languages" mode="make_row"/>
                <xsl:apply-templates select="imdi:Contact|Contact" mode="make_row"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!--    
        Resources
    -->
    <xsl:template match="imdi:Resources|Resources">
        <xsl:call-template name="make_table_content_click">
            <xsl:with-param name="title">
                <xsl:value-of select="name(.)"/>
            </xsl:with-param>
            <xsl:with-param name="style">
                <xsl:value-of select="name(.)"/>
            </xsl:with-param>
            <xsl:with-param name="content">
                <xsl:if test="count(imdi:MediaFile) &gt; 0 or count(MediaFile) &gt; 0 ">
                    <xsl:apply-templates select="imdi:MediaFile|MediaFile" mode="make_row"/>
                </xsl:if>
                <xsl:if test="count(imdi:WrittenResource) &gt; 0 or count(WrittenResource) &gt; 0 ">
                    <xsl:apply-templates select="imdi:WrittenResource|WrittenResource" mode="make_row"/>
                </xsl:if>
                <xsl:if test="count(imdi:Source) &gt; 0 or count(Source) &gt; 0 ">
                    <xsl:apply-templates select="imdi:Source|Source" mode="make_row"/>
                </xsl:if>
                <xsl:if test="count(imdi:LexiconResource) &gt; 0 or count(LexiconResource) &gt; 0 ">
                    <xsl:apply-templates select="imdi:LexiconResource|LexiconResource" mode="make_row"/>
                </xsl:if>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!--
        MediaFile
     -->
    <xsl:template match="imdi:MediaFile|MediaFile">
        <xsl:call-template name="make_table_content_click">
            <xsl:with-param name="title">
                <xsl:value-of select="name(.)"/>: <xsl:value-of select="imdi:Type|Type"/>
            </xsl:with-param>
            <xsl:with-param name="style">
                <xsl:value-of select="name(.)"/>
            </xsl:with-param>
            <xsl:with-param name="content">
                <xsl:call-template name="make_row_table">
                    <xsl:with-param name="content">
                        <xsl:call-template name="table_tag_value_row">
                            <xsl:with-param name="tag">Resource Id</xsl:with-param>
                            <xsl:with-param name="value" select="@ResourceId"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_tag_value_row">
                            <xsl:with-param name="tag">Resource Link</xsl:with-param>
                            <xsl:with-param name="value" select="imdi:ResourceLink|ResourceLink"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Type|Type"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Format|Format"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Size|Size"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Quality|Quality"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_tag_value_row">
                            <xsl:with-param name="tag">Recording Conditions</xsl:with-param>
                            <xsl:with-param name="value" select="imdi:RecordingConditions|RecordingConditions"/>
                        </xsl:call-template>
                    </xsl:with-param>
                </xsl:call-template>
                <xsl:apply-templates select="imdi:TimePosition|TimePosition" mode="make_row"/>
                <xsl:apply-templates select="imdi:Description|Description" mode="make_row"/>
                <xsl:apply-templates select="imdi:Keys|Keys" mode="make_row"/>
                <xsl:apply-templates select="imdi:Access|Access" mode="make_row"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!--
        WrittenResource
    -->
    <xsl:template match="imdi:WrittenResource|WrittenResource">
        <xsl:call-template name="make_table_content_click">
            <xsl:with-param name="title">
                <xsl:value-of select="name(.)"/>: <xsl:value-of select="imdi:Type|Type"/>
            </xsl:with-param>
            <xsl:with-param name="style">
                <xsl:value-of select="name(.)"/>
            </xsl:with-param>
            <xsl:with-param name="content">
                <xsl:call-template name="make_row_table">
                    <xsl:with-param name="content">
                        <xsl:call-template name="table_tag_value_row">
                            <xsl:with-param name="tag">Resource Id</xsl:with-param>
                            <xsl:with-param name="value" select="@ResourceId"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_tag_value_row">
                            <xsl:with-param name="tag">Resource Link</xsl:with-param>
                            <xsl:with-param name="value" select="imdi:ResourceLink|ResourceLink"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_tag_value_row">
                            <xsl:with-param name="tag">Media Resource Link</xsl:with-param>
                            <xsl:with-param name="value" select="imdi:MediaResourceLink|MediaResourceLink"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Type|Type"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_tag_value_row">
                            <xsl:with-param name="tag">Sub Type</xsl:with-param>
                            <xsl:with-param name="value" select="imdi:SubType|SubType"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Format|Format"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Date|Date"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Size|Size"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Derivation|Derivation"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_tag_value_row">
                            <xsl:with-param name="tag">Content Encoding</xsl:with-param>
                            <xsl:with-param name="value" select="imdi:ContentEncoding|ContentEncoding"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_tag_value_row">
                            <xsl:with-param name="tag">Character Encoding</xsl:with-param>
                            <xsl:with-param name="value" select="imdi:CharacterEncoding|CharacterEncoding"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_tag_value_row">
                            <xsl:with-param name="tag">Language ID</xsl:with-param>
                            <xsl:with-param name="value" select="imdi:LanguageId|LanguageId"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="tag">Anonymized</xsl:with-param>
                            <xsl:with-param name="value" select="imdi:Anonymized|Anonymized"/>
                        </xsl:call-template>
                    </xsl:with-param>
                </xsl:call-template>
                <xsl:apply-templates select="imdi:Validation|Validation" mode="make_row"/>
                <xsl:apply-templates select="imdi:Access|Access" mode="make_row"/>
                <xsl:apply-templates select="imdi:Description|Description" mode="make_row"/>
                <xsl:apply-templates select="imdi:Keys|Keys" mode="make_row"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!--
        LexiconResource
    -->
    <xsl:template match="imdi:LexiconResource|LexiconResource">
        <xsl:call-template name="make_table_content">
            <xsl:with-param name="title">
                <xsl:value-of select="name(.)"/>
            </xsl:with-param>
            <xsl:with-param name="style">
                <xsl:value-of select="name(.)"/>
            </xsl:with-param>
            <xsl:with-param name="content">
                <xsl:call-template name="make_row_table">
                    <xsl:with-param name="content">
                        <xsl:call-template name="table_tag_value_row">
                            <xsl:with-param name="tag">Resource Link</xsl:with-param>
                            <xsl:with-param name="value" select="imdi:ResourceLink|ResourceLink"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_tag_value_row">
                            <xsl:with-param name="tag">Media Resource Link</xsl:with-param>
                            <xsl:with-param name="value" select="imdi:MediaResourceLink|MediaResourceLink"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Type|Type"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_tag_value_row">
                            <xsl:with-param name="tag">Schema Reference</xsl:with-param>
                            <xsl:with-param name="value" select="imdi:SchemaRef|SchemaRef"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Format|Format"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Date|Date"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Size|Size"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_tag_value_row">
                            <xsl:with-param name="tag">Character Encoding</xsl:with-param>
                            <xsl:with-param name="value" select="imdi:CharacterEncoding|CharacterEncoding"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_tag_value_row">
                            <xsl:with-param name="tag"># Head Entries</xsl:with-param>
                            <xsl:with-param name="value" select="imdi:NoHeadEntries|NoHeadEntries"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_tag_value_row">
                            <xsl:with-param name="tag"># Sub Entries</xsl:with-param>
                            <xsl:with-param name="value" select="imdi:NoSubEntries|NoSubEntries"/>
                        </xsl:call-template>
                        <xsl:call-template name="make_row_table">
                            <xsl:with-param name="content">
                                <tr>
                                    <td align="right" width="150">
                                        <div class="IMDI_element_name">Meta Languages:</div>
                                    </td>
                                    <td>
                                        <div class="IMDI_element_value">
                                            <xsl:for-each select="imdi:MetaLanguages/imdi:Language|MetaLanguages/Language">
                                                <xsl:value-of select="text()"/>
                                                <xsl:if test="position()&lt;last()">, </xsl:if>
                                            </xsl:for-each>
                                        </div>
                                    </td>
                                </tr>
                            </xsl:with-param>
                        </xsl:call-template>
                        <xsl:apply-templates select="imdi:Description|Description" mode="make_row"/>
                        <xsl:apply-templates select="imdi:Keys|Keys" mode="make_row"/>
                        <xsl:apply-templates select="imdi:LexicalEntry|LexicalEntry" mode="make_row"/>
                        <xsl:apply-templates select="imdi:Access|Access" mode="make_row"/>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!--    
        LexicalEntry
    -->
    <xsl:template match="imdi:LexicalEntry|LexicalEntry">
        <xsl:call-template name="make_table_content_click">
            <xsl:with-param name="title">
                <xsl:value-of select="name(.)"/>
            </xsl:with-param>
            <xsl:with-param name="style">
                <xsl:value-of select="name(.)"/>
            </xsl:with-param>
            <xsl:with-param name="content">
                <xsl:call-template name="make_row_table">
                    <xsl:with-param name="content">
                        <xsl:call-template name="table_tag_value_row">
                            <xsl:with-param name="tag">Headword Type</xsl:with-param>
                            <xsl:with-param name="value" select="imdi:HeadWordType|HeadWordType"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="tag">Orthography</xsl:with-param>
                            <xsl:with-param name="value" select="imdi:Orthography|Orthography"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Morphology|Morphology"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_tag_value_row">
                            <xsl:with-param name="tag">Morphosyntax</xsl:with-param>
                            <xsl:with-param name="value" select="imdi:MorphoSyntax|MorphoSyntax"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="tag">Syntax</xsl:with-param>
                            <xsl:with-param name="value" select="imdi:Syntax|Syntax"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="tag">Phonology</xsl:with-param>
                            <xsl:with-param name="value" select="imdi:Phonology|Phonology"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="tag">Semantics</xsl:with-param>
                            <xsl:with-param name="value" select="imdi:Semantics|Semantics"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="tag">Etymology</xsl:with-param>
                            <xsl:with-param name="value" select="imdi:Etymology|Etymology"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="tag">Usage</xsl:with-param>
                            <xsl:with-param name="value" select="imdi:Usage|Usage"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="tag">Frequency</xsl:with-param>
                            <xsl:with-param name="value" select="imdi:Frequency|Frequency"/>
                        </xsl:call-template>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!--
        References    
    -->
    <xsl:template match="imdi:References|References">
        <xsl:call-template name="make_table_content_click">
            <xsl:with-param name="title">
                <xsl:value-of select="name(.)"/>
            </xsl:with-param>
            <xsl:with-param name="style">
                <xsl:value-of select="name(.)"/>
            </xsl:with-param>
            <xsl:with-param name="content">
                <xsl:apply-templates select="imdi:Description|Description" mode="make_row"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!--
        CommunicationContext    
    -->
    <xsl:template match="imdi:CommunicationContext|CommunicationContext">
        <xsl:call-template name="make_table_content_click">
            <xsl:with-param name="title">
                <xsl:value-of select="name(.)"/>
            </xsl:with-param>
            <xsl:with-param name="style">
                <xsl:value-of select="name(.)"/>
            </xsl:with-param>
            <xsl:with-param name="content">
                <xsl:call-template name="table_row">
                    <xsl:with-param name="value" select="imdi:Interactivity|Interactivity"/>
                </xsl:call-template>
                <xsl:call-template name="table_tag_value_row">
                    <xsl:with-param name="tag">Planning Type</xsl:with-param>
                    <xsl:with-param name="value" select="imdi:PlanningType|PlanningType"/>
                </xsl:call-template>
                <xsl:call-template name="table_row">
                    <xsl:with-param name="value" select="imdi:Involvement|Involvement"/>
                </xsl:call-template>
                <xsl:call-template name="table_tag_value_row">
                    <xsl:with-param name="tag">Social Context</xsl:with-param>
                    <xsl:with-param name="value" select="imdi:SocialContext|SocialContext"/>
                </xsl:call-template>
                <xsl:call-template name="table_tag_value_row">
                    <xsl:with-param name="tag">Event Structure</xsl:with-param>
                    <xsl:with-param name="value" select="imdi:EventStructure|EventStructure"/>
                </xsl:call-template>
                <xsl:call-template name="table_row">
                    <xsl:with-param name="value" select="imdi:Channel|Channel"/>
                </xsl:call-template>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!--
        Validation
    -->
    <xsl:template match="imdi:Validation|Validation">
        <xsl:call-template name="make_table_content_click">
            <xsl:with-param name="title">
                <xsl:value-of select="name(.)"/>
            </xsl:with-param>
            <xsl:with-param name="style">
                <xsl:value-of select="name(.)"/>
            </xsl:with-param>
            <xsl:with-param name="content">
                <xsl:call-template name="make_row_table">
                    <xsl:with-param name="content">
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Type|Type"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Level|Level"/>
                        </xsl:call-template>
                    </xsl:with-param>
                </xsl:call-template>
                <xsl:apply-templates select="imdi:Description|Description" mode="make_row"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!--    
        Source
    -->
    <xsl:template match="imdi:Source|Source">
        <xsl:call-template name="make_table_content_click">
            <xsl:with-param name="title">
                <xsl:value-of select="name(.)"/>: <xsl:value-of select="imdi:Format|Format"/>
            </xsl:with-param>
            <xsl:with-param name="style">
                <xsl:value-of select="name(.)"/>
            </xsl:with-param>
            <xsl:with-param name="content">
                <xsl:call-template name="make_row_table">
                    <xsl:with-param name="content">
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Format|Format"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Id|Id"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Quality|Quality"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_tag_value_row">
                            <xsl:with-param name="tag">Resource References</xsl:with-param>
                            <xsl:with-param name="value" select="@ResourceRefs"/>
                        </xsl:call-template>
                    </xsl:with-param>
                </xsl:call-template>
                <xsl:apply-templates select="imdi:TimePosition|TimePosition|imdi:CounterPosition|CounterPosition" mode="make_row"/>
                <xsl:apply-templates select="imdi:Description|Description" mode="make_row"/>
                <xsl:apply-templates select="imdi:Keys|Keys" mode="make_row"/>
                <xsl:apply-templates select="imdi:Access|Access" mode="make_row"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!--
        Contact
    -->
    <xsl:template match="imdi:Contact|Contact">
        <xsl:call-template name="make_table_content_click">
            <xsl:with-param name="title">
                <xsl:value-of select="name(.)"/>
            </xsl:with-param>
            <xsl:with-param name="style">
                <xsl:value-of select="name(.)"/>
            </xsl:with-param>
            <xsl:with-param name="content">
                <xsl:call-template name="make_row_table">
                    <xsl:with-param name="content">
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Name|Name"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Address|Address"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Email|Email"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Organisation|Organisation"/>
                        </xsl:call-template>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!--
        Access
    -->
    <xsl:template match="imdi:Access|Access">
        <xsl:call-template name="make_table_content_click">
            <xsl:with-param name="title">
                <xsl:value-of select="name(.)"/>
            </xsl:with-param>
            <xsl:with-param name="style">
                <xsl:value-of select="name(.)"/>
            </xsl:with-param>
            <xsl:with-param name="content">
                <xsl:call-template name="make_row_table">
                    <xsl:with-param name="content">
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Availability|Availability"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Date|Date"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Owner|Owner"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Publisher|Publisher"/>
                        </xsl:call-template>
                    </xsl:with-param>
                </xsl:call-template>
                <xsl:apply-templates select="imdi:Description|Description" mode="make_row"/>
                <xsl:apply-templates select="imdi:Contact|Contact" mode="make_row"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!--
        TimePosition    
    -->
    <xsl:template match="imdi:TimePosition|TimePosition">
        <xsl:call-template name="make_table_content">
            <xsl:with-param name="title">
                <xsl:value-of select="name(.)"/>
            </xsl:with-param>
            <xsl:with-param name="style">
                <xsl:value-of select="name(.)"/>
            </xsl:with-param>
            <xsl:with-param name="content">
                <xsl:call-template name="make_row_table">
                    <xsl:with-param name="content">
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Start|Start"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:End|End"/>
                        </xsl:call-template>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!--
        CounterPosition
    -->
    <xsl:template match="imdi:CounterPosition|CounterPosition">
        <xsl:call-template name="make_table_content">
            <xsl:with-param name="title">
                <xsl:value-of select="name(.)"/>
            </xsl:with-param>
            <xsl:with-param name="style">
                <xsl:value-of select="name(.)"/>
            </xsl:with-param>
            <xsl:with-param name="content">
                <xsl:call-template name="make_row_table">
                    <xsl:with-param name="content">
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:Start|Start"/>
                        </xsl:call-template>
                        <xsl:call-template name="table_row">
                            <xsl:with-param name="value" select="imdi:End|End"/>
                        </xsl:call-template>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!--
        Description
    -->
    <xsl:template match="imdi:Description|Description">
        <xsl:variable name="text">
            <xsl:apply-templates select="text()"/>
        </xsl:variable>
        <xsl:if test="normalize-space($text) != ''">
            <xsl:call-template name="make_table_content">
                <xsl:with-param name="title">
                    <span class="IMDI_element_name">Description </span>
                    <xsl:if test="string-length(@LanguageId)&gt;0">
                        <span class="IMDI_attribute_name">(Language: <xsl:value-of select="@LanguageId"/>) </span>
                    </xsl:if>
                </xsl:with-param>
                <xsl:with-param name="style">
                    <xsl:value-of select="name(.)"/>
                </xsl:with-param>
                <xsl:with-param name="content">
                    <xsl:if test="string-length(@Link)&gt;0">
                        <xsl:element name="div">
                            <xsl:choose>
                                <xsl:when test="starts-with(@Link,'file:')">
                                    <div class="IMDI_element_value">
                                        <xsl:value-of select="@Link"/>
                                    </div>
                                </xsl:when>
                                <xsl:when test="$info-linking = 'true'">
                                    <xsl:element name="a">
                                        <xsl:attribute name="href">
                                            <xsl:value-of select="@Link"/>
                                        </xsl:attribute>
                                        <span class="IMDI_link">
                                            <xsl:text>Read more</xsl:text>
                                        </span>
                                    </xsl:element>
                                    <span class="IMDI_element_value">
                                        <xsl:text> (</xsl:text>
                                        <xsl:value-of select="@Link"/>
                                        <xsl:text>)</xsl:text>
                                    </span>
                                </xsl:when>
                            </xsl:choose>
                        </xsl:element>
                    </xsl:if>
                    <pre>
                        <xsl:value-of select="$text"/>
                    </pre>
                </xsl:with-param>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>
    <!--
        Keys    
    -->
    <xsl:template match="imdi:Keys|Keys">
        <xsl:if test="count(imdi:Key|Key)&gt;0">
            <xsl:call-template name="make_table_content_click">
                <xsl:with-param name="title">
                    <xsl:value-of select="name(.)"/>
                </xsl:with-param>
                <xsl:with-param name="style">
                    <xsl:value-of select="name(.)"/>
                </xsl:with-param>
                <xsl:with-param name="content">
                    <xsl:apply-templates select="imdi:Key|Key"/>
                </xsl:with-param>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>
    <xsl:template match="imdi:Key|Key">
        <tr>
            <td align="right" valign="top" width="150">
                <div class="IMDI_element_name">
                    <xsl:value-of select="@Name"/>:</div>
            </td>
            <td class="IMDI_element_value">
                <xsl:value-of select="text()"/>
                <xsl:if test="count(@Link)&gt;0">
                    <xsl:text> (</xsl:text>
                    <xsl:value-of select="@Type"/>: <xsl:value-of select="@Link"/>) </xsl:if>
            </td>
        </tr>
    </xsl:template>
    <!--
        Make row
    -->
    <xsl:template match="imdi:*|*" mode="make_row">
        <tr>
            <td width="100%" class="IMDI_element_value">
                <xsl:apply-templates select="."/>
            </td>
        </tr>
    </xsl:template>
    <!--
        Make row with content enclosed in a table
    -->
    <xsl:template name="make_row_table">
        <xsl:param name="content"/>
        <tr>
            <td width="100%">
                <table class="IMDI_elements_table">
                    <xsl:copy-of select="$content"/>
                </table>
            </td>
        </tr>
    </xsl:template>
    <!--
        Make table with static content
    -->
    <xsl:template name="make_table_content">
        <xsl:param name="title"/>
        <xsl:param name="style"/>
        <xsl:param name="content"/>
        <table>
            <xsl:attribute name="class">
                <xsl:value-of select="$style"/>
            </xsl:attribute>
            <xsl:call-template name="header">
                <xsl:with-param name="title" select="$title"/>
                <xsl:with-param name="style" select="$style"/>
            </xsl:call-template>
            <tr>
                <td width="100%">
                    <xsl:call-template name="make_table">
                        <xsl:with-param name="content">
                            <xsl:call-template name="make_row_table">
                                <xsl:with-param name="content" select="$content"/>
                            </xsl:call-template>
                        </xsl:with-param>
                    </xsl:call-template>
                </td>
            </tr>
        </table>
    </xsl:template>
    <!--
        Make table with dynamic content
    -->
    <xsl:template name="make_table_content_click">
        <xsl:param name="title"/>
        <xsl:param name="style"/>
        <xsl:param name="content"/>
        <xsl:variable name="tid" select="generate-id()"/>
        <table>
            <xsl:attribute name="class">
                <xsl:value-of select="$style"/>
            </xsl:attribute>
            <xsl:call-template name="header">
                <xsl:with-param name="title" select="$title"/>
                <xsl:with-param name="style" select="$style"/>
                <xsl:with-param name="id" select="$tid"/>
            </xsl:call-template>
            <tr>
                <td width="100%">
                    <xsl:call-template name="make_table">
                        <xsl:with-param name="id" select="$tid"/>
                        <xsl:with-param name="content">
                            <xsl:call-template name="make_row_table">
                                <xsl:with-param name="content" select="$content"/>
                            </xsl:call-template>
                        </xsl:with-param>
                    </xsl:call-template>
                </td>
            </tr>
        </table>
    </xsl:template>
    <!--
        Make table with specified content and id
    -->
    <xsl:template name="make_table">
        <xsl:param name="id"/>
        <xsl:param name="content"/>
        <table>
            <xsl:choose>
                <xsl:when test="$id">
                    <xsl:attribute name="class">IMDI_click_table</xsl:attribute>
                    <xsl:attribute name="id">
                        <xsl:value-of select="$id"/>
                    </xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="class">IMDI_content_table</xsl:attribute>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:copy-of select="$content"/>
        </table>
    </xsl:template>
    <!--
        Table tag-value row
    -->
    <xsl:template name="table_tag_value_row">
        <xsl:param name="tag"/>
        <xsl:param name="value"/>
        <xsl:if test="$tag">
            <tr>
                <td align="right" valign="top" width="150">
                    <div class="IMDI_element_name">
                        <xsl:value-of select="$tag"/>:</div>
                </td>
                <td valign="top">
                    <div class="IMDI_element_value">
                        <xsl:choose>
                            <xsl:when test="count($value/@XXX-Ref) &gt; 0">
                                <xsl:element name="a">
                                    <xsl:attribute name="href">
                                        <xsl:value-of select="concat(substring-before($value/@XXX-Ref, '.imdi'), '.html')"/>
                                    </xsl:attribute>
                                    <xsl:attribute name="class">IMDI_link</xsl:attribute>
                                    <xsl:value-of select="$value"/>
                                </xsl:element>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="$value"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </div>
                </td>
            </tr>
        </xsl:if>
    </xsl:template>
    <!--
        Table row - get (tag,value) from value node
    -->
    <xsl:template name="table_row">
        <xsl:param name="value"/>
        <xsl:call-template name="table_tag_value_row">
            <xsl:with-param name="tag" select="name($value)"/>
            <xsl:with-param name="value" select="$value"/>
        </xsl:call-template>
    </xsl:template>
    <!--
       Header of metadata group
    -->
    <xsl:template name="header">
        <xsl:param name="title"/>
        <xsl:param name="style"/>
        <xsl:param name="id"/>
        <tr>
            <th>
                <xsl:attribute name="class">
                    <xsl:value-of select="$style"/>
                </xsl:attribute>
                <xsl:choose>
                    <xsl:when test="$id">
                        <xsl:if test="$icon-closed">
                            <img>
                                <xsl:attribute name="src">
                                    <xsl:value-of select="$icon-closed"/>
                                </xsl:attribute>
                            </img>
                            <xsl:text> </xsl:text>
                        </xsl:if>
                        <xsl:element name="a">
                            <xsl:attribute name="class">
                                <xsl:value-of select="$style"/>
                            </xsl:attribute>
                            <xsl:attribute name="href">javascript:change_status("<xsl:value-of select="$id"/>")</xsl:attribute>
                            <xsl:value-of select="$title"/>
                        </xsl:element>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$title"/>
                    </xsl:otherwise>
                </xsl:choose>
            </th>
        </tr>
    </xsl:template>
    <!--
        Custom template for corpus link tag-value
    -->
    <xsl:template name="named_link_row">
        <xsl:param name="name"/>
        <xsl:param name="link"/>
        <xsl:variable name="html-ref">
            <xsl:value-of select="concat(substring-before($link, '.imdi'), '.html')"/>
        </xsl:variable>
        <tr>
            <td align="right" valign="top" width="150">
                <div class="IMDI_element_name">Link:</div>
            </td>
            <td valign="top">
                <div class="IMDI_element_value">
                    <xsl:element name="a">
                        <xsl:attribute name="href">
                            <xsl:value-of select="$html-ref"/>
                        </xsl:attribute>
                        <xsl:attribute name="class">IMDI_link</xsl:attribute>
                        <xsl:value-of select="$name"/>
                    </xsl:element>
                </div>
            </td>
        </tr>
    </xsl:template>
</xsl:stylesheet>
