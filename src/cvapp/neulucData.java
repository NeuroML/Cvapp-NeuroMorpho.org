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

class neulucData extends Object {

    Vector<nlpoint> points;
    double[][] lineList;
    double[][] pointList;
    // stuff used in recursive write functions;
    int nseg;
    String sourceFileName = null;
    StringBuffer sb, sb1, sb2, sb3;
    boolean trustLineList = false;
    boolean trustPointList = false;
    int nLines = 0;
    int nPoints = 0;
    int rootpoint = 0;
    boolean SWCwrite = false;
    boolean GENESISwrite = false;
    boolean HOCwrite = false;
    int cwi = 0;
    double[] cdp = {0., 0., 0.};
    int pcount = 0;
    int maxType = 0;
    boolean cleaning = false;
    int nremoved;
    NumberFormat form;
    boolean havePointNeedingRadius;
    nlpoint pointNeedingRadius;
    int[] usedTypes;

    String[] sectionTypes = {"undefined", "soma",
        "axon", "dendrite",
        "apical_dendrite", "custom-1",
        "custom-2", "custom-n"};
    
    String[] headerField = {"ORIGINAL_SOURCE",
        "CREATURE",
        "REGION",
        "FIELD/LAYER",
        "TYPE",
        "CONTRIBUTOR",
        "REFERENCE",
        "RAW",
        "EXTRAS",
        "SOMA_AREA",
        "SHRINKAGE_CORRECTION",
        "VERSION_NUMBER",
        "VERSION_DATE",
        "*********************************************"};
    String headerText = " ";

    neulucData() {
        points = new Vector<nlpoint>();
        form = NumberFormat.getInstance();
        // NMO Developer : Added to remove insertion of comma in saved file if number exceeds 999.
        form.setGroupingUsed(false);
    }

    public int getCode(String s) {
        int icode = -1;
        for (int i = 0; i < sectionTypes.length; i++) {
            if (s.equals(sectionTypes[i])) {
                icode = i;
            }
        }

        return icode;
    }

    public String getHeaderText() {
        return headerText;
    }

    public void setHeaderText(String s) {
        headerText = s;
        checkHeader();
    }

    public void updateHeaderText(String s) {

        if (s == null) {
            return;
        }

        // header -> s + (fields in current header not in s);
        StringTokenizer sto = new StringTokenizer(headerText, "\n\r");
        int nfo = sto.countTokens();
        String[] sao = new String[nfo];
        String[] fao = new String[nfo];
        for (int i = 0; i < nfo; i++) {
            sao[i] = sto.nextToken();
            StringTokenizer stt = new StringTokenizer(sao[i], "# ");
            if (stt.hasMoreTokens()) {
                fao[i] = stt.nextToken();
            }
        }

        StringTokenizer stn = new StringTokenizer(s, "\n\r");
        StringBuilder sBuff = new StringBuilder();
        while (stn.hasMoreTokens()) {
            String sn = stn.nextToken();
            for (int i = 0; i < nfo; i++) {
                if (fao[i] != null && sn.indexOf(fao[i]) > 0) {
                    fao[i] = null;
                }
            }
            sBuff.append(sn);
            sBuff.append("\n");
        }

        for (int i = 0; i < nfo; i++) {
            if (fao[i] != null) {
                sBuff.append(sao[i]);
                sBuff.append("\n");
            }
        }
        headerText = sBuff.toString();
    }

    public String getSourceFileName() {
        return sourceFileName;
    }

    public void setSourceFileName(String s) {
        sourceFileName = s;
    }

    public void rescale(double sx, double sy, double sz) {
        // never cll this except via setScale, which keeps track of the 
        // scaling applied to the cell;
        int np = points.size();
        for (int i = 0; i < np; i++) {
            nlpoint p = points.elementAt(i);
            p.x *= sx;
            p.y *= sy;
            p.z *= sz;
        }
        trustLineList = false;
        trustPointList = false;
    }

    public double[] getScale() {
        checkHeader();
        double[] scl = new double[3];
        StringTokenizer st = new StringTokenizer(headerText, "\r\n");
        boolean got = false;
        while (!got && st.hasMoreTokens()) {
            String s = st.nextToken();
            int n;
            if ((n = s.indexOf("SCALE")) > 0) {
                got = true;
                StringTokenizer st1 = new StringTokenizer(s.substring(n + 5, s.length()));
                if (st1.countTokens() != 3) {
                    System.out.println("cant parse shrinkage data in file header");
                    System.out.println(s);
                } else {
                    scl[0] = (new Double(st1.nextToken())).doubleValue();
                    scl[1] = (new Double(st1.nextToken())).doubleValue();
                    scl[2] = (new Double(st1.nextToken())).doubleValue();
                }
            }
        }
        return scl;
    }

