/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io;

import config.GeneralConfig;
import evoLevel.LevelConfig;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author andre
 */
public class LevelConfigManager {
    
    public void saveRunTime(String name){        
        String pathStr = name.replace("/", File.separator).replace("\\", File.separator);
        String folderStr = LevelConfig.folder.replace("/", File.separator).replace("\\", File.separator);
        Integer borderS = GeneralConfig.borderSize;
        Double edgeS = GeneralConfig.edgeSize;
        Integer minNC = LevelConfig.minNodeCount;
        Integer maxNC = LevelConfig.maxNodeCount;
        Double minNS = LevelConfig.minNodeSize;
        Double maxNS = LevelConfig.maxNodeSize;
        Double minED = LevelConfig.minEdgeDistance;
        Double bFactor = LevelConfig.barabasiFactor;
        Integer links = LevelConfig.maxLinksPerStep;
        Integer nip = LevelConfig.areaIntersectionPenalty;
        Integer eip = LevelConfig.edgeIntersectionPenalty;
        Integer ncp = LevelConfig.nodeCountPenalty;
        Integer edp = LevelConfig.edgeDistancePentalty;
        Double crxP = LevelConfig.crossoverProb;
        Double mutP = LevelConfig.mutationProb;
        Double refP = LevelConfig.refinementProb;
        Double xL = LevelConfig.nodeXLeap;
        Double yL = LevelConfig.nodeYLeap;
        Double zL = LevelConfig.nodeZLeap;
        Integer pop = LevelConfig.popSize;
        Integer gen = LevelConfig.maxGen;
        Boolean useDA = LevelConfig.useDesiredAngles;
        Boolean useAvgSP = LevelConfig.useAverageShortestPath;
        Boolean useMNC = LevelConfig.useMaximizeNodeCount;
        //Boolean useINL = LevelConfig.useIdealNonLinearity;
        int[] angles = LevelConfig.desiredAngles;
        //Integer iNL = LevelConfig.idealNonLinearity;
        saveConfig(pathStr,
                folderStr,
                borderS, edgeS,
                minNC, maxNC, minNS, maxNS,
                minED, bFactor, links,
                nip, eip, ncp, edp,
                crxP, mutP, refP,
                xL, yL, zL,
                pop, gen,
                useDA, useAvgSP, useMNC,
                angles);
    }
    
    public void saveConfig(String path,
            String folder,
            Integer borderS, Double edgeS,
            Integer minNC, Integer maxNC, Double minNS, Double maxNS,
            Double minED, Double bFactor, Integer links,
            Integer nip, Integer eip, Integer ncp, Integer edp,
            Double crxP, Double mutP, Double refP,
            Double xL, Double yL, Double zL,
            Integer pop, Integer gen,
            Boolean useDA, Boolean useASP, Boolean useMNC,
            int[] angles)
    {
        PrintWriter pw = null;
        try {
            JSONObject json = new JSONObject();
            json.put("folder", folder.replace("/", File.separator).replace("\\", File.separator));
            json.put("borderSize", borderS);
            json.put("edgeSize", edgeS);
            
            json.put("minNodeCount", minNC);
            json.put("maxNodeCount", maxNC);
            json.put("minNodeSize", minNS);
            json.put("maxNodeSize", maxNS);
            
            json.put("minEdgeDistance", minED);
            
            json.put("barabasiFactor", bFactor);
            json.put("maxLinksPerStep", links);
            
            json.put("areaIntersectionPenalty", nip);
            json.put("edgeIntersectionPenalty", eip);
            json.put("nodeCountPenalty", ncp);
            json.put("edgeDistancePentalty", edp);
            
            json.put("crossoverProb", crxP);
            json.put("mutationProb", mutP);
            json.put("refinementProb", refP);
            
            json.put("nodeXLeap", xL);
            json.put("nodeYLeap", yL);
            json.put("nodeZLeap", zL);
            
            json.put("popSize", pop);
            json.put("maxGen", gen);
            
            json.put("useDesiredAngles", useDA);
            json.put("useAverageShortestPath", useASP);
            //json.put("useIdealNonLinearity", useINL);
            json.put("useMaximizeNodeCount", useMNC);
            
            JSONArray array = new JSONArray();
            for(int i = 0; i < angles.length; i++)
                array.add(angles[i]);
            json.put("desiredAngles", array);
            //json.put("idealNonLinearity", iNL);
            
            // non-user defined
            json.put("scaleFactor", 50);
            json.put("expansionProb", 0.05);
            
            File file = new File(path);
            pw = new PrintWriter(file);
            json.writeJSONString(pw);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LevelConfigManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(LevelConfigManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            pw.close();
        }
    }
    
    public void loadConfig(String folder, String filename){
        try {
            LevelFileReader reader = new LevelFileReader(folder, filename);
            String raw = reader.readRawString();
            
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(raw);
            
            LevelConfig.folder = ((String) json.get("folder")).replace("/", File.separator).replace("\\", File.separator);
            GeneralConfig.borderSize = (int)(long) json.get("borderSize");
            GeneralConfig.edgeSize = (double) json.get("edgeSize");
            LevelConfig.minNodeCount = (int) (long) json.get("minNodeCount");
            LevelConfig.maxNodeCount = (int) (long) json.get("maxNodeCount");
            LevelConfig.minNodeSize = (double) json.get("minNodeSize");
            LevelConfig.maxNodeSize = (double) json.get("maxNodeSize");
            LevelConfig.minEdgeDistance = (double) json.get("minEdgeDistance");
            LevelConfig.barabasiFactor = (double) json.get("barabasiFactor");
            LevelConfig.maxLinksPerStep = (int) (long) json.get("maxLinksPerStep");
            LevelConfig.areaIntersectionPenalty = (int)(long) json.get("areaIntersectionPenalty");
            LevelConfig.edgeIntersectionPenalty = (int)(long) json.get("edgeIntersectionPenalty");
            LevelConfig.nodeCountPenalty = (int)(long) json.get("nodeCountPenalty");
            LevelConfig.edgeDistancePentalty = (int)(long) json.get("edgeDistancePentalty");
            LevelConfig.crossoverProb = (double) json.get("crossoverProb");
            LevelConfig.mutationProb = (double) json.get("mutationProb");
            LevelConfig.refinementProb = (double) json.get("refinementProb");
            LevelConfig.nodeXLeap = (double) json.get("nodeXLeap");
            LevelConfig.nodeYLeap = (double) json.get("nodeYLeap");
            LevelConfig.nodeZLeap = (double) json.get("nodeZLeap");
            LevelConfig.popSize = (int)(long) json.get("popSize");
            LevelConfig.maxGen = (int)(long) json.get("maxGen");
            LevelConfig.useDesiredAngles = (boolean) json.get("useDesiredAngles");
            LevelConfig.useAverageShortestPath = (boolean) json.get("useAverageShortestPath");
            //LevelConfig.useIdealNonLinearity = (boolean) json.get("useIdealNonLinearity");
            LevelConfig.useMaximizeNodeCount = (boolean) json.get("useMaximizeNodeCount");
            
            JSONArray array = (JSONArray) json.get("desiredAngles");
            int[] angles = new int[array.size()];
            for(int i = 0; i < angles.length; i++){
                angles[i] = (int)(long) array.get(i);
            }
            LevelConfig.desiredAngles = angles;
            //LevelConfig.idealNonLinearity = (int)(long) json.get("idealNonLinearity");
        } catch (ParseException ex) {
            Logger.getLogger(LevelConfigManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }   
}
