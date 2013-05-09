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

import java.util.*;
import java.text.*;

class hocWriter extends Object {

    Vector<nlpoint> points;
    String[] sectionTypes;
    int[] segPerTyp;
    StringBuilder sectionCreation;
    StringBuilder pointsInfo;
    NumberFormat form;
    //float connectionToSoma = 1;
    //boolean verbose = true;
    boolean verbose = false;
    // Where points were obtained from, usually filename
    private String morphologyOrigin;
    private ArrayList<nlpoint> allSomaPoints = new ArrayList<nlpoint>();

    boolean substitutedCylSoma = false;
    
    int countLines = 0;
    int maxLines = 300;
    String currentSectionName = null;

    public hocWriter(Vector<nlpoint> pts, String[] st, String morphologyOrigin) {
        points = pts;
        sectionTypes = st;
        form = NumberFormat.getInstance();
        // NMO Developer : Added to remove insertion of comma in saved file if number exceeds 999.
        form.setGroupingUsed(false);
        this.morphologyOrigin = morphologyOrigin;
    }

    private void pt3dappend(nlpoint p, double dy) {
        pointsInfo.append("  pt3dadd(");
        pointsInfo.append(form.format(p.x));
        pointsInfo.append(",");
        pointsInfo.append(form.format(p.y + dy));
        pointsInfo.append(",");
        pointsInfo.append(form.format(p.z));
        pointsInfo.append(",");
        pointsInfo.append(form.format(2 * p.r));
        pointsInfo.append(") \n");
    }

