/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evoLevel;

/**
 *
 * @author andre
 */
public class LevelConfig {

    public static int borderSize = 5;
    public static double edgeSize = 6;
    
    public static int minNodeCount = 25;
    public static int maxNodeCount = 50;
    
    public static double minEdgeDistance = 5;
    
    public static int areaIntersectionPenalty =  100;
    public static int edgeIntersectionPenalty = 1000;
    public static int nodeCountPenalty        = 1000;
    public static int edgeDistancePentalty    = 1000;
    
    public static double minNodeSize =  6.0;
    public static double maxNodeSize = 10.0;
    public static int scaleFactor = 50;
    
    public static double expansionProb = 0.05; // used on graphX crossover
    
    public static double barabasiFactor = 4; // delta
    public static int maxLinksPerStep = 2; // (m)
    
    public static double crossoverProb  =  0.9;
    public static double mutationProb   =  0.1;
    public static double refinementProb =  0.025;
    
    public static double nodeXLeap = 1.25;
    public static double nodeYLeap = 1.25;
    public static double nodeZLeap = 0;
    
    public static int popSize = 100;
    public static int maxGen  = 200;
    
    public static int[] desiredAngles = {0, 90, 180};
    //public static int[] desiredAngles = {};
    
    public static int idealNonLinearity = 3; // attention: minimum value is 1
    
    public static boolean useDesiredAngles = true;
    public static boolean useAverageShortestPath = true;
    public static boolean useMaximizeNodeCount = false;
    public static boolean useIdealNonLinearity = true;
    
    public static boolean useRefinement = true;
    
    public static String folder = "D:\\MEGA\\posdoc\\MASGameBuilder2\\Assets\\StreamingAssets\\";
    
    public static int numberOfProcess = 16;
}
