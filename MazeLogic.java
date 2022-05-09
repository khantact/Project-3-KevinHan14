import java.nio.charset.Charset;
import java.util.*;

//A collection of game logic operations necessary to the Mazes and Manticores game
public abstract class MazeLogic {

    // chars representing the four cardinal directions
    public static final char NORTH = 'N';
    public static final char EAST = 'E';
    public static final char WEST = 'W';
    public static final char SOUTH = 'S';
    public static final char[] DIRECTIONS = { NORTH, EAST, WEST, SOUTH };

    // chars representing the different possible occupants in a Maze Room
    public static final char MANTICORE = 'M';
    public static final char CLOAK = 'C';
    public static final char TREASURE = 'T';
    public static final char LADDER = 'L';
    public static final char NO_OCCUPANT = 'X';
    public static final char[] OCCUPANTS = { MANTICORE, CLOAK, TREASURE, LADDER };

    // Custom
    public static final int MAX_ATTRIBUTES = 5;

    // *************************************************************************************
    // * *
    // * DO NOT MODIFY ANY OF THE VARIABLES ABOVE THIS LINE *
    // * You may add final variables, but cannot add any new class/instance
    // variables! *
    // * *
    // *************************************************************************************

    // Given a 2D array of Strings representing the maze data read from an
    // appropriately
    // formatted .txt file (see lab writeup for details), constructs a graph of Room
    // objects
    // representing the maze.
    //
    // Returns the Room object that is the maze entrace/exit (ie, contains the
    // ladder).
    public static Room buildMazeGraph(String[][] mazeData) {
        Room[][] mazeHelper = new Room[mazeData.length][mazeData[0].length];
        char[] mazeComponents = new char[MAX_ATTRIBUTES];

        // to help you construct the graph, let's make a local 2D array of Rooms,
        // the same dimensions as our maze (per mazeData)
        for (int row = 0; row < mazeHelper.length; row++) {
            for (int col = 0; col < mazeHelper[row].length; col++) {
                mazeHelper[row][col] = new Room();
                if (mazeData[row][col] != null && mazeData[row][col].indexOf(LADDER) != -1) {
                    mazeHelper[row][col].setOccupant(LADDER);
                } else if (mazeData[row][col] != null && mazeData[row][col].indexOf(MANTICORE) != -1) {
                    mazeHelper[row][col].setOccupant(MANTICORE);
                } else if (mazeData[row][col] != null && mazeData[row][col].indexOf(CLOAK) != -1) {
                    mazeHelper[row][col].setOccupant(CLOAK);
                } else if (mazeData[row][col] != null && mazeData[row][col].indexOf(TREASURE) != -1) {
                    mazeHelper[row][col].setOccupant(TREASURE);
                }
            }
        }
        for (int row = 0; row < mazeHelper.length; row++) {
            for (int col = 0; col < mazeHelper[row].length; col++) {
                if (mazeData[row][col] != null) {
                    mazeComponents = mazeData[row][col].toCharArray();
                } else {
                    continue;
                }
                for (char attributes : mazeComponents) {
                    if (indexOfChar(DIRECTIONS, attributes) != -1) {
                        if (attributes == NORTH) {
                            mazeHelper[row][col].addDoor(attributes, mazeHelper[row - 1][col]);
                        } else if (attributes == SOUTH) {
                            mazeHelper[row][col].addDoor(attributes, mazeHelper[row + 1][col]);
                        } else if (attributes == WEST) {
                            mazeHelper[row][col].addDoor(attributes, mazeHelper[row][col - 1]);
                        } else if (attributes == EAST) {
                            mazeHelper[row][col].addDoor(attributes, mazeHelper[row][col + 1]);
                        }
                    }
                }

            }
        }
        for (int row = 0; row < mazeHelper.length; row++) {
            for (int col = 0; col < mazeHelper[row].length; col++) {
                if (mazeHelper[row][col].getOccupant() == LADDER) {
                    return mazeHelper[row][col];
                }

            }
        }
        // print2DArr(mazeData);
        print2DArr(mazeHelper);
        // what next?
        // you need to set each room's attributes approriately
        // (what doors do each room have? what occupants?)

        // it may be useful to print out mazeData to get a sense of what its contents
        // look like
        // (hint, there might be a helper function below that is useful here!)

        return null; // placeholder... return Room containing entrance to maze (ie the ladder)
    }

    // Finds the distance to the nearest manticore from the target room.
    // Distance returned is "number of rooms away" -- ie if nearest manticore is 2
    // rooms away,
    // this function returns 2.
    // If there is no manticore within maxDist (inclusive) rooms away, returns -1.
    // Returns 0 if manticore is in target room.
    public static int distanceToNearestManticore(Room target, int maxDist) {
        // Let's use a Queue to help in this search
        // Think of the Queue as your "to-check" list -- it stores the rooms
        // you need to look for a manticore in order of distance away from target
        Queue<Room> toCheck = new LinkedList<Room>();
        // remember, Queue is an INTERFACE -- check the API to find implementing
        // classes!

        // Since the room closest to target is itself, we will we start with target in
        // the Queue
        toCheck.add(target);

        // We need to look at all the rooms that are 0 to maxDist moves away from target
        // The queue will help us!
        for (int dist = 0; dist <= maxDist; dist++) {
            int size = toCheck.size();
            for (int i = 0; i < size; i++) {
                Room currentRoom = toCheck.remove();
                if (currentRoom.getOccupant() == MANTICORE) {
                    return dist;
                }
                for (Room r : currentRoom.getAllConnectedRooms()) {
                    toCheck.add(r);
                }
            }

            // First we want to check all rooms 0 moves away from target for a manticore
            // Then all rooms 1 move away... and so on
            // You finish the rest!
        }

        return -1; // placeholder
    }

