<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:cml="http://morphml.org/channelml/schema"
    xmlns:meta="http://morphml.org/metadata/schema" 
    xmlns:net="http://morphml.org/networkml/schema" >

    <xsl:import href="../ReadableUtils.xsl"/>
    
<!--

    This file is used to convert NetworkML files to a "neuroscientist friendly" HTML view
  
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



<!--Main template-->


<xsl:template match="/net:networkml">

<xsl:if test="count(/net:networkml/meta:notes) &gt; 0">
<table frame="box" rules="all" align="centre" cellpadding="4" width ="100%">
    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">General notes</xsl:with-param>
        <xsl:with-param name="comment">Notes present in NetworkML file</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="/net:networkml/meta:notes"/>&lt;/b&gt;</xsl:with-param>
     </xsl:call-template>
</table>
</xsl:if>

<br/>
<xsl:if test="count(/net:networkml/meta:properties) &gt; 0">
<table frame="box" rules="all" align="centre" cellpadding="4" width ="100%">
    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Properties</xsl:with-param>
        <xsl:with-param name="value">&lt;table&gt;
                                        <xsl:for-each select="/net:networkml/meta:properties/meta:property">
                                            &lt;tr&gt; 
                                                &lt;td&gt;<xsl:value-of select="meta:tag"/>&lt;/td&gt; 
                                                &lt;td&gt;= &lt;b&gt;<xsl:value-of select="meta:value"/>&lt;/b&gt;&lt;/td&gt;
                                            &lt;/tr&gt;</xsl:for-each>
                                        &lt;/table&gt;</xsl:with-param>
     </xsl:call-template>
</table>
</xsl:if>

<br/>

<xsl:apply-templates  select="net:populations"/>
<xsl:apply-templates  select="net:projections"/>
<xsl:apply-templates  select="net:inputs"/>

</xsl:template>

<!--End Main template-->


<xsl:template match="net:populations">
    
    <xsl:param name="wholeNetwork" />
    
    <h3>Populations:</h3>

    
        <xsl:for-each select="net:population">
            
            <xsl:element name="a">
                <xsl:attribute name="name">Population_<xsl:value-of select="@name"/></xsl:attribute>
            </xsl:element>
            
            <table frame="box" rules="all" align="centre" cellpadding="4" width ="100%">
                
            
                <xsl:call-template name="tableRow">
                    <xsl:with-param name="name">Name</xsl:with-param>
                    <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="@name"/>&lt;/b&gt;</xsl:with-param>
                </xsl:call-template> 

                <xsl:if test="count(meta:notes) &gt; 0">
                    <xsl:call-template name="tableRow">
                        <xsl:with-param name="name">Notes</xsl:with-param>
                        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="meta:notes"/>&lt;/b&gt;</xsl:with-param>
                     </xsl:call-template>
                </xsl:if>
                
                <xsl:call-template name="tableRow">
                    <xsl:with-param name="name">Cell Type</xsl:with-param>
                    <xsl:with-param name="value">
                        <!-- One or other of attribute @cell_type or net:cell_type should be present-->
                        <xsl:if test="$wholeNetwork = 'yes'">&lt;a href="#CellType_<xsl:value-of select="net:cell_type"/><xsl:value-of select="@cell_type"/>"&gt;</xsl:if>
                        &lt;b&gt;<xsl:value-of select="net:cell_type"/><xsl:value-of select="@cell_type"/>&lt;/b&gt;
                        <xsl:if test="$wholeNetwork = 'yes'">&lt;/a&gt;</xsl:if>
                    </xsl:with-param>
                </xsl:call-template> 
                
                <xsl:apply-templates  select="net:pop_location"/>
                
                <xsl:apply-templates  select="net:instances"/>
                 
                 
            </table>
<br/>
        </xsl:for-each>
</xsl:template>


<xsl:template match="net:pop_location">

    <xsl:apply-templates  select="net:random_arrangement"/>
    <xsl:apply-templates  select="net:grid_arrangement"/>
    
</xsl:template>



<xsl:template match="net:grid_arrangement">
    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Arrangement of cells:</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;Placed regularly in a grid&lt;/b&gt;&lt;br/&gt;
        Location of cell bodies: &lt;b&gt;
            <xsl:apply-templates select="net:spherical_location"/>
            <xsl:apply-templates select="net:rectangular_location"/>&lt;/b&gt;&lt;br/&gt;
            <xsl:apply-templates select="net:spacing"/>
            <xsl:apply-templates select="net:non_spatial_grid"/>
            
        </xsl:with-param>
    </xsl:call-template> 
