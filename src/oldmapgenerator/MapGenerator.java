/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oldmapgenerator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import org.json.simple.JSONObject;
import java.io.FileWriter;
import java.io.IOException;
/**
 *
 * @author andre
 */
public class MapGenerator {

    private int[][] map;
    
    private String seed;
    
    private int width, height;
    private int border;
    private int smoothRounds;
    private int wallThresholdSize;
    private int roomThresholdSize;
    private int randomFillPercent;
    
    private boolean useRandomSeed;
    
    public MapGenerator(){
        width = 128;
        height = 64;
        border = 5;
        smoothRounds = 5;
        wallThresholdSize = 25;
        roomThresholdSize = 75;
        map = new int[width][height];
        seed = "Kurumin";
        
        useRandomSeed = false;
        randomFillPercent = 45;
    }
    
    public MapGenerator(int _width, int _height){
        this();
        width = _width;
        height = _height;
        map = new int[width][height];
    }
    
    public void generate(){
        randomFillMap();
        
        for (int i = 0; i < smoothRounds; i++)
        {
            smoothMap();
        }
   
        processMap();
        proccessBoard();
    }
    
    public void writeFile(String basePath){
        JSONObject obj = new JSONObject();
        String rawMap = new String();
        
        obj.put("method", "CellularAutomata");
        obj.put("type", "cave");
        obj.put("seed", seed);
        obj.put("size", width+"x"+height);
        obj.put("border", border);
        obj.put("smooth", smoothRounds);
        obj.put("room_threshold", roomThresholdSize);
        obj.put("wall_threshold", wallThresholdSize);
        obj.put("fill_percent", randomFillPercent);
        obj.put("random_seed", useRandomSeed);

        try (FileWriter dataFile = new FileWriter(basePath+"data_"+seed+".json"))
        {
            dataFile.write(obj.toJSONString());
            dataFile.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (FileWriter mapFile = new FileWriter(basePath + "map_" + seed + ".txt")) {
            for (int y = 0; y < map[0].length; y++) {
                for (int x = 0; x < map.length; x++) {
                    rawMap += map[x][y];
                }
                rawMap += "\n";
            }
            mapFile.write(rawMap);
            mapFile.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(obj);
        System.out.println(rawMap);
    }
    
    private void randomFillMap(){
        if (useRandomSeed)
        {
            //Random random = new Random(System.currentTimeMillis());
            seed = String.format("%s", System.currentTimeMillis());
        }
        System.out.println("Seed: "+seed);
        Random random = new Random(seed.hashCode());
        
        for(int x = 0; x < width; x++)
        {
            for(int y = 0; y < height; y++)
            {
                if (x == 0 || x == width - 1 || y == 0 || y == height - 1)
                {
                    map[x][y] = 1;
                }
                else
                {
                    map[x][y] = random.nextDouble()*100 < randomFillPercent ? 1 : 0;
                }
                //System.out.print(map[x][y]);
            }
            //System.out.println();
        }
    }
    
    private void smoothMap()
    {
        for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                int neighbourWallTiles = getSurroundingWallCount(x, y);

                if (neighbourWallTiles > 4)
                    map[x][y] = 1;
                else if (neighbourWallTiles < 4)
                    map[x][y] = 0;

            }
        }
    }
    
    private int getSurroundingWallCount(int gridX, int gridY)
    {
        int wallCount = 0;
        for (int neighbourX = gridX - 1; neighbourX <= gridX + 1; neighbourX++)
        {
            for (int neighbourY = gridY - 1; neighbourY <= gridY + 1; neighbourY++)
            {
                if (neighbourX >= 0 && neighbourX < width && neighbourY >= 0 && neighbourY < height)
                {
                    if (neighbourX != gridX || neighbourY != gridY)
                    {
                        wallCount += map[neighbourX][neighbourY];
                    }
                }
                else
                {
                    wallCount++;
                }
            }
        }

        return wallCount;
    }
    
    private void proccessBoard()
    {
        int[][] borderedMap = new int[width + border * 2][height + border * 2];

        for (int x = 0; x < borderedMap.length; x++)
        {
            for (int y = 0; y < borderedMap[0].length; y++)
            {
                if (x >= border && x < width + border && y >= border && y < height + border)
                {
                    borderedMap[x][y] = map[x-border][y-border];
                }
                else
                {
                    borderedMap[x][y] = 1;
                }
            }
        }
        map = borderedMap;
    }
    
