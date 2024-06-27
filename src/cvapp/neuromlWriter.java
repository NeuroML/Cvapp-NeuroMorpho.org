package cvapp;
/*
cvapp - neuronal morphology viewer, editor and file converter
Copyright (C) 1998  Robert Cannon

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
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

please send comments, bugs, and feature requests to rcc1@soton.ac.uk
or see http://www.neuro.soton.ac.uk/cells/

 */

import java.io.File;
import java.util.*;
import java.text.*;

class neuromlWriter extends Object {

    //private boolean verbose = true;
    private boolean verbose = false;
    
    private Vector<nlpoint> points;
    private String[] sectionTypes;

    // Where points were obtained from, usually filename
    private String morphologyOrigin;
    private int[] segPerTyp;
    private StringBuilder segmentContent;
    private StringBuilder groupContent;
    private NumberFormat form;
    
    private final static String INDENT = "  ";
    
    private HashMap<Integer, Integer> cableIdsVsIndices = new HashMap<Integer, Integer>();
    private HashMap<Integer, Integer> pointIndicesVsSegIds = new HashMap<Integer, Integer>();

    private Hashtable<String, ArrayList<String>> segmentGroups = new Hashtable<String, ArrayList<String>>();
    
    private int nextSegmentId = 0;
    private int nextCableId = 0;

    private String CABLE_PREFIX_V2 = "Cable_";

    public enum NeuroMLVersion
    {
        NEUROML_VERSION_1_8_1
        {
            @Override
            public String toString()
            {
                return "1.8.1";
            }
        },
        NEUROML_VERSION_2_alpha
        {
            @Override
            public String toString()
            {
                return "2alpha";
            }
        },
        NEUROML_VERSION_2_beta
        {
            @Override
            public String toString()
            {
                return "2beta4";
            }
        },
        NEUROML_VERSION_2_3_1
        {
            @Override
            public String toString()
            {
                return "2.3.1";
            }
        };

        public boolean isVersion2()
        {
            return !isVersion1();
        }

        public boolean isVersion2beta()
        {
            return this.equals(NEUROML_VERSION_2_beta);
        }

        public boolean isVersion1()
        {
            return this.toString().startsWith("1.");
        }
    };
    
    
    /*public enum SomaType
    {
        SPHERICAL_SOMA,
        SUBSTITUTED_3_POINT_SOMA;
    }*/
    
    
    private static final String UNKNOWN_PARENT = "UNKNOWN_PARENT_";

    neuromlWriter(Vector<nlpoint> pts, String[] st, String morphologyOrigin) {
        points = pts;
        sectionTypes = st;
        this.morphologyOrigin = morphologyOrigin;
        form = NumberFormat.getInstance();
        // NMO Developer : Added to remove insertion of comma in saved file if number exceeds 999.
        form.setGroupingUsed(false);
    }

