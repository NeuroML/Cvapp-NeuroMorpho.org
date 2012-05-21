<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:mml="http://morphml.org/morphml/schema"
    xmlns:meta="http://morphml.org/metadata/schema"
    xmlns:nml="http://morphml.org/neuroml/schema"
    xmlns:cml="http://morphml.org/channelml/schema"
    xmlns:bio="http://morphml.org/biophysics/schema"
    xmlns:net="http://morphml.org/networkml/schema"
    exclude-result-prefixes="mml meta nml net cml bio">
        
<!-- Note that in the stylesheet below, you must use the namespaces as defined above, not as in the xml document -->
<!--

    This file is used to convert NeuroML files (morphology and/or network structure)
    to X3D files, for visualisation of 3D structure in any browser with an X3D plugin or 
    X3D standalone application
    
    Funding for this work has been received from the Medical Research Council and the 
    Wellcome Trust. This file was initially developed as part of the neuroConstruct project
    
    Author: Padraig Gleeson & Aditya Gilra
    
    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.
    
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
    
-->

<xsl:variable name="showAxes">1</xsl:variable>
<xsl:variable name="defaultCellRadius">5</xsl:variable>
<xsl:variable name="defaultSomaAppearance">0.5 0.5 0.5</xsl:variable>
<xsl:variable name="defaultDendriteAppearance">0.2 0.2 0.2</xsl:variable>

<xsl:output method="xml" indent="yes" />

<xsl:template  match="/">
    
<X3D profile="Immersive" version="3.1" xmlns:xsd='http://www.w3.org/2001/XMLSchema-instance' xsd:noNamespaceSchemaLocation=' http://www.web3d.org/specifications/x3d-3.1.xsd '>
    <xsl:comment>Main Scene</xsl:comment>
    <Scene>
        <Background skyColor="0.6 0.7 0.9"/>
        <Viewpoint description="Down z axis, 500 microns away" position="0 0 500"/> 
        <Viewpoint description="Down z axis, 200 microns away" position="0 0 200"/> 
        <Viewpoint description="Down z axis, 2mm away" position="0 0 2000"/> 
        
    <xsl:if test="$showAxes = 1">
        <xsl:call-template name="showAxes"/>
    
    </xsl:if> 
    
    <xsl:apply-templates select="nml:neuroml"/>
    
    <xsl:apply-templates select="mml:morphml"/>
    
    <xsl:apply-templates select="net:networkml"/>
    
    </Scene>

    
</X3D>

</xsl:template>

