### Cvapp for NeuroMorpho.org

[![Java CI](https://github.com/pgleeson/Cvapp-NeuroMorpho.org/actions/workflows/ant.yml/badge.svg)](https://github.com/pgleeson/Cvapp-NeuroMorpho.org/actions/workflows/ant.yml)

This is a version of [Cvapp](http://www.compneuro.org/CDROM/docs/cvapp.html), originally developed by Robert Cannon,
which has been customised as a 3D viewer for [**SWC files**](#swc-format-documentation) for http://NeuroMorpho.Org with updated export functionality for NEURON,
GENESIS & NeuroML versions 1 & 2.

![Cvapp Screenshot](https://github.com/pgleeson/Cvapp-NeuroMorpho.org/raw/master/examples/Screenshot.JPG)


This application is available to use (through Java Webstart) on NeuroMorpho.org.
Browse to the page for a single cell and press **3D Neuron Viewer**.

This application can also be run standalone for visualising/exporting cells in SWC format. Get a clone of the code from GitHub with:

    git clone git://github.com/pgleeson/Cvapp-NeuroMorpho.org.git
    cd Cvapp-NeuroMorpho.org

To view an example cell, build the code and run against one of the sample SWC cells
in the examples folder:

    ./make.sh
    ./run.sh examples/dCH-cobalt.CNG.swc

or on Windows:

    make.bat
    run.bat examples\dCH-cobalt.CNG.swc

Contact ruchisparekh@gmail.com and p.gleeson@ucl.ac.uk for more details.

### Other SWC related links

#### SWC format documentation

See *An on-line archive of reconstructed hippocampal neurons, [Cannon et al. 1998](https://www.sciencedirect.com/science/article/pii/S0165027098000910)*.
See also "What is SWC format?" at http://neuromorpho.org/myfaq.jsp.  

#### Using SWC viewer on NeuroMorpho.Org

A short tutorial on using the SWC viewer on NeuroMorpho.Org and exporting to NeuroML
can be found [here](https://github.com/NeuralEnsemble/NeuroinformaticsTutorial/blob/master/Exercises/Exercise1_NeuroMorpho_to_OSB.md).

#### SWC to NeuroML using neuroConstruct

SWC can also be [loaded in to neuroConstruct](http://www.neuroconstruct.org/docs/import.html#Cvapp+%28SWC+files%29),
visualized, edited, passive electrical properties and ion channel conductance densities added and the cell files exported to NeuroML 1 & 2.

#### NeuroML to SWC

A script for conversion of NeuroML 2 cells to SWC can be found here: https://github.com/NeuroML/pyNeuroML/tree/master/pyneuroml/swc.