    public String nmlString(NeuroMLVersion version, boolean morphologyOnly) {
        if (points.size() < 2 || sectionTypes.length < 2 || !sectionTypes[1].equals("soma")) {
            System.out.println("Error: null data or section types in hocWrite");
            return "";
        }
        if (version.isVersion1() && morphologyOnly) {
            System.out.println("Error: morphologyOnly option only supported by version 2");
            return "";
        }

        String cellName = "cell1";
        try{
            File f = new File(morphologyOrigin);
            cellName = f.getName();
            if (cellName.endsWith(".swc"))
                cellName = cellName.substring(0,cellName.length()-4);
            cellName = cellName.replace(".", "_");
            cellName = cellName.replace("-", "_");
            if (Character.isDigit(cellName.charAt(0))) {
                cellName = "Cell_"+cellName;
            } 
        } catch (Exception e) {
            // stick with original name
        }

        //default starting point;
        nlpoint startPoint = null;

        int numPoints = points.size();
        segPerTyp = new int[numPoints];
        
        for (int i = 0; i < numPoints; i++) {
            nlpoint p = ithPoint(i);
            p.imark = 0;
            if (startPoint == null && p.nlcode == 1) {
                // check for precicely one somatic neighbour;
                int numSomaticNeighbs = 0;
                int jsom = -1;
                for (int j = 0; j < p.nnbr; j++) {
                    if (p.pnbr[j].nlcode == 1) {
                        numSomaticNeighbs++;
                        jsom = j;
                    }
                }
                if (numSomaticNeighbs == 1) {
                    // ok got it - if > 1 neighbours, put the somatic one first;
                    if (jsom > 0) {
                        nlpoint dum = p.pnbr[jsom];
                        p.pnbr[jsom] = p.pnbr[0];
                        p.pnbr[0] = dum;
                    }
                    startPoint = p;
                }
            }
        }
        
        if (startPoint == null) {
            startPoint = ithPoint(0);
        }

        //sb1 = new StringBuilder();
        segmentContent = new StringBuilder();
        groupContent = new StringBuilder();

        if (version.isVersion1()) {
            segmentContent.append(INDENT+INDENT+INDENT+"<segments xmlns=\"http://morphml.org/morphml/schema\">\n\n");
            groupContent.append("\n"+INDENT+INDENT+INDENT+"<cables xmlns=\"http://morphml.org/morphml/schema\">\n\n");
        } else if (version.isVersion2()) {
            segmentContent.append(INDENT+INDENT+INDENT+"<morphology id=\"morphology_"+cellName+"\">\n\n");
        }



        // write the points to mainContent;
        parseTree(startPoint, startPoint, true, true, version);



        StringBuilder sbf = new StringBuilder();

        if (version.isVersion1())
        {
            sbf.append("<neuroml xmlns=\"http://morphml.org/neuroml/schema\"\n"+
                INDENT+"xmlns:meta=\"http://morphml.org/metadata/schema\"\n"+
                INDENT+"xmlns:mml=\"http://morphml.org/morphml/schema\"\n"+
                INDENT+"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n"+
                INDENT+"xsi:schemaLocation=\"http://morphml.org/neuroml/schema http://www.neuroml.org/NeuroMLValidator/NeuroMLFiles/Schemata/v1.8.1/Level1/NeuroML_Level1_v1.8.1.xsd\"\n"+
                INDENT+"length_units=\"micrometer\">\n\n");
        } else if (version.isVersion2()) {

            String nmlId = cellName;
            if (morphologyOnly)
                nmlId += "_morphology";
            sbf.append("<neuroml xmlns=\"http://www.neuroml.org/schema/neuroml2\"\n"+
                INDENT+"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n"+
                INDENT+"xsi:schemaLocation=\"http://www.neuroml.org/schema/neuroml2  https://raw.github.com/NeuroML/NeuroML2/development/Schemas/NeuroML2/NeuroML_v"+version+".xsd\"\n"+
                INDENT+"id=\""+nmlId+"\">\n\n");
        }

        String metaPrefix="";

        if (version.isVersion1()){
            sbf.append(INDENT+"<cells>\n");
            sbf.append(INDENT+INDENT+"<cell name=\""+cellName+"\">\n");
            metaPrefix = "meta:";

        } else if (version.isVersion2() && !morphologyOnly){
            
            sbf.append(INDENT+"<cell id=\""+cellName+"\">\n");
        }

        sbf.append(INDENT+INDENT+INDENT+"<"+metaPrefix+"notes>\n        Neuronal morphology exported in NeuroML v"+version+" from CVapp (NeuroMorpho.org version)\n"
            + "        Original file: "+morphologyOrigin+" </"+metaPrefix+"notes>\n\n");
        
       

        if (version.isVersion1()){
            segmentContent.append(INDENT+INDENT+INDENT+"</segments>\n");
            groupContent.append(INDENT+INDENT+INDENT+"</cables>\n\n");
        } else if (version.isVersion2()){
            for(String key: segmentGroups.keySet())
            {
                ArrayList<String> members = segmentGroups.get(key);
                StringBuilder sb = new StringBuilder();
                sb.append(INDENT+INDENT+INDENT+INDENT+"<segmentGroup id=\""+key+"\" >\n");
                for (String member: members) {
                    if (member.startsWith(CABLE_PREFIX_V2)) {
                        sb.append(INDENT+INDENT+INDENT+INDENT+INDENT+"<include segmentGroup=\""+member+"\"/>\n");
                    } else {
                        sb.append(INDENT+INDENT+INDENT+INDENT+INDENT+"<member segment=\""+member+"\"/>\n");
                    }
                }
                sb.append(INDENT+INDENT+INDENT+INDENT+"</segmentGroup>\n\n");
                if (key.startsWith(CABLE_PREFIX_V2)) {
                    groupContent.insert(0, sb);
                } else {
                    groupContent.append(sb);
                }


            }
        }
        
        if (segmentContent.indexOf(UNKNOWN_PARENT)>0)
        {
            System.out.println("segmentIdsVsIndices: "+pointIndicesVsSegIds.get(7));
        }
        
        sbf.append(segmentContent);
        
        sbf.append(groupContent);
        

        if (version.isVersion1())
        {
            sbf.append(INDENT+INDENT+"</cell>\n");
            sbf.append(INDENT+"</cells>\n");
        } else if (version.isVersion2())
        {
            sbf.append(INDENT+INDENT+INDENT+"</morphology>\n\n");
            
            if (!morphologyOnly)
            {
                sbf.append(INDENT+"<!-- No biophysical properties, as this cell was generated from an SWC morphology file -->\n\n");

                sbf.append(INDENT+"</cell>\n");
            }
        }
        
        sbf.append("</neuroml>\n");


        return sbf.toString();
    }
    
    
    private void pointAppend(nlpoint p, double dz, String loc) {
        segmentContent.append(INDENT+INDENT+INDENT+INDENT+INDENT+"<"+loc+" x=\"");
        segmentContent.append(form.format(p.x));
        segmentContent.append("\" y=\"");
        segmentContent.append(form.format(p.y));
        segmentContent.append("\" z=\"");
        segmentContent.append(form.format(p.z + dz));
        segmentContent.append("\" diameter=\"");
        segmentContent.append(form.format(2 * p.r));
        segmentContent.append("\"/>\n");
    }
    