    // Finds the shortest path between the player and their next objective, using a
    // BREADTH-FIRST SEARCH!
    // The path is returned as a String of directions, for example if "WSEENESSW"
    // was returned,
    // that means the fastest path from start to target is to traverse West, then
    // South, and so on...
    // The booleans hasCloak and hasTreasure indicate whether the player has the
    // invisibility
    // cloak and treasure, respectively, in their possession.
    // Returns null if there is no possible path to the target.
    public static String findShortestPathToObjective(Room player, boolean hasCloak, boolean hasTreasure) {
        Queue<Room> toCheck = new LinkedList<Room>();
        HashMap<Room, Character> history = new HashMap<Room, Character>();

        String path = "";
        String path_Final = "";

        toCheck.add(player);
        history.put(player, null);
        if (!hasCloak) {
            while (!toCheck.isEmpty()) {
                Room currentRoom = toCheck.poll();
                for (Character c : currentRoom.getAllDoorDirections()) {
                    if (!history.containsKey(currentRoom.go(c)) && currentRoom.go(c).getOccupant() != MANTICORE) {
                        toCheck.add(currentRoom.go(c));
                        history.put(currentRoom.go(c), c);
                    }
                    if (currentRoom.getOccupant() == CLOAK) {
                        while (history.get(currentRoom) != null) {
                            path += getOppositeDir(history.get(currentRoom));
                            currentRoom = currentRoom.go(getOppositeDir(history.get(currentRoom)));
                        }
                        break;
                    }
                }
            }

            for (int i = path.length() - 1; i >= 0; i--)
                path_Final += getOppositeDir(path.charAt(i));

        } else if (hasCloak && !hasTreasure) {
            while (!toCheck.isEmpty()) {
                Room currentRoom = toCheck.poll();
                for (Character c : currentRoom.getAllDoorDirections()) {
                    if (!history.containsKey(currentRoom.go(c))) {
                        toCheck.add(currentRoom.go(c));
                        history.put(currentRoom.go(c), c);
                    }
                    if (currentRoom.getOccupant() == TREASURE) {
                        while (history.get(currentRoom) != null) {
                            path += getOppositeDir(history.get(currentRoom));
                            currentRoom = currentRoom.go(getOppositeDir(history.get(currentRoom)));
                        }
                        break;
                    }
                }
            }
            for (int i = path.length() - 1; i >= 0; i--)
                path_Final += getOppositeDir(path.charAt(i));

        } else if (hasCloak && hasTreasure) {
            while (!toCheck.isEmpty()) {
                Room currentRoom = toCheck.poll();
                for (Character c : currentRoom.getAllDoorDirections()) {
                    if (!history.containsKey(currentRoom.go(c))) {
                        toCheck.add(currentRoom.go(c));
                        history.put(currentRoom.go(c), c);
                    }
                    if (currentRoom.getOccupant() == LADDER) {
                        while (history.get(currentRoom) != null) {
                            path += getOppositeDir(history.get(currentRoom));
                            currentRoom = currentRoom.go(getOppositeDir(history.get(currentRoom)));
                        }
                        break;
                    }
                }
            }
            for (int i = path.length() - 1; i >= 0; i--)
                path_Final += getOppositeDir(path.charAt(i));
        }

        return path_Final;
    }

    // ***** HELPER FUNCTIONS *****
    // May be useful in your implementation!

    // Given a cardinal direction as a char, returns the opposite direction.
    // Example 'N' for north would return 'S' for south, 'W' returns 'E', etc.
    public static Character getOppositeDir(char dir) {
        if (dir == NORTH)
            return SOUTH;
        else if (dir == SOUTH)
            return NORTH;
        else if (dir == EAST)
            return WEST;
        else if (dir == WEST)
            return EAST;
        throw new IllegalArgumentException("ERROR! Char argument: '" + dir + "' is not a valid direction!");
    }

    // Prints a 2D array in a nice, neat, readable format -- useful for debugging!
    private static void print2DArr(Object[][] arr) {
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[i].length; j++) {
                // System.out.print(arr[i][j]);
                System.out.print(((Room) arr[i][j]).getAllDoorDirections());
                System.out.print(((Room) arr[i][j]).getOccupant());
                if (j != arr[i].length - 1)
                    System.out.print(", ");
            }
            System.out.println();
        }
    }

    // returns the index that a specified char appears in an array of chars
    // or returns -1 if the target char is not in the array
    public static int indexOfChar(char[] arr, char ch) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == ch)
                return i;
        }
        return -1;
    }

}