</xsl:template>


<xsl:template match="net:random_arrangement">
    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Arrangement of cells:</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;Randomly placed in region&lt;/b&gt;&lt;br/&gt;
        Population size: &lt;b&gt;<xsl:value-of select="net:population_size"/>&lt;/b&gt;&lt;br/&gt;
        Location of cell bodies: &lt;b&gt;
            <xsl:apply-templates select="net:spherical_location"/>
            <xsl:apply-templates select="net:rectangular_location"/>&lt;b&gt;
        </xsl:with-param>
    </xsl:call-template> 
</xsl:template>



<xsl:template match="net:non_spatial_grid">
    Number in x dimension of grid: &lt;b&gt;<xsl:value-of select="@x"/>&lt;/b&gt;&lt;br/&gt;
    <xsl:if test="count(@y)&gt;0">Number in y dimension of grid: &lt;b&gt;<xsl:value-of select="@y"/>&lt;/b&gt;&lt;br/&gt;</xsl:if>
    <xsl:if test="count(@z)&gt;0">Number in z dimension of grid: &lt;b&gt;<xsl:value-of select="@z"/>&lt;/b&gt;&lt;br/&gt;</xsl:if>

</xsl:template>


<xsl:template match="net:spacing">
    <xsl:if test="count(@x)&gt;0">Spacing in x direction: &lt;b&gt;<xsl:value-of select="@x"/>&lt;/b&gt;&lt;br/&gt;</xsl:if>
    <xsl:if test="count(@y)&gt;0">Spacing in y direction: &lt;b&gt;<xsl:value-of select="@y"/>&lt;/b&gt;&lt;br/&gt;</xsl:if>
    <xsl:if test="count(@z)&gt;0">Spacing in z direction: &lt;b&gt;<xsl:value-of select="@z"/>&lt;/b&gt;&lt;br/&gt;</xsl:if>
</xsl:template>

<xsl:template match="net:spherical_location">
    Sphere of diameter <xsl:value-of select="meta:center/@diameter"/> located at (<xsl:value-of select="meta:center/@x"/>, <xsl:value-of select="meta:center/@y"/>, <xsl:value-of select="meta:center/@z"/>)
</xsl:template>


<xsl:template match="net:rectangular_location">
    Rectangular box from point (<xsl:value-of select="meta:corner/@x"/>, <xsl:value-of select="meta:corner/@y"/>, <xsl:value-of select="meta:corner/@z"/>) of width: <xsl:value-of select="meta:size/@width"/>, height: <xsl:value-of select="meta:size/@height"/> and depth: <xsl:value-of select="meta:size/@depth"/>
</xsl:template>


<xsl:template match="net:instances">
    <xsl:call-template name="tableRow">
        <xsl:with-param name="name"><xsl:value-of select="count(net:instance)"/> Instances <xsl:if test="count(net:instance) != @size">
            &lt;p&gt;&lt;b&gt;NOTE: Size attribute of instances: <xsl:value-of select="@size"/>, but number found: <xsl:value-of select="count(net:instance)"/>&lt;/b&gt;&lt;/p&gt;
        </xsl:if></xsl:with-param>
        <xsl:with-param name="value">
            <xsl:for-each select="net:instance">&lt;p&gt;&lt;b&gt;<xsl:value-of select="@id"/>: (<xsl:value-of select="net:location/@x"/>, <xsl:value-of select="net:location/@y"/>, <xsl:value-of select="net:location/@z"/>) <xsl:for-each select="@node_id">Computational node: <xsl:value-of select="."/></xsl:for-each>
            &lt;/b&gt;&lt;/p&gt;</xsl:for-each></xsl:with-param>
    </xsl:call-template> 
    
</xsl:template>


<xsl:template match="net:potentialSynapticLocation | net:potential_syn_loc">
    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Potential Synaptic Location</xsl:with-param>
        <xsl:with-param name="comment">The synapse type specified is allowed on a subset of cell segments/cables</xsl:with-param>
        <xsl:with-param name="value"><xsl:choose><xsl:when test="net:synapse_direction = 'pre'">A presynaptic</xsl:when>
        <xsl:when test="net:synapse_direction = 'post'">A postsynaptic</xsl:when>
        <xsl:when test="net:synapse_direction = 'preAndOrPost'">Either a pre or a postsynaptic</xsl:when></xsl:choose>  
 connection using type: &lt;b&gt;<xsl:value-of select="net:synapse_type"/>&lt;/b&gt; is allowed on: &lt;b&gt;<xsl:for-each select="net:group"><xsl:value-of select="."/> </xsl:for-each>&lt;/b&gt;</xsl:with-param>
    </xsl:call-template> 
    