    private ArrayList<String> getGroupsForType(nlpoint p){
        ArrayList<String> groups = new ArrayList<String>();
        groups.add("all");
        switch (p.nlcode) {
            case 1:
                groups.add("soma_group");
                groups.add("color_white");
                break;
            case 2:
                groups.add("axon_group");
                groups.add("color_grey");
                break;
            case 3:
                groups.add("basal_dendrite"); 
                groups.add("dendrite_group");
                groups.add("color_green");
                break;
            case 4:
                groups.add("apical_dendrite"); 
                groups.add("dendrite_group");
                groups.add("color_magenta");
                break;
            case 5:
                groups.add("SWC_group_5"); 
                groups.add("dendrite_group"); // Assume it's a dendrite
                break;
            case 6:
                groups.add("SWC_group_6"); 
                groups.add("dendrite_group"); // Assume it's a dendrite
                break;
            case 7:
                groups.add("SWC_group_7"); 
                groups.add("dendrite_group"); // Assume it's a dendrite
                break;
            case 8:
                groups.add("SWC_group_8"); 
                groups.add("dendrite_group"); // Assume it's a dendrite
                break;
            case 9:
                groups.add("SWC_group_9"); 
                groups.add("dendrite_group"); // Assume it's a dendrite
                break;
            case 0:
                groups.add("SWC_group_0_assuming_soma"); 
                groups.add("soma_group"); // Assume it's a soma
                break;
            case -1:
                groups.add("SWC_group_-1_assuming_soma"); 
                groups.add("soma_group");   // Assume it's a soma
                break;
        }
                    
        
        return groups;
    }

