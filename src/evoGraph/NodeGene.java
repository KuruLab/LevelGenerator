/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evoGraph;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author andre
 */
public class NodeGene {
    
    int[] xyz;
    double width;
    double height;
    
    ArrayList<NodeGene> connectedNodes;

    public NodeGene() {
        connectedNodes = new ArrayList<>();
        width = 0;
        height = 0;
        xyz = new int[3];
    }

    public NodeGene(int[] xyz) {
        this();
        this.xyz = xyz;
    }

    public int[] getXYZ() {
        return xyz;
    }

    public void setXYZ(int[] xyz) {
        this.xyz = xyz;
    }
    
    public ArrayList<NodeGene> getConnectedNodes() {
        return connectedNodes;
    }

    public void setConnectedNodes(ArrayList<NodeGene> connectedNodes) {
        this.connectedNodes = connectedNodes;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }
    
    @Override
    public NodeGene clone(){
        int[] xyzClone = new int[3];
        for(int i = 0; i < xyzClone.length; i++){
            xyzClone[i] = this.xyz[i];
        }
        double widthClone = this.width;
        double heightClone = this.height;
        
        NodeGene clone = new NodeGene();
        clone.setXYZ(xyzClone);
        clone.setWidth(widthClone);
        clone.setHeight(heightClone);
        for(int i = 0; i < connectedNodes.size(); i++)
            if(!clone.getConnectedNodes().contains(connectedNodes.get(i)))
                clone.getConnectedNodes().add(connectedNodes.get(i));
            
        return clone;
    }
    
    @Override
    public String toString(){
        String str = "{\n"+
                     "xyz: "+Arrays.toString(xyz)+"\n"+
                     "width: "+width+"\n"+
                     "height: "+height+"\n"+
                     "}\n";     
        return str;       
    }
    
    public String toHashString(){
        String str = Arrays.toString(xyz);
        return str;
               
    }
}