</xsl:template>


<xsl:template match="net:projections">
    
    <xsl:param name="wholeNetwork" />
    <h3>Projections:</h3>

<xsl:variable name="xmlFileUnitSystem"><xsl:value-of select="@units"/></xsl:variable>   
    
    
            <table frame="box" rules="all" align="centre" cellpadding="4" width ="100%">
                <xsl:call-template name="tableRow">
                    <xsl:with-param name="name">Units</xsl:with-param>
                    <xsl:with-param name="comment">Unit system used in synapse properties, etc. below</xsl:with-param>
                    <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="$xmlFileUnitSystem"/>&lt;/b&gt;</xsl:with-param>
                </xsl:call-template>  
            </table>
            <br/>

    
        <xsl:for-each select="net:projection">
            
            <table frame="box" rules="all" align="centre" cellpadding="4" width ="100%">
            
                <xsl:call-template name="tableRow">
                    <xsl:with-param name="name">Projection </xsl:with-param>
                    <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="@name"/>&lt;/b&gt;</xsl:with-param>
                </xsl:call-template>     
                
                <xsl:if test="count(meta:notes) &gt; 0">
                    <xsl:call-template name="tableRow">
                        <xsl:with-param name="name">Notes</xsl:with-param>
                        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="meta:notes"/>&lt;/b&gt;</xsl:with-param>
                     </xsl:call-template>
                </xsl:if>
        
                <xsl:call-template name="tableRow">
                    <xsl:with-param name="name">From:</xsl:with-param>
                    <xsl:with-param name="value">&lt;a href="#Population_<xsl:value-of select="net:source"/><xsl:value-of select="@source"/>"&gt; 
                                                 &lt;b&gt;<xsl:value-of select="net:source"/><xsl:value-of select="@source"/>&lt;/b&gt;&lt;/a&gt; </xsl:with-param>
                </xsl:call-template>          
   
                <xsl:call-template name="tableRow">
                    <xsl:with-param name="name">To:</xsl:with-param>
                    <xsl:with-param name="value">&lt;a href="#Population_<xsl:value-of select="net:target"/><xsl:value-of select="@target"/>"&gt; 
                                                 &lt;b&gt;<xsl:value-of select="net:target"/><xsl:value-of select="@target"/>&lt;/b&gt;&lt;/a&gt;</xsl:with-param>
                </xsl:call-template> 
                
                <xsl:apply-templates  select="net:synapse_props">
                    <xsl:with-param name="wholeNetwork"><xsl:value-of select="$wholeNetwork"/></xsl:with-param>
                </xsl:apply-templates>
                
                <xsl:apply-templates  select="net:connections"/>
                                
            </table>
        <br/>
        </xsl:for-each>
</xsl:template>



