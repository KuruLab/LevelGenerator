/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oldmapgenerator;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author andre
 */
public class Room implements Comparable<Room> {

    public List<Coord> tiles;
    public List<Coord> edgeTiles;
    public List<Room> connectedRooms;
    public int roomSize;

    public boolean isAccessibleFromMainRoom;
    public boolean isMAinRoom;

    public Room() {
    }

    public Room(List<Coord> roomTiles, int[][] map) {
        tiles = roomTiles;
        roomSize = tiles.size();
        connectedRooms = new ArrayList<Room>();

        edgeTiles = new ArrayList<Coord>();
        for (Coord tile : tiles) {
            for (int x = tile.tileX - 1; x <= tile.tileX + 1; x++) {
                for (int y = tile.tileY - 1; y <= tile.tileY + 1; y++) {
                    if (x == tile.tileX || y == tile.tileY) {
                        if (map[x][y] == 1) {
                            edgeTiles.add(tile);
                        }
                    }
                }
            }
        }
    }

    public void setAccessibleFromMainRoom() {
        if (!isAccessibleFromMainRoom) {
            isAccessibleFromMainRoom = true;
            for (Room connectedRoom : connectedRooms) {
                connectedRoom.setAccessibleFromMainRoom();
            }
        }
    }

    public static void connectRooms(Room roomA, Room roomB) {
        if (roomA.isAccessibleFromMainRoom) {
            roomB.setAccessibleFromMainRoom();
        } else if (roomB.isAccessibleFromMainRoom) {
            roomA.setAccessibleFromMainRoom();
        }

        roomA.connectedRooms.add(roomB);
        roomB.connectedRooms.add(roomA);
    }

    public boolean isConnected(Room otherRoom) {
        return connectedRooms.contains(otherRoom);
    }

    @Override
    public int compareTo(Room other) {
        Integer otherRoom = other.roomSize;
        return otherRoom.compareTo(roomSize);
    }

}
