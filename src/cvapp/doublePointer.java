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



// why on earth can't this be done with a Double - it must be possible???


public class doublePointer extends Object {
  double value;

  public doublePointer (double d) {
    value = d;
  }

  public void set (double d) {
    value = d;
  }

  public double value () {
    return value;
  }

  public void setValue (String s) {
    value = (Double.valueOf(s)).doubleValue();
  }

}


