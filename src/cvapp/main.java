package cvapp;

import java.io.*;
import java.net.URL;
import java.util.Vector;
import javax.swing.JApplet;

/*
"main.java" is the main entry point for stripped down cvapp.
SWC files present on the website are used as input to program. Hence the parameter accepted by the code is SWC url.
JNLP file present on the website are used to call the code and give the necessary CNG.swc file to it.
*/
public class main extends JApplet
{

    public main()
    {
    }

    public static void main(String argv[])
    {
    	//String a = "http://neuromorpho.org/neuroMorpho/dableFiles/borst/CNG%20version/dCH-cobalt.CNG.swc"; // For developer use
    	String a = argv[0];
        neuronEditorFrame nef = new neuronEditorFrame(600, 500);
        nef.setReadWrite(true, true);
        nef.validate();
        nef.setVisible(true);
        try
        {
            URL u = new URL(a);
            int indexof = a.lastIndexOf('/')+1;
            String directory = a.substring(0,indexof);
            String fileName = a.substring(indexof, a.length());
            String sdata[] = readStringArrayFromURL(u);
//            System.out.println((new StringBuilder("FileLength-->")).append(sdata.length).toString());
            nef.setTitle("3DViewer (Modified from CVAPP with permission)-Neuron: "+fileName);
            nef.loadFile(sdata,directory,fileName);
        }
        catch(Exception exception) { }
    }

	/**
	This method receives the URL as input and returns the same as array of string.
	*/
    public static String[] readStringArrayFromURL(URL u)
    {
        Vector vs = new Vector();
        String sdat[] = (String[])null;
        if(u != null)
        {
            try
            {
                java.io.InputStream in = u.openStream();
                BufferedReader bis = new BufferedReader(new InputStreamReader(in));
                do
                {
                    String line = bis.readLine();
                    if(line == null)
                    {
                        break;
                    }
                    vs.addElement(line);
                } while(true);
            }
            catch(IOException ex)
            {
                System.out.println("URL read error ");
            }
            if(vs.size() > 0)
            {
                sdat = new String[vs.size()];
                for(int i = 0; i < vs.size(); i++)
                {
                    sdat[i] = (String)(String)vs.elementAt(i);
                }

            }
        }
        return sdat;
    }
}