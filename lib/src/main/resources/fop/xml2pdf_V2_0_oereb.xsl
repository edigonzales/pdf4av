<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet  version="3.0" exclude-result-prefixes="geometry extract data" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:oereb="http://oereb.so.ch" xmlns:geometry="http://www.interlis.ch/geometry/1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:fox="http://xmlgraphics.apache.org/fop/extensions" xmlns:extract="http://schemas.geo.admin.ch/V_D/OeREB/2.0/Extract" xmlns:data="http://schemas.geo.admin.ch/V_D/OeREB/2.0/ExtractData" xmlns:array="http://www.w3.org/2005/xpath-functions/array">
  <xsl:output method="xml" indent="yes"/>
  <xsl:param name="localeUrl" select="'Resources.de.resx'"/>
  <xsl:variable name="locale" as="xs:string" select="substring($localeUrl, 11, 2)"/>
  <xsl:variable name="localeXml" select="document($localeUrl)/*"/>
  <xsl:variable name="OverlayImage">
    <xsl:value-of select="oereb:createOverlayImage(extract:GetExtractByIdResponse/data:Extract/data:RealEstate/data:Limit, extract:GetExtractByIdResponse/data:Extract/data:RealEstate/data:PlanForLandRegisterMainPage, $locale)"/>
  </xsl:variable>
  <xsl:decimal-format name="swiss" decimal-separator="." grouping-separator="'"/>
  <xsl:template match="extract:GetExtractByIdResponse/data:Extract">
    <fo:root language="de" font-family="Cadastra" font-weight="400" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:fox="http://xmlgraphics.apache.org/fop/extensions" xmlns:xsd="https://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xml:lang="en">
      <fo:layout-master-set>
        <fo:simple-page-master master-name="mainPage" page-height="297mm" page-width="210mm" margin-top="10mm" margin-bottom="12mm" margin-left="18mm" margin-right="18mm">
          <fo:region-body margin-top="30mm" margin-bottom="5mm" background-color="transparent"/>
          <fo:region-before extent="19mm" background-color="transparent"/>
          <fo:region-after extent="3mm" background-color="transparent"/>
        </fo:simple-page-master>
      </fo:layout-master-set>
      <fo:page-sequence master-reference="mainPage" id="page-sequence-id">
        <xsl:call-template name="insertHeaderAndFooter"/>
        <fo:flow flow-name="xsl-region-body">
          <fo:block-container height="28mm" background-color="transparent">
            <fo:block line-height="21pt" linefeed-treatment="preserve" font-weight="700" font-size="18pt">
              <xsl:value-of select="$localeXml/data[@name='MainPage.Title']/value/text()"/>
            </fo:block>
          </fo:block-container>
          <fo:block-container height="107mm" background-color="transparent">
            <fo:block font-size="0pt" padding="0mm" margin="0mm" line-height="0mm">
              <fo:external-graphic border="0.4pt solid black" width="173.7mm" height="99mm" scaling="non-uniform" content-width="scale-to-fit" content-height="scale-to-fit" fox:alt-text="PlanForLandRegisterMainPageImage">
              <!-- Weil man das PlanForLandRegisterMainPage-Element der Extension Function übergibt,
              muss keine Image-WMS-if/else-Unterscheidung gemacht werden wie z.B. bei den Logos.-->
                <xsl:attribute name="src">
                  <xsl:text>url('data:</xsl:text>
                  <xsl:text>image/png;base64,</xsl:text>
                  <!--<xsl:message><xsl:value-of select="$OverlayImage"/></xsl:message>-->
                  <xsl:value-of select="oereb:createPlanForLandRegisterMainPageImage(data:RealEstate/data:PlanForLandRegisterMainPage, $OverlayImage, $locale)"/>
                  <xsl:text>')</xsl:text>
                </xsl:attribute>
              </fo:external-graphic>
            </fo:block>
          </fo:block-container>
          <fo:block-container font-size="8pt" background-color="transparent">
            <fo:table table-layout="fixed" width="100%">
              <fo:table-column column-width="68mm"/>
              <fo:table-column column-width="106mm"/>
              <fo:table-body>
                <fo:table-row border-bottom="0.2pt solid black" vertical-align="middle" line-height="6mm">
                  <fo:table-cell>
                    <fo:block font-weight="700">
                      <xsl:value-of select="$localeXml/data[@name='MainPage.RealEstate_DPR.Number']/value/text()"/>
                    </fo:block>
                  </fo:table-cell>
                  <fo:table-cell>
                    <fo:block font-weight="700">
                      <xsl:value-of select="data:RealEstate/data:Number"/>
                    </fo:block>
                  </fo:table-cell>
                </fo:table-row>
                <fo:table-row border-bottom="0.2pt solid black" vertical-align="middle" line-height="6mm">
                  <fo:table-cell>
                    <fo:block>
                      <xsl:value-of select="$localeXml/data[@name='MainPage.RealEstate_DPR.Type']/value/text()"/>
                    </fo:block>
                  </fo:table-cell>
                  <fo:table-cell>
                    <fo:block>
                      <xsl:value-of select="oereb:extractMultilingualText(data:RealEstate/data:Type/data:Text/data:LocalisedText, $locale)"/>
                    </fo:block>
                  </fo:table-cell>
                </fo:table-row>
                <fo:table-row border-bottom="0.2pt solid black" vertical-align="middle" line-height="6mm">
                  <fo:table-cell>
                    <fo:block>E-GRID</fo:block>
                  </fo:table-cell>
                  <fo:table-cell>
                    <fo:block>
                      <xsl:value-of select="data:RealEstate/data:EGRID"/>
                    </fo:block>
                  </fo:table-cell>
                </fo:table-row>
                <fo:table-row border-bottom="0.2pt solid black" vertical-align="middle" line-height="6mm">
                  <fo:table-cell>
                    <fo:block>
                      <xsl:value-of select="$localeXml/data[@name='MainPage.Municipality_FosNr']/value/text()"/>
                    </fo:block>
                  </fo:table-cell>
                  <fo:table-cell>
                    <fo:block><xsl:value-of select="data:RealEstate/data:MunicipalityName"/> (<xsl:value-of select="data:RealEstate/data:MunicipalityCode"/>)</fo:block>
                  </fo:table-cell>
                </fo:table-row>
                <xsl:if test="data:RealEstate/data:SubunitOfLandRegister">
                  <fo:table-row border-bottom="0.2pt solid black" vertical-align="middle" line-height="6mm">
                    <fo:table-cell>
                      <fo:block>
                        <xsl:value-of select="data:RealEstate/data:SubunitOfLandRegisterDesignation"/>
                      </fo:block>
                    </fo:table-cell>
                    <fo:table-cell>
                      <fo:block>
                        <xsl:value-of select="data:RealEstate/data:SubunitOfLandRegister"/>
                      </fo:block>
                    </fo:table-cell>
                  </fo:table-row>
                </xsl:if>
                <fo:table-row border-bottom="0.2pt solid black" vertical-align="middle" line-height="6mm">
                  <fo:table-cell>
                    <fo:block>
                      <xsl:value-of select="$localeXml/data[@name='MainPage.LandRegistryArea']/value/text()"/>
                    </fo:block>
                  </fo:table-cell>
                  <fo:table-cell>
                    <fo:block line-height-shift-adjustment="disregard-shifts"><xsl:value-of select="format-number(data:RealEstate/data:LandRegistryArea, &quot;#'###&quot;, &quot;swiss&quot;)"/> m<fo:inline baseline-shift="super" font-size="60%">2</fo:inline></fo:block>
                  </fo:table-cell>
                </fo:table-row>
                <fo:table-row border-bottom="0.2pt solid black" vertical-align="middle" line-height="6mm">
                  <fo:table-cell>
                    <fo:block>
                      <xsl:value-of select="$localeXml/data[@name='MainPage.UpdateDateCS']/value/text()"/>
                    </fo:block>
                  </fo:table-cell>
                  <fo:table-cell>
                    <fo:block line-height-shift-adjustment="disregard-shifts"><xsl:value-of select="format-dateTime(/extract:GetExtractByIdResponse/data:Extract/data:UpdateDateCS,'[D01].[M01].[Y0001]')"/></fo:block>
                  </fo:table-cell>
                </fo:table-row>
              </fo:table-body>
            </fo:table>
          </fo:block-container>
          <fo:block-container margin-top="8mm" font-size="8pt" background-color="transparent">
            <fo:table table-layout="fixed" width="100%">
              <fo:table-column column-width="68mm"/>
              <fo:table-column column-width="106mm"/>
              <fo:table-body>
                <fo:table-row border-bottom="0.2pt solid black" vertical-align="middle" line-height="6mm">
                  <fo:table-cell>
                    <fo:block font-weight="700">
                      <xsl:value-of select="$localeXml/data[@name='MainPage.ExtractIdentifier']/value/text()"/>
                    </fo:block>
                  </fo:table-cell>
                  <fo:table-cell>
                    <fo:block font-weight="700">
                      <xsl:value-of select="data:ExtractIdentifier"/>
                    </fo:block>
                  </fo:table-cell>
                </fo:table-row>
                <fo:table-row border-bottom="0.2pt solid black" vertical-align="middle" line-height="6mm">
                  <fo:table-cell>
                    <fo:block>
                      <xsl:value-of select="$localeXml/data[@name='MainPage.CreationDate']/value/text()"/>
                    </fo:block>
                  </fo:table-cell>
                  <fo:table-cell>
                    <fo:block>
                      <xsl:value-of select="format-dateTime(data:CreationDate,'[D01].[M01].[Y0001]')"/>
                    </fo:block>
                  </fo:table-cell>
                </fo:table-row>
                <fo:table-row border-bottom="0.2pt solid black" vertical-align="middle" line-height="11pt" height="6mm">
                  <!-- Text darf vertikal nicht zentriert werden (wie bei den einzeiligen Zellen).  
                  Rechnerisch komme ich zwar auf 4.5pt (6mm Zeileinhöhe = 17pt minus 8pt für die Schrift), 
                  3.5pt passt aber besser zu den einzeiligen Zellen. Wahrscheinlich ist kann man 8pt 
                  nicht einfach so in die Rechnung einfliessen lassen.-->
                  <fo:table-cell padding-top="3.5pt">
                    <fo:block>
                      <xsl:value-of select="$localeXml/data[@name='MainPage.ResponsibleOffice']/value/text()"/>
                    </fo:block>
                  </fo:table-cell>
                  <fo:table-cell padding-top="3.5pt" padding-bottom="3.5pt">
                    <fo:block linefeed-treatment="preserve">
                      <xsl:value-of select="oereb:extractMultilingualText(data:PLRCadastreAuthority/data:Name/data:LocalisedText, $locale)"/>
                      <xsl:text>&#x000A;</xsl:text>
                      <xsl:value-of select="data:PLRCadastreAuthority/data:Street"/>
                      <xsl:text> </xsl:text>
                      <xsl:value-of select="data:PLRCadastreAuthority/data:Number"/>
                      <xsl:text>&#x000A;</xsl:text>
                      <xsl:value-of select="data:PLRCadastreAuthority/data:PostalCode"/>
                      <xsl:text> </xsl:text>
                      <xsl:value-of select="data:PLRCadastreAuthority/data:City"/>
                      <xsl:text>&#x000A;</xsl:text>
                      <fo:inline>
                        <fo:basic-link text-decoration="none" color="rgb(76,143,186)">
                          <xsl:attribute name="external-destination">
                            <xsl:value-of select="oereb:decodeURL(oereb:extractMultilingualText(data:PLRCadastreAuthority/data:OfficeAtWeb/data:LocalisedText, $locale))"/>
                          </xsl:attribute>
                          <!--<xsl:value-of select="data:PLRCadastreAuthority/data:OfficeAtWeb/data:LocalisedText[data:Language = 'fr']/data:Text"/>-->
                            <xsl:value-of select="oereb:decodeURL(oereb:extractMultilingualText(data:PLRCadastreAuthority/data:OfficeAtWeb/data:LocalisedText, $locale))"/>
                        </fo:basic-link>
                      </fo:inline>
                    </fo:block>
                  </fo:table-cell>
                </fo:table-row>
              </fo:table-body>
            </fo:table>
          </fo:block-container>
          <fo:block-container height="13mm" background-color="transparent">
            <fo:block page-break-before="always" line-height="18pt" linefeed-treatment="preserve" font-weight="700" font-size="15pt">
              <xsl:value-of select="$localeXml/data[@name='ContentPage.Title']/value/text()"/>
            </fo:block>
          </fo:block-container>
          <fo:block-container background-color="transparent">
            <fo:block line-height="11pt" linefeed-treatment="preserve" font-weight="700" font-size="8pt">
              <xsl:value-of select="$localeXml/data[@name='ContentPage.ConcernedTheme_Part1']/value/text()"/>
              <xsl:text> </xsl:text>
              <xsl:value-of select="data:RealEstate/data:Number"/>
              <xsl:text> </xsl:text>
              <xsl:value-of select="$localeXml/data[@name='ContentPage.ConcernedTheme_Part2']/value/text()"/>
              <xsl:text> </xsl:text>
              <xsl:value-of select="data:RealEstate/data:MunicipalityName"/>
              <xsl:if test="data:RealEstate/data:SubunitOfLandRegister">
                <xsl:text>, </xsl:text>
                <xsl:value-of select="data:RealEstate/data:SubunitOfLandRegister"/>
              </xsl:if>
              <xsl:text> </xsl:text>
              <xsl:value-of select="$localeXml/data[@name='ContentPage.ConcernedTheme_Part3']/value/text()"/>
            </fo:block>
          </fo:block-container>
          <fo:block-container>
            <fo:block font-size="0pt" padding="0mm" margin="0mm" line-height="0mm">
              <fo:leader leader-pattern="rule" leader-length="100%" rule-style="solid" rule-thickness="0.2pt"/>
            </fo:block>
          </fo:block-container>
          
          <!-- Inhaltsverzeichnis -->
          <xsl:apply-templates select="data:RealEstate" mode="toc"/>

          <fo:block-container background-color="transparent">
            <fo:block line-height="12pt" linefeed-treatment="preserve" font-weight="700" font-size="8pt">
              <xsl:value-of select="$localeXml/data[@name='ContentPage.NotConcernedTheme']/value/text()"/>
            </fo:block>
          </fo:block-container>
          <fo:block-container margin-bottom="1mm">
            <fo:block font-size="0pt" padding="0mm" margin="0mm" line-height="0mm">
              <fo:leader leader-pattern="rule" leader-length="100%" rule-style="solid" rule-thickness="0.2pt"/>
            </fo:block>
          </fo:block-container>
          <fo:block-container margin-bottom="10mm" font-size="8pt" line-height="11pt" background-color="transparent">
            <fo:table table-layout="fixed" width="100%">
              <fo:table-body>
                <xsl:choose>
                  <xsl:when test="data:NotConcernedTheme">
                    <xsl:for-each select="data:NotConcernedTheme">
                      <fo:table-row vertical-align="middle">
                        <fo:table-cell>
                          <fo:block>
                            <xsl:value-of select="oereb:extractMultilingualText(data:Text/data:LocalisedText, $locale)"/>
                          </fo:block>
                        </fo:table-cell>
                      </fo:table-row>
                    </xsl:for-each>
                  </xsl:when>
                  <xsl:otherwise>
                    <fo:table-row vertical-align="middle">
                      <fo:table-cell>
                        <fo:block>&#x2013;</fo:block>
                      </fo:table-cell>
                    </fo:table-row>
                  </xsl:otherwise>
                </xsl:choose>
              </fo:table-body>
            </fo:table>
          </fo:block-container>
          <fo:block-container background-color="transparent">
            <fo:block line-height="12pt" linefeed-treatment="preserve" font-weight="700" font-size="8pt">
              <xsl:value-of select="$localeXml/data[@name='ContentPage.ThemeWithoutData']/value/text()"/>
            </fo:block>
          </fo:block-container>
          <fo:block-container margin-bottom="1mm">
            <fo:block font-size="0pt" padding="0mm" margin="0mm" line-height="0mm">
              <fo:leader leader-pattern="rule" leader-length="100%" rule-style="solid" rule-thickness="0.2pt"/>
            </fo:block>
          </fo:block-container>
          <!-- margin-bottom ist heuristisch -->
          <fo:block-container margin-bottom="10mm" font-weight="400" font-size="8.5pt" line-height="11.5pt" font-family="Cadastra" background-color="transparent">
            <fo:table table-layout="fixed" width="100%">
              <fo:table-body>
                <xsl:choose>
                  <xsl:when test="data:ThemeWithoutData">
                    <xsl:for-each select="data:ThemeWithoutData">
                      <fo:table-row vertical-align="middle">
                        <fo:table-cell>
                          <fo:block>
                            <xsl:value-of select="oereb:extractMultilingualText(data:Text/data:LocalisedText, $locale)"/>
                          </fo:block>
                        </fo:table-cell>
                      </fo:table-row>
                    </xsl:for-each>
                  </xsl:when>
                  <xsl:otherwise>
                    <fo:table-row vertical-align="middle">
                      <fo:table-cell>
                        <fo:block>&#x2013;</fo:block>
                      </fo:table-cell>
                    </fo:table-row>
                  </xsl:otherwise>
                </xsl:choose>
              </fo:table-body>
            </fo:table>
          </fo:block-container>
          <fo:block-container>
            <fo:block>
              <fo:footnote>
                <fo:inline/>
                <fo:footnote-body>
                  <fo:block keep-together.within-column="always">
                    <fo:block-container margin-top="0mm" margin-bottom="0mm" font-size="6pt" line-height="8pt" background-color="transparent">
                      <fo:table table-layout="fixed" width="100%" background-color="transparent">
                        <fo:table-column column-width="87mm"/>
                        <fo:table-column column-width="87mm"/>
                        <fo:table-body>
                          <fo:table-row vertical-align="top">
                            <fo:table-cell padding-right="1.5mm" background-color="transparent">
                              <fo:block font-weight="700">
                                <xsl:value-of select="$localeXml/data[@name='ContentPage.GeneralInformation']/value/text()"/>
                              </fo:block>
                              <fo:block>
                                <xsl:call-template name="textRenderer">
                                  <xsl:with-param name="textContent" select="oereb:extractMultilingualText(data:GeneralInformation/data:LocalisedText, $locale)" />
                                </xsl:call-template>
                              </fo:block>
                              <fo:block margin-top="2.2mm" font-weight="700">
                                <xsl:value-of select="oereb:extractMultilingualText(data:Disclaimer[1]/data:Title/data:LocalisedText, $locale)"/>
                              </fo:block>
                              <fo:block>
                                <xsl:call-template name="textRenderer">
                                  <xsl:with-param name="textContent" select="oereb:extractMultilingualText(data:Disclaimer[1]/data:Content/data:LocalisedText, $locale)" />
                                </xsl:call-template>
                              </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding-left="1.5mm" background-color="transparent">
                              <fo:block font-weight="700">
                                <xsl:value-of select="oereb:extractMultilingualText(data:Disclaimer[2]/data:Title/data:LocalisedText, $locale)"/>
                              </fo:block>
                              <fo:block>
                                <xsl:call-template name="textRenderer">
                                  <xsl:with-param name="textContent" select="oereb:extractMultilingualText(data:Disclaimer[2]/data:Content/data:LocalisedText, $locale)" />
                                </xsl:call-template>
                              </fo:block>
                              <!-- Ein wenig handgestrickt. Normalerweise gibt es drei Disclamer. Wie macht man beliebig viele Disclamer?
                              Man würde wohl eine Loop machen. Müsste irgendwie links/rechts-fähig sein.
                              Fürs erste reicht es so. (Anforderung Kt. SH) -->
                              <xsl:if test="data:Disclaimer[3]/data:Title/data:LocalisedText">
                                <fo:block  margin-top="2.2mm" font-weight="700">
                                  <xsl:value-of select="oereb:extractMultilingualText(data:Disclaimer[3]/data:Title/data:LocalisedText, $locale)"/>
                                </fo:block>
                                <fo:block>
                                  <xsl:call-template name="textRenderer">
                                    <xsl:with-param name="textContent" select="oereb:extractMultilingualText(data:Disclaimer[3]/data:Content/data:LocalisedText, $locale)" />
                                  </xsl:call-template>
                                </fo:block>
                              </xsl:if>
                              <fo:block margin-top="2.2mm">
                                <xsl:if test="data:QRCode">
                                  <fo:table table-layout="fixed" width="100%">
                                    <fo:table-column column-width="35mm"/>
                                    <fo:table-column column-width="40mm"/>
                                    <fo:table-body>
                                      <fo:table-row vertical-align="top">
                                        <fo:table-cell>
                                          <fo:block font-weight="700">
                                            <xsl:value-of select="$localeXml/data[@name='ContentPage.QRCode']/value/text()"/>
                                          </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell padding-left="4mm">
                                          <fo:block>
                                            <fo:external-graphic width="20mm" height="20mm" content-width="scale-to-fit" content-height="scale-to-fit" fox:alt-text="QRCode">
                                              <xsl:attribute name="src">
                                                <xsl:text>url('data:</xsl:text>
                                                <xsl:text>image/png;base64,</xsl:text>
                                                <xsl:value-of select="data:QRCode"/>
                                                <xsl:text>')</xsl:text>
                                              </xsl:attribute>
                                            </fo:external-graphic>
                                          </fo:block>
                                        </fo:table-cell>
                                      </fo:table-row>
                                    </fo:table-body>
                                  </fo:table>
                                </xsl:if>
                              </fo:block>
                            </fo:table-cell>
                          </fo:table-row>
                        </fo:table-body>
                      </fo:table>
                      <fo:block/>
                    </fo:block-container>
                  </fo:block>
                </fo:footnote-body>
              </fo:footnote>
            </fo:block>
          </fo:block-container>
        </fo:flow>
      </fo:page-sequence>
      <xsl:apply-templates select="data:RealEstate" mode="sheet"/>
      <xsl:call-template name="insertGlossary"/>
    </fo:root>
  </xsl:template>

  <xsl:template match="data:RealEstate" mode="sheet">
    <xsl:if test="(../data:ConcernedTheme)">
      <fo:page-sequence master-reference="mainPage" id="page-sequence-id">
        <xsl:call-template name="insertHeaderAndFooter"/>
        <fo:flow flow-name="xsl-region-body">
          <fo:block>
            <xsl:for-each-group select="data:RestrictionOnLandownership" group-by="data:Theme/data:Code">

                <xsl:if test="current-group()/data:Theme/data:SubCode">
                  <xsl:for-each-group select="current-group()" group-by="data:Theme/data:SubCode">
                    <xsl:for-each-group select="current-group()" group-by="data:Lawstatus/data:Code">
                      <xsl:sort data-type="number" order="ascending" select="(number(data:Lawstatus/data:Code='inForce') * 1) + (number(data:Lawstatus/data:Code='changeWithPreEffect') * 2) + (number(data:Lawstatus/data:Code='changeWithoutPreEffect') * 3)"/>

                      <fo:block-container background-color="transparent">
                        <fo:block id="{generate-id()}" page-break-before="always" linefeed-treatment="preserve" font-weight="700" font-size="15pt" line-height="18pt">
                          <xsl:value-of select="oereb:extractMultilingualText(data:Theme/data:Text/data:LocalisedText, $locale)"/>
                        </fo:block>
                      </fo:block-container>
                      <fo:block-container>
                        <!-- 2mm sind circa. 3mm sind bereits vorhanden. Kann nicht (?) besser gesteuert werden.-->
                        <fo:block margin-top="1mm" font-size="8pt" line-height="11pt" font-weight="700" background-color="transparent">
                          <xsl:value-of select="oereb:extractMultilingualText(data:Lawstatus/data:Text/data:LocalisedText, $locale)"/>
                        </fo:block>
                      </fo:block-container>
                      <xsl:call-template name="handleRestrictionOnLandownership"/>
                    </xsl:for-each-group>
                  </xsl:for-each-group>
                </xsl:if>

                <xsl:if test="not(current-group()/data:Theme/data:SubCode)">



                    <xsl:for-each-group select="current-group()" group-by="data:Lawstatus/data:Code">
                      <xsl:sort data-type="number" order="ascending" select="(number(data:Lawstatus/data:Code='inForce') * 1) + (number(data:Lawstatus/data:Code='changeWithPreEffect') * 2) + (number(data:Lawstatus/data:Code='changeWithoutPreEffect') * 3)"/>

<!--
    <xsl:message>??fubar</xsl:message>  
    <xsl:message><xsl:value-of select="current-group()/data:Theme"/></xsl:message>  
    <xsl:message><xsl:value-of select="count(current-group())"/></xsl:message>  
-->
                      <fo:block-container background-color="transparent">
                        <fo:block id="{generate-id()}" page-break-before="always" linefeed-treatment="preserve" font-weight="700" font-size="15pt" line-height="18pt">
                          <xsl:value-of select="oereb:extractMultilingualText(data:Theme/data:Text/data:LocalisedText, $locale)"/>
                        </fo:block>
                      </fo:block-container>
                      <fo:block-container>
                        <fo:block margin-top="1mm" font-size="8pt" line-height="11pt" font-weight="700" background-color="transparent">
                          <xsl:value-of select="oereb:extractMultilingualText(data:Lawstatus/data:Text/data:LocalisedText, $locale)"/>
                        </fo:block>
                      </fo:block-container>
                      <xsl:call-template name="handleRestrictionOnLandownership"/>
                  </xsl:for-each-group>
                </xsl:if>

              </xsl:for-each-group>
          </fo:block>
        </fo:flow>
      </fo:page-sequence>
    </xsl:if>
  </xsl:template>

  <xsl:template match="data:RealEstate" mode="toc">
    <fo:block-container margin-bottom="10mm" font-size="8pt" background-color="transparent">
      <fo:block>
        <xsl:choose>
          <xsl:when test="(../data:ConcernedTheme)">
            <fo:table table-layout="fixed" width="100%">
              <fo:table-column column-width="7mm"/>
              <fo:table-column column-width="167mm"/>
              <fo:table-body>
                <fo:table-row vertical-align="middle">
                  <fo:table-cell>
                    <fo:block margin-top="1mm" margin-bottom="1.8mm" font-weight="700" font-size="6pt">
                      <xsl:value-of select="$localeXml/data[@name='Page']/value/text()"/>
                    </fo:block>
                  </fo:table-cell>
                  <fo:table-cell>
                    <fo:block/>
                  </fo:table-cell>
                </fo:table-row>
                <xsl:for-each-group select="data:RestrictionOnLandownership" group-by="data:Theme/data:Code">
                    
                    <xsl:if test="current-group()/data:Theme/data:SubCode">
                      <xsl:for-each-group select="current-group()" group-by="data:Theme/data:SubCode">
                        <xsl:for-each-group select="current-group()" group-by="data:Lawstatus/data:Code">
                          <xsl:sort data-type="number" order="ascending" select="(number(data:Lawstatus/data:Code='inForce') * 1) + (number(data:Lawstatus/data:Code='changeWithPreEffect') * 2) + (number(data:Lawstatus/data:Code='changeWithoutPreEffect') * 3)"/>

                          <fo:table-row line-height="5mm" border-bottom="0.2pt solid black" vertical-align="middle">
                            <fo:table-cell>
                              <fo:block>
                                <fo:basic-link internal-destination="{generate-id(.)}">
                                  <fo:page-number-citation ref-id="{generate-id(.)}"/>
                                </fo:basic-link>
                              </fo:block>
                            </fo:table-cell>
                            <fo:table-cell>
                              <fo:block>
                                <fo:basic-link internal-destination="{generate-id(.)}">
                                  <xsl:value-of select="oereb:extractMultilingualText(data:Theme/data:Text/data:LocalisedText, $locale)"/>
                                  <xsl:if test="not(data:Lawstatus/data:Code = 'inForce')">
                                    <xsl:text> (</xsl:text>
                                    <xsl:value-of select="oereb:extractMultilingualText(data:Lawstatus/data:Text/data:LocalisedText, $locale)"></xsl:value-of>
                                    <xsl:text>)</xsl:text>
                                  </xsl:if>
                                </fo:basic-link>
                              </fo:block>
                            </fo:table-cell>
                          </fo:table-row>

                        </xsl:for-each-group>
                      </xsl:for-each-group>
                    </xsl:if>

                    <xsl:if test="not(current-group()/data:Theme/data:SubCode)">
                      <xsl:for-each-group select="current-group()" group-by="data:Lawstatus/data:Code">
                        <xsl:sort data-type="number" order="ascending" select="(number(data:Lawstatus/data:Code='inForce') * 1) + (number(data:Lawstatus/data:Code='changeWithPreEffect') * 2) + (number(data:Lawstatus/data:Code='changeWithoutPreEffect') * 3)"/>

                        <fo:table-row line-height="6mm" border-bottom="0.2pt solid black" vertical-align="middle">
                          <fo:table-cell>
                            <fo:block>
                              <fo:basic-link internal-destination="{generate-id(.)}">
                                <fo:page-number-citation ref-id="{generate-id(.)}"/>
                              </fo:basic-link>
                            </fo:block>
                          </fo:table-cell>
                          <fo:table-cell>
                            <fo:block>
                              <fo:basic-link internal-destination="{generate-id(.)}">
                                <xsl:value-of select="oereb:extractMultilingualText(data:Theme/data:Text/data:LocalisedText, $locale)"/>
                                <xsl:if test="not(data:Lawstatus/data:Code = 'inForce')">
                                  <xsl:text> (</xsl:text>
                                  <xsl:value-of select="oereb:extractMultilingualText(data:Lawstatus/data:Text/data:LocalisedText, $locale)"></xsl:value-of>
                                  <xsl:text>)</xsl:text>
                                </xsl:if>
                              </fo:basic-link>
                            </fo:block>
                          </fo:table-cell>
                        </fo:table-row>

                      </xsl:for-each-group>
                    </xsl:if>

                </xsl:for-each-group>

              </fo:table-body>
            </fo:table>
          </xsl:when>
          <xsl:otherwise>
            <fo:block margin-top="1mm" margin-bottom="2.8mm" font-weight="400" font-size="6.5pt">–</fo:block>
          </xsl:otherwise>
        </xsl:choose>
      </fo:block>
    </fo:block-container>
  </xsl:template>

  <xsl:template name="handleRestrictionOnLandownership">
    
<!--
    <xsl:message>??fubar</xsl:message>  
    <xsl:message><xsl:value-of select="count(current-group())"/></xsl:message>  
    <xsl:for-each-group select="current-group()" group-by="data:TypeCode">
      <xsl:message>TypeCode: <xsl:value-of select="data:TypeCode"/></xsl:message>  
    </xsl:for-each-group>
-->

    <!-- margin-top: heuristisch -->
    <fo:block-container margin-top="4mm" height="105mm" background-color="transparent">
      <!-- Das funktioniert nicht, wenn es in einem Thema unterschiedliche Bilder gibt.
	    Das kann vorkommen, wenn die Bilder z.B. von WMS-Requests mit unterschliedlichen
	    Layern stammen. Bei uns wäre das bei der "Nutzungsplanung überlagernd" der Fall. 
	    Diese besteht aus drei Einzellayer. Wir sprechen diese Subthema aber mit dem 
	    Gruppenlayer-Namen an. -->
      <!-- Zu Beginn von V2_0 war ich nicht sicher, ob überhaupt mehrere Images/Nodes zu 
      behandeln sind. Scheint aber doch der Fall zu sein.
      Morgen danach: Nein, es kann nur immer ein Image/Node sein. Ich lass aber den 
      Code und die Logik (auch in der Extension Function) wie in V1_0 bis auf 
      Weiteres.-->
      <!-- Wenn wir hier die current-group() wiederum gruppieren und zwar gruppiert natürlich
      dem Image, gibt es logischerweiser nur noch ein Image, d.h. es darf nur noch ein Image
      geben. Aus diesem Grund könnte man den Code wohl schon vereinfachen. (Aber Achtung: das 
      Image alleine reicht nicht, ich brauche layerIndex und Opazität.)
      Das Ganze funktioniert abr wie im ersten Kommentar erwähnt nur, wenn es unterschiedliche 
      Bilder sind, z.B. für jede Grundnutzung nur die jeweilige Grundnutzung etc. Dann muss aber 
      zwingen alles andere transparent sein. Die unterschiedlichen Verhalten könnte man wohl 
      schon generisch lösen. Dieser Code hier (inkl. Extension Function) müsste aber angepasst 
      werden.
      -->

      <!-- Blobs verwenden und group by Blobs. Siehe Kommentare oben: Die Blobs (also die Base64-Strings müssen identisch sein.)-->
      <xsl:if test="data:Map/data:Image and not(data:Map/data:ReferenceWMS)">
        <!--<xsl:message>Use Blob...</xsl:message>-->

        <xsl:for-each-group select="current-group()" group-by="data:Map/data:Image">
          <xsl:sort order="ascending" select="data:Information/data:LocalisedText/data:Text"/>

          <!--<xsl:message>You may see this only one per (Sub)theme...</xsl:message>-->

          <fo:block font-size="0pt" padding="0mm" margin="0mm" line-height="0mm">
            <fo:external-graphic border="0.4pt solid black" width="174mm" height="99mm" scaling="uniform" content-width="scale-to-fit" content-height="scale-to-fit" fox:alt-text="RestrictionOnLandownershipImage">
              <xsl:attribute name="src">
                <xsl:text>url('data:</xsl:text>
                <xsl:text>image/png;base64,</xsl:text>
                  <xsl:value-of select="oereb:createRestrictionOnLandownershipImages(data:Map, ../data:PlanForLandRegister, $OverlayImage, $locale)"/>
                <xsl:text>')</xsl:text>
              </xsl:attribute>
            </fo:external-graphic>
          </fo:block>
        </xsl:for-each-group>
      </xsl:if>

      <!-- Blobs verwenden aber group by WMS-Url. Siehe Kommentare oben. Ist im Kanton SH der Fall. Noch nicht ganz klar warum. Die WMS-Url (die 
      unterwegs vom oereb-web-service syntaktisch (?) gefixed werden) sind identisch. Die Bilder aber nicht. Die Bilder wiederum werden mit den
      gefixede URL hergestellt im oereb-web-service.-->
      <xsl:if test="data:Map/data:Image and data:Map/data:ReferenceWMS">
        <!--<xsl:message>Use Blob but group by ReferenceWMS</xsl:message>-->

        <xsl:for-each-group select="current-group()" group-by="data:Map/data:ReferenceWMS">
          <xsl:sort order="ascending" select="data:Information/data:LocalisedText/data:Text"/>

          <!--<xsl:message>You may see this only one per (Sub)theme...</xsl:message>-->

          <fo:block font-size="0pt" padding="0mm" margin="0mm" line-height="0mm">
            <fo:external-graphic border="0.4pt solid black" width="174mm" height="99mm" scaling="uniform" content-width="scale-to-fit" content-height="scale-to-fit" fox:alt-text="RestrictionOnLandownershipImage">
              <xsl:attribute name="src">
                <xsl:text>url('data:</xsl:text>
                <xsl:text>image/png;base64,</xsl:text>
                  <xsl:value-of select="oereb:createRestrictionOnLandownershipImages(data:Map, ../data:PlanForLandRegister, $OverlayImage, $locale)"/>
                <xsl:text>')</xsl:text>
              </xsl:attribute>
            </fo:external-graphic>
          </fo:block>
        </xsl:for-each-group>
      </xsl:if>

      <!-- WMS-Url verwenden für das Herstellen der Bilder und gruppieren. -->
      <xsl:if test="data:Map/data:ReferenceWMS and not(data:Map/data:Image)">
        <!-- Funktioniert nur, wenn die GetMap-Requests syntaktisch identisch sind. -->
        <!-- Siehe auch Kommentar oben. -->
        <!--<xsl:message>Use ReferenceWMS...</xsl:message>-->

        <xsl:for-each-group select="current-group()" group-by="data:Map/data:ReferenceWMS">
          <xsl:sort order="ascending" select="data:Information/data:LocalisedText/data:Text"/>

          <!--<xsl:message>You may see this only one per (Sub)theme...</xsl:message>-->

          <fo:block font-size="0pt" padding="0mm" margin="0mm" line-height="0mm">
            <fo:external-graphic border="0.4pt solid black" width="174mm" height="99mm" scaling="uniform" content-width="scale-to-fit" content-height="scale-to-fit" fox:alt-text="RestrictionOnLandownershipImage">
              <xsl:attribute name="src">
                <xsl:text>url('data:</xsl:text>
                <xsl:text>image/png;base64,</xsl:text>
                  <xsl:value-of select="oereb:createRestrictionOnLandownershipImages(data:Map, ../data:PlanForLandRegister, $OverlayImage, $locale)"/>
                <xsl:text>')</xsl:text>
              </xsl:attribute>
            </fo:external-graphic>
          </fo:block>
        </xsl:for-each-group>
      </xsl:if>
    </fo:block-container>
    <fo:block-container font-size="8pt" background-color="transparent">
      <fo:table table-layout="fixed" width="100%">
        <fo:table-column column-width="68mm"/>
        <fo:table-column column-width="10mm"/>
        <fo:table-column column-width="59mm"/>
        <fo:table-column column-width="20mm"/>
        <fo:table-column column-width="17mm"/>
        <fo:table-body>
          <fo:table-row border-bottom="0.2pt solid black" vertical-align="middle" line-height="5mm">
            <fo:table-cell>
              <fo:block/>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block/>
            </fo:table-cell>
            <fo:table-cell text-align="left">
              <fo:block font-size="6pt">
                <xsl:value-of select="$localeXml/data[@name='RestrictionPage.Type']/value/text()"/>
              </fo:block>
            </fo:table-cell>
            <fo:table-cell text-align="right">
              <fo:block font-size="6pt">
                <xsl:value-of select="$localeXml/data[@name='RestrictionPage.Share']/value/text()"/>
              </fo:block>
            </fo:table-cell>
            <fo:table-cell text-align="right">
              <fo:block font-size="6pt">
                <xsl:value-of select="$localeXml/data[@name='RestrictionPage.PartInPercent']/value/text()"/>
              </fo:block>
            </fo:table-cell>
          </fo:table-row>
          <fo:table-row min-height="1.5mm">
            <fo:table-cell>
              <fo:block/>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block/>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block/>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block/>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block/>
            </fo:table-cell>
          </fo:table-row>
          <!-- Die Gruppierung ist exotisch bis gewagt. Auslöser ist der Umstand, dass die Kombiniation
	        TypeCode und TypeCodeList nicht eindeutig ist. In V1_0 wird erwähnt, dass das bei uns in der NPL
          bei geschützten Objekten (N820/N821) der Fall ist. Diese unterscheiden sich aber in der Artcodeliste.
          Jedoch sind nun die KbS des Bundes problematisch, da dort Punkt- und Flächengeometrien mit identischem
          Artcode und Artcodeliste geliefert werden. Im Gegensatz zu V1_0 sind die Symbole nun unterschiedlich.

	        Aus diesem Grund muss der Geometrietyp beim Gruppieren mitberücksichtigt werden. Das erreicht man,
	        indem man prüft, ob AreaShare, LengthShare oder NrOfPoints-Elemente vorhanden sind (nicht deren 
	        Werte).
	         -->
          <xsl:for-each-group select="current-group()" group-by="concat(data:TypeCode, data:TypeCodeList, string(exists(data:AreaShare)), string(exists(data:LengthShare)), string(exists(data:NrOfPoints)))">
            <xsl:sort lang="de" order="ascending" select="data:Information/data:LocalisedText/data:Text"/>
            <!--<fo:block linefeed-treatment="preserve" font-weight="400" font-size="11pt" font-family="Cadastra"><xsl:value-of select="data:Information/data:LocalisedText/data:Text"/></fo:block>-->
            <fo:table-row vertical-align="middle" line-height="5mm">
              <fo:table-cell>
                <xsl:if test="position()=1">
                  <fo:block font-weight="700">
                    <xsl:value-of select="$localeXml/data[@name='RestrictionPage.Legend']/value/text()"/>
                  </fo:block>
                </xsl:if>
                <xsl:if test="position()!=1">
                  <fo:block/>
                </xsl:if>
              </fo:table-cell>
              <fo:table-cell display-align="before">
                <fo:block font-size="0pt" padding="0.5mm" margin="0mm" line-height="0mm">
                  <fo:external-graphic width="6mm" height="3mm" content-width="scale-to-fit" content-height="scale-to-fit" scaling="uniform" fox:alt-text="Symbol">
                    <xsl:if test="data:Symbol">
                      <xsl:attribute name="src">
                        <xsl:text>url('data:</xsl:text>
                        <xsl:text>image/png;base64,</xsl:text>
                        <xsl:value-of select="oereb:fixImage(data:Symbol)"/>
                        <xsl:text>')</xsl:text>
                      </xsl:attribute>
                    </xsl:if>
                    <xsl:if test="data:SymbolRef and not(data:Symbol)">
                      <xsl:attribute name="src">
                        <xsl:text>url('data:</xsl:text>
                        <xsl:text>image/png;base64,</xsl:text>
                        <xsl:value-of select="oereb:fixImage(data:SymbolRef)"/>
                        <xsl:text>')</xsl:text>
                      </xsl:attribute>
                    </xsl:if>
                  </fo:external-graphic>
                </fo:block>
              </fo:table-cell>
              <fo:table-cell display-align="center" text-align="left" line-height="11pt">
                <fo:block>
                  <xsl:value-of select="oereb:extractMultilingualText(data:LegendText/data:LocalisedText, $locale)"/>
                </fo:block>
                <!-- Zum Testen der Summenbildung und Gruppierung-->
                <!--<xsl:message>TypeCode: <xsl:value-of select="data:TypeCode"/></xsl:message>-->
              </fo:table-cell>
              <fo:table-cell text-align="right">
                <xsl:if test="data:AreaShare">
                  <fo:block line-height-shift-adjustment="disregard-shifts">
                    <xsl:choose>
                      <xsl:when test="sum(current-group()/data:AreaShare) &lt; 0.1">
                        <xsl:text>&lt; 0.1</xsl:text>
                      </xsl:when>
                      <xsl:otherwise>
                        <xsl:value-of select="format-number(sum(current-group()/data:AreaShare), &quot;#'###&quot;, &quot;swiss&quot;)"/>
                      </xsl:otherwise>
                    </xsl:choose>
                    <xsl:text> m</xsl:text>
                    <fo:inline baseline-shift="super" font-size="60%">2</fo:inline>
                  </fo:block>
                </xsl:if>
                <xsl:if test="data:LengthShare">
                  <fo:block line-height-shift-adjustment="disregard-shifts">
                    <xsl:choose>
                      <xsl:when test="sum(current-group()/data:LengthShare) &lt; 0.1">
                        <xsl:text>&lt; 0.1</xsl:text>
                      </xsl:when>
                      <xsl:otherwise>
                        <xsl:value-of select="format-number(sum(current-group()/data:LengthShare), &quot;#'###&quot;, &quot;swiss&quot;)"/>
                      </xsl:otherwise>
                    </xsl:choose>
                    <xsl:text> m</xsl:text>
                  </fo:block>
                </xsl:if>
                <xsl:if test="data:NrOfPoints">
                  <fo:block line-height-shift-adjustment="disregard-shifts">
                    <xsl:value-of select="format-number(sum(current-group()/data:NrOfPoints), &quot;#'###&quot;, &quot;swiss&quot;)"/> Elemente
                  </fo:block>
                </xsl:if>
                <fo:block/>
              </fo:table-cell>
              <fo:table-cell text-align="right">
                <xsl:if test="data:PartInPercent">
                  <fo:block>
                    <xsl:choose>
                      <xsl:when test="sum(current-group()/data:PartInPercent) &lt; 0.1">
                        <xsl:text>&lt; 0.1</xsl:text>
                      </xsl:when>
                      <xsl:otherwise>
                        <xsl:value-of select="format-number(sum(current-group()/data:PartInPercent), &quot;#'##0.0&quot;, &quot;swiss&quot;)"/>
                      </xsl:otherwise>
                    </xsl:choose>
                    <xsl:text>%</xsl:text>
                  </fo:block>
                </xsl:if>
                <fo:block/>
              </fo:table-cell>
            </fo:table-row>
          </xsl:for-each-group>
          <fo:table-row min-height="1mm">
            <fo:table-cell>
              <fo:block/>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block/>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block/>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block/>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block/>
            </fo:table-cell>
          </fo:table-row> 
        </fo:table-body>
      </fo:table>
    </fo:block-container>
    <xsl:variable name="restrictionTypeCode" select="../data:RestrictionOnLandownership/data:TypeCode"/>
    <xsl:variable name="restrictionTypeCodelist" select="../data:RestrictionOnLandownership/data:TypeCodelist"/>
    <xsl:if test="current-group()/data:Map/data:OtherLegend[not(data:TypeCode = $restrictionTypeCode and data:TypeCodelist = $restrictionTypeCodelist)]">
      <fo:block-container font-size="8pt" background-color="transparent">
        <fo:table table-layout="fixed" width="100%">
          <fo:table-column column-width="68mm"/>
          <fo:table-column column-width="10mm"/>
          <fo:table-column column-width="59mm"/>
          <fo:table-column column-width="20mm"/>
          <fo:table-column column-width="17mm"/>
          <fo:table-body>
            <fo:table-row min-height="1mm" border-top="0.2pt solid black">
              <fo:table-cell>
                <fo:block/>
              </fo:table-cell>
              <fo:table-cell>
                <fo:block/>
              </fo:table-cell>
              <fo:table-cell>
                <fo:block/>
              </fo:table-cell>
              <fo:table-cell>
                <fo:block/>
              </fo:table-cell>
              <fo:table-cell>
                <fo:block/>
              </fo:table-cell>
            </fo:table-row>
            <!-- Siehe Kommentare in V1_0. Die Anforderungen sind tricky.-->
            <!-- In V1_0 habe ich als Kriterum statt des Typcodes den Text verwendet. Wahrscheinlich weil die Symbole bei uns aggregiert sind. Momentan finde
            ich es aber sinnvoller den Artcode zu verwenden. -->
            <!-- Die Variablen sind bereits weiter oben definiert, damit überhaupt nicht ein block-container mit Linie gezeichnet wird, wenn es gar keine Einträge gibt.-->
            <xsl:for-each-group select="current-group()/data:Map/data:OtherLegend[not(data:TypeCode = $restrictionTypeCode and data:TypeCodelist = $restrictionTypeCodelist)]" group-by="concat(data:TypeCode, '|', data:TypeCodelist, '|', data:Symbol)">
              <xsl:sort lang="de" order="ascending" select="oereb:extractMultilingualText(data:LegendText/data:LocalisedText, $locale)"/>
              <fo:table-row vertical-align="middle" line-height="5mm">
                <fo:table-cell>
                  <xsl:if test="position()=1">
                    <fo:block font-weight="700">
                      <xsl:value-of select="$localeXml/data[@name='RestrictionPage.OtherLegend']/value/text()"/>
                    </fo:block>
                  </xsl:if>
                  <xsl:if test="position()!=1">
                    <fo:block/>
                  </xsl:if>
                </fo:table-cell>
                <fo:table-cell display-align="before">
                  <fo:block font-size="0pt" padding="0.5mm" margin="0mm" line-height="0mm">
                    <fo:external-graphic width="6mm" height="3mm" content-width="scale-to-fit" content-height="scale-to-fit" scaling="uniform" fox:alt-text="Symbol">
                      <xsl:if test="data:Symbol">
                        <xsl:attribute name="src">
                          <xsl:text>url('data:</xsl:text>
                          <xsl:text>image/png;base64,</xsl:text>
                          <xsl:value-of select="oereb:fixImage(data:Symbol)"/>
                          <xsl:text>')</xsl:text>
                        </xsl:attribute>
                      </xsl:if>
                      <xsl:if test="data:SymbolRef and not(data:Symbol)">
                        <xsl:attribute name="src">
                          <xsl:text>url('data:</xsl:text>
                          <xsl:text>image/png;base64,</xsl:text>
                          <xsl:value-of select="oereb:fixImage(data:SymbolRef)"/>
                          <xsl:text>')</xsl:text>
                        </xsl:attribute>
                      </xsl:if>
                    </fo:external-graphic>
                  </fo:block>
                </fo:table-cell>
                <fo:table-cell display-align="center" text-align="left" line-height="11pt">
                  <fo:block>
                    <xsl:value-of select="oereb:extractMultilingualText(data:LegendText/data:LocalisedText, $locale)"/>
                  </fo:block>
                </fo:table-cell>
                <fo:table-cell text-align="right">
                  <!-- This is just to get the same line height as in the table above. -->
                  <fo:block line-height-shift-adjustment="disregard-shifts" color="white" visibility="hidden">
                    <xsl:text>m</xsl:text>
                    <fo:inline baseline-shift="super" font-size="60%">
                      <xsl:text> </xsl:text>
                    </fo:inline>
                  </fo:block>
                </fo:table-cell>
                <fo:table-cell text-align="right">
                  <fo:block/>
                </fo:table-cell>
              </fo:table-row>
            </xsl:for-each-group>
            <fo:table-row min-height="1mm" border-bottom="0.2pt solid black">
              <fo:table-cell>
                <fo:block/>
              </fo:table-cell>
              <fo:table-cell>
                <fo:block/>
              </fo:table-cell>
            </fo:table-row>
          </fo:table-body>
        </fo:table>
      </fo:block-container>
    </xsl:if>  
    <fo:block-container font-weight="400" font-size="8pt" background-color="transparent">
      <fo:table table-layout="fixed" width="100%">
        <fo:table-column column-width="68mm"/>
        <fo:table-column column-width="106mm"/>
        <fo:table-body>
          <fo:table-row min-height="1mm" border-top="0.2pt solid black">
            <fo:table-cell>
              <fo:block/>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block/>
            </fo:table-cell>
          </fo:table-row>
          <xsl:for-each-group select="current-group()/data:LegalProvisions[data:Type/data:Code='LegalProvision']" group-by="oereb:extractMultilingualText(data:TextAtWeb/data:LocalisedText, $locale)">
            <xsl:sort lang="de" order="ascending" select="data:Index"/>
            <xsl:sort lang="de" order="ascending" select="oereb:extractMultilingualText(data:Title/data:LocalisedText, $locale)"/>
            <fo:table-row font-weight="400">
              <fo:table-cell padding-top="1.5pt">
                <xsl:if test="position()=1">
                  <fo:block font-weight="700">
                    <xsl:value-of select="$localeXml/data[@name='RestrictionPage.LegalProvision']/value/text()"/>
                  </fo:block>
                </xsl:if>
                <xsl:if test="position()!=1">
                  <fo:block/>
                </xsl:if>
              </fo:table-cell>
              <fo:table-cell padding-top="1.5pt" padding-bottom="1.5pt">
                <fo:block font-size="8pt" line-height="11pt">
                  <xsl:choose>
                    <xsl:when test="data:OfficialNumber"><xsl:value-of select="oereb:extractMultilingualText(data:Title/data:LocalisedText, $locale)"/>, <xsl:value-of select="oereb:extractMultilingualText(data:OfficialNumber/data:LocalisedText, $locale)"/><xsl:text>:</xsl:text></xsl:when>
                    <xsl:otherwise>
                      <xsl:value-of select="oereb:extractMultilingualText(data:Title/data:LocalisedText, $locale)"/>
                      <xsl:text>:</xsl:text>
                    </xsl:otherwise>
                  </xsl:choose>
                </fo:block>
                <fo:block font-size="6pt" line-height="8pt" margin-left="3mm" margin-top="0mm">
                  <fo:basic-link text-decoration="none" color="rgb(76,143,186)">
                    <xsl:attribute name="external-destination">
                      <xsl:value-of select="oereb:decodeURL(oereb:extractMultilingualText(data:TextAtWeb/data:LocalisedText, $locale))"/>
                    </xsl:attribute>
                    <!-- https://stackoverflow.com/questions/4350788/xsl-fo-force-wrap-on-table-entries/33689540#33689540 -->
                    <xsl:value-of select="replace(replace(oereb:decodeURL(oereb:extractMultilingualText(data:TextAtWeb/data:LocalisedText, $locale)), '(\P{Zs}{13})', '$1​'), '​(\p{Zs})','$1')"/>
                  </fo:basic-link>
                </fo:block>
              </fo:table-cell>
            </fo:table-row>
          </xsl:for-each-group>
          <fo:table-row min-height="1mm" border-bottom="0.2pt solid black">
            <fo:table-cell>
              <fo:block/>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block/>
            </fo:table-cell>
          </fo:table-row>
        </fo:table-body>
      </fo:table>
    </fo:block-container>
    <fo:block-container font-weight="400" font-size="8pt" background-color="transparent">
      <fo:table table-layout="fixed" width="100%">
        <fo:table-column column-width="68mm"/>
        <fo:table-column column-width="106mm"/>
        <fo:table-body>
          <fo:table-row min-height="1mm" border-top="0.2pt solid black">
            <fo:table-cell>
              <fo:block/>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block/>
            </fo:table-cell>
          </fo:table-row>
          <xsl:for-each-group select="current-group()/data:LegalProvisions[data:Type/data:Code='Law']" group-by="oereb:extractMultilingualText(data:TextAtWeb/data:LocalisedText, $locale)">
            <xsl:sort lang="de" order="ascending" select="data:Index"/>
            <xsl:sort lang="de" order="ascending" select="oereb:extractMultilingualText(data:Title/data:LocalisedText, $locale)"/>
            <fo:table-row font-weight="400">
              <fo:table-cell background-color="transparent" padding-top="1.5pt">
                <xsl:if test="position()=1">
                  <fo:block font-weight="700">
                    <xsl:value-of select="$localeXml/data[@name='RestrictionPage.Law']/value/text()"/>
                  </fo:block>
                </xsl:if>
                <xsl:if test="position()!=1">
                  <fo:block/>
                </xsl:if>
              </fo:table-cell>
              <fo:table-cell background-color="transparent" padding-top="1.5pt" padding-bottom="1.5pt">
                <fo:block font-size="8pt" line-height="11pt">
                  <xsl:value-of select="oereb:extractMultilingualText(data:Title/data:LocalisedText, $locale)"/>
                  <xsl:if test="oereb:extractMultilingualText(data:Abbreviation/data:LocalisedText, $locale)">
                    <xsl:text> (</xsl:text>
                    <xsl:value-of select="oereb:extractMultilingualText(data:Abbreviation/data:LocalisedText, $locale)"/>
                    <xsl:text>)</xsl:text>
                  </xsl:if>
                  <xsl:if test="oereb:extractMultilingualText(data:OfficialNumber/data:LocalisedText, $locale)">
                    <xsl:text>, </xsl:text>
                    <xsl:value-of select="oereb:extractMultilingualText(data:OfficialNumber/data:LocalisedText, $locale)"/>
                  </xsl:if>
                  <xsl:text>:</xsl:text>
                </fo:block>
                <fo:block font-size="6pt" line-height="8pt" margin-left="3mm" margin-top="0mm">
                  <fo:basic-link text-decoration="none" color="rgb(76,143,186)">
                    <xsl:attribute name="external-destination">
                      <xsl:value-of select="oereb:decodeURL(oereb:extractMultilingualText(data:TextAtWeb/data:LocalisedText, $locale))"/>
                    </xsl:attribute>
                    <xsl:value-of select="replace(replace(oereb:decodeURL(oereb:extractMultilingualText(data:TextAtWeb/data:LocalisedText, $locale)), '(\P{Zs}{13})', '$1​'), '​(\p{Zs})','$1')"/>
                  </fo:basic-link>
                </fo:block>
              </fo:table-cell>
            </fo:table-row>
          </xsl:for-each-group>
          <fo:table-row min-height="1mm" border-bottom="0.2pt solid black">
            <fo:table-cell>
              <fo:block/>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block/>
            </fo:table-cell>
          </fo:table-row>
        </fo:table-body>
      </fo:table>
    </fo:block-container>
    <xsl:if test="current-group()/data:LegalProvisions[data:Type/data:Code='Hint']">
      <fo:block-container font-weight="400" font-size="8pt" background-color="transparent">
        <fo:table table-layout="fixed" width="100%">
          <fo:table-column column-width="68mm"/>
          <fo:table-column column-width="106mm"/>
          <fo:table-body>
            <fo:table-row min-height="1mm" border-top="0.2pt solid black" vertical-align="middle" line-height="5mm">
              <fo:table-cell>
                <fo:block/>
              </fo:table-cell>
              <fo:table-cell>
                <fo:block/>
              </fo:table-cell>
            </fo:table-row>
            <xsl:for-each-group select="current-group()/data:LegalProvisions[data:Type/data:Code='Hint']" group-by="oereb:extractMultilingualText(data:TextAtWeb/data:LocalisedText, $locale)">
              <xsl:sort lang="de" order="ascending" select="data:Index"/>
              <xsl:sort lang="de" order="ascending" select="oereb:extractMultilingualText(data:Title/data:LocalisedText, $locale)"/>
              <fo:table-row vertical-align="middle" line-height="5mm" font-weight="400">
                <fo:table-cell>
                  <xsl:if test="position()=1">
                    <fo:block font-weight="700">
                      <xsl:value-of select="$localeXml/data[@name='RestrictionPage.Hint']/value/text()"/>
                    </fo:block>
                  </xsl:if>
                  <xsl:if test="position()!=1">
                    <fo:block/>
                  </xsl:if>
                </fo:table-cell>
                <fo:table-cell display-align="center">
                  <fo:block font-size="8pt" line-height="11pt">
                    <xsl:value-of select="oereb:extractMultilingualText(data:Title/data:LocalisedText, $locale)"/>
                    <xsl:text>:</xsl:text>
                  </fo:block>
                  <fo:block font-size="6pt" line-height="8pt" margin-left="3mm" margin-top="0mm">
                    <fo:basic-link text-decoration="none" color="rgb(76,143,186)">
                      <xsl:attribute name="external-destination">
                        <xsl:value-of select="oereb:decodeURL(oereb:extractMultilingualText(data:TextAtWeb/data:LocalisedText, $locale))"/>
                      </xsl:attribute>
                      <xsl:value-of select="replace(replace(oereb:decodeURL(oereb:extractMultilingualText(data:TextAtWeb/data:LocalisedText, $locale)), '(\P{Zs}{13})', '$1​'), '​(\p{Zs})','$1')"/>
                    </fo:basic-link>
                  </fo:block>
                </fo:table-cell>
              </fo:table-row>
            </xsl:for-each-group>
            <fo:table-row min-height="1mm" border-bottom="0.2pt solid black" vertical-align="middle" line-height="5mm">
              <fo:table-cell>
                <fo:block/>
              </fo:table-cell>
              <fo:table-cell>
                <fo:block/>
              </fo:table-cell>
            </fo:table-row>
          </fo:table-body>
        </fo:table>
      </fo:block-container>
    </xsl:if>
    <fo:block-container font-weight="400" font-size="8pt" background-color="transparent">
      <fo:table table-layout="fixed" width="100%">
        <fo:table-column column-width="68mm"/>
        <fo:table-column column-width="106mm"/>
        <fo:table-body>
          <fo:table-row min-height="1mm" border-top="0.2pt solid black">
            <fo:table-cell>
              <fo:block/>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block/>
            </fo:table-cell>
          </fo:table-row>
          <!-- <xsl:for-each-group select="current-group()/data:LegalProvisions/data:ResponsibleOffice" group-by="data:Name">  -->
          <xsl:for-each-group select="current-group()/data:ResponsibleOffice" group-by="data:Name">
            <xsl:sort lang="de" order="ascending" select="oereb:extractMultilingualText(data:Name/data:LocalisedText, $locale)"/>
            <fo:table-row line-height="5mm" font-weight="400">
              <fo:table-cell padding-top="1.5pt">
                <xsl:if test="position()=1">
                  <fo:block font-weight="700">
                    <xsl:value-of select="$localeXml/data[@name='RestrictionPage.ResponsibleOffice']/value/text()"/>
                  </fo:block>
                </xsl:if>
                <xsl:if test="position()!=1">
                  <fo:block/>
                </xsl:if>
              </fo:table-cell>
              <fo:table-cell padding-top="1.5pt" padding-bottom="1.5pt">
                <fo:block font-size="8pt">
                  <xsl:value-of select="oereb:extractMultilingualText(data:Name/data:LocalisedText, $locale)"/>
                  <xsl:if test="data:OfficeAtWeb/data:LocalisedText">
                    <xsl:text>:</xsl:text>
                  </xsl:if>
                </fo:block>
                <xsl:if test="data:OfficeAtWeb/data:LocalisedText">
                  <fo:block font-size="6pt" line-height="8pt" margin-left="3mm" margin-top="-1mm">
                    <fo:basic-link text-decoration="none" color="rgb(76,143,186)">
                      <xsl:attribute name="external-destination">
                        <xsl:value-of select="oereb:decodeURL(oereb:extractMultilingualText(data:OfficeAtWeb/data:LocalisedText, $locale))"/>
                      </xsl:attribute>
                      <!-- https://stackoverflow.com/questions/4350788/xsl-fo-force-wrap-on-table-entries/33689540#33689540 -->
                      <xsl:value-of select="replace(replace(oereb:decodeURL(oereb:extractMultilingualText(data:OfficeAtWeb/data:LocalisedText, $locale)), '(\P{Zs}{13})', '$1​'), '​(\p{Zs})','$1')"/>
                    </fo:basic-link>
                  </fo:block>
                </xsl:if>
              </fo:table-cell>
            </fo:table-row>
          </xsl:for-each-group>
          <fo:table-row min-height="1mm" border-bottom="0.2pt solid black">
            <fo:table-cell>
              <fo:block/>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block/>
            </fo:table-cell>
          </fo:table-row>
        </fo:table-body>
      </fo:table>
    </fo:block-container>

  </xsl:template>

  <xsl:template name="insertHeaderAndFooter">
    <fo:static-content flow-name="xsl-region-before">
      <fo:block>
        <fo:block-container absolute-position="absolute" top="0mm" left="0mm" background-color="transparent">
          <fo:block font-size="0pt" padding="0mm" margin="0mm" line-height="0mm">
            <fo:external-graphic width="44mm" content-width="scale-to-fit" fox:alt-text="FederalLogo">
              <xsl:if test="/extract:GetExtractByIdResponse/data:Extract/data:FederalLogo">
                <xsl:attribute name="src">
                  <xsl:text>url('data:</xsl:text>
                  <xsl:text>image/png;base64,</xsl:text>
                  <xsl:value-of select="oereb:fixImage(/extract:GetExtractByIdResponse/data:Extract/data:FederalLogo)"/>
                  <xsl:text>')</xsl:text>
                </xsl:attribute>
              </xsl:if>
              <xsl:if test="/extract:GetExtractByIdResponse/data:Extract/data:FederalLogoRef and not(/extract:GetExtractByIdResponse/data:Extract/data:FederalLogo)">
                <xsl:attribute name="src">
                  <xsl:text>url('data:</xsl:text>
                  <xsl:text>image/png;base64,</xsl:text>
                  <xsl:value-of select="oereb:fixImage(/extract:GetExtractByIdResponse/data:Extract/data:FederalLogoRef)"/>
                  <xsl:text>')</xsl:text>
                </xsl:attribute>
              </xsl:if>
            </fo:external-graphic>
          </fo:block>
        </fo:block-container>
        <fo:block-container absolute-position="absolute" top="0mm" left="56mm" background-color="transparent">
          <fo:block font-size="0pt" padding="0mm" margin="0mm" line-height="0mm">
            <fo:external-graphic border="0pt solid black" width="30mm" height="13mm" scaling="uniform" content-width="scale-to-fit" content-height="scale-to-fit" text-align="center" fox:alt-text="CantonalLogo">
              <xsl:if test="/extract:GetExtractByIdResponse/data:Extract/data:CantonalLogo">
                <xsl:attribute name="src">
                  <xsl:text>url('data:</xsl:text>
                  <xsl:text>image/png;base64,</xsl:text>
                  <xsl:value-of select="oereb:fixImage(/extract:GetExtractByIdResponse/data:Extract/data:CantonalLogo)"/>
                  <xsl:text>')</xsl:text>
                </xsl:attribute>
              </xsl:if>
              <xsl:if test="/extract:GetExtractByIdResponse/data:Extract/data:CantonalLogoRef and not(/extract:GetExtractByIdResponse/data:Extract/data:CantonalLogo)">
                <xsl:attribute name="src">
                  <xsl:text>url('data:</xsl:text>
                  <xsl:text>image/png;base64,</xsl:text>
                  <xsl:value-of select="oereb:fixImage(/extract:GetExtractByIdResponse/data:Extract/data:CantonalLogoRef)"/>
                  <xsl:text>')</xsl:text>
                </xsl:attribute>
              </xsl:if>
            </fo:external-graphic>
          </fo:block>
        </fo:block-container>
        <fo:block-container absolute-position="absolute" top="0mm" left="97mm" background-color="transparent">
          <fo:block font-size="0pt" padding="0mm" margin="0mm" line-height="0mm">
            <fo:external-graphic width="30mm" height="13mm" scaling="uniform" content-width="scale-to-fit" content-height="scale-to-fit" text-align="center" fox:alt-text="MunicipalityLogo">
              <xsl:if test="/extract:GetExtractByIdResponse/data:Extract/data:MunicipalityLogo">
                <xsl:attribute name="src">
                  <xsl:text>url('data:</xsl:text>
                  <xsl:text>image/png;base64,</xsl:text>
                  <xsl:value-of select="oereb:fixImage(/extract:GetExtractByIdResponse/data:Extract/data:MunicipalityLogo)"/>
                  <xsl:text>')</xsl:text>
                </xsl:attribute>
              </xsl:if>
              <xsl:if test="/extract:GetExtractByIdResponse/data:Extract/data:MunicipalityLogoRef and not(/extract:GetExtractByIdResponse/data:Extract/data:MunicipalityLogo)">
                <xsl:attribute name="src">
                  <xsl:text>url('data:</xsl:text>
                  <xsl:text>image/png;base64,</xsl:text>
                  <xsl:value-of select="oereb:fixImage(/extract:GetExtractByIdResponse/data:Extract/data:MunicipalityLogoRef)"/>
                  <xsl:text>')</xsl:text>
                </xsl:attribute>
              </xsl:if>
            </fo:external-graphic>
          </fo:block>
        </fo:block-container>
        <fo:block-container absolute-position="absolute" top="0mm" left="139mm" background-color="transparent">
          <fo:block font-size="0pt" padding="0mm" margin="0mm" line-height="0mm">
            <fo:external-graphic width="35mm" height="10mm" scaling="non-uniform" content-width="scale-to-fit" content-height="scale-to-fit" fox:alt-text="LogoPLRCadastre">
              <xsl:if test="/extract:GetExtractByIdResponse/data:Extract/data:LogoPLRCadastre">
                <xsl:attribute name="src">
                  <xsl:text>url('data:</xsl:text>
                  <xsl:text>image/png;base64,</xsl:text>
                  <xsl:value-of select="oereb:fixImage(/extract:GetExtractByIdResponse/data:Extract/data:LogoPLRCadastre)"/>
                  <xsl:text>')</xsl:text>
                </xsl:attribute>
              </xsl:if>
              <xsl:if test="/extract:GetExtractByIdResponse/data:Extract/data:LogoPLRCadastreRef and not(/extract:GetExtractByIdResponse/data:Extract/data:LogoPLRCadastre)">
                <xsl:attribute name="src">
                  <xsl:text>url('data:</xsl:text>
                  <xsl:text>image/png;base64,</xsl:text>
                  <xsl:value-of select="oereb:fixImage(/extract:GetExtractByIdResponse/data:Extract/data:LogoPLRCadastreRef)"/>
                  <xsl:text>')</xsl:text>
                </xsl:attribute>
              </xsl:if>
            </fo:external-graphic>
          </fo:block>
        </fo:block-container>
        <fo:block-container absolute-position="absolute" top="19mm" left="0mm">
          <fo:block font-size="0pt" padding="0mm" margin="0mm" line-height="0mm">
            <fo:leader leader-pattern="rule" leader-length="100%" rule-style="solid" rule-thickness="0.2pt"/>
          </fo:block>
        </fo:block-container>
      </fo:block>
    </fo:static-content>
    <fo:static-content flow-name="xsl-region-after">
      <fo:block-container absolute-position="absolute" top="0mm" left="0mm">
        <fo:block font-size="0pt" padding="0mm" margin="0mm" line-height="0mm">
          <fo:leader leader-pattern="rule" leader-length="100%" rule-style="solid" rule-thickness="0.8pt"/>
        </fo:block>
        <fo:table table-layout="fixed" width="100%" margin-top="0.5mm" font-size="6pt">
          <fo:table-column column-width="50%"/>
          <fo:table-column column-width="50%"/>
          <fo:table-body>
            <fo:table-row vertical-align="bottom">
              <fo:table-cell vertical-align="bottom">
                <fo:block vertical-align="bottom">
                  <xsl:value-of select="format-dateTime(/extract:GetExtractByIdResponse/data:Extract/data:CreationDate,'[D01].[M01].[Y0001]')"/>
                  <fo:inline padding-left="1em">
                    <xsl:value-of select="format-dateTime(/extract:GetExtractByIdResponse/data:Extract/data:CreationDate,'[H01]:[m01]:[s01]')"/>
                  </fo:inline>
                  <fo:inline padding-left="1em">
                    <xsl:value-of select="/extract:GetExtractByIdResponse/data:Extract/data:ExtractIdentifier"/>
                  </fo:inline>
                </fo:block>
              </fo:table-cell>
              <fo:table-cell text-align="right">
                <!-- <fo:block>Seite <fo:page-number/>/<fo:page-number-citation-last ref-id="page-sequence-id"/></fo:block> -->
                <fo:block><xsl:value-of select="$localeXml/data[@name='Page']/value/text()"/><xsl:text> </xsl:text><fo:page-number/>/<fo:page-number-citation ref-id="last-page"/></fo:block>
              </fo:table-cell>
            </fo:table-row>
          </fo:table-body>
        </fo:table>
      </fo:block-container>
    </fo:static-content>
  </xsl:template>

  <xsl:template name="insertGlossary">
    <fo:page-sequence master-reference="mainPage" id="page-sequence-id">
      <xsl:call-template name="insertHeaderAndFooter"/>
      <fo:flow flow-name="xsl-region-body">
        <fo:block>
          <fo:block-container height="13mm" background-color="transparent">
            <fo:block page-break-before="always" line-height="18pt" linefeed-treatment="preserve" font-weight="700" font-size="15pt">
              <xsl:value-of select="$localeXml/data[@name='GlossaryPage.Title']/value/text()"/>
            </fo:block>
          </fo:block-container>
          <fo:block-container font-size="8pt" line-height="11pt" background-color="transparent">
            <fo:table table-layout="fixed" width="100%">
              <fo:table-column column-width="174mm"/>
              <fo:table-body>
                <xsl:for-each select="data:Glossary">
                  <xsl:sort select="oereb:extractMultilingualText(data:Title/data:LocalisedText, $locale)" lang="de-CH"/>
                  <fo:table-row border-bottom="0.2pt solid black" vertical-align="middle" line-height="11pt">
                    <fo:table-cell padding-top="1mm" padding-bottom="1mm">
                      <fo:block>
                        <fo:inline font-weight="700"><xsl:value-of select="oereb:extractMultilingualText(data:Title/data:LocalisedText, $locale)"/>: </fo:inline>
                        <xsl:value-of select="oereb:extractMultilingualText(data:Content/data:LocalisedText, $locale)"/>
                      </fo:block>
                    </fo:table-cell>
                  </fo:table-row>
                </xsl:for-each>
              </fo:table-body>
            </fo:table>
          </fo:block-container>
        </fo:block>
        <fo:block id="last-page"/>
      </fo:flow>
    </fo:page-sequence>
  </xsl:template>

  <!-- Falls simple Werte zurückgeliefert werden, eher Funktion verwenden. Wenn neue Nodes 
  entstehen, dann eher named templates. -->
  <xsl:template name="textRenderer">
    <xsl:param name="textContent"/>
    <xsl:analyze-string select="$textContent" regex="(www|http://|https://)([\w.]*)?">
      <xsl:matching-substring>
        <fo:inline>
          <fo:basic-link text-decoration="none" color="rgb(76,143,186)">
            <xsl:attribute name="external-destination">
              <xsl:value-of select="."/>
            </xsl:attribute>
            <!--<xsl:value-of select="substring(regex-group(0),8)"/>-->
            <xsl:value-of select="regex-group(0)"/>
          </fo:basic-link>
        </fo:inline>
      </xsl:matching-substring>
      <xsl:non-matching-substring>
        <xsl:value-of select="."/>
      </xsl:non-matching-substring>
    </xsl:analyze-string>
  </xsl:template>

  <!-- Falls die Sprache nicht im XML vorhanden ist, wird das erste Element des 
  MultilingualTyps verwendet. -->
  <xsl:function name="oereb:extractMultilingualText">
    <xsl:param name="seq" as="item()*"/>
    <xsl:param name="locale" as="xs:string" />
    <xsl:choose>
      <xsl:when test="$seq/data:Language/text()[. = $locale]">
        <xsl:sequence select="$seq/data:Text/text()[../../data:Language = $locale]"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="$seq[1]/data:Text" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <xsl:function name="oereb:extractMultilingualBlob">
    <xsl:param name="seq" as="item()*"/>
    <xsl:param name="locale" as="xs:string" />
    <xsl:choose>
      <xsl:when test="$seq/data:Language/text()[. = $locale]">
        <xsl:sequence select="$seq/data:Blob/text()[../../data:Language = $locale]"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="$seq[1]/data:Blob" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

</xsl:stylesheet>