<xsl:template match="net:synapse_props">
    
    <xsl:param name="wholeNetwork" />
    
                   <xsl:call-template name="tableRow">
                    <xsl:with-param name="name">Synaptic properties</xsl:with-param>
                    <xsl:with-param name="value">&lt;p&gt;Type: &lt;b&gt;

                    <xsl:if test="$wholeNetwork = 'yes'">&lt;a href="#Synapse_<xsl:value-of select="net:synapse_type"/><xsl:value-of select="@synapse_type"/>"&gt;</xsl:if> <!-- Either should be present-->

                    <xsl:value-of select="net:synapse_type"/><xsl:value-of select="@synapse_type"/>

                    <xsl:if test="$wholeNetwork = 'yes'">&lt;/a&gt;</xsl:if>&lt;/b&gt;
                    
                    
                    <xsl:variable name="timeUnits"><xsl:call-template name="getUnitsInSystem"><xsl:with-param name="quantity">Time</xsl:with-param><xsl:with-param name="xmlFileUnitSystem"><xsl:value-of select="../../@units"/></xsl:with-param></xsl:call-template><xsl:value-of select="@units"/></xsl:variable>   
                    <xsl:variable name="voltUnits"><xsl:call-template name="getUnitsInSystem"><xsl:with-param name="quantity">Voltage</xsl:with-param><xsl:with-param name="xmlFileUnitSystem"><xsl:value-of select="../../@units"/></xsl:with-param></xsl:call-template><xsl:value-of select="@units"/></xsl:variable>   
                    
                    
                    &lt;/b&gt;&lt;/p&gt;
                    
                    <xsl:if test="count(@internal_delay) &gt; 0">&lt;p&gt;Delay: &lt;b&gt;<xsl:value-of select="@internal_delay"/> <xsl:value-of select="$timeUnits"/> (internal)&lt;/b&gt;&lt;/p&gt;</xsl:if>
                    <xsl:if test="count(@pre_delay) &gt; 0">&lt;p&gt;Delay: &lt;b&gt;<xsl:value-of select="@pre_delay"/> <xsl:value-of select="$timeUnits"/> (presynaptic component)&lt;/b&gt;&lt;/p&gt;</xsl:if>
                    <xsl:if test="count(@post_delay) &gt; 0">&lt;p&gt;Delay: &lt;b&gt;<xsl:value-of select="@post_delay"/>  <xsl:value-of select="$timeUnits"/>(postsynaptic component)&lt;/b&gt;&lt;/p&gt;</xsl:if>
                    <xsl:if test="count(@prop_delay) &gt; 0">&lt;p&gt;Delay: &lt;b&gt;<xsl:value-of select="@prop_delay"/> <xsl:value-of select="$timeUnits"/> (AP propagation component)&lt;/b&gt;&lt;/p&gt;</xsl:if>
                    <xsl:if test="count(@weight) &gt; 0">&lt;p&gt;Weight: &lt;b&gt;<xsl:value-of select="@weight"/>&lt;/b&gt;&lt;/p&gt;</xsl:if>
                    <xsl:if test="count(@threshold) &gt; 0">&lt;p&gt;Threshold: &lt;b&gt;<xsl:value-of select="@threshold"/> <xsl:value-of select="$voltUnits"/>&lt;/b&gt;&lt;/p&gt;</xsl:if>

                    
                    <xsl:for-each select="net:default_values">
                        <xsl:if test="count(@internal_delay) &gt; 0">&lt;p&gt;&lt;b&gt;Delay: <xsl:value-of select="@internal_delay"/> <xsl:value-of select="$timeUnits"/> (internal)&lt;/b&gt;&lt;/p&gt;</xsl:if>
                        <xsl:if test="count(@pre_delay) &gt; 0">&lt;p&gt;&lt;b&gt;Delay: <xsl:value-of select="@pre_delay"/> <xsl:value-of select="$timeUnits"/> (presynaptic component)&lt;/b&gt;&lt;/p&gt;</xsl:if>
                        <xsl:if test="count(@post_delay) &gt; 0">&lt;p&gt;&lt;b&gt;Delay: <xsl:value-of select="@post_delay"/>  <xsl:value-of select="$timeUnits"/>(postsynaptic component)&lt;/b&gt;&lt;/p&gt;</xsl:if>
                        <xsl:if test="count(@prop_delay) &gt; 0">&lt;p&gt;&lt;b&gt;Delay: <xsl:value-of select="@prop_delay"/> <xsl:value-of select="$timeUnits"/> (AP propagation component)&lt;/b&gt;&lt;/p&gt;</xsl:if>
                        <xsl:if test="count(@weight) &gt; 0">&lt;p&gt;&lt;b&gt;Weight: <xsl:value-of select="@weight"/>&lt;/b&gt;&lt;/p&gt;</xsl:if>
                        <xsl:if test="count(@threshold) &gt; 0">&lt;p&gt;&lt;b&gt;Threshold: <xsl:value-of select="@threshold"/> <xsl:value-of select="$voltUnits"/>&lt;/b&gt;&lt;/p&gt;</xsl:if>

                    </xsl:for-each>
                    </xsl:with-param>
                    
                    
                    
                   
                </xsl:call-template>     
</xsl:template>


