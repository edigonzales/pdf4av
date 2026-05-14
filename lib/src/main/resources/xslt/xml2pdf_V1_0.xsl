<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
        version="3.0"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:xs="http://www.w3.org/2001/XMLSchema"
        xmlns:fo="http://www.w3.org/1999/XSL/Format"
        xmlns:fox="http://xmlgraphics.apache.org/fop/extensions"
        xmlns:extract="http://schemas.geo.admin.ch/V_D/AV/1.0/Extract"
        xmlns:data="http://schemas.geo.admin.ch/V_D/AV/1.0/ExtractData"
        xmlns:geometry="http://www.interlis.ch/geometry/1.0"
        xmlns:av="http://pdf4av.so.ch/av"
        exclude-result-prefixes="extract data geometry av xs">
    <xsl:output method="xml" indent="yes"/>

    <xsl:param name="localeUrl" as="xs:string" select="'Resources.de.resx'"/>
    <xsl:param name="debugTableGrid" as="xs:boolean" select="false()"/>

    <xsl:variable name="localeFile" as="xs:string" select="tokenize($localeUrl, '/')[last()]"/>
    <xsl:variable name="locale" as="xs:string" select="replace($localeFile, '^Resources\.([^.]+)\.resx$', '$1')"/>
    <xsl:variable name="localeXml" select="document($localeUrl)/*"/>

    <xsl:variable name="page-width" as="xs:string" select="'210mm'"/>
    <xsl:variable name="page-height" as="xs:string" select="'297mm'"/>
    <xsl:variable name="margin-top" as="xs:string" select="'10mm'"/>
    <xsl:variable name="margin-bottom" as="xs:string" select="'0mm'"/>
    <xsl:variable name="margin-side" as="xs:string" select="'18mm'"/>
    <xsl:variable name="header-height" as="xs:string" select="'18mm'"/>
    <xsl:variable name="footer-height" as="xs:string" select="'10mm'"/>
    <xsl:variable name="plan-width" as="xs:string" select="'174mm'"/>
    <xsl:variable name="plan-height" as="xs:string" select="'99mm'"/>
    <xsl:variable name="title-page-top-layout-height" as="xs:string" select="'136mm'"/>
    <xsl:variable name="header-rule-top" as="xs:string" select="'17.93mm'"/>
    <xsl:variable name="title-optical-top-correction" as="xs:decimal" select="1.0"/>
    <xsl:variable name="title-line-box-top" as="xs:string"
                  select="concat(format-number(35 - 26 - $title-optical-top-correction, '0.###'), 'mm')"/>
    <xsl:variable name="title-box-height" as="xs:string" select="'42pt'"/>
    <xsl:variable name="plan-top" as="xs:string" select="'29mm'"/>
    <xsl:variable name="content-top-offset-mm" as="xs:decimal" select="26.0"/>
    <xsl:variable name="land-description-title-optical-top-correction" as="xs:decimal" select="1.0"/>
    <xsl:variable name="land-description-title-optical-bottom-correction-mm" as="xs:decimal" select="1.73"/>
    <xsl:variable name="land-description-title-line-box-top-mm" as="xs:decimal"
                  select="35 - $content-top-offset-mm - $land-description-title-optical-top-correction"/>
    <xsl:variable name="land-description-title-line-box-top" as="xs:string"
                  select="concat(format-number($land-description-title-line-box-top-mm, '0.###'), 'mm')"/>
    <xsl:variable name="land-description-title-font-size" as="xs:string" select="'15pt'"/>
    <xsl:variable name="land-description-title-line-height" as="xs:string" select="'18pt'"/>
    <xsl:variable name="land-description-title-line-height-mm" as="xs:decimal" select="18 * 25.4 div 72"/>
    <xsl:variable name="land-description-title-box-height" as="xs:string" select="'18pt'"/>
    <xsl:variable name="land-description-plan-gap-mm" as="xs:decimal" select="8.0"/>
    <xsl:variable name="land-description-plan-top-mm" as="xs:decimal"
                  select="$land-description-title-line-box-top-mm + $land-description-title-line-height-mm + $land-description-plan-gap-mm - $land-description-title-optical-bottom-correction-mm"/>
    <xsl:variable name="land-description-plan-top" as="xs:string"
                  select="concat(format-number($land-description-plan-top-mm, '0.###'), 'mm')"/>
    <xsl:variable name="land-description-top-layout-height-mm" as="xs:decimal" select="$land-description-plan-top-mm + 99.0"/>
    <xsl:variable name="land-description-top-layout-height" as="xs:string"
                  select="concat(format-number($land-description-top-layout-height-mm, '0.###'), 'mm')"/>
    <xsl:variable name="ownership-information-top-layout-height-mm" as="xs:decimal" select="$land-description-plan-top-mm"/>
    <xsl:variable name="ownership-information-top-layout-height" as="xs:string"
                  select="concat(format-number($ownership-information-top-layout-height-mm, '0.###'), 'mm')"/>
    <xsl:variable name="land-description-section-gap" as="xs:string" select="'8mm'"/>
    <xsl:variable name="land-description-heading-optical-bottom-correction-mm" as="xs:decimal" select="1.49"/>
    <xsl:variable name="land-description-heading-to-table-gap" as="xs:string"
                  select="concat(format-number(6 - $land-description-heading-optical-bottom-correction-mm, '0.###'), 'mm')"/>
    <xsl:variable name="land-description-heading-font-size" as="xs:string" select="'10pt'"/>
    <xsl:variable name="land-description-heading-line-height" as="xs:string" select="'12pt'"/>
    <xsl:variable name="land-description-responsible-office-heading-font-size" as="xs:string" select="'6pt'"/>
    <xsl:variable name="land-description-responsible-office-heading-line-height" as="xs:string" select="'8pt'"/>
    <xsl:variable name="land-description-responsible-office-heading-gap" as="xs:string" select="'1mm'"/>
    <xsl:variable name="land-description-responsible-office-font-size" as="xs:string" select="'6pt'"/>
    <xsl:variable name="land-description-responsible-office-line-height" as="xs:string" select="'8pt'"/>
    <xsl:variable name="land-description-table-font-size" as="xs:string" select="'8pt'"/>
    <xsl:variable name="land-description-table-header-sub-font-size" as="xs:string" select="'6pt'"/>
    <xsl:variable name="land-description-table-rule" as="xs:string" select="'0.25pt'"/>
    <xsl:variable name="land-description-row-height" as="xs:string" select="'6mm'"/>
    <xsl:variable name="land-description-land-cover-type-width" as="xs:string" select="'131mm'"/>
    <xsl:variable name="land-description-land-cover-area-width" as="xs:string" select="'15mm'"/>
    <xsl:variable name="land-description-land-cover-percent-width" as="xs:string" select="'28mm'"/>
    <xsl:variable name="land-description-building-type-width" as="xs:string" select="'40mm'"/>
    <xsl:variable name="land-description-building-egid-width" as="xs:string" select="'36mm'"/>
    <xsl:variable name="land-description-building-address-width" as="xs:string" select="'52mm'"/>
    <xsl:variable name="land-description-building-zip-width" as="xs:string" select="'11mm'"/>
    <xsl:variable name="land-description-building-city-width" as="xs:string" select="'35mm'"/>
    <xsl:variable name="projected-objects-number-width" as="xs:string" select="'25mm'"/>
    <xsl:variable name="projected-objects-egrid-width" as="xs:string" select="'45mm'"/>
    <xsl:variable name="projected-objects-type-width" as="xs:string" select="'48mm'"/>
    <xsl:variable name="projected-objects-previous-area-width" as="xs:string" select="'28mm'"/>
    <xsl:variable name="projected-objects-new-area-width" as="xs:string" select="'28mm'"/>
    <xsl:variable name="land-description-link-color" as="xs:string" select="'rgb(76,143,186)'"/>
    <xsl:variable name="table-label-width" as="xs:string" select="'68mm'"/>
    <xsl:variable name="table-value-width" as="xs:string" select="'106mm'"/>
    <xsl:variable name="row-height" as="xs:string" select="'6mm'"/>
    <xsl:variable name="rule-thin" as="xs:string" select="'0.25pt'"/>
    <xsl:variable name="rule-image" as="xs:string" select="'0.4pt'"/>
    <xsl:variable name="rule-footer" as="xs:string" select="'0.8pt'"/>
    <xsl:variable name="debug-table-color" as="xs:string" select="'rgb(226, 0, 122)'"/>
    <xsl:variable name="debug-row-color" as="xs:string" select="'rgb(0, 132, 214)'"/>
    <xsl:variable name="debug-cell-color" as="xs:string" select="'rgb(0, 153, 51)'"/>

    <xsl:decimal-format name="swiss" decimal-separator="." grouping-separator="'"/>

    <xsl:template match="/">
        <xsl:if test="not(extract:GetExtractByIdResponse/data:Extract)">
            <xsl:message terminate="yes">Root element GetExtractByIdResponse/Extract is required.</xsl:message>
        </xsl:if>
        <xsl:apply-templates select="extract:GetExtractByIdResponse/data:Extract"/>
    </xsl:template>

    <xsl:template match="extract:GetExtractByIdResponse/data:Extract">
        <fo:root font-family="Cadastra" font-weight="400" language="{$locale}" xml:lang="{$locale}">
            <xsl:call-template name="defineLayoutMasters"/>
            <fo:page-sequence master-reference="mainPage" id="page-sequence-id">
                <xsl:call-template name="insertHeaderAndFooter"/>
                <fo:flow flow-name="xsl-region-body">
                    <xsl:call-template name="insertTitlePage"/>
                    <xsl:call-template name="insertOwnershipInformationSection">
                        <xsl:with-param name="realEstate" select="data:RealEstate_DPR"/>
                    </xsl:call-template>
                    <xsl:call-template name="insertLandDescriptionSection">
                        <xsl:with-param name="realEstate" select="data:RealEstate_DPR"/>
                    </xsl:call-template>
                    <xsl:call-template name="insertProjectedObjectsSection">
                        <xsl:with-param name="realEstate" select="data:RealEstate_DPR"/>
                    </xsl:call-template>
                    <fo:block id="last-page"/>
                </fo:flow>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>

    <xsl:template name="defineLayoutMasters">
        <fo:layout-master-set>
            <fo:simple-page-master
                    master-name="mainPage"
                    page-height="{$page-height}"
                    page-width="{$page-width}"
                    margin-top="{$margin-top}"
                    margin-right="{$margin-side}"
                    margin-bottom="{$margin-bottom}"
                    margin-left="{$margin-side}">
                <fo:region-body margin-top="16mm" margin-bottom="21mm"/>
                <fo:region-before extent="{$header-height}"/>
                <fo:region-after extent="{$footer-height}"/>
            </fo:simple-page-master>
        </fo:layout-master-set>
    </xsl:template>

    <xsl:template name="insertHeaderAndFooter">
        <fo:static-content flow-name="xsl-region-before">
            <fo:block-container background-color="transparent"
                    width="100%"
                    height="{$header-height}"
                    margin="0mm"
                    padding="0mm"
                    space-before="0mm"
                    space-after="0mm">
                <fo:table table-layout="fixed" width="100%">
                    <xsl:call-template name="applyDebugTableAttributes"/>
                    <fo:table-column column-width="44mm"/>
                    <fo:table-column column-width="19mm"/>
                    <fo:table-column column-width="30mm"/>
                    <fo:table-column column-width="19mm"/>
                    <fo:table-column column-width="30mm"/>
                    <fo:table-column column-width="18mm"/>
                    <fo:table-column column-width="14mm"/>
                    <fo:table-body>
                        <fo:table-row height="13mm">
                            <xsl:call-template name="applyDebugTableRowAttributes"/>
                            <fo:table-cell display-align="before" padding="0mm">
                                <xsl:call-template name="applyDebugTableCellAttributes"/>
                                <xsl:call-template name="renderEmbeddedImage">
                                    <xsl:with-param name="imageData" select="normalize-space(data:FederalLogo)"/>
                                    <xsl:with-param name="width" select="'44mm'"/>
                                    <xsl:with-param name="height" select="'13mm'"/>
                                    <xsl:with-param name="altText" select="'FederalLogo'"/>
                                </xsl:call-template>
                            </fo:table-cell>
                            <fo:table-cell display-align="before" padding="0mm">
                                <xsl:call-template name="applyDebugTableCellAttributes"/>
                                <fo:block margin="0mm" padding="0mm" space-before="0mm" space-after="0mm" font-size="0pt" line-height="0pt"/>
                            </fo:table-cell>
                            <fo:table-cell display-align="before" padding="0mm">
                                <xsl:call-template name="applyDebugTableCellAttributes"/>
                                <xsl:call-template name="renderEmbeddedImage">
                                    <xsl:with-param name="imageData" select="normalize-space(data:CantonalLogo)"/>
                                    <xsl:with-param name="width" select="'30mm'"/>
                                    <xsl:with-param name="height" select="'13mm'"/>
                                    <xsl:with-param name="altText" select="'CantonalLogo'"/>
                                </xsl:call-template>
                            </fo:table-cell>
                            <fo:table-cell display-align="before" padding="0mm">
                                <xsl:call-template name="applyDebugTableCellAttributes"/>
                                <fo:block margin="0mm" padding="0mm" space-before="0mm" space-after="0mm" font-size="0pt" line-height="0pt"/>
                            </fo:table-cell>
                            <fo:table-cell display-align="before" padding="0mm">
                                <xsl:call-template name="applyDebugTableCellAttributes"/>
                                <xsl:call-template name="renderEmbeddedImage">
                                    <xsl:with-param name="imageData" select="normalize-space(data:MunicipalityLogo)"/>
                                    <xsl:with-param name="width" select="'30mm'"/>
                                    <xsl:with-param name="height" select="'13mm'"/>
                                    <xsl:with-param name="altText" select="'MunicipalityLogo'"/>
                                </xsl:call-template>
                            </fo:table-cell>
                            <fo:table-cell display-align="before" padding="0mm">
                                <xsl:call-template name="applyDebugTableCellAttributes"/>
                                <fo:block margin="0mm" padding="0mm" space-before="0mm" space-after="0mm" font-size="0pt" line-height="0pt"/>
                            </fo:table-cell>
                            <fo:table-cell display-align="before" padding="0mm">
                                <xsl:call-template name="applyDebugTableCellAttributes"/>
                                <xsl:call-template name="renderEmbeddedImage">
                                    <xsl:with-param name="imageData" select="normalize-space(data:PropertyInformationLogo)"/>
                                    <xsl:with-param name="width" select="'14mm'"/>
                                    <xsl:with-param name="height" select="'12mm'"/>
                                    <xsl:with-param name="altText" select="'PropertyInformationLogo'"/>
                                </xsl:call-template>
                            </fo:table-cell>
                        </fo:table-row>
                    </fo:table-body>
                </fo:table>
                <fo:block-container absolute-position="absolute" top="{$header-rule-top}" left="0mm" width="100%">
                    <fo:block font-size="0pt" line-height="0pt" margin="0mm" padding="0mm">
                        <fo:leader leader-pattern="rule" leader-length="100%" rule-style="solid" rule-thickness="{$rule-thin}"/>
                    </fo:block>
                </fo:block-container>
            </fo:block-container>
        </fo:static-content>

        <fo:static-content flow-name="xsl-region-after">
            <fo:block-container  background-color="transparent"
                    width="100%"
                    height="{$footer-height}"
                    margin="0mm"
                    padding="0mm"
                    space-before="0mm"
                    space-after="0mm">
                <fo:block-container absolute-position="absolute" top="0mm" left="0mm" width="100%">
                    <fo:block font-size="0pt" line-height="0pt" margin="0mm" padding="0mm">
                        <fo:leader leader-pattern="rule" leader-length="100%" rule-style="solid" rule-thickness="{$rule-footer}"/>
                    </fo:block>
                </fo:block-container>
                <fo:block-container absolute-position="absolute" top="0.9mm" left="0mm" width="100%">
                    <fo:table table-layout="fixed" width="100%">
                        <xsl:call-template name="applyDebugTableAttributes"/>
                        <fo:table-column column-width="146mm"/>
                        <fo:table-column column-width="28mm"/>
                        <fo:table-body>
                            <fo:table-row>
                                <xsl:call-template name="applyDebugTableRowAttributes"/>
                                <fo:table-cell>
                                    <xsl:call-template name="applyDebugTableCellAttributes"/>
                                    <fo:block font-size="6pt" line-height="8pt">
                                        <fo:inline>
                                            <xsl:value-of select="av:formatSwissDate(data:CreationDate)"/>
                                        </fo:inline>
                                        <fo:inline padding-left="4mm">
                                            <xsl:value-of select="av:formatSwissTime(data:CreationDate)"/>
                                        </fo:inline>
                                        <fo:inline padding-left="4mm">
                                            <xsl:value-of select="normalize-space(data:ExtractIdentifier)"/>
                                        </fo:inline>
                                    </fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <xsl:call-template name="applyDebugTableCellAttributes"/>
                                    <fo:block font-size="6pt" line-height="8pt" text-align="right">
                                        <xsl:value-of select="$localeXml/data[@name='Page']/value/text()"/>
                                        <xsl:text> </xsl:text>
                                        <fo:page-number/>
                                        <xsl:text>/</xsl:text>
                                        <fo:page-number-citation ref-id="last-page"/>
                                    </fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                        </fo:table-body>
                    </fo:table>
                </fo:block-container>
            </fo:block-container>
        </fo:static-content>
    </xsl:template>

    <xsl:template name="insertTitlePage">
        <xsl:variable name="realEstate" select="data:RealEstate_DPR"/>
        <xsl:variable name="propertyInfoAuthority" select="data:PropertyInformationAuthority"/>
        <xsl:variable name="disclaimer" select="data:Disclaimer/data:Content/data:LocalisedText"/>

        <fo:block-container height="{$title-page-top-layout-height}">
            <xsl:call-template name="insertTitlePageTitle"/>
            <xsl:call-template name="insertTitlePagePlan">
                <xsl:with-param name="realEstate" select="$realEstate"/>
            </xsl:call-template>
        </fo:block-container>
        <xsl:call-template name="insertTitlePagePropertyTable">
            <xsl:with-param name="realEstate" select="$realEstate"/>
        </xsl:call-template>
        <xsl:call-template name="insertTitlePageExtractTable">
            <xsl:with-param name="propertyInfoAuthority" select="$propertyInfoAuthority"/>
            <xsl:with-param name="disclaimer" select="$disclaimer"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="insertTitlePageTitle">
        <fo:block-container
                absolute-position="absolute"
                top="{$title-line-box-top}"
                left="0mm"
                width="100%"
                height="{$title-box-height}">
            <fo:block font-size="18pt" font-weight="700" line-height="21pt" linefeed-treatment="preserve">
                <xsl:value-of select="$localeXml/data[@name='MainPage.Title']/value/text()"/>
            </fo:block>
        </fo:block-container>
    </xsl:template>

    <xsl:template name="insertTitlePagePlan">
        <xsl:param name="realEstate" as="element(data:RealEstate_DPR)?"/>

        <xsl:variable name="planImage" as="xs:string"
                      select="av:createTitlePagePlanImage($realEstate/data:PlanForMainPage, $realEstate/data:Limit, $locale)"/>

        <fo:block-container
                absolute-position="absolute"
                top="{$plan-top}"
                left="0mm"
                width="{$plan-width}"
                height="{$plan-height}">
            <xsl:choose>
                <xsl:when test="normalize-space($planImage)">
                    <fo:block font-size="0pt" line-height="0pt">
                        <fo:external-graphic
                                border="{$rule-image} solid black"
                                width="{$plan-width}"
                                height="{$plan-height}"
                                scaling="non-uniform"
                                content-width="scale-to-fit"
                                content-height="scale-to-fit"
                                fox:alt-text="PlanForMainPage">
                            <xsl:attribute name="src"
                                           select="concat(&quot;url('data:image/png;base64,&quot;, normalize-space($planImage), &quot;')&quot;)"/>
                        </fo:external-graphic>
                    </fo:block>
                </xsl:when>
                <xsl:otherwise>
                    <fo:block-container
                            width="{$plan-width}"
                            height="{$plan-height}"
                            border="{$rule-thin} solid black"
                            display-align="center">
                        <fo:block text-align="center" font-size="8pt">Plan nicht verfuegbar</fo:block>
                    </fo:block-container>
                </xsl:otherwise>
            </xsl:choose>
        </fo:block-container>
    </xsl:template>

    <xsl:template name="insertTitlePagePropertyTable">
        <xsl:param name="realEstate" as="element(data:RealEstate_DPR)?"/>

        <fo:block-container font-size="8pt">
            <fo:table table-layout="fixed" width="100%">
                <xsl:call-template name="applyDebugTableAttributes"/>
                <fo:table-column column-width="{$table-label-width}"/>
                <fo:table-column column-width="{$table-value-width}"/>
                <fo:table-body>
                    <xsl:call-template name="renderKeyValueRow">
                        <xsl:with-param name="label" select="$localeXml/data[@name='MainPage.RealEstate_DPR.Number']/value/text()"/>
                        <xsl:with-param name="value" select="normalize-space($realEstate/data:Number)"/>
                        <xsl:with-param name="labelBold" select="true()"/>
                        <xsl:with-param name="valueBold" select="true()"/>
                    </xsl:call-template>
                    <xsl:call-template name="renderKeyValueRow">
                        <xsl:with-param name="label" select="$localeXml/data[@name='MainPage.RealEstate_DPR.Type']/value/text()"/>
                        <xsl:with-param name="value"
                                        select="av:extractMultilingualText($realEstate/data:Type/data:Text/data:LocalisedText, $locale)"/>
                    </xsl:call-template>
                    <xsl:call-template name="renderKeyValueRow">
                        <xsl:with-param name="label" select="'E-GRID'"/>
                        <xsl:with-param name="value" select="normalize-space($realEstate/data:EGRID)"/>
                    </xsl:call-template>
                    <xsl:call-template name="renderKeyValueRow">
                        <xsl:with-param name="label" select="$localeXml/data[@name='MainPage.Municipality_FosNr']/value/text()"/>
                        <xsl:with-param name="value"
                                        select="av:formatMunicipality($realEstate/data:MunicipalityName, $realEstate/data:MunicipalityCode)"/>
                    </xsl:call-template>
                    <xsl:call-template name="renderKeyValueRow">
                        <xsl:with-param name="label" select="$localeXml/data[@name='MainPage.Toponym']/value/text()"/>
                        <xsl:with-param name="value" select="string-join($realEstate/data:Toponym[normalize-space()], ', ')"/>
                    </xsl:call-template>
                    <xsl:call-template name="renderKeyValueRow">
                        <xsl:with-param name="label" select="$localeXml/data[@name='MainPage.LandRegistryArea']/value/text()"/>
                        <xsl:with-param name="value" select="av:formatSwissArea($realEstate/data:LandRegistryArea)"/>
                    </xsl:call-template>
                    <xsl:call-template name="renderKeyValueRow">
                        <xsl:with-param name="label" select="$localeXml/data[@name='MainPage.UpdateDateCS']/value/text()"/>
                        <xsl:with-param name="value" select="av:formatSwissDate(data:UpdateDateCS)"/>
                    </xsl:call-template>
                </fo:table-body>
            </fo:table>
        </fo:block-container>
    </xsl:template>

    <xsl:template name="insertTitlePageExtractTable">
        <xsl:param name="propertyInfoAuthority" as="element(data:PropertyInformationAuthority)?"/>
        <xsl:param name="disclaimer" as="element(data:LocalisedText)*"/>

        <xsl:variable name="authorityName"
                      select="av:extractMultilingualText($propertyInfoAuthority/data:Name/data:LocalisedText, $locale)"/>
        <xsl:variable name="authorityAddress"
                      select="av:formatAddressLine($propertyInfoAuthority/data:Street, $propertyInfoAuthority/data:Number)"/>
        <xsl:variable name="authorityPostalCity"
                      select="string-join(($propertyInfoAuthority/data:PostalCode, $propertyInfoAuthority/data:City) ! normalize-space(.), ' ')"/>
        <xsl:variable name="authorityWebsite"
                      select="av:extractMultilingualText($propertyInfoAuthority/data:OfficeAtWeb/data:LocalisedText, $locale)"/>
        <xsl:variable name="generalInformationText"
                      select="av:joinNonEmptyLines((
                          av:extractMultilingualText($disclaimer, $locale),
                          $authorityName,
                          $authorityAddress,
                          $authorityPostalCity
                      ))"/>

        <fo:block-container margin-top="4.5mm" font-size="8pt">
            <fo:table table-layout="fixed" width="100%">
                <xsl:call-template name="applyDebugTableAttributes"/>
                <fo:table-column column-width="{$table-label-width}"/>
                <fo:table-column column-width="{$table-value-width}"/>
                <fo:table-body>
                    <xsl:call-template name="renderKeyValueRow">
                        <xsl:with-param name="label" select="$localeXml/data[@name='MainPage.ExtractNumber']/value/text()"/>
                        <xsl:with-param name="value" select="normalize-space(data:ExtractIdentifier)"/>
                        <xsl:with-param name="labelBold" select="true()"/>
                        <xsl:with-param name="valueBold" select="true()"/>
                    </xsl:call-template>
                    <xsl:call-template name="renderKeyValueRow">
                        <xsl:with-param name="label" select="$localeXml/data[@name='MainPage.CreationDate']/value/text()"/>
                        <xsl:with-param name="value" select="av:formatSwissDate(data:CreationDate)"/>
                    </xsl:call-template>
                    <fo:table-row border-bottom="{$rule-thin} solid black">
                        <xsl:call-template name="applyDebugTableRowAttributes"/>
                        <fo:table-cell padding-top="2pt" padding-bottom="2pt">
                            <xsl:call-template name="applyDebugTableCellAttributes"/>
                            <fo:block>
                                <xsl:value-of select="$localeXml/data[@name='MainPage.GeneralInformation']/value/text()"/>
                            </fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding-top="2pt" padding-bottom="2pt">
                            <xsl:call-template name="applyDebugTableCellAttributes"/>
                            <fo:block linefeed-treatment="preserve" white-space-collapse="false" line-height="11pt">
                                <xsl:value-of select="$generalInformationText"/>
                            </fo:block>
                            <xsl:if test="normalize-space($authorityWebsite)">
                                <fo:block line-height="11pt">
                                    <fo:basic-link text-decoration="none" color="rgb(76,143,186)"
                                                   external-destination="{concat(&quot;url('&quot;, normalize-space($authorityWebsite), &quot;')&quot;)}">
                                        <xsl:value-of select="normalize-space($authorityWebsite)"/>
                                    </fo:basic-link>
                                </fo:block>
                            </xsl:if>
                        </fo:table-cell>
                    </fo:table-row>
                </fo:table-body>
            </fo:table>
        </fo:block-container>
    </xsl:template>

    <xsl:template name="insertLandDescriptionSection">
        <xsl:param name="realEstate" as="element(data:RealEstate_DPR)?"/>

        <xsl:variable name="landCovers" as="element(data:LandCover)*"
                      select="$realEstate/data:LandCover[av:isRealObjectStatus(data:Objectstatus)]"/>
        <xsl:variable name="realBuildingCandidates" as="element(data:Building)*"
                      select="$realEstate/data:Building[
                          let $egid := normalize-space(data:EGID)
                          return $egid and (
                              exists($realEstate/data:LandCover[
                                  normalize-space(data:EGID) = $egid
                                  and av:isRealObjectStatus(data:Objectstatus)
                              ])
                              or exists($realEstate/data:SingleObject[
                                  normalize-space(data:EGID) = $egid
                                  and av:isRealObjectStatus(data:Objectstatus)
                              ])
                          )
                      ]"/>
        <xsl:variable name="responsibleOffice" as="element(data:ResponsibleOffice)?" select="$realEstate/data:ResponsibleOffice"/>
        <xsl:variable name="responsibleOfficeLine" as="xs:string"
                      select="av:formatOfficeLine($responsibleOffice, $locale)"/>
        <xsl:variable name="responsibleOfficeWebsite" as="xs:string"
                      select="av:extractMultilingualText($responsibleOffice/data:OfficeAtWeb/data:LocalisedText, $locale)"/>

        <fo:block break-before="page"/>
        <fo:block-container height="{$land-description-top-layout-height}">
            <xsl:call-template name="insertLandDescriptionTitle"/>
            <xsl:call-template name="insertLandDescriptionPlan">
                <xsl:with-param name="realEstate" select="$realEstate"/>
            </xsl:call-template>
        </fo:block-container>

        <xsl:if test="exists($landCovers)">
            <xsl:call-template name="insertLandDescriptionLandCoverSection">
                <xsl:with-param name="landCovers" select="$landCovers"/>
                <xsl:with-param name="landRegistryArea" select="$realEstate/data:LandRegistryArea"/>
            </xsl:call-template>
        </xsl:if>

        <xsl:if test="exists($realBuildingCandidates)">
            <xsl:call-template name="insertLandDescriptionBuildingsSection">
                <xsl:with-param name="realEstate" select="$realEstate"/>
                <xsl:with-param name="buildings" select="$realBuildingCandidates"/>
            </xsl:call-template>
        </xsl:if>

        <xsl:if test="normalize-space($responsibleOfficeLine) or normalize-space($responsibleOfficeWebsite)">
            <xsl:call-template name="insertLandDescriptionResponsibleOfficeSection">
                <xsl:with-param name="responsibleOfficeLine" select="$responsibleOfficeLine"/>
                <xsl:with-param name="responsibleOfficeWebsite" select="$responsibleOfficeWebsite"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template name="insertOwnershipInformationSection">
        <xsl:param name="realEstate" as="element(data:RealEstate_DPR)?"/>

        <xsl:variable name="landRegisterOffice" as="element(data:LandRegisterOffice)?" select="$realEstate/data:LandRegisterOffice"/>
        <xsl:variable name="landRegisterOfficeLine" as="xs:string"
                      select="av:formatOfficeLine($landRegisterOffice, $locale)"/>
        <xsl:variable name="landRegisterOfficeWebsite" as="xs:string"
                      select="av:extractMultilingualText($landRegisterOffice/data:OfficeAtWeb/data:LocalisedText, $locale)"/>

        <xsl:if test="normalize-space($landRegisterOfficeLine) or normalize-space($landRegisterOfficeWebsite)">
            <fo:block break-before="page"/>
            <fo:block-container height="{$ownership-information-top-layout-height}">
                <xsl:call-template name="insertOwnershipInformationTitle"/>
            </fo:block-container>
            <xsl:call-template name="insertOwnershipInformationLandRegisterOfficeSection">
                <xsl:with-param name="landRegisterOfficeLine" select="$landRegisterOfficeLine"/>
                <xsl:with-param name="landRegisterOfficeWebsite" select="$landRegisterOfficeWebsite"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template name="insertProjectedObjectsSection">
        <xsl:param name="realEstate" as="element(data:RealEstate_DPR)?"/>

        <xsl:variable name="mutations" as="element(data:Mutation)*" select="$realEstate/data:Mutation"/>
        <xsl:variable name="projectedProperties" as="element(data:projectedProperty)*"
                      select="$mutations/data:projectedProperty"/>
        <xsl:variable name="plannedLandCoverBuildings" as="element(data:Building)*"
                      select="$realEstate/data:Building[
                          let $egid := normalize-space(data:EGID)
                          return $egid and exists(
                              $realEstate/data:LandCover[
                                  normalize-space(data:EGID) = $egid
                                  and av:isPlannedObjectStatus(data:Objectstatus)
                              ]
                          )
                      ]"/>
        <xsl:variable name="plannedSingleObjectBuildings" as="element(data:Building)*"
                      select="$realEstate/data:Building[
                          let $egid := normalize-space(data:EGID)
                          return $egid
                                 and not(exists(
                                     $realEstate/data:LandCover[
                                         normalize-space(data:EGID) = $egid
                                         and av:isPlannedObjectStatus(data:Objectstatus)
                                     ]
                                 ))
                                 and exists(
                                     $realEstate/data:SingleObject[
                                         normalize-space(data:EGID) = $egid
                                         and av:isPlannedObjectStatus(data:Objectstatus)
                                     ]
                                 )
                      ]"/>
        <xsl:variable name="projectedBuildings" as="element(data:Building)*"
                      select="($plannedLandCoverBuildings, $plannedSingleObjectBuildings)"/>
        <xsl:variable name="responsibleOffice" as="element(data:ResponsibleOffice)?" select="$realEstate/data:ResponsibleOffice"/>
        <xsl:variable name="responsibleOfficeLine" as="xs:string"
                      select="av:formatOfficeLine($responsibleOffice, $locale)"/>
        <xsl:variable name="responsibleOfficeWebsite" as="xs:string"
                      select="av:extractMultilingualText($responsibleOffice/data:OfficeAtWeb/data:LocalisedText, $locale)"/>

        <xsl:if test="exists($projectedProperties) or exists($projectedBuildings)">
            <fo:block break-before="page"/>
            <fo:block-container height="{$land-description-top-layout-height}">
                <xsl:call-template name="insertProjectedObjectsTitle"/>
                <xsl:call-template name="insertProjectedObjectsPlan">
                    <xsl:with-param name="realEstate" select="$realEstate"/>
                </xsl:call-template>
            </fo:block-container>

            <xsl:if test="exists($projectedProperties)">
                <xsl:call-template name="renderLandDescriptionSectionHeading">
                    <xsl:with-param name="label" select="$localeXml/data[@name='ProjectedObjects.ProjectedProperties.Title']/value/text()"/>
                </xsl:call-template>

                <fo:table table-layout="fixed" width="{$plan-width}" font-size="{$land-description-table-font-size}">
                    <xsl:call-template name="applyDebugTableAttributes"/>
                    <fo:table-column column-width="{$projected-objects-number-width}"/>
                    <fo:table-column column-width="{$projected-objects-egrid-width}"/>
                    <fo:table-column column-width="{$projected-objects-type-width}"/>
                    <fo:table-column column-width="{$projected-objects-previous-area-width}"/>
                    <fo:table-column column-width="{$projected-objects-new-area-width}"/>
                    <fo:table-header>
                        <fo:table-row>
                            <xsl:call-template name="applyDebugTableRowAttributes"/>
                            <xsl:call-template name="renderLandDescriptionHeaderCell">
                                <xsl:with-param name="title" select="$localeXml/data[@name='ProjectedObjects.ProjectedProperties.Number']/value/text()"/>
                            </xsl:call-template>
                            <xsl:call-template name="renderLandDescriptionHeaderCell">
                                <xsl:with-param name="title" select="$localeXml/data[@name='ProjectedObjects.ProjectedProperties.Egrid']/value/text()"/>
                            </xsl:call-template>
                            <xsl:call-template name="renderLandDescriptionHeaderCell">
                                <xsl:with-param name="title" select="$localeXml/data[@name='ProjectedObjects.ProjectedProperties.Type']/value/text()"/>
                            </xsl:call-template>
                            <xsl:call-template name="renderLandDescriptionHeaderCell">
                                <xsl:with-param name="title" select="$localeXml/data[@name='ProjectedObjects.ProjectedProperties.PreviousArea']/value/text()"/>
                                <xsl:with-param name="textAlign" select="'right'"/>
                            </xsl:call-template>
                            <xsl:call-template name="renderLandDescriptionHeaderCell">
                                <xsl:with-param name="title" select="$localeXml/data[@name='ProjectedObjects.ProjectedProperties.NewArea']/value/text()"/>
                                <xsl:with-param name="textAlign" select="'right'"/>
                            </xsl:call-template>
                        </fo:table-row>
                    </fo:table-header>
                    <fo:table-body>
                        <xsl:for-each select="$projectedProperties">
                            <xsl:variable name="egrid" as="xs:string" select="normalize-space(data:EGRID)"/>
                            <xsl:variable name="previousArea" as="xs:string"
                                          select="if ($egrid = normalize-space($realEstate/data:EGRID))
                                                  then av:formatSwissArea($realEstate/data:LandRegistryArea)
                                                  else ''"/>

                            <xsl:call-template name="renderProjectedObjectsPropertyRow">
                                <xsl:with-param name="number" select="normalize-space(data:Number)"/>
                                <xsl:with-param name="egrid" select="$egrid"/>
                                <xsl:with-param name="typeLabel"
                                                select="av:extractMultilingualText(data:Type/data:Text/data:LocalisedText, $locale)"/>
                                <xsl:with-param name="previousArea" select="$previousArea"/>
                                <xsl:with-param name="newArea" select="av:formatSwissArea(data:newParcelArea)"/>
                            </xsl:call-template>
                        </xsl:for-each>
                    </fo:table-body>
                </fo:table>
            </xsl:if>

            <xsl:if test="exists($projectedBuildings)">
                <xsl:call-template name="insertProjectedObjectsBuildingsSection">
                    <xsl:with-param name="realEstate" select="$realEstate"/>
                    <xsl:with-param name="buildings" select="$projectedBuildings"/>
                </xsl:call-template>
            </xsl:if>

            <xsl:if test="normalize-space($responsibleOfficeLine) or normalize-space($responsibleOfficeWebsite)">
                <xsl:call-template name="renderOfficeSection">
                    <xsl:with-param name="label" select="$localeXml/data[@name='LandDescription.ResponsibleOffice.Title']/value/text()"/>
                    <xsl:with-param name="officeLine" select="$responsibleOfficeLine"/>
                    <xsl:with-param name="officeWebsite" select="$responsibleOfficeWebsite"/>
                </xsl:call-template>
            </xsl:if>
        </xsl:if>
    </xsl:template>

    <xsl:template name="insertOwnershipInformationTitle">
        <fo:block-container
                absolute-position="absolute"
                top="{$land-description-title-line-box-top}"
                left="0mm"
                width="100%"
                height="{$land-description-title-box-height}">
            <fo:block font-size="{$land-description-title-font-size}" font-weight="700" line-height="{$land-description-title-line-height}">
                <xsl:value-of select="$localeXml/data[@name='OwnershipInformation.Title']/value/text()"/>
            </fo:block>
        </fo:block-container>
    </xsl:template>

    <xsl:template name="insertOwnershipInformationLandRegisterOfficeSection">
        <xsl:param name="landRegisterOfficeLine" as="xs:string"/>
        <xsl:param name="landRegisterOfficeWebsite" as="xs:string"/>

        <xsl:call-template name="renderOfficeSection">
            <xsl:with-param name="label" select="$localeXml/data[@name='LandDescription.ResponsibleOffice.Title']/value/text()"/>
            <xsl:with-param name="officeLine" select="$landRegisterOfficeLine"/>
            <xsl:with-param name="officeWebsite" select="$landRegisterOfficeWebsite"/>
            <xsl:with-param name="spaceBefore" select="'0mm'"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="insertLandDescriptionTitle">
        <fo:block-container
                absolute-position="absolute"
                top="{$land-description-title-line-box-top}"
                left="0mm"
                width="100%"
                height="{$land-description-title-box-height}">
            <fo:block font-size="{$land-description-title-font-size}" font-weight="700" line-height="{$land-description-title-line-height}">
                <xsl:value-of select="$localeXml/data[@name='LandDescription.Title']/value/text()"/>
            </fo:block>
        </fo:block-container>
    </xsl:template>

    <xsl:template name="insertProjectedObjectsTitle">
        <fo:block-container
                absolute-position="absolute"
                top="{$land-description-title-line-box-top}"
                left="0mm"
                width="100%"
                height="{$land-description-title-box-height}">
            <fo:block font-size="{$land-description-title-font-size}" font-weight="700" line-height="{$land-description-title-line-height}">
                <xsl:value-of select="$localeXml/data[@name='ProjectedObjects.Title']/value/text()"/>
            </fo:block>
        </fo:block-container>
    </xsl:template>

    <xsl:template name="insertLandDescriptionPlan">
        <xsl:param name="realEstate" as="element(data:RealEstate_DPR)?"/>

        <xsl:variable name="planImage" as="xs:string"
                      select="av:createPlanForLandDescriptionImage($realEstate/data:PlanForLandDescription, $realEstate/data:Limit, $locale)"/>

        <fo:block-container
                absolute-position="absolute"
                top="{$land-description-plan-top}"
                left="0mm"
                width="{$plan-width}"
                height="{$plan-height}">
            <xsl:choose>
                <xsl:when test="normalize-space($planImage)">
                    <fo:block font-size="0pt" line-height="0pt">
                        <fo:external-graphic
                                border="{$land-description-table-rule} solid black"
                                width="{$plan-width}"
                                height="{$plan-height}"
                                scaling="non-uniform"
                                content-width="scale-to-fit"
                                content-height="scale-to-fit"
                                fox:alt-text="PlanForLandDescription">
                            <xsl:attribute name="src"
                                           select="concat(&quot;url('data:image/png;base64,&quot;, normalize-space($planImage), &quot;')&quot;)"/>
                        </fo:external-graphic>
                    </fo:block>
                </xsl:when>
                <xsl:otherwise>
                    <fo:block-container
                            width="{$plan-width}"
                            height="{$plan-height}"
                            border="{$land-description-table-rule} solid black"
                            display-align="center">
                        <fo:block text-align="center" font-size="8pt">Plan nicht verfuegbar</fo:block>
                    </fo:block-container>
                </xsl:otherwise>
            </xsl:choose>
        </fo:block-container>
    </xsl:template>

    <xsl:template name="insertProjectedObjectsPlan">
        <xsl:param name="realEstate" as="element(data:RealEstate_DPR)?"/>

        <xsl:variable name="planImage" as="xs:string"
                      select="av:createPlanForProjectedObjectsImage($realEstate/data:PlanForProjectedObjects, $realEstate/data:Limit, $locale)"/>

        <fo:block-container
                absolute-position="absolute"
                top="{$land-description-plan-top}"
                left="0mm"
                width="{$plan-width}"
                height="{$plan-height}">
            <xsl:choose>
                <xsl:when test="normalize-space($planImage)">
                    <fo:block font-size="0pt" line-height="0pt">
                        <fo:external-graphic
                                border="{$land-description-table-rule} solid black"
                                width="{$plan-width}"
                                height="{$plan-height}"
                                scaling="non-uniform"
                                content-width="scale-to-fit"
                                content-height="scale-to-fit"
                                fox:alt-text="PlanForProjectedObjects">
                            <xsl:attribute name="src"
                                           select="concat(&quot;url('data:image/png;base64,&quot;, normalize-space($planImage), &quot;')&quot;)"/>
                        </fo:external-graphic>
                    </fo:block>
                </xsl:when>
                <xsl:otherwise>
                    <fo:block-container
                            width="{$plan-width}"
                            height="{$plan-height}"
                            border="{$land-description-table-rule} solid black"
                            display-align="center">
                        <fo:block text-align="center" font-size="8pt">Plan nicht verfuegbar</fo:block>
                    </fo:block-container>
                </xsl:otherwise>
            </xsl:choose>
        </fo:block-container>
    </xsl:template>

    <xsl:template name="insertLandDescriptionLandCoverSection">
        <xsl:param name="landCovers" as="element(data:LandCover)*"/>
        <xsl:param name="landRegistryArea" as="item()*"/>

        <xsl:call-template name="renderLandDescriptionSectionHeading">
            <xsl:with-param name="label" select="$localeXml/data[@name='LandDescription.LandCover.Title']/value/text()"/>
        </xsl:call-template>

        <fo:table table-layout="fixed" width="{$plan-width}" font-size="{$land-description-table-font-size}">
            <xsl:call-template name="applyDebugTableAttributes"/>
            <fo:table-column column-width="{$land-description-land-cover-type-width}"/>
            <fo:table-column column-width="{$land-description-land-cover-area-width}"/>
            <fo:table-column column-width="{$land-description-land-cover-percent-width}"/>
            <fo:table-header>
                <fo:table-row>
                    <xsl:call-template name="applyDebugTableRowAttributes"/>
                    <xsl:call-template name="renderLandDescriptionHeaderCell">
                        <xsl:with-param name="title" select="$localeXml/data[@name='LandDescription.LandCover.Type']/value/text()"/>
                        <xsl:with-param name="subtitle" select="'Bezeichnung'"/>
                    </xsl:call-template>
                    <xsl:call-template name="renderLandDescriptionHeaderCell">
                        <xsl:with-param name="title" select="$localeXml/data[@name='LandDescription.LandCover.Area']/value/text()"/>
                        <xsl:with-param name="textAlign" select="'right'"/>
                    </xsl:call-template>
                    <xsl:call-template name="renderLandDescriptionHeaderCell">
                        <xsl:with-param name="title" select="$localeXml/data[@name='LandDescription.LandCover.Percent']/value/text()"/>
                        <xsl:with-param name="textAlign" select="'right'"/>
                    </xsl:call-template>
                </fo:table-row>
            </fo:table-header>
            <fo:table-body>
                <xsl:for-each-group select="$landCovers" group-by="normalize-space(data:Type/data:Code)">
                    <xsl:sort select="av:landCoverSortKey(current-grouping-key())" data-type="number"/>
                    <xsl:sort select="count($landCovers[. &lt;&lt; current-group()[1]]) + 1" data-type="number"/>

                    <xsl:variable name="areaSum" as="xs:decimal" select="av:sumAreas(current-group()/data:AreaShare)"/>
                    <xsl:variable name="typeLabel" as="xs:string"
                                  select="av:extractMultilingualText(current-group()[1]/data:Type/data:Text/data:LocalisedText, $locale)"/>

                    <fo:table-row border-bottom="{$land-description-table-rule} solid black" vertical-align="middle" line-height="{$land-description-row-height}">
                        <xsl:call-template name="applyDebugTableRowAttributes"/>
                        <fo:table-cell>
                            <xsl:call-template name="applyDebugTableCellAttributes"/>
                            <fo:block>
                                <xsl:value-of select="$typeLabel"/>
                            </fo:block>
                        </fo:table-cell>
                        <fo:table-cell>
                            <xsl:call-template name="applyDebugTableCellAttributes"/>
                            <fo:block text-align="right" line-height-shift-adjustment="disregard-shifts">
                                <xsl:value-of select="av:formatSwissArea($areaSum)"/>
                            </fo:block>
                        </fo:table-cell>
                        <fo:table-cell>
                            <xsl:call-template name="applyDebugTableCellAttributes"/>
                            <fo:block text-align="right" line-height-shift-adjustment="disregard-shifts">
                                <xsl:value-of select="av:formatLandCoverPercent($areaSum, $landRegistryArea)"/>
                            </fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </xsl:for-each-group>
            </fo:table-body>
        </fo:table>
    </xsl:template>

    <xsl:template name="insertLandDescriptionBuildingsSection">
        <xsl:param name="realEstate" as="element(data:RealEstate_DPR)?"/>
        <xsl:param name="buildings" as="element(data:Building)*"/>

        <xsl:call-template name="renderLandDescriptionSectionHeading">
            <xsl:with-param name="label" select="$localeXml/data[@name='LandDescription.Buildings.Title']/value/text()"/>
        </xsl:call-template>

        <fo:table table-layout="fixed" width="{$plan-width}" font-size="{$land-description-table-font-size}">
            <xsl:call-template name="applyDebugTableAttributes"/>
            <fo:table-column column-width="{$land-description-building-type-width}"/>
            <fo:table-column column-width="{$land-description-building-egid-width}"/>
            <fo:table-column column-width="{$land-description-building-address-width}"/>
            <fo:table-column column-width="{$land-description-building-zip-width}"/>
            <fo:table-column column-width="{$land-description-building-city-width}"/>
            <fo:table-header>
                <fo:table-row>
                    <xsl:call-template name="applyDebugTableRowAttributes"/>
                    <xsl:call-template name="renderLandDescriptionHeaderCell">
                        <xsl:with-param name="title" select="$localeXml/data[@name='LandDescription.Buildings.Type']/value/text()"/>
                        <xsl:with-param name="subtitle" select="'Bezeichnung'"/>
                    </xsl:call-template>
                    <xsl:call-template name="renderLandDescriptionHeaderCell">
                        <xsl:with-param name="title" select="$localeXml/data[@name='LandDescription.Buildings.Egid']/value/text()"/>
                        <xsl:with-param name="subtitle" select="$localeXml/data[@name='LandDescription.Buildings.EgidLong']/value/text()"/>
                    </xsl:call-template>
                    <xsl:call-template name="renderLandDescriptionHeaderCell">
                        <xsl:with-param name="title" select="$localeXml/data[@name='LandDescription.Buildings.Address']/value/text()"/>
                        <xsl:with-param name="subtitle" select="$localeXml/data[@name='LandDescription.Buildings.AddressLong']/value/text()"/>
                    </xsl:call-template>
                    <xsl:call-template name="renderLandDescriptionHeaderCell">
                        <xsl:with-param name="title" select="$localeXml/data[@name='LandDescription.Buildings.Zip']/value/text()"/>
                    </xsl:call-template>
                    <xsl:call-template name="renderLandDescriptionHeaderCell">
                        <xsl:with-param name="title" select="$localeXml/data[@name='LandDescription.Buildings.City']/value/text()"/>
                    </xsl:call-template>
                </fo:table-row>
            </fo:table-header>
            <fo:table-body>
                <xsl:for-each select="$buildings">
                    <xsl:sort select="av:buildingOriginSortKey(av:buildingOrigin(., $realEstate))" data-type="number"/>

                    <xsl:variable name="building" as="element(data:Building)" select="."/>
                    <xsl:variable name="origin" as="xs:string" select="av:buildingOrigin($building, $realEstate)"/>
                    <xsl:variable name="egid" as="xs:string" select="normalize-space($building/data:EGID)"/>
                    <xsl:variable name="typeLabel" as="xs:string"
                                  select="av:buildingTypeLabel($building, $realEstate, $locale)"/>
                    <xsl:variable name="entrances" as="element(data:BuildingEntrance)*" select="$building/data:BuildingEntrance"/>

                    <xsl:if test="$origin = 'ambiguous'">
                        <xsl:message terminate="yes">Ambiguous building type match for EGID <xsl:value-of select="$egid"/>.</xsl:message>
                    </xsl:if>

                    <xsl:choose>
                        <xsl:when test="exists($entrances)">
                            <xsl:for-each select="$entrances">
                                <xsl:call-template name="renderLandDescriptionBuildingRow">
                                    <xsl:with-param name="typeLabel" select="if (position() = 1) then $typeLabel else ''"/>
                                    <xsl:with-param name="egid" select="if (position() = 1) then $egid else ''"/>
                                    <xsl:with-param name="address" select="av:formatAddressLine(data:Street, data:Number)"/>
                                    <xsl:with-param name="postalCode" select="normalize-space(data:PostalCode)"/>
                                    <xsl:with-param name="city" select="normalize-space(data:City)"/>
                                </xsl:call-template>
                            </xsl:for-each>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:call-template name="renderLandDescriptionBuildingRow">
                                <xsl:with-param name="typeLabel" select="$typeLabel"/>
                                <xsl:with-param name="egid" select="$egid"/>
                            </xsl:call-template>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
            </fo:table-body>
        </fo:table>
    </xsl:template>

    <xsl:template name="insertProjectedObjectsBuildingsSection">
        <xsl:param name="realEstate" as="element(data:RealEstate_DPR)?"/>
        <xsl:param name="buildings" as="element(data:Building)*"/>

        <xsl:call-template name="renderLandDescriptionSectionHeading">
            <xsl:with-param name="label" select="$localeXml/data[@name='ProjectedObjects.ProjectedBuildings.Title']/value/text()"/>
        </xsl:call-template>

        <fo:table table-layout="fixed" width="{$plan-width}" font-size="{$land-description-table-font-size}">
            <xsl:call-template name="applyDebugTableAttributes"/>
            <fo:table-column column-width="{$land-description-building-type-width}"/>
            <fo:table-column column-width="{$land-description-building-egid-width}"/>
            <fo:table-column column-width="{$land-description-building-address-width}"/>
            <fo:table-column column-width="{$land-description-building-zip-width}"/>
            <fo:table-column column-width="{$land-description-building-city-width}"/>
            <fo:table-header>
                <fo:table-row>
                    <xsl:call-template name="applyDebugTableRowAttributes"/>
                    <xsl:call-template name="renderLandDescriptionHeaderCell">
                        <xsl:with-param name="title" select="$localeXml/data[@name='LandDescription.Buildings.Type']/value/text()"/>
                        <xsl:with-param name="subtitle" select="'Bezeichnung'"/>
                    </xsl:call-template>
                    <xsl:call-template name="renderLandDescriptionHeaderCell">
                        <xsl:with-param name="title" select="$localeXml/data[@name='LandDescription.Buildings.Egid']/value/text()"/>
                        <xsl:with-param name="subtitle" select="$localeXml/data[@name='LandDescription.Buildings.EgidLong']/value/text()"/>
                    </xsl:call-template>
                    <xsl:call-template name="renderLandDescriptionHeaderCell">
                        <xsl:with-param name="title" select="$localeXml/data[@name='LandDescription.Buildings.Address']/value/text()"/>
                        <xsl:with-param name="subtitle" select="$localeXml/data[@name='LandDescription.Buildings.AddressLong']/value/text()"/>
                    </xsl:call-template>
                    <xsl:call-template name="renderLandDescriptionHeaderCell">
                        <xsl:with-param name="title" select="$localeXml/data[@name='LandDescription.Buildings.Zip']/value/text()"/>
                    </xsl:call-template>
                    <xsl:call-template name="renderProjectedBuildingCityAreaHeaderCell">
                        <xsl:with-param name="cityLabel" select="$localeXml/data[@name='LandDescription.Buildings.City']/value/text()"/>
                        <xsl:with-param name="areaLabel" select="$localeXml/data[@name='ProjectedObjects.ProjectedBuildings.Area']/value/text()"/>
                    </xsl:call-template>
                </fo:table-row>
            </fo:table-header>
            <fo:table-body>
                <xsl:for-each select="$buildings">
                    <xsl:variable name="building" as="element(data:Building)" select="."/>
                    <xsl:variable name="egid" as="xs:string" select="normalize-space($building/data:EGID)"/>
                    <xsl:variable name="typeLabel" as="xs:string"
                                  select="av:projectedBuildingTypeLabel($building, $realEstate, $locale)"/>
                    <xsl:variable name="areaShare" as="xs:string"
                                  select="av:projectedBuildingAreaLabel($building, $realEstate)"/>
                    <xsl:variable name="entrances" as="element(data:BuildingEntrance)*"
                                  select="$building/data:BuildingEntrance"/>

                    <xsl:choose>
                        <xsl:when test="exists($entrances)">
                            <xsl:for-each select="$entrances">
                                <xsl:call-template name="renderProjectedBuildingRow">
                                    <xsl:with-param name="typeLabel" select="if (position() = 1) then $typeLabel else ''"/>
                                    <xsl:with-param name="egid" select="if (position() = 1) then $egid else ''"/>
                                    <xsl:with-param name="address" select="av:formatAddressLine(data:Street, data:Number)"/>
                                    <xsl:with-param name="postalCode" select="normalize-space(data:PostalCode)"/>
                                    <xsl:with-param name="city" select="normalize-space(data:City)"/>
                                    <xsl:with-param name="area" select="if (position() = 1) then $areaShare else ''"/>
                                </xsl:call-template>
                            </xsl:for-each>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:call-template name="renderProjectedBuildingRow">
                                <xsl:with-param name="typeLabel" select="$typeLabel"/>
                                <xsl:with-param name="egid" select="$egid"/>
                                <xsl:with-param name="area" select="$areaShare"/>
                            </xsl:call-template>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
            </fo:table-body>
        </fo:table>
    </xsl:template>

    <xsl:template name="insertLandDescriptionResponsibleOfficeSection">
        <xsl:param name="responsibleOfficeLine" as="xs:string"/>
        <xsl:param name="responsibleOfficeWebsite" as="xs:string"/>

        <xsl:call-template name="renderOfficeSection">
            <xsl:with-param name="label" select="$localeXml/data[@name='LandDescription.ResponsibleOffice.Title']/value/text()"/>
            <xsl:with-param name="officeLine" select="$responsibleOfficeLine"/>
            <xsl:with-param name="officeWebsite" select="$responsibleOfficeWebsite"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="renderOfficeSection">
        <xsl:param name="label" as="xs:string?"/>
        <xsl:param name="officeLine" as="xs:string"/>
        <xsl:param name="officeWebsite" as="xs:string"/>
        <xsl:param name="spaceBefore" as="xs:string" select="$land-description-section-gap"/>

        <xsl:call-template name="renderLandDescriptionSectionHeading">
            <xsl:with-param name="label" select="$label"/>
            <xsl:with-param name="spaceBefore" select="$spaceBefore"/>
            <xsl:with-param name="fontSize" select="$land-description-responsible-office-heading-font-size"/>
            <xsl:with-param name="lineHeight" select="$land-description-responsible-office-heading-line-height"/>
            <xsl:with-param name="spaceAfter" select="$land-description-responsible-office-heading-gap"/>
        </xsl:call-template>

        <xsl:if test="normalize-space($officeLine)">
            <fo:block font-size="{$land-description-responsible-office-font-size}"
                      line-height="{$land-description-responsible-office-line-height}">
                <xsl:value-of select="$officeLine"/>
            </fo:block>
        </xsl:if>
        <xsl:if test="normalize-space($officeWebsite)">
            <fo:block font-size="{$land-description-responsible-office-font-size}"
                      line-height="{$land-description-responsible-office-line-height}">
                <fo:basic-link text-decoration="none" color="{$land-description-link-color}"
                               external-destination="{concat(&quot;url('&quot;, normalize-space($officeWebsite), &quot;')&quot;)}">
                    <xsl:value-of select="$officeWebsite"/>
                </fo:basic-link>
            </fo:block>
        </xsl:if>
    </xsl:template>

    <xsl:template name="renderLandDescriptionSectionHeading">
        <xsl:param name="label" as="xs:string?"/>
        <xsl:param name="spaceBefore" as="xs:string" select="$land-description-section-gap"/>
        <xsl:param name="spaceAfter" as="xs:string" select="$land-description-heading-to-table-gap"/>
        <xsl:param name="fontSize" as="xs:string" select="$land-description-heading-font-size"/>
        <xsl:param name="lineHeight" as="xs:string" select="$land-description-heading-line-height"/>

        <fo:block margin-top="{$spaceBefore}"
                  space-after="{$spaceAfter}"
                  font-size="{$fontSize}"
                  font-weight="700"
                  line-height="{$lineHeight}"
                  keep-with-next.within-page="always">
            <xsl:value-of select="$label"/>
        </fo:block>
    </xsl:template>

    <xsl:template name="renderLandDescriptionHeaderCell">
        <xsl:param name="title" as="xs:string?"/>
        <xsl:param name="subtitle" as="xs:string?" select="''"/>
        <xsl:param name="textAlign" as="xs:string" select="'left'"/>

        <fo:table-cell padding-bottom="1mm" border-bottom="{$land-description-table-rule} solid black">
            <xsl:call-template name="applyDebugTableCellAttributes"/>
            <fo:block text-align="{$textAlign}" font-size="{$land-description-table-font-size}" font-weight="700" line-height="11pt">
                <xsl:value-of select="$title"/>
            </fo:block>
            <xsl:if test="normalize-space($subtitle)">
                <fo:block text-align="{$textAlign}" font-size="{$land-description-table-header-sub-font-size}" font-weight="700" line-height="7pt">
                    <xsl:value-of select="$subtitle"/>
                </fo:block>
            </xsl:if>
        </fo:table-cell>
    </xsl:template>

    <xsl:template name="renderLandDescriptionBuildingRow">
        <xsl:param name="typeLabel" as="xs:string" select="''"/>
        <xsl:param name="egid" as="xs:string" select="''"/>
        <xsl:param name="address" as="xs:string" select="''"/>
        <xsl:param name="postalCode" as="xs:string" select="''"/>
        <xsl:param name="city" as="xs:string" select="''"/>

        <fo:table-row border-bottom="{$land-description-table-rule} solid black" vertical-align="middle" line-height="{$land-description-row-height}">
            <xsl:call-template name="applyDebugTableRowAttributes"/>
            <fo:table-cell>
                <xsl:call-template name="applyDebugTableCellAttributes"/>
                <fo:block>
                    <xsl:value-of select="$typeLabel"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell>
                <xsl:call-template name="applyDebugTableCellAttributes"/>
                <fo:block>
                    <xsl:value-of select="$egid"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell>
                <xsl:call-template name="applyDebugTableCellAttributes"/>
                <fo:block>
                    <xsl:value-of select="$address"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell>
                <xsl:call-template name="applyDebugTableCellAttributes"/>
                <fo:block>
                    <xsl:value-of select="$postalCode"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell>
                <xsl:call-template name="applyDebugTableCellAttributes"/>
                <fo:block>
                    <xsl:value-of select="$city"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>

    <xsl:template name="renderProjectedBuildingCityAreaHeaderCell">
        <xsl:param name="cityLabel" as="xs:string?"/>
        <xsl:param name="areaLabel" as="xs:string?"/>

        <fo:table-cell padding-bottom="1mm" border-bottom="{$land-description-table-rule} solid black">
            <xsl:call-template name="applyDebugTableCellAttributes"/>
            <fo:table table-layout="fixed" width="100%">
                <xsl:call-template name="applyDebugTableAttributes"/>
                <fo:table-column column-width="19mm"/>
                <fo:table-column column-width="16mm"/>
                <fo:table-body>
                    <fo:table-row>
                        <xsl:call-template name="applyDebugTableRowAttributes"/>
                        <fo:table-cell>
                            <xsl:call-template name="applyDebugTableCellAttributes"/>
                            <fo:block font-size="{$land-description-table-font-size}" font-weight="700" line-height="11pt">
                                <xsl:value-of select="$cityLabel"/>
                            </fo:block>
                        </fo:table-cell>
                        <fo:table-cell>
                            <xsl:call-template name="applyDebugTableCellAttributes"/>
                            <fo:block text-align="right" font-size="{$land-description-table-font-size}" font-weight="700" line-height="11pt">
                                <xsl:value-of select="$areaLabel"/>
                            </fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </fo:table-body>
            </fo:table>
        </fo:table-cell>
    </xsl:template>

    <xsl:template name="renderProjectedBuildingCityAreaCell">
        <xsl:param name="city" as="xs:string" select="''"/>
        <xsl:param name="area" as="xs:string" select="''"/>

        <fo:table-cell>
            <xsl:call-template name="applyDebugTableCellAttributes"/>
            <fo:table table-layout="fixed" width="100%">
                <xsl:call-template name="applyDebugTableAttributes"/>
                <fo:table-column column-width="19mm"/>
                <fo:table-column column-width="16mm"/>
                <fo:table-body>
                    <fo:table-row>
                        <xsl:call-template name="applyDebugTableRowAttributes"/>
                        <fo:table-cell>
                            <xsl:call-template name="applyDebugTableCellAttributes"/>
                            <fo:block>
                                <xsl:value-of select="$city"/>
                            </fo:block>
                        </fo:table-cell>
                        <fo:table-cell>
                            <xsl:call-template name="applyDebugTableCellAttributes"/>
                            <fo:block text-align="right" line-height-shift-adjustment="disregard-shifts">
                                <xsl:value-of select="$area"/>
                            </fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </fo:table-body>
            </fo:table>
        </fo:table-cell>
    </xsl:template>

    <xsl:template name="renderProjectedBuildingRow">
        <xsl:param name="typeLabel" as="xs:string" select="''"/>
        <xsl:param name="egid" as="xs:string" select="''"/>
        <xsl:param name="address" as="xs:string" select="''"/>
        <xsl:param name="postalCode" as="xs:string" select="''"/>
        <xsl:param name="city" as="xs:string" select="''"/>
        <xsl:param name="area" as="xs:string" select="''"/>

        <fo:table-row border-bottom="{$land-description-table-rule} solid black" vertical-align="middle" line-height="{$land-description-row-height}">
            <xsl:call-template name="applyDebugTableRowAttributes"/>
            <fo:table-cell>
                <xsl:call-template name="applyDebugTableCellAttributes"/>
                <fo:block>
                    <xsl:value-of select="$typeLabel"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell>
                <xsl:call-template name="applyDebugTableCellAttributes"/>
                <fo:block>
                    <xsl:value-of select="$egid"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell>
                <xsl:call-template name="applyDebugTableCellAttributes"/>
                <fo:block>
                    <xsl:value-of select="$address"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell>
                <xsl:call-template name="applyDebugTableCellAttributes"/>
                <fo:block>
                    <xsl:value-of select="$postalCode"/>
                </fo:block>
            </fo:table-cell>
            <xsl:call-template name="renderProjectedBuildingCityAreaCell">
                <xsl:with-param name="city" select="$city"/>
                <xsl:with-param name="area" select="$area"/>
            </xsl:call-template>
        </fo:table-row>
    </xsl:template>

    <xsl:template name="renderProjectedObjectsPropertyRow">
        <xsl:param name="number" as="xs:string" select="''"/>
        <xsl:param name="egrid" as="xs:string" select="''"/>
        <xsl:param name="typeLabel" as="xs:string" select="''"/>
        <xsl:param name="previousArea" as="xs:string" select="''"/>
        <xsl:param name="newArea" as="xs:string" select="''"/>

        <fo:table-row border-bottom="{$land-description-table-rule} solid black" vertical-align="middle" line-height="{$land-description-row-height}">
            <xsl:call-template name="applyDebugTableRowAttributes"/>
            <fo:table-cell>
                <xsl:call-template name="applyDebugTableCellAttributes"/>
                <fo:block>
                    <xsl:value-of select="$number"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell>
                <xsl:call-template name="applyDebugTableCellAttributes"/>
                <fo:block>
                    <xsl:value-of select="$egrid"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell>
                <xsl:call-template name="applyDebugTableCellAttributes"/>
                <fo:block>
                    <xsl:value-of select="$typeLabel"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell>
                <xsl:call-template name="applyDebugTableCellAttributes"/>
                <fo:block text-align="right" line-height-shift-adjustment="disregard-shifts">
                    <xsl:value-of select="$previousArea"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell>
                <xsl:call-template name="applyDebugTableCellAttributes"/>
                <fo:block text-align="right" line-height-shift-adjustment="disregard-shifts">
                    <xsl:value-of select="$newArea"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>

    <xsl:template name="renderEmbeddedImage">
        <xsl:param name="imageData" as="xs:string?"/>
        <xsl:param name="width" as="xs:string"/>
        <xsl:param name="height" as="xs:string"/>
        <xsl:param name="altText" as="xs:string"/>

        <fo:block
                margin="0mm"
                padding="0mm"
                space-before="0mm"
                space-after="0mm"
                font-size="0pt"
                line-height="0pt"
                text-align="center">
            <xsl:if test="normalize-space($imageData)">
                    <fo:external-graphic
                        width="{$width}"
                        height="{$height}"
                        scaling="uniform"
                        content-width="scale-to-fit"
                        content-height="scale-to-fit"
                        fox:alt-text="{$altText}">
                    <xsl:attribute name="src"
                                   select="concat(&quot;url('data:image/png;base64,&quot;, av:normalizeImage($imageData), &quot;')&quot;)"/>
                </fo:external-graphic>
            </xsl:if>
        </fo:block>
    </xsl:template>

    <xsl:template name="applyDebugTableAttributes">
        <xsl:if test="$debugTableGrid">
            <xsl:attribute name="border" select="concat($rule-thin, ' solid ', $debug-table-color)"/>
        </xsl:if>
    </xsl:template>

    <xsl:template name="applyDebugTableRowAttributes">
        <xsl:if test="$debugTableGrid">
            <xsl:attribute name="border" select="concat($rule-thin, ' solid ', $debug-row-color)"/>
        </xsl:if>
    </xsl:template>

    <xsl:template name="applyDebugTableCellAttributes">
        <xsl:if test="$debugTableGrid">
            <xsl:attribute name="border" select="concat($rule-thin, ' solid ', $debug-cell-color)"/>
        </xsl:if>
    </xsl:template>

    <xsl:template name="renderKeyValueRow">
        <xsl:param name="label" as="xs:string?"/>
        <xsl:param name="value" as="xs:string?"/>
        <xsl:param name="labelBold" as="xs:boolean" select="false()"/>
        <xsl:param name="valueBold" as="xs:boolean" select="false()"/>

        <fo:table-row border-bottom="{$rule-thin} solid black" vertical-align="middle" line-height="{$row-height}">
            <xsl:call-template name="applyDebugTableRowAttributes"/>
            <fo:table-cell>
                <xsl:call-template name="applyDebugTableCellAttributes"/>
                <fo:block font-weight="{if ($labelBold) then '700' else '400'}">
                    <xsl:value-of select="$label"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell>
                <xsl:call-template name="applyDebugTableCellAttributes"/>
                <fo:block font-weight="{if ($valueBold) then '700' else '400'}">
                    <xsl:value-of select="$value"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>

    <xsl:function name="av:extractMultilingualText" as="xs:string">
        <xsl:param name="localisedTexts" as="element(data:LocalisedText)*"/>
        <xsl:param name="requestedLocale" as="xs:string"/>

        <xsl:sequence
                select="normalize-space(string((
                    $localisedTexts[data:Language = $requestedLocale]/data:Text,
                    $localisedTexts[data:Language = 'de']/data:Text,
                    $localisedTexts[1]/data:Text
                )[1]))"/>
    </xsl:function>

    <xsl:function name="av:isRealObjectStatus" as="xs:boolean">
        <xsl:param name="objectStatus" as="item()*"/>

        <xsl:variable name="statusCode" as="xs:string"
                      select="lower-case(normalize-space(string(($objectStatus/data:Code)[1])))"/>
        <xsl:variable name="statusText" as="xs:string"
                      select="lower-case(normalize-space(string(($objectStatus)[1])))"/>

        <xsl:sequence select="$statusCode = ('actual', 'real') or ($statusCode = '' and $statusText = 'real')"/>
    </xsl:function>

    <xsl:function name="av:isPlannedObjectStatus" as="xs:boolean">
        <xsl:param name="objectStatus" as="item()*"/>

        <xsl:variable name="statusCode" as="xs:string"
                      select="lower-case(normalize-space(string(($objectStatus/data:Code)[1])))"/>
        <xsl:variable name="statusText" as="xs:string"
                      select="lower-case(normalize-space(string(($objectStatus)[1])))"/>

        <xsl:sequence
                select="$statusCode = ('planned', 'projected')
                        or ($statusCode = '' and $statusText = ('planned', 'projektiert'))"/>
    </xsl:function>

    <xsl:function name="av:projectedBuildingTypeLabel" as="xs:string">
        <xsl:param name="building" as="element(data:Building)"/>
        <xsl:param name="realEstate" as="element(data:RealEstate_DPR)?"/>
        <xsl:param name="requestedLocale" as="xs:string"/>

        <xsl:variable name="egid" as="xs:string" select="normalize-space($building/data:EGID)"/>

        <xsl:sequence
                select="
                    if ($egid and exists(
                            $realEstate/data:LandCover[
                                normalize-space(data:EGID) = $egid
                                and av:isPlannedObjectStatus(data:Objectstatus)
                            ]
                        ))
                    then av:extractMultilingualText(
                        $realEstate/data:LandCover[
                            normalize-space(data:EGID) = $egid
                            and av:isPlannedObjectStatus(data:Objectstatus)
                        ][1]/data:Type/data:Text/data:LocalisedText,
                        $requestedLocale
                    )
                    else if ($egid and exists(
                            $realEstate/data:SingleObject[
                                normalize-space(data:EGID) = $egid
                                and av:isPlannedObjectStatus(data:Objectstatus)
                            ]
                        ))
                    then av:extractMultilingualText(
                        $realEstate/data:SingleObject[
                            normalize-space(data:EGID) = $egid
                            and av:isPlannedObjectStatus(data:Objectstatus)
                        ][1]/data:Type/data:Text/data:LocalisedText,
                        $requestedLocale
                    )
                    else 'Gebäude'
                "/>
    </xsl:function>

    <xsl:function name="av:projectedBuildingAreaLabel" as="xs:string">
        <xsl:param name="building" as="element(data:Building)"/>
        <xsl:param name="realEstate" as="element(data:RealEstate_DPR)?"/>

        <xsl:variable name="egid" as="xs:string" select="normalize-space($building/data:EGID)"/>
        <xsl:variable name="landCoverAreaShare" as="item()*"
                      select="
                        if ($egid)
                        then (
                            $realEstate/data:LandCover[
                                normalize-space(data:EGID) = $egid
                                and av:isPlannedObjectStatus(data:Objectstatus)
                            ]/data:AreaShare[normalize-space(.)]
                        )[1]
                        else ()
                      "/>
        <xsl:variable name="singleObjectAreaShare" as="item()*"
                      select="
                        if ($egid)
                        then (
                            $realEstate/data:SingleObject[
                                normalize-space(data:EGID) = $egid
                                and av:isPlannedObjectStatus(data:Objectstatus)
                            ]/data:AreaShare[normalize-space(.)]
                        )[1]
                        else ()
                      "/>

        <xsl:sequence
                select="
                    if (exists($landCoverAreaShare))
                    then av:formatSwissArea($landCoverAreaShare)
                    else if (exists($singleObjectAreaShare))
                    then av:formatSwissArea($singleObjectAreaShare)
                    else ''
                "/>
    </xsl:function>

    <xsl:function name="av:buildingOrigin" as="xs:string">
        <xsl:param name="building" as="element(data:Building)"/>
        <xsl:param name="realEstate" as="element(data:RealEstate_DPR)?"/>

        <xsl:variable name="egid" as="xs:string" select="normalize-space($building/data:EGID)"/>
        <xsl:variable name="landCoverMatches" as="element(data:LandCover)*"
                      select="if ($egid)
                              then $realEstate/data:LandCover[
                                  normalize-space(data:EGID) = $egid
                                  and av:isRealObjectStatus(data:Objectstatus)
                              ]
                              else ()"/>
        <xsl:variable name="singleObjectMatches" as="element(data:SingleObject)*"
                      select="if ($egid)
                              then $realEstate/data:SingleObject[
                                  normalize-space(data:EGID) = $egid
                                  and av:isRealObjectStatus(data:Objectstatus)
                              ]
                              else ()"/>

        <xsl:sequence
                select="
                    if (count($landCoverMatches) = 1 and empty($singleObjectMatches)) then 'landcover'
                    else if (count($singleObjectMatches) = 1 and empty($landCoverMatches)) then 'singleobject'
                    else if (empty($landCoverMatches) and empty($singleObjectMatches)) then 'fallback'
                    else 'ambiguous'
                "/>
    </xsl:function>

    <xsl:function name="av:buildingTypeLabel" as="xs:string">
        <xsl:param name="building" as="element(data:Building)"/>
        <xsl:param name="realEstate" as="element(data:RealEstate_DPR)?"/>
        <xsl:param name="requestedLocale" as="xs:string"/>

        <xsl:variable name="egid" as="xs:string" select="normalize-space($building/data:EGID)"/>
        <xsl:variable name="origin" as="xs:string" select="av:buildingOrigin($building, $realEstate)"/>

        <xsl:sequence
                select="
                    if ($origin = 'landcover')
                    then av:extractMultilingualText(
                        $realEstate/data:LandCover[
                            normalize-space(data:EGID) = $egid
                            and av:isRealObjectStatus(data:Objectstatus)
                        ][1]/data:Type/data:Text/data:LocalisedText,
                        $requestedLocale
                    )
                    else if ($origin = 'singleobject')
                    then av:extractMultilingualText(
                        $realEstate/data:SingleObject[
                            normalize-space(data:EGID) = $egid
                            and av:isRealObjectStatus(data:Objectstatus)
                        ][1]/data:Type/data:Text/data:LocalisedText,
                        $requestedLocale
                    )
                    else 'Gebäude'
                "/>
    </xsl:function>

    <xsl:function name="av:formatSwissDate" as="xs:string">
        <xsl:param name="value" as="item()*"/>

        <xsl:sequence
                select="if (normalize-space(string($value)))
                        then format-dateTime(xs:dateTime(string($value)), '[D01].[M01].[Y0001]')
                        else ''"/>
    </xsl:function>

    <xsl:function name="av:formatSwissTime" as="xs:string">
        <xsl:param name="value" as="item()*"/>

        <xsl:sequence
                select="if (normalize-space(string($value)))
                        then format-dateTime(xs:dateTime(string($value)), '[H01]:[m01]:[s01]')
                        else ''"/>
    </xsl:function>

    <xsl:function name="av:formatSwissArea" as="xs:string">
        <xsl:param name="value" as="item()*"/>

        <xsl:sequence
                select="if (normalize-space(string($value)))
                        then concat(format-number(xs:decimal(string($value)), &quot;#'###&quot;, 'swiss'), ' m²')
                        else ''"/>
    </xsl:function>

    <xsl:function name="av:formatMunicipality" as="xs:string">
        <xsl:param name="name" as="item()*"/>
        <xsl:param name="code" as="item()*"/>

        <xsl:variable name="normalizedName" as="xs:string" select="normalize-space(string($name))"/>
        <xsl:variable name="normalizedCode" as="xs:string" select="normalize-space(string($code))"/>

        <xsl:sequence
                select="if ($normalizedName and $normalizedCode)
                        then concat($normalizedName, ' (', $normalizedCode, ')')
                        else string-join(($normalizedName, $normalizedCode)[. != ''], ' ')"/>
    </xsl:function>

    <xsl:function name="av:formatAddressLine" as="xs:string">
        <xsl:param name="street" as="item()*"/>
        <xsl:param name="number" as="item()*"/>

        <xsl:sequence select="string-join((normalize-space(string($street)), normalize-space(string($number)))[. != ''], ' ')"/>
    </xsl:function>

    <xsl:function name="av:joinNonEmptyLines" as="xs:string">
        <xsl:param name="parts" as="xs:string*"/>

        <xsl:sequence select="string-join($parts[normalize-space(.)], '&#x0A;')"/>
    </xsl:function>

    <xsl:function name="av:sumAreas" as="xs:decimal">
        <xsl:param name="values" as="item()*"/>

        <xsl:sequence
                select="sum(
                    for $value in $values
                    return
                        if (normalize-space(string($value)))
                        then xs:decimal(normalize-space(string($value)))
                        else ()
                )"/>
    </xsl:function>

    <xsl:function name="av:formatLandCoverPercent" as="xs:string">
        <xsl:param name="area" as="item()*"/>
        <xsl:param name="landRegistryArea" as="item()*"/>

        <xsl:variable name="normalizedArea" as="xs:string" select="normalize-space(string($area))"/>
        <xsl:variable name="normalizedLandRegistryArea" as="xs:string" select="normalize-space(string($landRegistryArea))"/>

        <xsl:sequence
                select="
                    if (not($normalizedArea) or not($normalizedLandRegistryArea) or xs:decimal($normalizedLandRegistryArea) = 0)
                    then ''
                    else
                        let $percent := xs:decimal($normalizedArea) div xs:decimal($normalizedLandRegistryArea) * 100
                        return
                            if ($percent lt 1)
                            then '&lt; 1%'
                            else concat(format-number(round($percent), '0'), '%')
                "/>
    </xsl:function>

    <xsl:function name="av:formatOfficeLine" as="xs:string">
        <xsl:param name="office" as="element()?"/>
        <xsl:param name="requestedLocale" as="xs:string"/>

        <xsl:variable name="officeName" as="xs:string"
                      select="av:extractMultilingualText($office/data:Name/data:LocalisedText, $requestedLocale)"/>
        <xsl:variable name="officeAddress" as="xs:string"
                      select="av:formatAddressLine($office/data:Street, $office/data:Number)"/>
        <xsl:variable name="officePostalCity" as="xs:string"
                      select="string-join(($office/data:PostalCode, $office/data:City) ! normalize-space(.), ' ')"/>

        <xsl:sequence select="string-join(($officeName, $officeAddress, $officePostalCity)[normalize-space(.)], ', ')"/>
    </xsl:function>

    <xsl:function name="av:landCoverSortKey" as="xs:integer">
        <xsl:param name="code" as="xs:string?"/>

        <xsl:sequence
                select="
                    if ($code = 'buildings') then 10
                    else if ($code = 'hard_surfaced.roads_tracks') then 20
                    else if ($code = 'hard_surfaced.sidewalk') then 21
                    else if ($code = 'hard_surfaced.traffic_island') then 22
                    else if ($code = 'hard_surfaced.railway') then 23
                    else if ($code = 'hard_surfaced.airport') then 24
                    else if ($code = ('hard_surfaced.waterbasin', 'hard_surfaced.water_basin')) then 25
                    else if ($code = ('hard_surfaced.other_hard_surfaced', 'hard_surfaced.other')) then 26
                    else if (starts-with($code, 'hard_surfaced.')) then 29
                    else if ($code = ('vegetated.meadow_arable_land_pasture', 'vegetated.arable_land_meadow_pasture')) then 30
                    else if ($code = ('vegetated.intensive_cultivation.vineyard', 'vegetated.vineyard')) then 31
                    else if ($code = ('vegetated.intensive_cultivation.other_intensive_cultivation', 'vegetated.other_intensive_cultivation')) then 32
                    else if ($code = 'vegetated.garden') then 33
                    else if ($code = ('vegetated.high_moor', 'vegetated.marsh')) then 34
                    else if ($code = ('vegetated.other_vegetated', 'vegetated.other')) then 35
                    else if (starts-with($code, 'vegetated.')) then 39
                    else if ($code = ('water.standing_water', 'waters.standing_water')) then 40
                    else if ($code = ('water.flowing_water', 'waters.flowing_water')) then 41
                    else if ($code = ('water.reed_belt', 'waters.reed_belt')) then 42
                    else if (starts-with($code, 'water.') or starts-with($code, 'waters.')) then 49
                    else if ($code = ('wooded.dense_forest', 'stocked.dense_forest')) then 50
                    else if ($code = ('wooded.wooded_pasture.dense', 'stocked.wooded_pasture.dense')) then 51
                    else if ($code = ('wooded.wooded_pasture.sparse', 'stocked.wooded_pasture.sparse')) then 52
                    else if ($code = ('wooded.other_wooded', 'stocked.other_stocked')) then 53
                    else if (starts-with($code, 'wooded.') or starts-with($code, 'stocked.')) then 59
                    else if ($code = ('without_vegetation.rock', 'vegetationless.rock')) then 60
                    else if ($code = ('without_vegetation.glacier_snowfield', 'vegetationless.glacier_snowfield')) then 61
                    else if ($code = ('without_vegetation.scree_sand', 'vegetationless.scree_sand')) then 62
                    else if ($code = ('without_vegetation.excavation_landfill', 'vegetationless.excavation_landfill')) then 63
                    else if ($code = ('without_vegetation.other_without_vegetation', 'vegetationless.other_vegetationless')) then 64
                    else if (starts-with($code, 'without_vegetation.') or starts-with($code, 'vegetationless.')) then 69
                    else 999
                "/>
    </xsl:function>

    <xsl:function name="av:buildingOriginSortKey" as="xs:integer">
        <xsl:param name="origin" as="xs:string?"/>

        <xsl:sequence
                select="
                    if ($origin = 'landcover') then 10
                    else if ($origin = 'singleobject') then 20
                    else if ($origin = 'fallback') then 30
                    else 40
                "/>
    </xsl:function>
</xsl:stylesheet>
