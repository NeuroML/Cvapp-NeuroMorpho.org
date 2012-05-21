package cvapp;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.Vector;
import javax.swing.*;
import javax.xml.XMLConstants;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

/*
"main.java" is the main entry point for stripped down cvapp.
SWC files present on the website are used as input to program. Hence the parameter accepted by the code is SWC url.
JNLP file present on the website are used to call the code and give the necessary CNG.swc file to it.
 */

public class main implements Runnable/*extends JApplet*/ {

    public static final String TEST_FLAG = "-test";
    public static final String TEST_ONE_FLAG = "-testone";
    //public static final String NML_ONLY_FLAG = "-nml";
    
    private String[] myArgs = null;
    

    public main(String[] args) {
        this.myArgs = args;
    }

    public static void main(String[] args) {
        
        //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        if (args.length == 1 && args[0].equals(TEST_FLAG)) {
            
            String[] filesToTest = new String[]{"examples/l22_cylsoma.swc",
                                                "examples/l22_sphersoma.swc",
                                                "examples/l22.swc",
                                                "examples/l22_small.swc",
                                                "examples/dCH-cobalt.CNG_small.swc",
                                                "examples/dCH-cobalt.CNG.swc"};
            
            for (String f : filesToTest) {
                String[] argsNew = new String[]{f,TEST_FLAG};
                SwingUtilities.invokeLater(new main(argsNew));
            }
            
        } else {
            SwingUtilities.invokeLater(new main(args));
        }

    }
    
    public void run() {
        
        //String a = "http://neuromorpho.org/neuroMorpho/dableFiles/borst/CNG%20version/dCH-cobalt.CNG.swc"; // For developer use
        
        if (myArgs.length==0){
            String usage= "\nError, missing SWC file containing morphology!\n\nUsage: \n    java -cp build cvapp.main swc_file [-test]"
                    + "\n  or:\n    ./run.sh swc_file [-test]\n\nwhere swc_file is the file name or URL of the SWC morphology file\n";
            System.out.println(usage);
            System.exit(0);
            
        }
        
        String a = myArgs[0];
        
        try {
            
            if (!a.startsWith("http://")&&!a.startsWith("file://")){
                a = "file://"+(new File(a)).getCanonicalPath();
            }
                   
            neuronEditorFrame nef = null;
            nef = new neuronEditorFrame(700, 600);

            //nef.validate();
            nef.pack();
            centerWindow(nef);
            
            nef.setVisible(true);
          

            nef.setReadWrite(true, true);
            int indexof = a.lastIndexOf('/') + 1;
            String directory = a.substring(0, indexof);
            String fileName = a.substring(indexof, a.length());
        
            URL u = new URL(a);
            String sdata[] = readStringArrayFromURL(u);
            
            nef.setTitle("3DViewer (Modified from CVAPP with permission)-Neuron: " + fileName);
            nef.loadFile(sdata, directory, fileName);

            if (myArgs.length==2 && myArgs[1].equals(TEST_ONE_FLAG)){
                //Thread.sleep(1000);
                doTests(nef, fileName);
            }

            if (myArgs.length==2 && myArgs[1].equals(TEST_FLAG)){
                //Thread.sleep(1000);
                doTests(nef, fileName);
                
                File exampleDir = new File("twoCylSwc");
                for (File f: exampleDir.listFiles())
                {
                    if (f.getName().endsWith(".swc"))
                    {
                        sdata = fileString.readStringArrayFromFile(f.getAbsolutePath());
                        nef.setTitle("3DViewer (Modified from CVAPP with permission)-Neuron: " + f.getName());
                        nef.loadFile(sdata, f.getParent(), f.getName());
                        doTests(nef, f.getAbsolutePath());
                    }
                }
                exampleDir = new File("spherSomaSwc");
                for (File f: exampleDir.listFiles())
                {
                    if (f.getName().endsWith(".swc"))
                    {
                        sdata = fileString.readStringArrayFromFile(f.getAbsolutePath());
                        nef.setTitle("3DViewer (Modified from CVAPP with permission)-Neuron: " + f.getName());
                        nef.loadFile(sdata, f.getParent(), f.getName());
                        doTests(nef, f.getAbsolutePath());
                    }
                }
                exampleDir = new File("caseExamples");
                for (File f: exampleDir.listFiles())
                {
                    if (f.getName().endsWith(".swc"))
                    {
                        sdata = fileString.readStringArrayFromFile(f.getAbsolutePath());
                        nef.setTitle("3DViewer (Modified from CVAPP with permission)-Neuron: " + f.getName());
                        nef.loadFile(sdata, f.getParent(), f.getName());
                        doTests(nef, f.getAbsolutePath());
                    }
                }
            }
           
        } catch (Exception exception) {
            System.err.println("Error while handling SWC file ("+a+")");
            exception.printStackTrace();
        }
    }

    /*
     * Carries out a number of tests, generates NEURON, GENESIS and NeuroML code etc.
     */
    private static void doTests(neuronEditorFrame nef, String fileName){
        System.out.println("Testing Cvapp/NeuroMorpho.Org by generating NEURON, GENESIS and NeuroML files for "+fileName);
        File tempDir = new File("temp");
        if (!tempDir.exists()) tempDir.mkdir();

        neuronEditorPanel nep = nef.getNeuronEditorPanel();

        String rootFileName = fileName;
        if (rootFileName.toLowerCase().endsWith(".swc")) {
            rootFileName = rootFileName.substring(0, rootFileName.length()-4);
        }
        
        if (rootFileName.lastIndexOf(System.getProperty("file.separator"))>0) {
            rootFileName = rootFileName.substring(rootFileName.lastIndexOf(System.getProperty("file.separator"))+1);
        }

        // NEURON save...

        String neuronFileName = rootFileName+".hoc";
        File neuronFile = new File(tempDir, neuronFileName);
        File neuronTestFile = new File(tempDir, rootFileName+"_test.hoc");


        nep.writeStringToFile(nep.getCell().HOCwriteNS(), neuronFile.getAbsolutePath());

        StringBuilder sbNeuTest = new StringBuilder();
        sbNeuTest.append("load_file(\"nrngui.hoc\")\n");
        sbNeuTest.append("load_file(\"../neuronUtils/nCtools.hoc\")\n");
        sbNeuTest.append("load_file(\"../neuronUtils/cellCheck.hoc\")\n");
        sbNeuTest.append("load_file(\"nrngui.hoc\")\n");
        sbNeuTest.append("load_file(\""+neuronFileName+"\")\n\n");
        sbNeuTest.append("forall morph()\n");
        System.out.println("--------------------------------------------------------------");
        nep.writeStringToFile(sbNeuTest.toString(), neuronTestFile.getAbsolutePath());

        System.out.println("Saved NEURON representation of the file to: "+neuronFile.getAbsolutePath()+": "+neuronFile.exists());
        System.out.println("--------------------------------------------------------------");

        // GENESIS save...

        String genesisFileName = rootFileName+".p";
        File genesisFile = new File(tempDir, genesisFileName);
        File genesisTestFile = new File(tempDir, rootFileName+"_test.g");

        nep.writeStringToFile(nep.getCell().GENESISwriteHR(), genesisFile.getAbsolutePath());
        
        StringBuilder sbGenTest = new StringBuilder();


        sbGenTest.append("include compartments \n");
        sbGenTest.append("create neutral /library\n");
        sbGenTest.append("disable /library\n");
        sbGenTest.append("ce /library\n");
        sbGenTest.append("make_cylind_compartment\n");
        sbGenTest.append("make_cylind_symcompartment\n");
        sbGenTest.append("make_sphere_compartment\n");
        sbGenTest.append("ce /\n");
        sbGenTest.append("echo \"Prototype compartments created, reading cell from "+genesisFileName+"\"\n");
        sbGenTest.append("readcell "+genesisFileName+" /mycell\n\n");

        sbGenTest.append("create xform /form [0,0,400,400] -nolabel\n");
        sbGenTest.append("create xdraw /form/draw [0,0,100%,100%] -wx 0.002 -wy 0.002 -transform ortho3d -bg white\n");
        sbGenTest.append("setfield /form/draw xmin -3.0E-4 xmax 3.0E-4 ymin -3.0E-4 ymax 3.0E-4 vx 0.0 vy 0.0 vz -0.002\n");
        sbGenTest.append("create xcell /form/draw/cell -path \"/mycell/##[][TYPE=compartment],/mycell/##[][TYPE=symcompartment]\" -colfield Vm -colmin -0.07 -colmax 0.03 -diarange -5\n");
        sbGenTest.append("xcolorscale hot\n");
        sbGenTest.append("xshow /form\n\n");
        sbGenTest.append("showfield /mycell/##[][TYPE=compartment] **\n\n");
        
        nep.writeStringToFile(sbGenTest.toString(), genesisTestFile.getAbsolutePath());

        System.out.println("Saved GENESIS representation of the file to: "+genesisFile.getAbsolutePath()+": "+genesisFile.exists());
        System.out.println("--------------------------------------------------------------");


        // NeuroML save...

        String nmlFileName = rootFileName+".xml";
        File nmlFile = new File(tempDir, nmlFileName);


        nep.writeStringToFile(nep.getCell().writeNeuroML_v1_8_1(), nmlFile.getAbsolutePath());

        System.out.println("Saved NeuroML representation of the file to: "+nmlFile.getAbsolutePath()+": "+nmlFile.exists());
        
        File schemaFile = new File("Schemas/v1.8.1/Level3/NeuroML_Level3_v1.8.1.xsd");
        try
        {
            Source schemaFileSource = new StreamSource(schemaFile);

            //schemaFile = "http://sourceforge.net/apps/trac/neuroml/export/809/NeuroML2/Schemas/NeuroML2/NeuroML_v2alpha.xsd";
            //schemaFileSource = new StreamSource(schemaFile);

            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            Schema schema = factory.newSchema(schemaFileSource);

            Validator validator = schema.newValidator();

            Source xmlFileSource = new StreamSource(nmlFile);

            validator.validate(xmlFileSource);

            System.out.println("****   File: "+nmlFile+" is VALID according to "+schemaFile+"!!!    ****");

        }
        catch (Exception ex)
        {
            System.err.println("Problem validating xml file: "+ nmlFile.getAbsolutePath()+" according to "+schemaFile+"!!!");
            ex.printStackTrace();

            System.exit(1);
        }
    }

    /**
    This method receives the URL as input and returns the same as array of string.
     */
    public static String[] readStringArrayFromURL(URL u) {
        Vector vs = new Vector();
        String sdat[] = (String[]) null;
        if (u != null) {
            try {
                java.io.InputStream in = u.openStream();
                BufferedReader bis = new BufferedReader(new InputStreamReader(in));
                do {
                    String line = bis.readLine();
                    if (line == null) {
                        break;
                    }
                    vs.addElement(line);
                } while (true);
            } catch (IOException ex) {
                System.out.println("URL read error ");
            }
            if (vs.size() > 0) {
                sdat = new String[vs.size()];
                for (int i = 0; i < vs.size(); i++) {
                    sdat[i] = (String) (String) vs.elementAt(i);
                }

            }
        }
        return sdat;
    }
    
    /*
     * Places the Window object at the center of the screen
     */
    public static void centerWindow(Window win) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        Dimension dlgSize = win.getSize();

        win.setLocation((screenSize.width - dlgSize.width) / 2,
            (screenSize.height - dlgSize.height) / 2);

    }
}