<xsl:template match="net:connections">

    <xsl:variable name="timeUnits"><xsl:call-template name="getUnitsInSystem"><xsl:with-param name="quantity">Time</xsl:with-param><xsl:with-param name="xmlFileUnitSystem"><xsl:value-of select="../../@units"/></xsl:with-param></xsl:call-template><xsl:value-of select="@units"/></xsl:variable>   
    <xsl:variable name="voltUnits"><xsl:call-template name="getUnitsInSystem"><xsl:with-param name="quantity">Voltage</xsl:with-param><xsl:with-param name="xmlFileUnitSystem"><xsl:value-of select="../../@units"/></xsl:with-param></xsl:call-template><xsl:value-of select="@units"/></xsl:variable>   

    
    <xsl:if test="count(net:connection) &gt; 0">
        <xsl:call-template name="tableRow">
            <xsl:with-param name="name"><xsl:value-of select="count(net:connection)"/> connection instance(s):</xsl:with-param>
            <xsl:with-param name="value">
                <xsl:for-each select="net:connection">
                   
                        &lt;p&gt;&lt;b&gt;<xsl:value-of select="@id"/>: From <xsl:if test="count(net:pre/@segment_id) &gt; 0 or count(@pre_segment_id) &gt; 0">
                            segment <xsl:value-of select="net:pre/@segment_id"/><xsl:value-of select="@pre_segment_id"/>
                            <xsl:if test="count(net:pre/@fraction_along) &gt; 0 or count(@pre_fraction_along) &gt; 0"> (fract along: 
                            <xsl:value-of select="net:pre/@fraction_along"/><xsl:value-of select="@pre_fraction_along"/>)</xsl:if> on </xsl:if>
                            source cell <xsl:value-of select="net:pre/@cell_id"/><xsl:value-of select="@pre_cell_id"/>
                            to <xsl:if test="count(net:post/@segment_id) &gt; 0 or count(@post_segment_id) &gt; 0"> 
                                segment <xsl:value-of select="net:post/@segment_id"/><xsl:value-of select="@post_segment_id"/>
                            <xsl:if test="count(net:post/@fraction_along) &gt; 0 or count(@post_fraction_along) &gt; 0"> (fract along: 
                            <xsl:value-of select="net:post/@fraction_along"/><xsl:value-of select="@post_fraction_along"/>)</xsl:if> on </xsl:if>
                            target cell <xsl:value-of select="net:post/@cell_id"/><xsl:value-of select="@post_cell_id"/>

                        <xsl:for-each select="net:properties">
                        <xsl:if test="count(@synapse_type) &gt; 0">, Synapse: <xsl:value-of select="@synapse_type"/></xsl:if>
                        <xsl:if test="count(@internal_delay) &gt; 0">, delay: <xsl:value-of select="@internal_delay"/><xsl:value-of select="$timeUnits"/>  (internal)</xsl:if>
                        <xsl:if test="count(@pre_delay) &gt; 0">, delay: <xsl:value-of select="@pre_delay"/>  <xsl:value-of select="$timeUnits"/> (presynaptic component)</xsl:if>
                        <xsl:if test="count(@post_delay) &gt; 0">, delay: <xsl:value-of select="@post_delay"/> <xsl:value-of select="$timeUnits"/> (postsynaptic component)</xsl:if>
                        <xsl:if test="count(@prop_delay) &gt; 0">, delay: <xsl:value-of select="@prop_delay"/> <xsl:value-of select="$timeUnits"/> (AP propagation component)</xsl:if>
                        <xsl:if test="count(@weight) &gt; 0">, weight: <xsl:value-of select="@weight"/></xsl:if>
                        <xsl:if test="count(@threshold) &gt; 0">, threshold: <xsl:value-of select="@threshold"/> <xsl:value-of select="$voltUnits"/></xsl:if>
                        </xsl:for-each>


    &lt;/b&gt;&lt;/p&gt;
                </xsl:for-each>
            </xsl:with-param>
        </xsl:call-template>     
    </xsl:if>
</xsl:template>


<xsl:template match="net:inputs">
<h3>Inputs:</h3>