    private String getXyzd(nlpoint p) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(form.format(p.x));
        sb.append(",");
        sb.append(form.format(p.y));
        sb.append(",");
        sb.append(form.format(p.z));
        sb.append(",");
        sb.append(form.format(2 * p.r));
        sb.append(")");
        return sb.toString();
    }

    public void recHOCTraceNS(nlpoint ppar, nlpoint p, boolean newseg, int mysegind) {

        boolean singleSomaPoint = false;
        if (verbose) {
            pointsInfo.append("\n//-------------------------------\n//  New section: " + newseg + "...\n");
            pointsInfo.append("\n//  Adding point: " + p.toString().replaceAll("\n", "\n//  ") + "\n");
            pointsInfo.append("\n//  Its parent:   " + ppar.toString().replaceAll("\n", "\n//  ") + "\n");
        }
        if (p.isSomaPoint()) {
            allSomaPoints.add(p);
        }

        int ityp = p.nlcode;
        if (ityp < 0) {
            ityp = 0;
        }

        String nSectionName = segmentName(ityp) + "[" + form.format(segPerTyp[ityp]) + "]";

        if (newseg) {
            countLines = 0;
            currentSectionName = nSectionName;
            
            mysegind = segPerTyp[ityp];

            if (ppar == p /* First point...*/) {

                pointsInfo.append("\n{access " + nSectionName + "}\n");
            }

            pointsInfo.append("\n" + nSectionName + " {\n");
            pointsInfo.append("  pt3dclear()\n");
            segPerTyp[ityp]++;


            if (ppar != p) {      // Not identical, i.e. not first point...

                if (!(ppar.isSomaPoint() && !p.isSomaPoint())) { // NOT Parent is soma and this isn't
                    pt3dappend(ppar, 0.0);
                } else {
                    pointsInfo.append("\n  //  Note: not adding point: " + getXyzd(ppar) + " on parent, as this is a connection to the soma.\n");
                    pointsInfo.append("  //  connect statement above means that this section is connected by a zero resistance wire to that point.\n\n");
                }

            }

            if (ppar == p /* First point...*/) {
                singleSomaPoint = true;
                for (int i = 0; i < p.nnbr; i++) {
                    singleSomaPoint = singleSomaPoint && !p.pnbr[i].isSomaPoint();
                }
                if (verbose) {
                    pointsInfo.append("\n//   singleSomaPoint: " + singleSomaPoint + "\n\n");
                }
                if (singleSomaPoint && p.isSomaPoint()) {
                    if (p.r == 0) {
                        pointsInfo.append("\nWARNING! Zero radius soma in original SWC. This should be manually edited before this morphology is used in a simulation!\n\n");
                        pt3dappend(p, 0);
                    } else {
                        pointsInfo.append("\n//  Single soma point of radius: " + p.r + "um at "+getXyzd(p)+" in original SWC. Assuming this meant a spherical soma of that radius."
                            + "\n//  Creating cylindrical section along y axis centred at this point, extending " + p.r + "um in each direction.\n\n");
                        pt3dappend(p, -1 * p.r);
                        pt3dappend(p, p.r);
                        substitutedCylSoma = true;
                    }

                }
            }
        } // end if (newseg)

        if (!singleSomaPoint) {
            if (countLines>=maxLines) {
                
                pointsInfo.append("\n} // Breaking long procedure...\n" + currentSectionName + " {\n");
                countLines = 0;
            }
            //pointsInfo.append("Line: "+countLines+"\n");
            pt3dappend(p, 0.0);
            countLines++;
        }
        p.imark = 1;

        int numNeighbsNotDone = 0;
        boolean diffTypeAnyNeighb = false;
        for (int i = 0; i < p.nnbr; i++) {
            if (p.pnbr[i].imark == 0) {
                numNeighbsNotDone++;
                if (p.pnbr[i].nlcode != p.nlcode) {
                    diffTypeAnyNeighb = true;
                }
            }
        }

        boolean nextNew = false;
        if (diffTypeAnyNeighb || numNeighbsNotDone > 1) {
            nextNew = true;
        }

        if (verbose) {
            pointsInfo.append("\n//   diffTypeAnyNeighb: " + diffTypeAnyNeighb + ", numNeighbsNotDone: " + numNeighbsNotDone + "\n\n");
        }

        for (int i = 0; i < p.nnbr; i++) {
            if (p.pnbr[i].imark == 0) {
                nlpoint pd = p.pnbr[i];

                boolean somaConnect = false;
                if (p.isSomaPoint() && pd.isSomaPoint()) {
                    if (verbose) {
                        pointsInfo.append("\n//   Ensuring single section soma\n\n");
                    }
                    somaConnect = true;
                }

                if (nextNew && !somaConnect) {
                    //pointsInfo.append("\n} // end of points list for "+nSectionName+"\n\n{");
                    pointsInfo.append("\n} // end of points list\n\n{");
                    pointsInfo.append(segmentName(p.nlcode));
                    pointsInfo.append("[");
                    pointsInfo.append(form.format(mysegind));
                    pointsInfo.append("] connect ");

                    pointsInfo.append(segmentName(pd.nlcode));
                    pointsInfo.append("[");
                    pointsInfo.append(form.format(
                        segPerTyp[pd.nlcode < 0 ? 0 : pd.nlcode]));
                    double connectionFract = (p.isSomaPoint() && !pd.isSomaPoint()) ? getConnFractSoma(p) : 1;
                    pointsInfo.append("](0), " + form.format(connectionFract) + "}\n");
                }
                recHOCTraceNS(p, pd, (nextNew && !somaConnect), mysegind);
            }
        }
    }

    /*
     * Get the fraction along (0 to 1) the soma section of the indicated point
     * @returns the fraction or -1 if the point isn't found...
     */
    private double getConnFractSoma(nlpoint p) {
        //System.out.println("allSomaPoints: "+allSomaPoints);
        if (allSomaPoints.isEmpty()) {
            return -1;
        } else if (allSomaPoints.size() == 1) {
            if (substitutedCylSoma)
                return 0.5;
            return 1;
        } else if (allSomaPoints.size() == 2) {
            if (p.colocated(allSomaPoints.get(0))) {
                return 0;
            }
            if (p.colocated(allSomaPoints.get(1))) {
                return 1;
            }
        } else {
            if (p.colocated(allSomaPoints.get(0))) {
                return 0;
            }
            if (p.colocated(allSomaPoints.get(allSomaPoints.size() - 1))) {
                return 1;
            }

            double totLen = 0;
            for (int i = 1; i < allSomaPoints.size(); i++) {
                //System.out.println("totLen: "+totLen+", p "+getXyzd(p)+", (i) "
                //    +getXyzd(allSomaPoints.get(i))+", (i-1) "+getXyzd(allSomaPoints.get(i-1)));
                totLen = totLen + allSomaPoints.get(i).distance(allSomaPoints.get(i - 1));
            }
            double partialLen = 0;
            for (int i = 1; i < allSomaPoints.size(); i++) {
                //System.out.println("totLen: "+totLen+" par "+partialLen+", p "+getXyzd(p)+", allSomaPoints.get(i) "+getXyzd(allSomaPoints.get(i)));
                partialLen = partialLen + allSomaPoints.get(i).distance(allSomaPoints.get(i - 1));

                if (p.colocated(allSomaPoints.get(i))) {
                    return (partialLen / totLen);
                }

            }

        }


        return -1;
    }

    public String hocString() {

        if (points.size() < 2 || sectionTypes.length < 2
            || !sectionTypes[1].equals("soma")) {
            System.out.println("Error: null data or section types in hocWrite");
            return "";
        }

        //default starting point;
        nlpoint p0 = null;

        int numPoints = points.size();
        segPerTyp = new int[numPoints];

        for (int i = 0; i < numPoints; i++) {
            nlpoint p = ithPoint(i);
            p.imark = 0;
            if (p0 == null && p.nlcode == 1) {
                // check for precicely one somatic neighbour;
                int nsn = 0;
                int jsom = -1;
                for (int j = 0; j < p.nnbr; j++) {
                    if (p.pnbr[j].nlcode == 1) {
                        nsn++;
                        jsom = j;
                    }
                }
                if (nsn == 1) {
                    // ok got it - if > 1 neighbours, put the somatic one first;
                    if (jsom > 0) {
                        nlpoint dum = p.pnbr[jsom];
                        p.pnbr[jsom] = p.pnbr[0];
                        p.pnbr[0] = dum;
                    }
                    p0 = p;
                }
            }
        }
        if (p0 == null) {
            p0 = ithPoint(0);
        }

        sectionCreation = new StringBuilder();
        pointsInfo = new StringBuilder();

        // write the points to pointsInfo;
        recHOCTraceNS(p0, p0, true, 0);


        // declare segment arrays - bit of overkill, but negligible in time;
        String styp = " ";
        for (int i = 0; i < numPoints; i++) {
            if ((segPerTyp[i]) > 0) {
                styp = segmentName(i);
                sectionCreation.append("{create ");
                sectionCreation.append(styp);
                sectionCreation.append("[");
                sectionCreation.append(form.format(segPerTyp[i]).trim());
                sectionCreation.append("]}\n");
            }
        }
        sectionCreation.append("\n");


        StringBuilder sbf = new StringBuilder("//  NEURON hoc file generated from CVapp (NeuroMorpho.org version)\n");
        sbf.append("//  Original file: " + morphologyOrigin + " \n\n");
        sbf.append("//  Generated "+(new Date()).toString()+"\n\n");
        sbf.append(sectionCreation.toString());
        sbf.append(pointsInfo.toString());
        sbf.append("\n}");
        return sbf.toString();
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
        return points.elementAt(i);
    }
}