<xsl:template match="mml:morphml | nml:neuroml">

    <xsl:text>

    </xsl:text>
    <xsl:comment>Processing &lt;morphml>/&lt;neuroml> element</xsl:comment>
    <xsl:choose>

      <!-- when populations of cells are present: -->
      <xsl:when test="net:populations">
        <xsl:for-each select="net:populations/net:population">
          <xsl:variable name="pop_name"><xsl:value-of select="@name"/></xsl:variable>
          <xsl:variable name="cell_type"><xsl:value-of select="@cell_type"/></xsl:variable>

          <xsl:variable name="myColor">
            <xsl:choose>
              <xsl:when test="position() = 1">1 0 0</xsl:when>
              <xsl:when test="position() = 2">0 1 0</xsl:when>
              <xsl:when test="position() = 3">0 0 1</xsl:when>
              <xsl:when test="position() = 4">1 0 1</xsl:when>
              <xsl:when test="position() = 5">0 1 1</xsl:when>
              <xsl:when test="position() = 6">1 1 0</xsl:when>
              <xsl:otherwise><xsl:value-of select="$defaultSomaAppearance"/></xsl:otherwise>
            </xsl:choose>
           </xsl:variable>
           
          <xsl:variable name="somaAppearance"><xsl:choose>
              <xsl:when test="count(meta:properties/meta:property[@tag='color']) &gt; 0"><xsl:value-of select="meta:properties/meta:property[@tag='color']/@value"/></xsl:when>
              <xsl:otherwise><xsl:value-of select="$myColor"/></xsl:otherwise>
          </xsl:choose></xsl:variable>
          <xsl:variable name="dendriteAppearance"><xsl:choose>
              <xsl:when test="count(meta:properties/meta:property[@tag='color']) &gt; 0"><xsl:value-of select="meta:properties/meta:property[@tag='color']/@value"/></xsl:when>
              <xsl:otherwise><xsl:value-of select="$myColor"/></xsl:otherwise>
          </xsl:choose></xsl:variable>

          <xsl:for-each select="net:instances/net:instance">
              <xsl:variable name="cell_x"><xsl:value-of select="net:location/@x"/></xsl:variable>
              <xsl:variable name="cell_y"><xsl:value-of select="net:location/@y"/></xsl:variable>
              <xsl:variable name="cell_z"><xsl:value-of select="net:location/@z"/></xsl:variable>

              <xsl:comment>Cell <xsl:value-of select="@id"/> of <xsl:value-of select="$pop_name"/> is located at: (<xsl:value-of select="$cell_x"/>, <xsl:value-of select="$cell_y"/>, <xsl:value-of select="$cell_z"/>)</xsl:comment>
           
              <xsl:call-template name="positioned_cell_draw">
                <xsl:with-param name="cell_type" select="$cell_type"/>
                <xsl:with-param name="x" select="$cell_x"/>
                <xsl:with-param name="y" select="$cell_y"/>
                <xsl:with-param name="z" select="$cell_z"/>
                <xsl:with-param name="somaAppearance" select="$somaAppearance"/>
                <xsl:with-param name="dendriteAppearance" select="$dendriteAppearance"/>
              </xsl:call-template>

          </xsl:for-each>
          
        </xsl:for-each>
        <xsl:for-each select="net:projections/net:projection">
            <xsl:variable name="src"><xsl:value-of select="net:source"/><xsl:value-of select="@source"/></xsl:variable> <!-- Only one of attr or sub element should be present-->
            <xsl:variable name="tgt"><xsl:value-of select="net:target"/><xsl:value-of select="@target"/></xsl:variable> <!-- Only one of attr or sub element should be present-->
            <xsl:comment>Projection <xsl:value-of select="@name"/> between <xsl:value-of select="$src"/> and <xsl:value-of select="$tgt"/></xsl:comment>
            
            <xsl:for-each select="net:connections/net:connection">
                <xsl:variable name="preCellId"><xsl:value-of select="net:pre/@cell_id"/><xsl:value-of select="@pre_cell_id"/></xsl:variable> <!-- Only one of attr or sub element should be present-->
                <xsl:variable name="postCellId"><xsl:value-of select="net:post/@cell_id"/><xsl:value-of select="@post_cell_id"/></xsl:variable> <!-- Only one of attr or sub element should be present-->


                <xsl:variable name="preSegmentId"><xsl:choose>
                    <xsl:when test="count(net:pre) &gt; 0"><xsl:value-of select="net:pre/@segment_id"/></xsl:when>
                    <xsl:when test="count(@pre_segment_id) &gt; 0"><xsl:value-of select="@pre_segment_id"/></xsl:when>
                    <xsl:otherwise>0</xsl:otherwise></xsl:choose>
                </xsl:variable>

                <xsl:variable name="postSegmentId"><xsl:choose>
                    <xsl:when test="count(net:post) &gt; 0"><xsl:value-of select="net:post/@segment_id"/></xsl:when>
                    <xsl:when test="count(@post_segment_id) &gt; 0"><xsl:value-of select="@post_segment_id"/></xsl:when>
                    <xsl:otherwise>0</xsl:otherwise></xsl:choose>
                </xsl:variable>

                <xsl:variable name="preFract"><xsl:choose>
                    <xsl:when test="count(net:pre) &gt; 0"><xsl:value-of select="net:pre/@fraction_along"/></xsl:when>
                    <xsl:when test="count(@pre_fraction_along) &gt; 0"><xsl:value-of select="@pre_fraction_along"/></xsl:when>
                    <xsl:otherwise>0.5</xsl:otherwise></xsl:choose>
                </xsl:variable>

                <xsl:variable name="postFract"><xsl:choose>
                    <xsl:when test="count(net:post) &gt; 0"><xsl:value-of select="net:post/@fraction_along"/></xsl:when>
                    <xsl:when test="count(@post_fraction_along) &gt; 0"><xsl:value-of select="@post_fraction_along"/></xsl:when>
                    <xsl:otherwise>0.5</xsl:otherwise></xsl:choose>
                </xsl:variable>
                
            <xsl:comment>Connection between <xsl:value-of select="$preCellId"/> (seg: <xsl:value-of select="$preSegmentId"/>,  <xsl:value-of select="$preFract"/>) and <xsl:value-of
            select="$postCellId"/> (seg: <xsl:value-of select="$postSegmentId"/>,  <xsl:value-of select="$postFract"/>)</xsl:comment>
            
                <xsl:variable name="preCellType">
                    <xsl:for-each select="/nml:neuroml/net:populations/net:population[@name = $src]">
                        <xsl:value-of select="@cell_type"/>
                    </xsl:for-each>
                </xsl:variable>
                <xsl:variable name="postCellType">
                    <xsl:for-each select="/nml:neuroml/net:populations/net:population[@name = $tgt]">
                        <xsl:value-of select="@cell_type"/>
                    </xsl:for-each>
                </xsl:variable>

                <xsl:variable name="preLocation">
                  <xsl:for-each select="/nml:neuroml/net:populations/net:population[@name = $src]/net:instances/net:instance[@id = $preCellId]/net:location">
                      <xsl:call-template name="writeLocation">
                        <xsl:with-param name="CellType" select="$preCellType"/>
                        <xsl:with-param name="SegId" select="$preSegmentId"/>
                        <xsl:with-param name="Fract" select="$preFract"/>
                        <xsl:with-param name="Cellx" select="@x"/>
                        <xsl:with-param name="Celly" select="@y"/>
                        <xsl:with-param name="Cellz" select="@z"/>
                      </xsl:call-template>
                  </xsl:for-each>
                </xsl:variable>
                
                <xsl:variable name="postLocation">
                  <xsl:for-each select="/nml:neuroml/net:populations/net:population[@name = $tgt]/net:instances/net:instance[@id = $postCellId]/net:location">
                      <xsl:call-template name="writeLocation">
                        <xsl:with-param name="CellType" select="$postCellType"/>
                        <xsl:with-param name="SegId" select="$postSegmentId"/>
                        <xsl:with-param name="Fract" select="$postFract"/>
                        <xsl:with-param name="Cellx" select="@x"/>
                        <xsl:with-param name="Celly" select="@y"/>
                        <xsl:with-param name="Cellz" select="@z"/>
                      </xsl:call-template>
                  </xsl:for-each>
                </xsl:variable>
                
                <Transform>
                    <Shape>
                        <Appearance>
                            <Material/>
                        </Appearance>
                        <LineSet vertexCount="2">
                            <xsl:element name="Coordinate">
                                <xsl:attribute name="point"><xsl:value-of select="$preLocation"/>, <xsl:value-of select="$postLocation"/></xsl:attribute>
                            </xsl:element>
                            <Color color="0 1 0, 1 0 0"/><!-- Green to red-->
                        </LineSet>
                    </Shape>
                </Transform>

            </xsl:for-each> 
        </xsl:for-each>

      </xsl:when>
      <!-- this displays single untranslated cells if there are no populations -->
      <xsl:otherwise>

        <xsl:comment>Displaying each cell present</xsl:comment>

        <xsl:for-each select="mml:cells/mml:cell | nml:cells/nml:cell">
            <xsl:apply-templates select="."/>
        </xsl:for-each>

      </xsl:otherwise>
    </xsl:choose>