    private void processMap()
    {
        List<List<Coord>> wallRegions = getRegions(1);

        for (List<Coord> wallRegion : wallRegions)
        {
            if (wallRegion.size() < wallThresholdSize)
            {
                for (Coord tile : wallRegion)
                {
                    map[tile.tileX][tile.tileY] = 0;
                }
            }
        }

        List<List<Coord>> roomRegions = getRegions(0);
        List<Room> survivingRooms = new ArrayList<>();

        for (List<Coord> roomRegion : roomRegions)
        {
            if (roomRegion.size() < roomThresholdSize)
            {
                for (Coord tile : roomRegion)
                {
                    map[tile.tileX][tile.tileY] = 1;
                }
            }
            else
            {
                survivingRooms.add(new Room(roomRegion, map));
            }
        }
        if(!survivingRooms.isEmpty()){
            Collections.sort(survivingRooms);
            survivingRooms.get(0).isMAinRoom = true;
            survivingRooms.get(0).isAccessibleFromMainRoom = true;
            connectClosestRooms(survivingRooms, false);
        }
    }
    
    private void connectClosestRooms(List<Room> allRooms, boolean forceAccessibilityFromMainRoom)
    {

        List<Room> roomListA = new ArrayList<>();
        List<Room> roomListB = new ArrayList<>();

        if (forceAccessibilityFromMainRoom)
        {
            for(Room room : allRooms)
            {
                if (room.isAccessibleFromMainRoom)
                {
                    roomListB.add(room);
                }
                else
                {
                    roomListA.add(room);
                }
            }
        }
        else
        {
            roomListA = allRooms;
            roomListB = allRooms;
        }

        int bestDistance = 0;
        Coord bestTileA = new Coord();
        Coord bestTileB = new Coord();
        Room bestRoomA = new Room();
        Room bestRoomB = new Room();
        boolean possibleConnectionFound = false;

        for (Room roomA : roomListA)
        {
            if (!forceAccessibilityFromMainRoom)
            {
                possibleConnectionFound = false;
                if(roomA.connectedRooms.size() > 0)
                {
                    continue;
                }
            }

            for (Room roomB : roomListB)
            {
                if (roomA == roomB || roomA.isConnected(roomB))
                {
                    continue;
                }

                for (int tileIndexA = 0; tileIndexA < roomA.edgeTiles.size(); tileIndexA++)
                {
                    for (int tileIndexB = 0; tileIndexB < roomB.edgeTiles.size(); tileIndexB++)
                    {
                        Coord tileA = roomA.edgeTiles.get(tileIndexA);
                        Coord tileB = roomB.edgeTiles.get(tileIndexB);
                        int distanceBetweenRooms = (int)(Math.pow(tileA.tileX - tileB.tileX, 2) + Math.pow(tileA.tileY - tileB.tileY, 2));

                        if (distanceBetweenRooms < bestDistance || !possibleConnectionFound)
                        {
                            bestDistance = distanceBetweenRooms;
                            possibleConnectionFound = true;
                            bestTileA = tileA;
                            bestTileB = tileB;
                            bestRoomA = roomA;
                            bestRoomB = roomB;
                        }
                    }
                }
            }

            if (possibleConnectionFound && !forceAccessibilityFromMainRoom)
            {
                createPassage(bestRoomA, bestRoomB, bestTileA, bestTileB);
            }
        }

        if(possibleConnectionFound && forceAccessibilityFromMainRoom)
        {
            createPassage(bestRoomA, bestRoomB, bestTileA, bestTileB);
            connectClosestRooms(allRooms, true);
        }

        if (!forceAccessibilityFromMainRoom)
        {
            connectClosestRooms(allRooms, true);
        }
    }
     
    private void createPassage(Room roomA, Room roomB, Coord tileA, Coord tileB)
    {
        Room.connectRooms(roomA, roomB);
        //Debug.DrawLine(CoordToWorldPoint(tileA), CoordToWorldPoint(tileB), Color.green, 100);

        List<Coord> line = getLine(tileA, tileB);
        for(Coord c : line)
        {
            drawCircle(c, 2);
        }
    }
    
