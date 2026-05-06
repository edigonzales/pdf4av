<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
        version="3.0"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:xs="http://www.w3.org/2001/XMLSchema"
        xmlns:fo="http://www.w3.org/1999/XSL/Format">
    <xsl:output method="xml" indent="yes"/>
    <xsl:param name="localeUrl" as="xs:string"/>

    <xsl:variable name="localeXml" select="document($localeUrl)/root"/>

    <xsl:template match="/">
        <xsl:if test="not(/document)">
            <xsl:message terminate="yes">Root element &lt;document&gt; is required.</xsl:message>
        </xsl:if>
        <xsl:if test="not(normalize-space(/document/title))">
            <xsl:message terminate="yes">Element &lt;title&gt; is required.</xsl:message>
        </xsl:if>
        <xsl:if test="empty(/document/content/paragraph)">
            <xsl:message terminate="yes">At least one &lt;paragraph&gt; is required.</xsl:message>
        </xsl:if>
        <xsl:apply-templates select="/document"/>
    </xsl:template>

    <xsl:template match="document">
        <fo:root font-family="Cadastra">
            <fo:layout-master-set>
                <fo:simple-page-master
                        master-name="main"
                        page-height="297mm"
                        page-width="210mm"
                        margin-top="20mm"
                        margin-right="18mm"
                        margin-bottom="20mm"
                        margin-left="18mm">
                    <fo:region-body/>
                </fo:simple-page-master>
            </fo:layout-master-set>
            <fo:page-sequence master-reference="main">
                <fo:flow flow-name="xsl-region-body">
                    <fo:block font-size="18pt" font-weight="700" space-after="10mm">
                        <xsl:value-of select="title"/>
                    </fo:block>

                    <xsl:if test="meta/*">
                        <fo:block space-after="8mm" padding="4mm" border="0.5pt solid #666666">
                            <xsl:if test="normalize-space(meta/author)">
                                <fo:block font-size="9pt">
                                    <fo:inline font-weight="700">
                                        <xsl:value-of select="$localeXml/data[@name='label.author']/value"/>
                                    </fo:inline>
                                    <xsl:text>: </xsl:text>
                                    <xsl:value-of select="meta/author"/>
                                </fo:block>
                            </xsl:if>
                            <xsl:if test="normalize-space(meta/subject)">
                                <fo:block font-size="9pt">
                                    <fo:inline font-weight="700">
                                        <xsl:value-of select="$localeXml/data[@name='label.subject']/value"/>
                                    </fo:inline>
                                    <xsl:text>: </xsl:text>
                                    <xsl:value-of select="meta/subject"/>
                                </fo:block>
                            </xsl:if>
                            <xsl:if test="normalize-space(meta/keywords)">
                                <fo:block font-size="9pt">
                                    <fo:inline font-weight="700">
                                        <xsl:value-of select="$localeXml/data[@name='label.keywords']/value"/>
                                    </fo:inline>
                                    <xsl:text>: </xsl:text>
                                    <xsl:value-of select="meta/keywords"/>
                                </fo:block>
                            </xsl:if>
                        </fo:block>
                    </xsl:if>

                    <xsl:for-each select="content/paragraph">
                        <fo:block font-size="11pt" line-height="16pt" space-after="5mm">
                            <xsl:value-of select="."/>
                        </fo:block>
                    </xsl:for-each>
                </fo:flow>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>
</xsl:stylesheet>