</xsl:template>

<xsl:template name="writeLocation">
    <xsl:param name="CellType"/>
    <xsl:param name="SegId"/>
    <xsl:param name="Fract"/>
    <xsl:param name="Cellx"/>
    <xsl:param name="Celly"/>
    <xsl:param name="Cellz"/>

    <xsl:for-each select="/nml:neuroml/nml:cells/nml:cell[@name = $CellType]/mml:segments/mml:segment[@id = $SegId]">

      <xsl:variable name="SegDx" select="mml:distal/@x"/>
      <xsl:variable name="SegDy" select="mml:distal/@y"/>
      <xsl:variable name="SegDz" select="mml:distal/@z"/>



        <xsl:variable name="SegPx">
            <xsl:choose>
                <xsl:when test="count(mml:proximal) &gt; 0"><xsl:value-of select="mml:proximal/@x"/></xsl:when>
                <xsl:otherwise><xsl:variable name="parent"><xsl:value-of select="@parent"/></xsl:variable>
                <xsl:for-each select="../mml:segment[@id = $parent]"><xsl:value-of select="mml:distal/@x"/></xsl:for-each></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="SegPy">
            <xsl:choose>
                <xsl:when test="count(mml:proximal) &gt; 0"><xsl:value-of select="mml:proximal/@y"/></xsl:when>
                <xsl:otherwise><xsl:variable name="parent"><xsl:value-of select="@parent"/></xsl:variable>
                <xsl:for-each select="../mml:segment[@id = $parent]"><xsl:value-of select="mml:distal/@y"/></xsl:for-each></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="SegPz">
            <xsl:choose>
                <xsl:when test="count(mml:proximal) &gt; 0"><xsl:value-of select="mml:proximal/@z"/></xsl:when>
                <xsl:otherwise><xsl:variable name="parent"><xsl:value-of select="@parent"/></xsl:variable>
                <xsl:for-each select="../mml:segment[@id = $parent]"><xsl:value-of select="mml:distal/@z"/></xsl:for-each></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>



      <xsl:variable name="Segx" select="$SegPx + ($SegDx - $SegPx) * $Fract"/>
      <xsl:variable name="Segy" select="$SegPy + ($SegDy - $SegPy) * $Fract"/>
      <xsl:variable name="Segz" select="$SegPz + ($SegDz - $SegPz) * $Fract"/>

      <xsl:value-of select="$Cellx+$Segx"/><xsl:text>  </xsl:text><xsl:value-of select="$Celly+$Segy"/><xsl:text>  </xsl:text><xsl:value-of select="$Cellz+$Segz"/>
    </xsl:for-each>