    public void parseTree(nlpoint parentPoint, nlpoint thisPoint, boolean newCable, boolean newCell, NeuroMLVersion version) {
        
        if (verbose) System.out.println("\n-- Handling point: "+thisPoint+"NewCable: "+newCable+"\n-- With parent point: "+parentPoint);
        
        int thisType = thisPoint.nlcode;
        if (thisType < 0) {
            thisType = 0;
        }
        
        int cableId = -1;

        if (newCable) {
            
            cableId = nextCableId;
            nextCableId++;
            cableIdsVsIndices.put(thisPoint.myIndex, cableId);
     
            
        } else {
            cableId = cableIdsVsIndices.get(parentPoint.myIndex);
            cableIdsVsIndices.put(thisPoint.myIndex, cableId);
        }
        
        float fractAlongParentCable = 1;
        
        
        if (newCell 
            /*&& p.nnbr==1  One neighbour */
            && thisPoint.pnbr[0].nlcode==1 /* This neighbor is a soma too */)
        {
            segmentContent.append(INDENT+INDENT+INDENT+INDENT+"<!-- First point is of a multi point soma => not spherical! -->\n\n");
            if (verbose) segmentContent.append(INDENT+INDENT+INDENT+INDENT+"<!-- \n"+thisPoint.toString()+"\n -->\n");
            //segIdOffset = -1;
        }
        else if(thisPoint.nlcode!=1       /* This isn't soma type*/
                && parentPoint.nlcode==1 /* Parent is soma type*/
                /*&& ppar.myIndex==0    Parent is first point */)
        {
            segmentContent.append(INDENT+INDENT+INDENT+INDENT+"<!-- Parent point is on soma! Not creating 'real' segment -->\n\n");
            if (verbose) segmentContent.append(INDENT+INDENT+INDENT+INDENT+"<!-- \n"+thisPoint.toString()+"\n -->\n");
            
            if (parentPoint.myIndex==0 // parent was first point
                && !pointIndicesVsSegIds.containsKey(parentPoint.myIndex)  /* Parent point not distal point */)
                fractAlongParentCable = 0;
        }
        else
        {
            if (verbose) segmentContent.append(INDENT+INDENT+INDENT+INDENT+"<!-- Normal segment... -->\n\n");
            if (verbose) segmentContent.append(INDENT+INDENT+INDENT+INDENT+"<!-- \n"+thisPoint.toString()+"\n -->\n");
            int segId = nextSegmentId;
            nextSegmentId++;
            pointIndicesVsSegIds.put(thisPoint.myIndex, segId);
            
            segmentContent.append(INDENT+INDENT+INDENT+INDENT+"<segment id=\""+segId+"\" name=\""+"Seg_"+segId+"\"");

            boolean appendDisjointedProximal = false;
                
            if (verbose) System.out.println("pointIndicesVsSegIds: "+ pointIndicesVsSegIds);
            
            String parentElementV2 = "";

            // Add parent segment details
            if (segId>0 && parentPoint.myIndex>=0) {
                if (!pointIndicesVsSegIds.containsKey(parentPoint.myIndex)) {
                    
                    //if (verbose) segmentContent.append(INDENT+INDENT+INDENT+INDENT+"<!-- fff -->\n\n");
                    
                    if (parentPoint.pnbr[0].nlcode==1 // parent of missing parent is soma
                        /*&& parentPoint.pnbr[0].myIndex==0*/
                        && pointIndicesVsSegIds.containsKey(parentPoint.pnbr[0].myIndex)) // parent of missing parent has known index
                    {
                        int parSegId = pointIndicesVsSegIds.get(parentPoint.pnbr[0].myIndex);
                        if (version.isVersion1()) {
                            segmentContent.append(" parent=\""+parSegId+"\"");
                        } else {
                            parentElementV2 = "<parent segment=\""+parSegId+"\"/>";
                        }

                        appendDisjointedProximal = true;
                    }
                    else if (parentPoint.pnbr[0].nlcode==1 // parent of missing parent is soma
                        && parentPoint.pnbr[0].myIndex==0 ) // parent of missing parent is start of soma
                    {
                        if (version.isVersion1()) {
                            segmentContent.append(" parent=\"0\"");
                        } else {
                            parentElementV2 = "<parent segment=\"0\"/>";
                        }
                        appendDisjointedProximal = true;
                    }
                    /*else if (parentPoint.pnbr[0].pnbr[0].nlcode==1 // parent of missing parent is soma
                        && pointIndicesVsSegIds.containsKey(parentPoint.myIndex)) {        
                        segmentContent.append(" parent=\"0\"");
                        appendDisjointedProximal = true;
                    }*/
                    else {
                        if (version.isVersion1()) {
                            segmentContent.append(" parent=\""+UNKNOWN_PARENT+parentPoint.myIndex+"\"");
                        } else {
                            parentElementV2 = "<parent segment=\""+UNKNOWN_PARENT+parentPoint.myIndex+"\"/>";
                        }
                    }
                } else {
                    int parSegId = pointIndicesVsSegIds.get(parentPoint.myIndex);
                    if (version.isVersion1()) {
                        segmentContent.append(" parent=\""+parSegId+"\"");
                    } else {
                        parentElementV2 = "<parent segment=\""+parSegId+"\"/>";
                    }
                }
            }

            if(version.isVersion1())
            {
                segmentContent.append(" cable=\""+cableId+"\">\n");
            } else {
                segmentContent.append("> <!-- \"Cable\" is "+cableId+"-->\n"+INDENT+INDENT+INDENT+INDENT+INDENT+parentElementV2+"\n");
            }
            
            if (appendDisjointedProximal || newCable) {
                pointAppend(parentPoint, 0.0, "proximal");
            }

           
            
            if (thisPoint.nlcode==1       /* This is soma point */
                && parentPoint.nlcode==1  /* Parent is soma point */
                && !pointIndicesVsSegIds.containsKey(parentPoint.myIndex)  /* Parent point not yet included */
                ) {
                segmentContent.append(INDENT+INDENT+INDENT+INDENT+INDENT+"<!-- Cylindrical soma... -->\n");
                pointAppend(parentPoint, 0.0, "proximal");
            }

            pointAppend(thisPoint, 0.0, "distal");

            segmentContent.append(INDENT+INDENT+INDENT+INDENT+"</segment>  "+"\n\n");

            if (version.isVersion2()) {
                String cableName = CABLE_PREFIX_V2+cableId;
                if (!segmentGroups.containsKey(cableName)) {
                    segmentGroups.put(cableName, new ArrayList<String>());
                }
                ArrayList<String> members = segmentGroups.get(cableName);
                members.add(segId+"");
            }
        }

        
        
        if (newCable) {

            if (version.isVersion1()) {
                String fractInfo = (fractAlongParentCable==1)?"":" fract_along_parent=\""+fractAlongParentCable+"\"";
                groupContent.append(INDENT+INDENT+INDENT+INDENT+"<cable id=\""+cableId+"\" name=\""+segmentName(thisType)+"_"+form.format(segPerTyp[thisType])+"\""+fractInfo+">\n");
                if (verbose) groupContent.append(INDENT+INDENT+INDENT+INDENT+"<!-- \n"+thisPoint.toString()+"\n -->\n");
                for (String group: getGroupsForType(thisPoint)){
                    groupContent.append(INDENT+INDENT+INDENT+INDENT+INDENT+"<meta:group>"+group+"</meta:group>\n");
                }

                groupContent.append(INDENT+INDENT+INDENT+INDENT+"</cable>\n\n");
            } else if (version.isVersion2()) {
                

                for (String group: getGroupsForType(thisPoint)){
                    if (!segmentGroups.containsKey(group)) {
                        segmentGroups.put(group, new ArrayList<String>());
                    }
                    ArrayList<String> segments = segmentGroups.get(group);
                    segments.add(CABLE_PREFIX_V2+cableId);
                }

                /*
                String fractInfo = (fractAlongParentCable==1)?"":" fract_along_parent=\""+fractAlongParentCable+"\"";
                groupContent.append(INDENT+INDENT+INDENT+INDENT+"<segmentGroup id=\""+cableId+"\" "+fractInfo+">\n");
                if (verbose) groupContent.append(INDENT+INDENT+INDENT+"<!-- \n"+thisPoint.toString()+"\n -->\n");
                for (String group: getGroupsForType(thisPoint)){

                    //<include segmentGroup="parallelFiberNeg"/>
                    groupContent.append(INDENT+INDENT+INDENT+INDENT+INDENT+"<include segmentGroup=\""+group+"\"/>\n");
                }

                groupContent.append(INDENT+INDENT+INDENT+INDENT+"</segmentGroup>\n\n");*/

            }
            segPerTyp[thisType]++;

            
        } 
        
        
        thisPoint.imark = 1; // Mark this point as done

        int numNeighbsNotDone = 0;
        boolean diffTypeAnyNeighb = false;

        for (int i = 0; i < thisPoint.nnbr; i++) {
            if (thisPoint.pnbr[i].imark == 0) {     //neighbour not done yet
                numNeighbsNotDone++;
                if (thisPoint.pnbr[i].nlcode != thisPoint.nlcode) {
                    diffTypeAnyNeighb = true;
                }
            }
        }
        

        for (int i = 0; i < thisPoint.nnbr; i++) {
            nlpoint nextPoint = thisPoint.pnbr[i];
            if (nextPoint.imark == 0) {
                newCable = false;

                if (thisPoint.nlcode==1 && nextPoint.nlcode==1)
                    newCable = false;
                else if (diffTypeAnyNeighb || numNeighbsNotDone>1)
                    newCable = true;
                    
                //System.out.println("----------------\nthisPoint: "+thisPoint+"nextPoint: "+nextPoint+", newCable: "+newCable+", diffTypeAnyNeighb: "+diffTypeAnyNeighb);
                parseTree(thisPoint, nextPoint, newCable, false, version);
            }
        }
        
        
    }

    private String segmentName(int ityp) {
        String styp = "";
        if (ityp >= 5) {
            styp = "user" + form.format(ityp).trim();
        } else {
            if (ityp < 0) {
                System.out.println("warning: negative segment type converted "
                        + "to \"unknown\" ");
                ityp = 0;
            }
            styp = sectionTypes[ityp];
        }
        return styp;
    }

    private nlpoint ithPoint(int i) {
        return (nlpoint) (points.elementAt(i));
    }
}


