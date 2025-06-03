package org.code;

//import javax.swing.JFrame;

import org.code.neighborhood.Painter;
//import org.code.neighborhood.gui.PainterVisualizer;

public class MyNeighborhood {

    public static void main(String[] args) {
        Painter myPainter = new Painter(0,0,"east",12);

    


        //move and paint with your painter
        myPainter.move();
        myPainter.paint("red");
        for(int i = 0; i < 3; i++){
            myPainter.turnLeft();
        }
        myPainter.move();
        

    }

    
    
}