</xsl:template>


<xsl:template name="single_cell_draw" match="mml:cell | nml:cell | cell">

    <xsl:param name="somaAppearance" />
    <xsl:param name="dendriteAppearance" />

    <xsl:comment>Displaying a single instance of Cell: <xsl:value-of select="@name"/> with <xsl:value-of select="count(mml:segments/mml:segment)"/> segments</xsl:comment>
    
    <xsl:for-each select="mml:segments/mml:segment">

        <xsl:variable name="proximal">
            <xsl:choose>
                <xsl:when test="count(mml:proximal) &gt; 0">
                    <xsl:value-of select="mml:proximal/@x"/><xsl:text>  </xsl:text><xsl:value-of select="mml:proximal/@y"/><xsl:text>  </xsl:text><xsl:value-of select="mml:proximal/@z"/>
                    </xsl:when>
                <xsl:otherwise><xsl:variable name="parent"><xsl:value-of select="@parent"/></xsl:variable>
                <xsl:for-each select="../mml:segment[@id = $parent]"><xsl:value-of select="mml:distal/@x"/><xsl:text>  </xsl:text><xsl:value-of select="mml:distal/@y"/><xsl:text>  </xsl:text><xsl:value-of select="mml:distal/@z"/></xsl:for-each></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:variable name="distal">
            <xsl:value-of select="mml:distal/@x"/><xsl:text>  </xsl:text><xsl:value-of select="mml:distal/@y"/><xsl:text>  </xsl:text><xsl:value-of select="mml:distal/@z"/>
        </xsl:variable>

        <xsl:choose>
            <xsl:when test="$proximal = $distal">
                <xsl:comment>Spherical segment...</xsl:comment>
                <xsl:variable name="radius"><xsl:value-of select="mml:distal/@diameter div 2.0"/></xsl:variable>
                <xsl:variable name="location"><xsl:value-of select="mml:distal/@x"/><xsl:text>  </xsl:text><xsl:value-of select="mml:distal/@y"/><xsl:text>  </xsl:text><xsl:value-of select="mml:distal/@z"/></xsl:variable>

                <xsl:element name="Transform">
                    <xsl:attribute name="translation"><xsl:value-of select="$location"/></xsl:attribute>

            <Shape>
                <Appearance>
                    <xsl:element name="Material"><xsl:attribute name="diffuseColor"><xsl:value-of select="$somaAppearance"/></xsl:attribute></xsl:element>
                </Appearance>
                <xsl:element name="Sphere">
                    <xsl:attribute name="radius"><xsl:value-of select="$radius"/></xsl:attribute>
                </xsl:element>

            </Shape>

                </xsl:element>
            </xsl:when>
            <!--
            Add a cylinder when the soma seg prox radius = dist radius...
            
            <xsl:when test="@id = 0 and mml:distal/@diameter = mml:proximal/@diameter ">
                <xsl:comment>Cylindrical soma segment...</xsl:comment>
                <xsl:variable name="radius"><xsl:value-of select="mml:distal/@diameter div 2.0"/></xsl:variable>
                <xsl:variable name="location"><xsl:value-of select="mml:distal/@x"/><xsl:text>  </xsl:text><xsl:value-of select="mml:distal/@y"/><xsl:text>  </xsl:text><xsl:value-of select="mml:distal/@z"/></xsl:variable>

                <xsl:element name="Transform">
                    <xsl:attribute name="translation"><xsl:value-of select="$location"/></xsl:attribute>

            <Shape>
                <Appearance>
                    <xsl:element name="Material"><xsl:attribute name="diffuseColor"><xsl:value-of select="$somaAppearance"/></xsl:attribute></xsl:element>
                </Appearance>
                <xsl:element name="Cylinder">
                    <xsl:attribute name="radius"><xsl:value-of select="$radius"/></xsl:attribute>
                </xsl:element>

            </Shape>

                </xsl:element>
            </xsl:when>-->

            <xsl:otherwise>

            <Transform>
                <Shape>
                    <Appearance>
                        <Material/>
                    </Appearance>
                    <LineSet vertexCount="2">
                        <xsl:element name="Coordinate">
                            <xsl:attribute name="point"><xsl:value-of select="$proximal"/>, <xsl:value-of select="$distal"/></xsl:attribute>
                        </xsl:element>
                        <xsl:element name="Color"><xsl:attribute name="color"><xsl:value-of select="$dendriteAppearance"/>, <xsl:value-of select="$dendriteAppearance"/></xsl:attribute></xsl:element>

                    </LineSet>
                </Shape>
            </Transform>

            </xsl:otherwise>
        </xsl:choose>


    </xsl:for-each>
    <xsl:comment>Finished displaying Cell: <xsl:value-of select="@name"/></xsl:comment>
    
