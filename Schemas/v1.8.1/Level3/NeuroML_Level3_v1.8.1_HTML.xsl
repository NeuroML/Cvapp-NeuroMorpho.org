<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:mml="http://morphml.org/morphml/schema"
    xmlns:meta="http://morphml.org/metadata/schema"
    xmlns:nml="http://morphml.org/neuroml/schema"
    xmlns:cml="http://morphml.org/channelml/schema"
    xmlns:bio="http://morphml.org/biophysics/schema"
    xmlns:net="http://morphml.org/networkml/schema" >
    
    <xsl:import href="NetworkML_v1.8.1_HTML.xsl"/>
    <xsl:import href="../Level2/NeuroML_Level2_v1.8.1_HTML.xsl"/>

<!--

    This file is used to convert a Level 3 NeuroML files to a "neuroscientist friendly" view
    
    Funding for this work has been received from the Medical Research Council and the 
    Wellcome Trust. This file was initially developed as part of the neuroConstruct project
    
    Author: Padraig Gleeson
    Copyright 2009 University College London
    
    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.
    
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
    
-->

<xsl:output method="html" indent="yes" />

<!--Main Level 1 template-->

<xsl:template match="/mml:morphml">
<h3>NeuroML Level 1 file</h3>

<xsl:if test="count(/mml:morphml/meta:notes) &gt; 0">
<table frame="box" rules="all" align="centre" cellpadding="4" width ="100%">
    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">General notes</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="/mml:morphml/meta:notes"/>&lt;/b&gt;</xsl:with-param>
     </xsl:call-template>
</table>
</xsl:if>



<xsl:if test="count(/mml:morphml/meta:properties) &gt; 0">
<br/>
<table frame="box" rules="all" align="centre" cellpadding="4" width ="100%">

    <xsl:apply-templates  select="/mml:morphml/meta:properties"/>
    
</table>
</xsl:if>

<xsl:apply-templates  select="/mml:morphml/mml:cells"/>

<br/>

</xsl:template>
<!--End Main template-->



<!--Main Level 2/3 template-->

<xsl:template match="/nml:neuroml">
<h3>NeuroML Level 3 file</h3>

<xsl:if test="count(/nml:neuroml/meta:notes) &gt; 0">
<table frame="box" rules="all" align="centre" cellpadding="4" width ="100%">
    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">General notes</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="/nml:neuroml/meta:notes"/>&lt;/b&gt;</xsl:with-param>
     </xsl:call-template>
</table>
</xsl:if>


<xsl:if test="count(/net:neuroml/meta:properties) &gt; 0">
<br/>
<table frame="box" rules="all" align="centre" cellpadding="4" width ="100%">

    <xsl:apply-templates  select="/mml:morphml/meta:properties"/>
</table>
</xsl:if>

<xsl:apply-templates  select="/nml:neuroml/nml:cells"/>

<br/>

<table frame="box" rules="all" align="centre" cellpadding="4" width ="100%">
    
<xsl:apply-templates  select="/nml:neuroml/nml:channels"/>


<br/>

<xsl:apply-templates  select="//net:populations">
        <xsl:with-param name="wholeNetwork">yes</xsl:with-param>
</xsl:apply-templates>
<br/>
<xsl:apply-templates  select="//net:projections">
        <xsl:with-param name="wholeNetwork">yes</xsl:with-param>
</xsl:apply-templates>
<xsl:apply-templates  select="//net:inputs">
        <xsl:with-param name="wholeNetwork">yes</xsl:with-param>
</xsl:apply-templates>

</table>


<br/>

</xsl:template>
<!--End Main template-->




<xsl:template match="nml:cell|mml:cell">
<xsl:element name="a">
    <xsl:attribute name="name">CellType_<xsl:value-of select="@name"/></xsl:attribute>
</xsl:element>
<h3>Cell: <xsl:value-of select="@name"/></h3>


<table frame="box" rules="all" align="centre" cellpadding="4" width ="100%">
<xsl:call-template name="tableRow">
        <xsl:with-param name="name">Name</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="@name"/>&lt;/b&gt;</xsl:with-param>
</xsl:call-template>


<xsl:apply-templates select="nml:status"/>
<xsl:apply-templates select="mml:status"/>

<xsl:if test="count(meta:notes) &gt; 0">
<xsl:call-template name="tableRow">
        <xsl:with-param name="name">Description</xsl:with-param>
        <xsl:with-param name="comment">As described in the NeuroML file</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="meta:notes"/>&lt;/b&gt;</xsl:with-param>
</xsl:call-template>
</xsl:if>

<xsl:apply-templates  select="meta:properties"/>

<xsl:apply-templates select="meta:authorList"/>

<xsl:apply-templates select="meta:publication"/>

<xsl:apply-templates select="meta:neuronDBref"/>



<xsl:call-template name="tableRow">
        <xsl:with-param name="name">Total number of segments</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="count(mml:segments/mml:segment)"/>&lt;/b&gt;</xsl:with-param>
</xsl:call-template>

<xsl:call-template name="tableRow">
        <xsl:with-param name="name">Total number of cables</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="count(mml:cables/mml:cable)"/>
        with <xsl:value-of select="count(mml:cables/mml:cable[meta:group='soma_group'])"/> soma cable(s),
        <xsl:value-of select="count(mml:cables/mml:cable[meta:group='dendrite_group'])"/> dendritic cable(s)
        and <xsl:value-of select="count(mml:cables/mml:cable[meta:group='axon_group'])"/> axonal cable(s)&lt;/b&gt;
        </xsl:with-param>
</xsl:call-template>


<xsl:apply-templates  select="mml:cables"/>

</table>

<xsl:apply-templates  select="nml:biophysics"/>



</xsl:template>


<xsl:template match="nml:biophysics">

<table frame="box" rules="all" align="centre" cellpadding="4" width ="100%">

<h3>Biophysical properties of cell: <xsl:value-of select="../@name"/></h3>



    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Unit system of biophysical entities</xsl:with-param>
        <xsl:with-param name="comment">This can be either <b>SI Units</b> or <b>Physiological Units</b></xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="@units"/>&lt;/b&gt;</xsl:with-param>
     </xsl:call-template>
     
     
     <xsl:apply-templates/>
     
</table> <!-- round off table...-->
</xsl:template>










</xsl:stylesheet>
