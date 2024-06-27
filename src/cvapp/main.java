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
    public static final String TEST_NOGUI_FLAG = "-testnogui";
    public static final String NEUROML1_EXPORT_FLAG = "-exportnml1";
    public static final String NEUROML2_EXPORT_FLAG = "-exportnml2";
    public static final String TEST_ONE_FLAG = "-testone";
    //public static final String NML_ONLY_FLAG = "-nml";

    public static final String LATEST_NEUROML_V2_SCHEMA = "Schemas/v2/NeuroML_v2.3.1.xsd";
    
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
                    + "\n  or:\n    ./run.sh swc_file ["+TEST_FLAG+"|"+TEST_NOGUI_FLAG+"|"+NEUROML1_EXPORT_FLAG+"|"+NEUROML2_EXPORT_FLAG+"]\n\n"
                    + "where swc_file is the file name or URL of the SWC morphology file\n";
            System.out.println(usage);
            System.exit(1);
            
        }
        
        String a = myArgs[0];
        File baseDir = new File(".");
        if ((new File(a)).exists()){
            baseDir = (new File(a)).getParentFile();
        }
            
        
        try {
            
            File root = new File(main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile();
        
            if (!a.startsWith("http://")&&!a.startsWith("file://")){
                
                if (System.getProperty("os.name").toLowerCase().indexOf("indows") > 0)
                    a = "file:///"+(new File(a)).getCanonicalPath();
                else 
                    a = "file://"+(new File(a)).getCanonicalPath();
            }
                   
            
            boolean supressGui = false;
            
            if (myArgs.length==2 && 
                    (myArgs[1].equals(TEST_NOGUI_FLAG) || 
                     myArgs[1].equals(NEUROML1_EXPORT_FLAG) || 
                     myArgs[1].equals(NEUROML2_EXPORT_FLAG))){
                supressGui = true;
            }
            
            neuronEditorFrame nef = null;
            nef = new neuronEditorFrame(700, 600, supressGui);

            //nef.validate();
            nef.pack();
            centerWindow(nef);
            
            
            nef.setVisible(!supressGui);
          

            nef.setReadWrite(true, true);
            int indexof = a.lastIndexOf('/') + 1;
            String directory = a.substring(0, indexof);
            String fileName = a.substring(indexof, a.length());
        
            URL u = new URL(a);
            String sdata[] = readStringArrayFromURL(u);
            
            nef.setTitle("3DViewer (Modified from CVAPP with permission)-Neuron: " + fileName);
            nef.loadFile(sdata, directory, fileName);
            System.out.println("Loaded: "+fileName);

            if (myArgs.length==2 && myArgs[1].equals(TEST_ONE_FLAG)){
                //Thread.sleep(1000);
                doTests(nef, fileName, null);
            }
            else if (myArgs.length==2 && myArgs[1].equals(NEUROML1_EXPORT_FLAG)){
                File rootFile = (new File(baseDir, fileName)).getAbsoluteFile();
                
                String nml1FileName = rootFile.getName().endsWith(".swc") ? 
                                         rootFile.getName().substring(0, rootFile.getName().length()-4)+".xml" : 
                                         rootFile.getName()+".xml";
                
                File nml1File = new File(rootFile.getParentFile(), nml1FileName);
                
                neuronEditorPanel nep = nef.getNeuronEditorPanel();

                nep.writeStringToFile(nep.getCell().writeNeuroML_v1_8_1(), nml1File.getAbsolutePath());

                System.out.println("Saved NeuroML representation of the file to: "+nml1File.getAbsolutePath()+": "+nml1File.exists());
                
                File v1schemaFile = new File(root, "Schemas/v1.8.1/Level3/NeuroML_Level3_v1.8.1.xsd");

                validateXML(nml1File, v1schemaFile);
                
                System.exit(0);
            }
            else if (myArgs.length==2 && myArgs[1].equals(NEUROML2_EXPORT_FLAG)){
                
                File rootFile = (new File(baseDir, fileName)).getAbsoluteFile();
          
                String nml2FileName = rootFile.getName().endsWith(".swc") ? 
                                         rootFile.getName().substring(0, rootFile.getName().length()-4)+".cell.nml" : 
                                         rootFile.getName()+".cell.nml";
                
                if (Character.isDigit(nml2FileName.charAt(0))) {
                    nml2FileName = "Cell_"+nml2FileName;
                } 
                
                File nml2File = new File(rootFile.getParentFile(), nml2FileName);
                
                neuronEditorPanel nep = nef.getNeuronEditorPanel();

                nep.writeStringToFile(nep.getCell().writeNeuroML_v2(), nml2File.getAbsolutePath());

                System.out.println("Saved the NeuroML representation of the file to: "+nml2File.getAbsolutePath()+": "+nml2File.exists());

                validateXML(nml2File, new File(root, LATEST_NEUROML_V2_SCHEMA));
                
                System.exit(0);
            }
            else if (myArgs.length==2 && (myArgs[1].equals(TEST_FLAG) || (myArgs[1].equals(TEST_NOGUI_FLAG)))){
                //Thread.sleep(1000);
                doTests(nef, fileName, null);
                
                File exampleDir = new File("twoCylSwc");
                for (File f: exampleDir.listFiles())
                {
                    if (f.getName().endsWith(".swc"))
                    {
                        sdata = fileString.readStringArrayFromFile(f.getAbsolutePath());
                        nef.setTitle("3DViewer (Modified from CVAPP with permission)-Neuron: " + f.getName());
                        nef.loadFile(sdata, f.getParent(), f.getName());
                        doTests(nef, f.getAbsolutePath(), null);
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
                        doTests(nef, f.getAbsolutePath(), null);
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
                        doTests(nef, f.getAbsolutePath(), "caseExamples/NeuroML2");
                    }
                }
                
                if (supressGui)
                    System.exit(0);
            }
           
        } catch (Exception exception) {
            System.err.println("Error while handling SWC file ("+a+")");
            exception.printStackTrace();
            System.exit(1);
        }
    }

    /*
     * Carries out a number of tests, generates NEURON, GENESIS and NeuroML code etc.
     */
    private static void doTests(neuronEditorFrame nef, String fileName, String nmlExportDirname){
        System.out.println("Testing Cvapp/NeuroMorpho.Org by generating NEURON, GENESIS and NeuroML files for "+fileName);
        File tempDir = new File("temp");
        if (!tempDir.exists()) tempDir.mkdir();
        File nmlExportDir;
        if (nmlExportDirname==null)
            nmlExportDir = tempDir;
        else
            nmlExportDir = new File(nmlExportDirname);

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

        String nml1FileName = rootFileName+".xml";
        File nml1File = new File(tempDir, nml1FileName);

        nep.writeStringToFile(nep.getCell().writeNeuroML_v1_8_1(), nml1File.getAbsolutePath());

        System.out.println("Saved NeuroML representation of the file to: "+nml1File.getAbsolutePath()+": "+nml1File.exists());

        File v1schemaFile = new File("Schemas/v1.8.1/Level3/NeuroML_Level3_v1.8.1.xsd");

        validateXML(nml1File, v1schemaFile);

        String nml2FileName = rootFileName+".cell.nml";
        
        if (Character.isDigit(nml2FileName.charAt(0))) {
            nml2FileName = "Cell_"+nml2FileName;
        } 
        File nml2File = new File(nmlExportDir, nml2FileName);

        nep.writeStringToFile(nep.getCell().writeNeuroML_v2(), nml2File.getAbsolutePath());

        System.out.println("Saved NeuroML representation of the file to: "+nml2File.getAbsolutePath()+": "+nml2File.exists());

        validateXMLWithURL(nml2File, LATEST_NEUROML_V2_SCHEMA);
        
    }

    private static void validateXML(File nmlFile, File schemaFile)
    {

        try
        {
            Source schemaFileSource = new StreamSource(schemaFile);

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
    private static void validateXMLWithURL(File nmlFile, String schemaUrl)
    {

        try
        {
            Source schemaFileSource = new StreamSource(schemaUrl);

            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            Schema schema = factory.newSchema(schemaFileSource);

            Validator validator = schema.newValidator();

            Source xmlFileSource = new StreamSource(nmlFile);

            validator.validate(xmlFileSource);

            System.out.println("****   File: "+nmlFile+" is VALID according to "+schemaUrl+"!!!    ****");

        }
        catch (Exception ex)
        {
            System.err.println("Problem validating xml file: "+ nmlFile.getAbsolutePath()+" according to "+schemaUrl+"!!!");
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