</xsl:template>


<xsl:template name="positioned_cell_draw">
    <xsl:param name="cell_type"></xsl:param> <!-- empyt cell_type -->
    <xsl:param name="x" select="'0'"/>
    <xsl:param name="y" select="'0'"/>
    <xsl:param name="z" select="'0'"/>
    <xsl:param name="somaAppearance"/>
    <xsl:param name="dendriteAppearance"/>

    <xsl:for-each select="/nml:neuroml/nml:cells/nml:cell[@name=$cell_type]">

         <xsl:element name="Transform">
            <xsl:attribute name="translation"><xsl:value-of select="$x"/><xsl:text> </xsl:text><xsl:value-of select="$y"/><xsl:text> </xsl:text><xsl:value-of select="$z"/></xsl:attribute>


            <xsl:call-template name="single_cell_draw">
                <xsl:with-param name="somaAppearance" select="$somaAppearance"/>
                <xsl:with-param name="dendriteAppearance" select="$dendriteAppearance"/>
           </xsl:call-template>

            </xsl:element>
    </xsl:for-each>
</xsl:template>

<xsl:template match="net:networkml">
    <xsl:text>

    </xsl:text>
    <xsl:comment>Processing &lt;networkml> element</xsl:comment>
        
    <xsl:for-each select="net:populations/net:population">

        <xsl:variable name="myColor">
            <xsl:choose>
              <xsl:when test="position() = 1">1 0 0</xsl:when>
              <xsl:when test="position() = 2">0 1 0</xsl:when>
              <xsl:when test="position() = 3">0 0 1</xsl:when>
              <xsl:when test="position() = 4">1 0 1</xsl:when>
              <xsl:when test="position() = 5">0 1 1</xsl:when>
              <xsl:when test="position() = 6">1 1 0</xsl:when>
              <xsl:otherwise><xsl:value-of select="$defaultSomaAppearance"/></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:variable name="somaAppearance"><xsl:choose>
          <xsl:when test="count(meta:properties/meta:property[@tag='color']) &gt; 0"><xsl:value-of select="meta:properties/meta:property[@tag='color']/@value"/></xsl:when>
          <xsl:otherwise><xsl:value-of select="$myColor"/></xsl:otherwise>
        </xsl:choose></xsl:variable>
        
        <xsl:for-each select="net:instances/net:instance">
            <xsl:variable name="location"><xsl:value-of select="net:location/@x"/><xsl:text>  </xsl:text><xsl:value-of select="net:location/@y"/><xsl:text>  </xsl:text><xsl:value-of select="net:location/@z"/></xsl:variable>
            <xsl:element name="Transform">
                <xsl:attribute name="translation"><xsl:value-of select="$location"/></xsl:attribute>
        
        <Shape>
            <Appearance>
                <xsl:element name="Material"><xsl:attribute name="diffuseColor"><xsl:value-of select="$somaAppearance"/></xsl:attribute></xsl:element>
            </Appearance>
            <xsl:element name="Sphere">
                <xsl:attribute name="radius"><xsl:value-of select="$defaultCellRadius"/></xsl:attribute>
            </xsl:element>
            
        </Shape>   
            
            </xsl:element>
        </xsl:for-each>
    </xsl:for-each>
        
    <xsl:for-each select="net:projections/net:projection">
        <xsl:variable name="src"><xsl:value-of select="net:source"/><xsl:value-of select="@source"/></xsl:variable> <!-- Only one of attr or sub element should be present-->
        <xsl:variable name="tgt"><xsl:value-of select="net:target"/><xsl:value-of select="@target"/></xsl:variable> <!-- Only one of attr or sub element should be present-->
        
        <xsl:comment>Projection <xsl:value-of select="@name"/> between <xsl:value-of select="$src"/> and <xsl:value-of select="$tgt"/></xsl:comment>
        
        <xsl:for-each select="net:connections/net:connection">
            <xsl:variable name="preCellId"><xsl:value-of select="net:pre/@cell_id"/><xsl:value-of select="@pre_cell_id"/></xsl:variable> <!-- Only one of attr or sub element should be present-->
            <xsl:variable name="postCellId"><xsl:value-of select="net:post/@cell_id"/><xsl:value-of select="@post_cell_id"/></xsl:variable> <!-- Only one of attr or sub element should be present-->
            
            <xsl:variable name="preLocation">
                <xsl:for-each select="../../../../net:populations/net:population[@name = $src]/net:instances/net:instance[@id = $preCellId]">
                    <xsl:value-of select="net:location/@x"/><xsl:text>  </xsl:text><xsl:value-of select="net:location/@y"/><xsl:text>  </xsl:text><xsl:value-of select="net:location/@z"/>
                </xsl:for-each>
            </xsl:variable>
            
            <xsl:variable name="postLocation">
                <xsl:for-each select="../../../../net:populations/net:population[@name = $tgt]/net:instances/net:instance[@id = $postCellId]">
                    <xsl:value-of select="net:location/@x"/><xsl:text>  </xsl:text><xsl:value-of select="net:location/@y"/><xsl:text>  </xsl:text><xsl:value-of select="net:location/@z"/>
                </xsl:for-each>
            </xsl:variable>
            
            <Transform>
                <Shape>
                    <Appearance>
                        <Material/>
                    </Appearance>
                    <LineSet vertexCount="2">
                        <xsl:element name="Coordinate">
                            <xsl:attribute name="point"><xsl:value-of select="$preLocation"/>, <xsl:value-of select="$postLocation"/></xsl:attribute>
                        </xsl:element>
                        <Color color="0 1 0, 1 0 0"/><!-- Green to red-->
                    </LineSet>
                </Shape>
            </Transform>
            
            
            
        </xsl:for-each> 
    </xsl:for-each>
    