<xsl:variable name="xmlFileUnitSystem"><xsl:value-of select="@units"/></xsl:variable>   

            <table frame="box" rules="all" align="centre" cellpadding="4" width ="100%">
                <xsl:call-template name="tableRow">
                    <xsl:with-param name="name">Units</xsl:with-param>
                    <xsl:with-param name="comment">Unit system used in synapse properties, etc. below</xsl:with-param>
                    <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="$xmlFileUnitSystem"/>&lt;/b&gt;</xsl:with-param>
                </xsl:call-template>  
            </table>
            <br/>

    <xsl:for-each select="net:input">

        <table frame="box" rules="all" align="centre" cellpadding="4" width ="100%">

            <xsl:call-template name="tableRow">
                <xsl:with-param name="name">Input</xsl:with-param>
                <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="@name"/>&lt;/b&gt;</xsl:with-param>
            </xsl:call-template>     
            
            <xsl:if test="count(meta:notes) &gt; 0">
                <xsl:call-template name="tableRow">
                    <xsl:with-param name="name">Notes</xsl:with-param>
                    <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="meta:notes"/>&lt;/b&gt;</xsl:with-param>
                 </xsl:call-template>
            </xsl:if>

            <xsl:apply-templates  select="net:pulse_input"/>
            <xsl:apply-templates  select="net:random_stim"/>
            <xsl:apply-templates  select="net:target"/>
        </table>
        <br/>
        
    </xsl:for-each>

</xsl:template>

<xsl:template match="net:pulse_input">

    <xsl:variable name="timeUnits"><xsl:call-template name="getUnitsInSystem"><xsl:with-param name="quantity">Time</xsl:with-param><xsl:with-param name="xmlFileUnitSystem"><xsl:value-of select="../../@units"/></xsl:with-param></xsl:call-template><xsl:value-of select="@units"/></xsl:variable>   
    <xsl:variable name="currentUnits"><xsl:call-template name="getUnitsInSystem"><xsl:with-param name="quantity">Current</xsl:with-param><xsl:with-param name="xmlFileUnitSystem"><xsl:value-of select="../../@units"/></xsl:with-param></xsl:call-template><xsl:value-of select="@units"/></xsl:variable>   

    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Pulse input</xsl:with-param>
        <xsl:with-param name="value">&lt;p&gt;Delay: &lt;b&gt;<xsl:value-of select="@delay"/> <xsl:value-of select="$timeUnits"/> &lt;/b&gt;&lt;/p&gt;
                                     &lt;p&gt;Duration: &lt;b&gt;<xsl:value-of select="@duration"/> <xsl:value-of select="$timeUnits"/> &lt;/b&gt;&lt;/p&gt;
                                     &lt;p&gt;Amplitude: &lt;b&gt;<xsl:value-of select="@amplitude"/> <xsl:value-of select="$currentUnits"/> &lt;/b&gt;&lt;/p&gt;</xsl:with-param>
    </xsl:call-template>     

</xsl:template>


<xsl:template match="net:random_stim">

    <xsl:variable name="freqUnits"><xsl:call-template name="getUnitsInSystem"><xsl:with-param name="quantity">InvTime</xsl:with-param><xsl:with-param name="xmlFileUnitSystem"><xsl:value-of select="../../@units"/></xsl:with-param></xsl:call-template><xsl:value-of select="@units"/></xsl:variable>   

    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Random stimulation</xsl:with-param>
        <xsl:with-param name="value">&lt;p&gt;Frequency: &lt;b&gt;<xsl:value-of select="@frequency"/> <xsl:value-of select="$freqUnits"/>&lt;/b&gt;&lt;/p&gt;
                                     &lt;p&gt;Synaptic mechanism: &lt;b&gt;<xsl:value-of select="@synaptic_mechanism"/>&lt;/b&gt;&lt;/p&gt;</xsl:with-param>
    </xsl:call-template>     

</xsl:template>




<xsl:template match="net:target">

    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Target of stimulation</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="@cell_group"/><xsl:value-of select="@population"/>&lt;/b&gt;</xsl:with-param> <!-- Only one of these should be present...-->
    </xsl:call-template>     
    
    <xsl:if test="count(net:sites/net:site) &gt; 0">

        <xsl:call-template name="tableRow">
            <xsl:with-param name="name">Cells receiving input:</xsl:with-param>
            <xsl:with-param name="value">
                <xsl:for-each select="net:sites/net:site">
                   
                    &lt;p&gt;&lt;b&gt;
                        Cell: <xsl:value-of select="@cell_id"/>
                        <xsl:if test="count(@segment_id) &gt; 0">, segment <xsl:value-of select="@segment_id"/> </xsl:if>
                        <xsl:if test="count(@fraction_along) &gt; 0"> (fract along: <xsl:value-of select="@fraction_along"/>)</xsl:if> 
                    &lt;/b&gt;&lt;/p&gt;
                </xsl:for-each>
                      
            </xsl:with-param>
        </xsl:call-template>          
        
    </xsl:if>

</xsl:template>







</xsl:stylesheet>
