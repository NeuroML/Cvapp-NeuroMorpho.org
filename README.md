### Cvapp for NeuroMorpho.org

This is a version of [Cvapp](http://www.compneuro.org/CDROM/docs/cvapp.html), originally developed by Robert Cannon,
which has been customised as a 3D viewer for http://neuromorpho.org with updated export functionality for NEURON, 
GENESIS & NeuroML versions 1 & 2.

This application is available to use (through Java Webstart) on NeuroMorpho.org. 
Browse to the page for a single cell and press **3D Neuron Viewer**.

This application can also be run standalone for visualising/exporting cells in SWC format. Get a clone of the code from GitHub with:

    git clone git://github.com/pgleeson/Cvapp-NeuroMorpho.org.git
    cd Cvapp-NeuroMorpho

To view an example cell, build the code and run against one of the sample SWC cells 
in the examples folder:

    ./make.sh 
    ./run.sh examples/dCH-cobalt.CNG.swc

or on Windows:

    make.bat
    run.bat examples\dCH-cobalt.CNG.swc
  
Contact ruchisparekh@gmail.com and p.gleeson@ucl.ac.uk for more details.

[![Build Status](https://travis-ci.org/pgleeson/Cvapp-NeuroMorpho.org.svg?branch=master)](https://travis-ci.org/pgleeson/Cvapp-NeuroMorpho.org)