</xsl:template>

    

<xsl:template name="showAxes">
    <xsl:text>
        
    </xsl:text>
    <xsl:comment>Drawing an xyz axes from (-100, -100, -100) to (100, 100, 100) with green X axis, yellow Y axis, red Z axis</xsl:comment>
        <!-- X axis-->
    <Transform rotation="0 0 1 -1.570795">
        <Shape>
            <Appearance><Material diffuseColor="0 1 0"/></Appearance>
            <Cylinder height="200" radius="0.5"/>
        </Shape>
        <Transform translation="0 105 0">
            <Shape>
                <Appearance><Material diffuseColor="0 1 0"/></Appearance>
                <Cone height="10" bottomRadius= "1"/>
            </Shape>
        </Transform>
    </Transform>
    
    <!-- Y axis-->
    <Transform>
        <Shape>
            <Appearance><Material diffuseColor="1 1 0"/></Appearance>
            <Cylinder height="200" radius="0.5"/>
        </Shape>
        <Transform translation="0 105 0">
            <Shape>
                <Appearance><Material diffuseColor="1 1 0"/></Appearance>
                <Cone height="10" bottomRadius= "1"/>
            </Shape>
        </Transform>
    </Transform>
    
    <!-- Z axis-->
    <Transform rotation="1 0 0 1.570795">
        <Shape>
            <Appearance><Material diffuseColor="1 0 0"/></Appearance>
            <Cylinder height="200" radius="0.5"/>
        </Shape>
        <Transform translation="0 105 0">
            <Shape>
                <Appearance><Material diffuseColor="1 0 0"/></Appearance>
                <Cone height="10" bottomRadius= "1"/>
            </Shape>
        </Transform>
    </Transform>
    
</xsl:template>

</xsl:stylesheet>
