/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package experiments;

import evoGraph.Config;

/**
 *
 * @author andre
 */
public class OnlyASPandDIN extends Experiment{
    public static void main(String args[]){  
       OnlyASPandDIN exp = new OnlyASPandDIN();
       exp.run();
    }
    
    public OnlyASPandDIN() {
        super();
    }
    
    @Override
    public void setup(){
        super.setup();
        
        Config.useMaximizeNodeCount = false;
        Config.minNodeCount = 25;
        Config.maxNodeCount = 50;
        
        Config.useDesiredAngles = false;
        Config.useAverageShortestPath = true; 
        Config.useIdealNonLinearity = true; 
        Config.desiredAngles = new int[0]; 
        
        Config.idealNonLinearity = 3;
        
        Config.folder = "D:\\Mega\\posdoc\\MapGenerator\\experiments\\only_asp_and_din\\";
    }
}