    public void setScale(double[] scl) {
        double[] sc0 = getScale();
        rescale(scl[0] / sc0[0], scl[1] / sc0[1], scl[2] / sc0[2]);
        StringTokenizer st = new StringTokenizer(headerText, "\r\n");
        StringBuilder sBuff = new StringBuilder();
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            if (s.indexOf("SCALE") > 0) {
                sBuff.append("# SCALE " + mytrim(scl[0]) + " "
                        + mytrim(scl[1]) + " "
                        + mytrim(scl[2]) + "\n");
            } else {
                sBuff.append(s);
                sBuff.append("\n");
            }
        }
        setHeaderText(sBuff.toString());

    }

    private String mytrim(double d) {
        return (" " + d + "       ").substring(1, 6);
    }

    public void fill(String[] s, String fname) {

        // check file type 
        String fend = fname.substring(fname.length() - 3, fname.length());
        if (fend.equals("swc")) {
            fillFromSwcFile(s);
        } else if (fend.equals("asc")) {
            //	 check for lines beginning with "[" as indicative of nl v2;
            int nsb = 0;
            int nlx = s.length;
            for (int i = 1; i < 100 && i < nlx; i++) {
                if ((s[i].trim()).startsWith("[")) {
                    nsb++;
                }
            }

            if (nsb > 0) {
                fillFromNlFile(s);
            } else {
                fillFromNl3File(s);
            }

        } else {
            System.out.println("unknown file extension: " + fend);
            System.out.println("expecting .swc, or .asc (for NL v2 or v3)");
        }

        checkHeader();


    }

    private void checkHeader() {
        StringTokenizer st = new StringTokenizer(headerText, "\n\r");
        StringBuilder sBuff = new StringBuilder();

        int nhf = headerField.length;

        boolean[] bgot = new boolean[nhf];
        boolean gotShrink = false;

        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            if (s.startsWith("#")) {

                for (int j = 0; j < nhf; j++) {
                    if (s.indexOf(headerField[j]) > 0) {
                        bgot[j] = true;
                    }
                    if (s.indexOf("SCALE") > 0) {
                        gotShrink = true;
                    }
                }
            }
            if (s.startsWith("#")) {
                sBuff.append(s);
            } else {
                sBuff.append("# ");
                sBuff.append(s);
            }
            sBuff.append("\n");
        }

        for (int i = 0; i < nhf; i++) {
            if (!bgot[i]) {
                sBuff.append("# ");
                sBuff.append(headerField[i]);
                sBuff.append(" \n");
            }
        }

        if (!gotShrink) {
            sBuff.append("# SCALE 1.0 1.0 1.0 \n");
        }
        headerText = sBuff.toString();
    }


    public void fillFromSwcFile(String[] s) {
        String ts;
        points.removeAllElements();
        StringBuilder hsb = new StringBuilder();

        int i0, i1, iprev;
        double[] d = new double[7];
        nlpoint latestPoint = null;
        nlpoint newPoint = null;
        double x, y, z, r;

        int ptind = 0;
        int ns = s.length;
        for (int i = 0; i < ns; i++) {
            ts = s[i];
            if (ts.startsWith("#")) {
                hsb.append(ts);
                hsb.append("\n");
            } else if (ts.length() > 10) {
                StringTokenizer st = new StringTokenizer(ts, ", ");
                if (st.countTokens() == 7) {
                    for (int j = 0; j < 7; j++) {
                        d[j] = Double.valueOf(st.nextToken()).doubleValue();
                    }
                    i0 = (int) d[0];
                    i1 = (int) d[1];
                    x = d[2];
                    y = d[3];
                    z = d[4];
                    r = d[5];
                    iprev = (int) d[6];

                    if (i % 100 == 0) {
                        System.out.println(" " + i + " " + i0 + " "
                                + x + " " + y + " " + z);
                    }

                    if (ptind != i0 - 1) {
                        System.out.println("swc read error " + i0 + " " + ptind);
                    }

                    if (iprev > 0) {
                        latestPoint = (nlpoint) (points.elementAt(iprev - 1));
                    } else {
                        latestPoint = null;
                    }

                    newPoint = new nlpoint(ptind, i1, latestPoint, x, y, z, r);
                    points.addElement(newPoint);
                    if (latestPoint != null) {
                        latestPoint.addNeighbor(newPoint);
                    }
                    ptind++;
                }
            }
        }
        System.out.println("read " + ptind + " points");
        enforceCommutativity();
        trustLineList = false;
        trustPointList = false;
        headerText = hsb.toString();
    }

    private int ptconv(int minor) {
        int m = minor;
        if (minor == 1 || minor == 2) {
            m = 3; // dendrite;
        } else if (minor == 21 || minor == 22) {
            m = 2; // axon;
        } else if (minor == 41 || minor == 42) {
            m = 1; //somal
        } else if (minor == 61 || minor == 62) {
            m = 4; // apical dendrite;
        }
        return m;

    }

    public void fillFromNlFile(String[] s) {
        String ts;
        headerText = " ";
        points.removeAllElements();
        double[] d = new double[6];
        int i, major, minor;
        nlpoint latestPoint = null;
        nlpoint newPoint = null;
        double x, y, z, r;

        int ptind = 0;
        int ns = s.length;
        for (i = 0; i < ns; i++) {
            ts = s[i];
            if (ts.length() > 10 && ts.substring(0, 1).equals("[")) {
                StringTokenizer st = new StringTokenizer(ts, "[](), ");
                for (int j = 0; j < 6; j++) {
                    d[j] = Double.valueOf(st.nextToken()).doubleValue();
                }
                major = (int) d[0];
                minor = (int) d[1];
                x = d[2];
                y = d[3];
                z = d[4];
                r = d[5];

                if (i % 100 == 0) {
                    System.out.println(" " + i + " " + major + " "
                            + x + " " + y + " " + z);
                }

                if (major == 0) {
                    // no op
                } else if (major == 1) {
                    // check if we have a point just aded by a preexisting 
                    // node of unknown radius - if so, set it to the current radius
                    if (havePointNeedingRadius) {
                        pointNeedingRadius.r = r;
                        havePointNeedingRadius = false;
                    }


                    // draw line to this point from current point
                    newPoint = new nlpoint(ptind, ptconv(minor),
                            latestPoint, x, y, z, r);
                    points.addElement(newPoint);
                    latestPoint.addNeighbor(newPoint);
                    latestPoint = newPoint;
                    ptind++;

                } else if (major == 10 || major == 33) {
                    // 10 -  start new object - not necessary here.
                    // 33 -  former process ending converted to ordinary point
                    // (whatever that means)
                } else if (major == 2) {
                    // 2 -  move to this point
                    nlpoint oldnode = havePointHere(x, y, z);

                    if (oldnode == null) {
                        newPoint = new nlpoint(ptind, ptconv(minor), x, y, z, r);
                        points.addElement(newPoint);
                        latestPoint = newPoint;
                        ptind++;
                    } else {
                        latestPoint = oldnode;
                    }
                    havePointNeedingRadius = true;
                    pointNeedingRadius = latestPoint;


                } else if (major == 3) {
                    // draw marker at this point
                } else if (major == 5) {
                    // no op
                } else if (major == 32) {
                    // one record parameter - ignore for now
                } else {
                    System.out.println("unknown major code: " + major);
                }

            }
        }
        System.out.println("read " + ptind + " points");
        enforceCommutativity();
        trustLineList = false;
        trustPointList = false;
    }

    public void fillFromNl3File(String[] sa) {
        (new nl3parser()).loadFile(sa, this);
    }

    public String write() {
        unwrite();
        SWCwrite = true;
        sb = new StringBuffer();
        sb.append(headerText);
        rootpoint = maxRadiusPoint();
        tracePoint(rootpoint);

        SWCwrite = false;
        String s = sb.toString();
        sb = null;
        return s;
    }

    public void unwrite() {
        GENESISwrite = false;
        SWCwrite = false;
        HOCwrite = false;
    }

    private void pt3dappend(nlpoint p) {
        sb3.append("   pt3dadd(");
        sb3.append(form.format(p.x));
        sb3.append(",");
        sb3.append(form.format(p.y));
        sb3.append(",");
        sb3.append(form.format(p.z));
        sb3.append(",");
        sb3.append(form.format(2 * p.r));
        sb3.append(")\n");
    }

    private void offsetpt3dappend(nlpoint p) {
        sb3.append("   pt3dadd(");
        sb3.append(form.format(p.x));
        sb3.append(",");
        sb3.append(form.format(p.y));
        sb3.append(",");
        sb3.append(form.format(p.z + 0.01));
        sb3.append(",");
        sb3.append(form.format(2 * p.r));
        sb3.append(")\n");
    }

    public void recHOCTrace(nlpoint ppar, nlpoint p, boolean[] done,
            boolean newseg, int parentseg) {
        p.imark = 1;
        boolean begseg = false;
        if (!done[p.myIndex]) {
            done[p.myIndex] = true;

            // NB fudge here: segements numbered from 0, including soma as 
            // a (one point) segment. Hoc file has a soma line, then dendrites
            // numbere from 0, so Hoc dend index is nseg-1;

            if (newseg) {
                if (parentseg == 0) {
                    sb2.append("connect dend[");
                    sb2.append(form.format(nseg - 1));
                    sb2.append("](0), ");
                    sb2.append("soma(0.5)\n");
                } else if (parentseg > 0) {
                    sb2.append("connect dend[");
                    sb2.append(form.format(nseg - 1));
                    sb2.append("](0), ");
                    sb2.append("dend[");
                    sb2.append(form.format(parentseg - 1));
                    sb2.append("](1)\n");
                } else {
                    // neg parent means no parent - ie, we are the soma;
                }

                if (parentseg < 0) {
                    sb3.append("soma {\n");

                } else {
                    sb3.append("dend[");
                    sb3.append(form.format(nseg - 1).trim());
                    sb3.append("] {\n");
                }
                if (ppar != null) {
                    pt3dappend(ppar);
                } else {
                    // add an arbitrary small offset point for the soma;
                    offsetpt3dappend(p);
                }

                nseg++;
            }

            if (p.nnbr == 1 && done[p.pnbr[0].myIndex]) {
                // got to the tip of a segment;
                pt3dappend(p);
                sb3.append("}\n");

            } else if (p.nnbr == 2 && (done[p.pnbr[0].myIndex]
                    != done[p.pnbr[1].myIndex])) {
                // inside a segment;
                pt3dappend(p);

            } else if (p.nnbr == 0) {
                System.out.println("error - disconnected point " + p.myIndex);

            } else {
                // got to a branch point;
                pt3dappend(p);
                sb3.append("}\n");
                begseg = true;
            }

            int cnseg = nseg - 1;
            for (int i = p.nnbr - 1; i >= 0; i--) {
                p.pnbr[i].parent = p;
                recHOCTrace(p, p.pnbr[i], done, begseg, cnseg);
            }
        }
    }

    public void HOCtrace(int ip) {
        enforceCommutativity();
        nseg = 0;
        if (pointList != null) {
            int np = points.size();
            boolean[] done = new boolean[np];
            rootpoint = ip;
            for (int i = 0; i < np; i++) {
                done[i] = false;
            }
            nlpoint p = (nlpoint) (points.elementAt(ip));
            p.parent = null;
            p.writeIndex = 0;
            cwi = 1;

            recHOCTrace(null, (nlpoint) (points.elementAt(ip)), done, true, -1);
            trustLineList = false;
            trustPointList = false;
        }
    }

    private int maxRadiusPoint() {
        int rp = -1;
        maxType = 0;
        double rmax = -1.0;
        double rtmp;
        int np = points.size();
        for (int i = 0; i < np; i++) {
            nlpoint ptmp = (nlpoint) (points.elementAt(i));
            if ((rtmp = ptmp.r) > rmax) {
                rp = i;
                rmax = rtmp;
            }
            if (ptmp.nlcode > maxType) {
                maxType = ptmp.nlcode;
            }
        }
        return rp;
    }

    public String HOCwrite() {
        unwrite();
        HOCwrite = true;
        rootpoint = maxRadiusPoint();

        sb1 = new StringBuffer();
        sb2 = new StringBuffer();
        sb3 = new StringBuffer();

        HOCtrace(rootpoint);

        sb1.append("ndend = ");
        sb1.append(form.format(nseg - 1));
        sb1.append("\n");
        sb1.append("create soma, dend[ndend]\n");
        sb1.append("access soma\n");
        sb1.append("  \n");
        sb2.append("  \n");


        HOCwrite = false;
        StringBuilder sbf = new StringBuilder();
        sbf.append(sb1.toString());
        sbf.append(sb2.toString());
        sbf.append(sb3.toString());
        return sbf.toString();
    }

    public String HOCwriteNS() {
        unwrite();
        //      rootpoint = maxRadiusPoint();
        // above also sets maxType to the maximal nlcode;

        hocWriter hw = new hocWriter(points, sectionTypes, sourceFileName);
        return hw.hocString();
    }

    public String writeNeuroML_v1_8_1() {
        unwrite();
        //      rootpoint = maxRadiusPoint();
        // above also sets maxType to the maximal nlcode;

        neuromlWriter nw = new neuromlWriter(points, sectionTypes, sourceFileName);
        return nw.nmlString(neuromlWriter.NeuroMLVersion.NEUROML_VERSION_1_8_1, false);
    }

    public String writeNeuroML_v2() {
        unwrite();
        //      rootpoint = maxRadiusPoint();
        // above also sets maxType to the maximal nlcode;

        neuromlWriter nw = new neuromlWriter(points, sectionTypes, sourceFileName);
        return nw.nmlString(neuromlWriter.NeuroMLVersion.NEUROML_VERSION_2_3_1, false);
    }
    public String writeNeuroML_v2_morphologyOnly() {
        unwrite();
        //      rootpoint = maxRadiusPoint();
        // above also sets maxType to the maximal nlcode;

        neuromlWriter nw = new neuromlWriter(points, sectionTypes, sourceFileName);
        return nw.nmlString(neuromlWriter.NeuroMLVersion.NEUROML_VERSION_2_3_1, true);
    }

    public String GENESISwriteHR() {
        unwrite();
        //      rootpoint = maxRadiusPoint();
        // above also sets maxType to the maximal nlcode;

        genesisWriter gw = new genesisWriter(points, sectionTypes, sourceFileName);
        return gw.hierarchicalString();
    }

    /*
    public String GENESISwrite() {
        unwrite();
        //      rootpoint = maxRadiusPoint();
        // above also sets maxType to the maximal nlcode;

        genesisWriter gw = new genesisWriter(points, sectionTypes, sourceFileName);
        gw.setFlatStyle(true);
        return gw.hierarchicalString();
    }*/

    public String oldGENESISwrite() {
        unwrite();
        GENESISwrite = true;
        sb = new StringBuffer();
        sb.append("// genesis \n");
        sb.append("*cartesian \n");
        sb.append("*absolute \n");
        sb.append("*asymmetric \n");
        sb.append("*set_compt_param RM 1.0      //ohm*m^2\n");
        sb.append("*set_compt_param RA 1.0      //ohm*m \n");
        sb.append("*set_compt_param CM 0.03     //F/m^2 \n");
        sb.append("*set_compt_param EREST_ACT   -0.06   // volts \n");

        rootpoint = maxRadiusPoint();
        tracePoint(rootpoint);

        GENESISwrite = false;
        String s = sb.toString();
        sb = null;
        return s;
    }

    public void clean() {
        int ipass = 0;
        tracePoint(rootpoint);
        cleaning = true;
        do {
            ipass++;
            nremoved = 0;
            tracePoint(rootpoint);
            removeFloating();
            System.out.println(" pass " + ipass + "  removed "
                    + nremoved + " zero length branches");
        } while (nremoved != 0);
        cleaning = false;
    }

    public nlpoint havePointHere(double x, double y, double z) {
        nlpoint p, q;
        double r, rbest = 100.0;
        int i, ibest = -1;
        p = null;
        // check to see if there is a point with (nearly) these coordinates,
        // and if so, return it
        int np = points.size();
        for (i = 0; i < np; i++) {
            q = (nlpoint) (points.elementAt(i));
            r = (x - q.x) * (x - q.x) + (y - q.y) * (y - q.y) + (z - q.z) * (z - q.z);
            if (r < rbest) {
                ibest = i;
                rbest = r;
            }
        }
        if (rbest < 0.5) {
            p = (nlpoint) (points.elementAt(ibest));
//	 System.out.println ("assuming join " + rbest + " " + ibest);
        }
        return p;
    }

    public void reindexPoints() {
        int np = points.size();
        for (int i = 0; i < np; i++) {
            ((nlpoint) (points.elementAt(i))).myIndex = i;
        }
    }

    public void enforceCommutativity() {
        reindexPoints();
        int np = points.size();
        for (int i = 0; i < np; i++) {
            nlpoint p1 = (nlpoint) (points.elementAt(i));
            for (int inbr = 0; inbr < p1.nnbr; inbr++) {
                nlpoint p2 = p1.pnbr[inbr];
                boolean got = false;
                for (int k = 0; k < p2.nnbr; k++) {
                    if (p2.pnbr[k] == p1) {
                        got = true;
                    }
                }
                if (!got) {
                    System.out.println("correcting non commuting link: "
                            + p1.myIndex + " " + p2.myIndex);
                    p2.addNeighbor(p1);
                }
            }
        }
    }

    public void recAddLine(nlpoint p, int[] nl, boolean[] done) {
        int k;
        nlpoint q;

        done[p.myIndex] = true;

        for (int i = 0; i < p.nnbr; i++) {

            q = (p.pnbr)[i];
            if (!done[q.myIndex]) {
                k = nl[0];
                lineList[k][0] = p.x;
                lineList[k][1] = p.y;
                lineList[k][2] = p.z;
                lineList[k][3] = p.r;

                lineList[k][4] = q.x;
                lineList[k][5] = q.y;
                lineList[k][6] = q.z;
                lineList[k][7] = q.r;

                if (p.shiftMark) {
                    lineList[k][0] += cdp[0];
                    lineList[k][1] += cdp[1];
                    lineList[k][2] += cdp[2];
                }


                if (q.shiftMark) {
                    lineList[k][4] += cdp[0];
                    lineList[k][5] += cdp[1];
                    lineList[k][6] += cdp[2];
                }


                if (p.imark >= 0 && q.imark >= 0) {
                    lineList[k][8] = 1.;
                } else {
                    lineList[k][8] = -1.;
                }
                if (p.nlcode == q.nlcode) {
                    lineList[k][9] = p.nlcode + 0.1;
                }
                nl[0]++;
                recAddLine(q, nl, done);
            }
        }
    }

    public double[][] getLineList() {
        int[] nl = new int[2];
        nlpoint p;

        if (lineList == null || !trustLineList) {
            int np = points.size();
            lineList = new double[np][10];
            boolean[] done = new boolean[np];
            for (int i = 0; i < np; i++) {
                done[i] = false;
            }

            for (int i = 0; i < np; i++) {
                if (!done[i]) {
                    p = (nlpoint) (points.elementAt(i));
                    if (p.myIndex != i) {
                        System.out.println("indexing mismatch " + i + " " + p.myIndex);
                    }
                    recAddLine(p, nl, done);
                }
            }
            trustLineList = true;
            nLines = nl[0];
        }
        return lineList;
    }

    public double[][] getPointList() {
        nlpoint p;
        if (pointList == null || !trustPointList) {
            int np = points.size();
            nPoints = np;

            pointList = new double[np][5];
            for (int i = 0; i < np; i++) {
                p = (nlpoint) (points.elementAt(i));
                pointList[i][0] = p.x;
                pointList[i][1] = p.y;
                pointList[i][2] = p.z;
                pointList[i][3] = p.r;
                pointList[i][4] = p.imark;
                if (p.identPoint != null) {
                    pointList[i][4] = 2;
                }
            }
            trustPointList = true;
        }
        return pointList;
    }

    public void recTrace(nlpoint p, boolean[] done) {
//      pointList[p.myIndex][3] = 1.0;
        p.imark = 1;
        int nfig = 6;
        int ntot = 11;


        if (!done[p.myIndex]) {
            if (SWCwrite) {
                p.writeIndex = cwi;
                cwi++;
                sb.append(" " + p.writeIndex + " " + p.nlcode + " ");
                sb.append(form.format(p.x));
                sb.append(" ");
                sb.append(form.format(p.y));
                sb.append(" ");
                sb.append(form.format(p.z));
                sb.append(" ");
                sb.append(form.format(p.r));
                sb.append(" ");
                sb.append(" " + (p.parent != null ? p.parent.writeIndex : -1)
                        + " \n");
            }



            if (GENESISwrite) {
                if (p.identPoint == null) {
                    p.writeIndex = cwi;
                    cwi++;
                    sb.append(" " + p.writeIndex + "_" + p.nlcode + " ");
                    if (p.parent != null) {
                        if (p.parent.identPoint == null) {
                            sb.append(p.parent.writeIndex + "_"
                                    + p.parent.nlcode + " ");
                        } else {
                            sb.append(p.parent.identPoint.writeIndex + "_"
                                    + p.parent.identPoint.nlcode + " ");
                        }
                    } else {
                        sb.append(" none ");
                    }
                    sb.append(form.format(p.x));
                    sb.append(" ");
                    sb.append(form.format(p.y));
                    sb.append(" ");
                    sb.append(form.format(p.z));
                    sb.append(" ");
                    sb.append(form.format(2. * p.r));
                    sb.append("\n");
                }
            }



            boolean ok = true;
            if (cleaning) {
                if (p.parent != null && p != p.parent
                        && p.pointSeparation(p.parent) < 1.e-6) {
                    boolean jnk = p.parent.removeNeighbor(p);

                    //	       p.print();
                    //	       p.parent.print();

                    for (int i = 0; i < p.nnbr; i++) {
                        p.parent.addNeighbor(p.pnbr[i]);
                        jnk = (p.pnbr[i]).removeNeighbor(p);
                        (p.pnbr[i]).addNeighbor(p.parent);
                        p.pnbr[i].parent = p.parent;
                    }
                    nullifyPoint(p);
                    //		  removePoint (p);
                    nremoved++;
                    ok = false;
                }
            }




            done[p.myIndex] = true;
            if (ok) {
                for (int i = p.nnbr - 1; i >= 0; i--) {

                    p.pnbr[i].parent = p;
                    recTrace(p.pnbr[i], done);
                }
            }
        }
    }

    public void tracePoint(int ip) {
        if (pointList != null) {
            int np = points.size();
            boolean[] done = new boolean[np];
            rootpoint = ip;
            for (int i = 0; i < np; i++) {
                done[i] = false;
            }
            nlpoint p = (nlpoint) (points.elementAt(ip));
            p.parent = null;
            p.writeIndex = 0;
            cwi = 1;

            recTrace((nlpoint) (points.elementAt(ip)), done);
            trustLineList = false;
            trustPointList = false;
        }
    }

    public void removeFloating() {
        int np = points.size();
        reindexPoints();
        for (int i = np - 1; i >= 0; i--) {
            nlpoint p = (nlpoint) (points.elementAt(i));
            if (p.nnbr == 0) {
                points.removeElementAt(i);
            }
        }
        reindexPoints();
    }

    public void separatePoints(int ip0, int ip1) {
        nlpoint p0, p1;
        p0 = (nlpoint) (points.elementAt(ip0));
        p1 = (nlpoint) (points.elementAt(ip1));

        // -------========= is this | right?
        boolean got0 = p0.removeNeighbor(p1);
        boolean got1 = p1.removeNeighbor(p0);
        if (!got0 && !got1) {
            System.out.println("error: points were not neighbours");
        } else {
            trustLineList = false;
            trustPointList = false;
        }
    }

    public void joinPoints(int ip0, int ip1) {
        nlpoint p0, p1;
        p0 = (nlpoint) (points.elementAt(ip0));
        p1 = (nlpoint) (points.elementAt(ip1));

        p0.addNeighbor(p1);
        p1.addNeighbor(p0);
        trustLineList = false;
    }

    public void mergePoints(int ip0, int ip1) {
        nlpoint p0, p1;
        p0 = (nlpoint) (points.elementAt(ip0));
        p1 = (nlpoint) (points.elementAt(ip1));

        for (int i = 0; i < p0.nnbr; i++) {
            nlpoint pp = p0.pnbr[i];
            pp.addNeighbor(p1);
            p1.addNeighbor(pp);
        }
        removePoint(p0);
        trustLineList = false;
    }

    public void identifyPoints(int ip0, int ip1) {
        nlpoint p0, p1;
        p0 = (nlpoint) (points.elementAt(ip0));
        p1 = (nlpoint) (points.elementAt(ip1));

        p0.addNeighbor(p1);
        p1.addNeighbor(p0);
        trustLineList = false;
        trustPointList = false;
        p0.identifyWith(p1);
    }

    public void addPoint(double[] xyzr) {
        points.addElement(new nlpoint(points.size(), -1,
                xyzr[0], xyzr[1], xyzr[2],
                xyzr.length >= 4 ? xyzr[3] : 1.0));
        reindexPoints();
        trustLineList = false;
        trustPointList = false;
    }

    public void addSegment(double[] xyzr1, double[] xyzr2) {
        nlpoint p1, p2;
        p1 = new nlpoint(points.size(), -1,
                xyzr1[0], xyzr1[1], xyzr1[2],
                xyzr1.length > 3 ? xyzr1[3] : 1.0);
        points.addElement(p1);
        p2 = new nlpoint(points.size(), -1,
                xyzr2[0], xyzr2[1], xyzr2[2],
                xyzr2.length > 3 ? xyzr2[3] : 1.0);
        points.addElement(p2);
        p1.addNeighbor(p2);
        p2.addNeighbor(p1);

        reindexPoints();
        trustLineList = false;
        trustPointList = false;
    }

    public void addPoint(double[] xyzr, int ipar) {
        nlpoint p0, pn;
        pn = new nlpoint(points.size(), -1, xyzr[0], xyzr[1], xyzr[2],
                xyzr.length >= 3 ? xyzr[3] : 1.0);

        if (ipar >= 0) {
            p0 = (nlpoint) (points.elementAt(ipar));
            pn.addNeighbor(p0);
            p0.addNeighbor(pn);
        }

        points.addElement(pn);
        reindexPoints();
        trustLineList = false;
        trustPointList = false;
    }

    public void addPoint(nlpoint pnew, nlpoint ppar, int ipt) {
        pnew.nlcode = ipt;
        pnew.myIndex = points.size();
        points.addElement(pnew);
        if (ppar != null) {
            ppar.addNeighbor(pnew);
            pnew.addNeighbor(ppar);
        }
        trustLineList = false;
        trustPointList = false;
    }

    public double[][] linesTo(int ip) {
        nlpoint p = (nlpoint) (points.elementAt(ip));
        double[][] ll = new double[p.nnbr][8];
        for (int k = 0; k < p.nnbr; k++) {
            nlpoint p1 = p.pnbr[k];
            ll[k][0] = p1.x;
            ll[k][1] = p1.y;
            ll[k][2] = p1.z;
            ll[k][3] = p1.r;
            ll[k][4] = p.x;
            ll[k][5] = p.y;
            ll[k][6] = p.z;
            ll[k][7] = p.r;
        }
        return ll;
    }

    public void setPointRadius(double[] xyzr, int ip) {
        nlpoint p0 = (nlpoint) (points.elementAt(ip));
        p0.setRadius(xyzr[3]);
        trustLineList = false;
        trustPointList = false;
    }

    private void markToShift(int i1) {
        for (Enumeration e = points.elements(); e.hasMoreElements();) {
            nlpoint tnlp = (nlpoint) (e.nextElement());
            tnlp.shiftMark = (tnlp.imark == i1);
        }
    }

    public void setPointPosition(double[] xyzr, int ip) {
        nlpoint p0 = (nlpoint) (points.elementAt(ip));
        double[] dxyz = new double[3];
        dxyz[0] = xyzr[0] - p0.x;
        dxyz[1] = xyzr[1] - p0.y;
        dxyz[2] = xyzr[2] - p0.z;

        p0.setPosition(xyzr);

        if (p0.imark == 1) {
            markToShift(1);
            p0.shiftMark = false;
            imposeShift(dxyz);
        }

        trustLineList = false;
        trustPointList = false;
    }

    public void addPoint(int ip0, int ip1) {
        nlpoint p0, p1, pn;
        p0 = (nlpoint) (points.elementAt(ip0));
        p1 = (nlpoint) (points.elementAt(ip1));

        boolean got0 = p0.removeNeighbor(p1);
        boolean got1 = p1.removeNeighbor(p0);
        if (!got0 && !got1) {
            System.out.println("error: points were not neighbours");
        } else {

            pn = new nlpoint(points.size(), 1,
                    0.5 * (p0.x + p1.x), 0.5 * (p0.y + p1.y),
                    0.5 * (p0.z + p1.z), 0.5 * (p0.r + p1.r));
            points.addElement(pn);
            p0.addNeighbor(pn);
            p1.addNeighbor(pn);
            pn.addNeighbor(p0);
            pn.addNeighbor(p1);
            pn.imark = 1;

            reindexPoints();
            trustLineList = false;
            trustPointList = false;
        }
    }

    public void removePoint(int ip0) {
        nlpoint p0;
        p0 = (nlpoint) (points.elementAt(ip0));
        for (Enumeration e = points.elements(); e.hasMoreElements();) {
            boolean jnk = ((nlpoint) (e.nextElement())).removeNeighbor(p0);
        }
        points.removeElementAt(ip0);

        trustLineList = false;
        trustPointList = false;
        reindexPoints();
    }

    public void removePoint(nlpoint p) {
        for (Enumeration e = points.elements(); e.hasMoreElements();) {
            boolean jnk = ((nlpoint) (e.nextElement())).removeNeighbor(p);
        }
        points.removeElement(p);

        trustLineList = false;
        trustPointList = false;
        reindexPoints();
    }

    public void nullifyPoint(nlpoint p) {
        for (Enumeration e = points.elements(); e.hasMoreElements();) {
            boolean jnk = ((nlpoint) (e.nextElement())).removeNeighbor(p);
        }
        p.nnbr = 0;
    }

    public void recHideBranch(nlpoint p) {
        nlpoint q;
        p.imark = p.imark - 1;
        for (int i = 0; i < p.nnbr; i++) {
            q = p.pnbr[i];
            if (q.imark == 2) {
                q.imark = 1;
                recHideBranch(q);
            } else {
                q.imark -= 1;
            }
        }
    }

    public void showLoops() {
        // mark every point
        nlpoint p;
        int np = points.size();
        for (int i = 0; i < np; i++) {
            p = (nlpoint) (points.elementAt(i));
            p.imark = p.nnbr;
        }
        int ndone = 1;
        while (ndone > 0) {
            ndone = 0;
            for (int i = 0; i < np; i++) {
                p = (nlpoint) (points.elementAt(i));
                if (p.imark == 1) {
                    recHideBranch(p);
                    ndone++;
                }
            }
            System.out.println("branch elimination " + ndone);
        }
        for (int i = 0; i < np; i++) {
            p = (nlpoint) (points.elementAt(i));
            if (p.imark <= 1) {
                p.imark = -1;
            }
        }

        trustPointList = false;
        trustLineList = false;
    }

    public void clearTrace() {
        for (Enumeration e = points.elements(); e.hasMoreElements();) {
            ((nlpoint) (e.nextElement())).imark = -1;
        }
        trustLineList = false;
        trustPointList = false;
    }

    public boolean recMarkSection(nlpoint p, boolean[] done,
            int mymark, int itarg) {
        boolean fnd = false;
        if (p.myIndex == itarg) {
            fnd = true;
            p.imark = mymark;
        }
        if (!done[p.myIndex]) {
            done[p.myIndex] = true;
            for (int i = 0; i < p.nnbr && !fnd; i++) {
                fnd = recMarkSection(p.pnbr[i], done, mymark, itarg);
                if (fnd) {
                    p.imark = mymark;
                }
            }
        }
        return fnd;
    }

    public void highlightSection(int i1, int i2) {
        clearTrace();
        if (points != null) {
            int np = points.size();
            boolean[] done = new boolean[np];
            rootpoint = i1;
            for (int i = 0; i < np; i++) {
                done[i] = false;
            }
            nlpoint p = (nlpoint) (points.elementAt(rootpoint));
            int mymark = 1;
            p.imark = mymark;
            boolean bb = recMarkSection(p, done, mymark, i2);
            if (!bb) {
                System.out.println("couldnt connect points");
            }

            trustPointList = false;
            trustLineList = false;
        }
    }

    public void highlightTree(int i1, int i2) {
        clearTrace();
        int mymark = 1;
        nlpoint p;
        if (points != null) {
            int np = points.size();
            boolean[] done = new boolean[np];
            for (int i = 0; i < np; i++) {
                done[i] = false;
            }
            p = (nlpoint) (points.elementAt(i1));
            p.imark = mymark;
            done[i1] = true;

            p = (nlpoint) (points.elementAt(i2));
            recTrace(p, done);

            trustPointList = false;
            trustLineList = false;
        }
    }

    public void highlightPoint(int i1) {
//      clearTrace();
        int mymark = 1;
        nlpoint p;
        if (points != null) {
            int np = points.size();
            p = (nlpoint) (points.elementAt(i1));
            p.imark = mymark;
            trustPointList = false;
            trustLineList = false;
        }
    }

    public void markHighlightedType(int ityp) {
        if (points != null) {
            int np = points.size();

            boolean[] done = new boolean[np];
            for (int i = 0; i < np; i++) {
                nlpoint p = (nlpoint) (points.elementAt(i));
                if (p.imark == 1) {
                    p.nlcode = ityp;
                }
            }

            trustPointList = false;
            trustLineList = false;
        }
    }

    public void deleteHighlighted() {
        if (points != null) {
            int np = points.size();

            for (int i = np - 1; i >= 0; i--) {
                nlpoint p = (nlpoint) (points.elementAt(i));
                if (p.imark == 1) {

                    for (Enumeration e = points.elements(); e.hasMoreElements();) {
                        boolean jnk = ((nlpoint) (e.nextElement())).removeNeighbor(p);
                    }
                    points.removeElement(p);
                }
            }

            trustPointList = false;
            trustLineList = false;
            reindexPoints();
        }
    }

    public void recMarkShiftPoints(nlpoint p, boolean[] done, int imk) {
        if (!done[p.myIndex]) {
            done[p.myIndex] = true;
            p.imark = imk;
            pcount++;
            for (int i = 0; i < p.nnbr; i++) {
                recMarkShiftPoints(p.pnbr[i], done, imk);
            }
        }
    }

    public void markShiftPoints(int ip) {
        int np = points.size();
        boolean[] done = new boolean[np];
        rootpoint = ip;
        for (int i = 0; i < np; i++) {
            done[i] = false;
        }
        nlpoint p = (nlpoint) (points.elementAt(ip));

        done[ip] = true;
        int[] ndaughter = new int[p.nnbr];
        int idrop = 0;
        int nmax = 0;
        for (int is = 0; is < p.nnbr; is++) {
            pcount = 0;
            recMarkShiftPoints(p.pnbr[is], done, is + 10);
            ndaughter[is] = pcount;
            if (pcount > nmax) {
                nmax = pcount;
                idrop = is;
            }
        }

        System.out.println("dropping " + idrop);


        for (int i = 0; i < points.size(); i++) {
            nlpoint p1 = (nlpoint) (points.elementAt(i));
            if (p1.imark == idrop + 10) {
                p1.imark = -1;
                p1.shiftMark = false;
            } else {
                p1.imark = 1;
                p1.shiftMark = true;
            }
        }

        trustLineList = false;
        trustPointList = false;

        System.out.println("marked shift points: " + ndaughter[0] + " "
                + ndaughter[1]);
    }

    public void setShift(double[] dat) {
        cdp = dat;
        trustLineList = false;
    }

    public void imposeShift(double[] dat) {
        cdp = dat;
        nlpoint p;
        int np = points.size();
        boolean[] done = new boolean[np];
        for (int i = 0; i < np; i++) {
            p = (nlpoint) (points.elementAt(i));
            if (p.shiftMark) {
                p.shift(cdp);
            }
        }
        trustLineList = false;
        for (int i = 0; i < 3; i++) {
            cdp[i] = 0.0;
        }
    }

    public void recMakeDG(nlpoint p, boolean[] done, int[] iny) {
        done[p.myIndex] = true;
        nlpoint pd;
        int itb = 0;
        double lppd;
        for (int i = 0; i < p.nnbr; i++) {

            pd = p.pnbr[i];
            if (!done[pd.myIndex]) {
                pd.parent = p;

                lppd = Math.sqrt((p.x - pd.x) * (p.x - pd.x)
                        + (p.y - pd.y) * (p.y - pd.y)
                        + (p.z - pd.z) * (p.z - pd.z));

                pd.dgx = p.dgx + lppd;
                pd.dgy = iny[0];

                if (itb > 0) {
                    iny[0]++;
                }
                itb++;
                recMakeDG(pd, done, iny);
            }
        }
    }

    public void makeDG() {
        if (points != null) {
            int np = points.size();

            int irp = 0;
            double rmax = ((nlpoint) (points.elementAt(irp))).r;
            for (int i = 0; i < np; i++) {
                double rt = ((nlpoint) (points.elementAt(i))).r;
                if (rt > rmax) {
                    irp = i;
                    rmax = rt;
                }
            }


            int[] iny = new int[1];
            iny[0] = 0;

            boolean[] done = new boolean[np];
            for (int i = 0; i < np; i++) {
                done[i] = false;
            }

            nlpoint p = (nlpoint) (points.elementAt(irp));
            p.dgx = 0.0;
            p.dgy = 0.0;

            recMakeDG(p, done, iny);
            trustPointList = false;
            trustLineList = false;
        }
    }
}