    private void drawCircle(Coord c, int r)
    {
        for (int x = -r; x <= r; x++)
        {
            for (int y = -r; y <= r; y++)
            {
                if (x * x + y * y <= r * r)
                {
                    int drawX = c.tileX + x;
                    int drawY = c.tileY + y;
                    if (isInMapRange(drawX, drawY))
                    {
                        map[drawX][drawY] = 0;
                    }
                }
            }
        }
    }

    private List<Coord> getLine(Coord from, Coord to)
    {
        List<Coord> line = new ArrayList<Coord>();

        int x = from.tileX;
        int y = from.tileY;

        int dx = to.tileX - from.tileX;
        int dy = to.tileY - from.tileY;

        boolean inverted = false;
        int step = (int) Math.signum(dx);
        int gradientStep = (int) Math.signum(dy);

        int longest = Math.abs(dx);
        int shortest = Math.abs(dy);

        if (longest < shortest)
        {
            inverted = true;
            longest = Math.abs(dy);
            shortest = Math.abs(dx);

            step = (int) Math.signum(dy);
            gradientStep = (int) Math.signum(dx);
        }

        int gradientAccumulation = longest / 2;
        for (int i = 0; i < longest; i++)
        {
            line.add(new Coord(x, y));

            if (inverted)
            {
                y += step;
            }
            else
            {
                x += step;
            }

            gradientAccumulation += shortest;
            if (gradientAccumulation >= longest)
            {
                if (inverted)
                {
                    x += gradientStep;
                }
                else
                {
                    y += gradientStep;
                }
                gradientAccumulation -= longest;
            }
        }

        return line;
    }
    
    private List<List<Coord>> getRegions(int tileType)
    {
        List<List<Coord>> regions = new ArrayList<List<Coord>>();
        int[][] mapFlags = new int[width][height];

        for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                if (mapFlags[x][y] == 0 && map[x][y] == tileType)
                {
                    List<Coord> newRegion = getRegionTiles(x, y);
                    regions.add(newRegion);

                    for (Coord tile : newRegion)
                    {
                        mapFlags[tile.tileX][tile.tileY] = 1;
                    }
                }
            }
        }

        return regions;
    }

    private List<Coord> getRegionTiles(int startX, int startY)
    {
        List<Coord> tiles = new ArrayList<Coord>();
        int[][] mapFlags = new int[width][height];
        int tileType = map[startX][startY];

        Queue<Coord> queue = new ArrayDeque<>();
        queue.add(new Coord(startX, startY));
        mapFlags[startX][startY] = 1;

        while (queue.size() > 0)
        {
            Coord tile = queue.poll();
            tiles.add(tile);

            for (int x = tile.tileX - 1; x <= tile.tileX + 1; x++)
            {
                for (int y = tile.tileY - 1; y <= tile.tileY + 1; y++)
                {
                    if (isInMapRange(x, y) && (y == tile.tileY || x == tile.tileX))
                    {
                        if (mapFlags[x][y] == 0 && map[x][y] == tileType)
                        {
                            mapFlags[x][y] = 1;
                            queue.add(new Coord(x, y));
                        }
                    }
                }
            }
        }

        return tiles;
    }

    private boolean isInMapRange(int x, int y)
    {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public int[][] getMap() {
        return map;
    }

    public void setMap(int[][] map) {
        this.map = map;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getBorder() {
        return border;
    }

    public void setBorder(int border) {
        this.border = border;
    }

    public int getSmoothRounds() {
        return smoothRounds;
    }

    public void setSmoothRounds(int smoothRounds) {
        this.smoothRounds = smoothRounds;
    }

    public int getWallThresholdSize() {
        return wallThresholdSize;
    }

    public void setWallThresholdSize(int wallThresholdSize) {
        this.wallThresholdSize = wallThresholdSize;
    }

    public int getRoomThresholdSize() {
        return roomThresholdSize;
    }

    public void setRoomThresholdSize(int roomThresholdSize) {
        this.roomThresholdSize = roomThresholdSize;
    }

    public boolean isUseRandomSeed() {
        return useRandomSeed;
    }

    public void setUseRandomSeed(boolean useRandomSeed) {
        this.useRandomSeed = useRandomSeed;
    }

    public int getRandomFillPercent() {
        return randomFillPercent;
    }

    public void setRandomFillPercent(int randomFillPercent) {
        this.randomFillPercent = randomFillPercent;
    }

    public String getSeed() {
        return seed;
    }

    public void setSeed(String seed) {
        this.seed = seed;
    }   
}
