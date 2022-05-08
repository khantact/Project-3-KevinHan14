import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.Timer;
import java.io.*;



//************************************************************************************
//*                                                                                  *
//*                                                                                  *
//*                    YOU ARE NOT ALLOWED TO MODIFY THIS CLASS                      *
//*           YOU ALSO DON'T NEED TO READ/TRACE/CALL ANY METHODS IN HERE             *
//*         (But feel free to take a look if you're feeling extra curious!)          *
//*                                                                                  *
//*                                                                                  *
//************************************************************************************






//Handles all of the graphics/UI logic for the Mazes and Manticores Game
//also has the code to manage the general control flow of the game
public class MazeWindow extends JComponent implements KeyListener,
    MouseListener, ActionListener{

    //Dimensions of game window
    private static final int FRAME_WIDTH = 1000;
    private static final int FRAME_HEIGHT = 800;

    //Size and spacing of cells in game map
    public static final int CELL_SIZE = 45;
    public static final int CELL_PADDING = 20;
    public static final int HALF_PADDING = CELL_PADDING / 2;

    //Max/min dimensions of maze
    public static final int MAX_CELLS_W = 10;
    public static final int MIN_CELLS_DIM = 2;
    public static final int MAX_CELLS_H = 10;

    //Number of "ticks" in "Cooldown" (ie time before user can perform another action after they do something)
    public static final int COOLDOWN_TICKS = 2;

    //Coordinates for text area
    private static final int TEXT_AREA_X = CELL_PADDING;
    private static final int TEXT_AREA_TOP_Y = MAX_CELLS_H * (CELL_SIZE + CELL_PADDING) + (HALF_PADDING * 5);
    private static final int TEXT_AREA_BOT_Y = (MAX_CELLS_H) * (CELL_SIZE + CELL_PADDING) + (HALF_PADDING * 8);

    //Max number of characters that can appear in a dialogue prompt in the text area
    private static final int MAX_DIALOGUE_LEN = 53;

    //Coordinate and spacing data for arrow buttons
    private static final int ARROWS_X = (MAX_CELLS_W + 1) * (CELL_SIZE + CELL_PADDING);
    private static final int ARROWS_Y = (MAX_CELLS_H - 6) * (CELL_SIZE + 2 * CELL_PADDING);

    //Filenames for various buttons/UI images
    private static final String[] ARROW_SRC = {"upArrow.png","leftArrow.png","downArrow.png","rightArrow.png"};
    private static final String[] ARROW_DIS_SRC = {"upArrowDisabled.png","leftArrowDisabled.png","downArrowDisabled.png","rightArrowDisabled.png"};
    private static final String LEGEND_SRC = "legend.png";
    private static final String CLOAK_SRC = "cloak.png";
    private static final String TREASURE_SRC = "treasure.png";
    private static final String PATH_BUTTON_SRC = "pathButton.png";
    private static final String PATH_BUTTON_DISABLED_SRC = "pathButtonDisabled.png";

    //Path to folder containing source images
    private static final String SRC_LOC = "./img_src/";
    private static BufferedImage[] arrowImg = new BufferedImage[ARROW_SRC.length];
    private static BufferedImage[] arrowDisImg = new BufferedImage[ARROW_DIS_SRC.length];
    //GUICell objects repurposed to be invisible hitboxes for the arrow keys/show path button
    private GUICell[] arrow_hitboxes = new GUICell[4];
    private GUICell pathHitbox;

    //Descriptions for different cardinal directions (used in dialogue box)
    private static final String DIR_DESC[] = {"NORTH", "WEST", "SOUTH", "EAST"};

    //Direction related constants -- tracks valid directions as well as their respective row/col offsets
    private static final char DIR_IDX[] = {MazeLogic.NORTH, MazeLogic.WEST, MazeLogic.SOUTH, MazeLogic.EAST};
    private static final int[][] DIR_OFFSET = {{-1,0}, {0,-1}, {1,0}, {0,1}};
    private static final int ROW_IDX = 0;
    private static final int COL_IDX = 1;

    //Indices of the different directional keys in the associated image array
    private static final int UP_IDX = 0;
    private static final int LEFT_IDX = 1;
    private static final int DOWN_IDX = 2;
    private static final int RIGHT_IDX = 3;

    //Dialogue texts for different user states and actions
    private final String TXT_ROOM_START = "You descend into the maze... it is dark and spooky!";
    private final String TXT_ROOM_MOVE = "You venture ";
    private final String TXT_ROOM_WALL = "...and are met by a wall (*THUD*)!";
    private final String TXT_ROOM_ENTRANCE = "...and spot the exit! You run out of the maze to safety!";
    private final String TXT_ROOM_ENTRANCE_NO_TREASURE = "...you spot the exit, but can't leave without the treasure!";
    private final String TXT_ROOM_CLOAK = "...and spot an invisibility cloak!  You take it!";
    private final String TXT_ROOM_EMPTY = "...the room lies empty!";
    private final String TXT_ROOM_TREASURE = "...and see treasure! The cloak lets you take it undetected!";
    private final String TXT_ROOM_TREASURE_NO_CLOAK = "...and see treasure! You wisely keep your distance.";
    private final String TXT_ROOM_MANTICORE = "...and walk right into a manticore!";
    private final String TXT_MANTICORE_HERE = "Thanks to your cloak, the manticore takes no notice of you! ";
    private final String TXT_MANTICORE_HERE_NO_CLOAK = "The manticore has spotted you... uh-oh!  Game Over!";
    private final String TXT_MANTICORE_ESCAPE = "You escaped with the treasure -- congratulations, you won in only ";
    private final String TXT_MANTICORE_CLOSE= "The walls shake with a load roar -- a manticore is very close!!";
    private final String TXT_MANTICORE_MED= "You hear a grumble -- a manticore is nearby!";
    private final String TXT_MANTICORE_FAR= "There is a faint shuffling in the distance -- a manticore is out there!";
    private final String TXT_MANTICORE_NONE = "The room is completely silent, you hear nothing!";

    private static final String TXT_DEBUG_ENABLED = "Debug Mode: Enabled";
    private static final String TXT_NO_PATH = "No viable path exists!";
    //Array of dialogues corresponding to various manticore distances (ie index 1 is 1 cell away, 2 is 2 cells away, etc)
    private final String[] TXT_MANTICORE_DIST = {"", TXT_MANTICORE_CLOSE, TXT_MANTICORE_MED, TXT_MANTICORE_FAR};
    //Maximum distance away from current player cell that game checks for and reacts to a Manticore
    private final int MAX_MANTICORE_DIST = 3;

    //Characters delineating rows/whitespace in the maze .txt files
    private static final char FILE_SEP = ',';
    private static final char SPACE = ' ';

    //Collection of all valid characters for use in validating a maze .txt file
    private static final char[] VALID_CHARS = {MazeLogic.NORTH, MazeLogic.EAST, MazeLogic.WEST, MazeLogic.SOUTH,
        MazeLogic.MANTICORE, MazeLogic.CLOAK, MazeLogic.TREASURE, SPACE, MazeLogic.LADDER};

    //Colors for the various cells in the map window as well as the highlight color for shortest paths.
    public static final Color MANTICORE_COLOR = Color.RED;
    public static final Color CLOAK_COLOR = Color.BLUE;
    public static final Color LADDER_COLOR = new Color(228, 102, 29);
    public static final Color TREASURE_COLOR = Color.YELLOW;
    public static final Color PLAYER_COLOR = Color.GREEN;
    public static final Color PATH_COLOR = Color.PINK;

    //Buffered images to store various UI image files to be rendered to window
    private BufferedImage legendImg, cloakImg, treasureImg, pathButtonImg, pathButtonDisabledImg;


    //Color and size attributes for the fonts to draw text in the game window
    private static final Color DIALOGUE_FONT_COLOR = Color.BLACK;
    private static final Color GAMEOVER_FONT_COLOR = Color.WHITE;
    private static final Color TEXT_BOX_WON_COLOR = new Color(0, 204, 0);
    private static final Color TEXT_BOX_LOST_COLOR = Color.RED;
    private static final Color DEBUG_FONT_COLOR = Color.RED;
    private static final int DIALOGUE_FONT_SIZE = 20;
    private static final int DEBUG_FONT_SIZE = 16;
    private static final Font FONT_DIALOGUE = new Font("Courier New", Font.BOLD, DIALOGUE_FONT_SIZE);
    private static final Font FONT_DEBUG = new Font("Courier New", Font.BOLD, DEBUG_FONT_SIZE);

    //Parameters for box to be drawn around the text area
    private static final RectangularShape TEXT_BOX = new Rectangle2D.Double(TEXT_AREA_X - HALF_PADDING, TEXT_AREA_TOP_Y - 3*HALF_PADDING, FRAME_WIDTH - TEXT_AREA_X - CELL_PADDING, FRAME_HEIGHT - TEXT_AREA_TOP_Y - (CELL_PADDING));
    private static final int TEXT_BOX_BORDER_WIDTH = 3;

    //The various game states (gameover lost/won, game in progress)
    private final static int STATE_LOST = -1;
    private final static int STATE_WON = 1;
    private final static int STATE_PLAYING = 0;

    //Frequency that the window "repaints" itself, in milliseconds
    private static final int REPAINT_INTERVAL = 300;

    //When an arrow button is clicked, there is a brief "cooldown" period before they can be pressed again
    //this variable tracks the number of "repaints" before the cooldown ends (or 0 if not in cooldown).
    private int cooldown = 0;

    //A 2D array of GUICell objects, storing the data to be drawn to the game screen in the
    //map window.  Also tracks their associated doors.  Is populated based off of the graph of Room
    //objects generated by the respective function in MazeLogic
    private GUICell[][] cellsGUI;


    //Strings to store the dialogue text to be drawn to the game window
    private String dialogueLine1, dialogueLine2, pathTxt;

    //tracks the position of the player
    private int playerRow = 0;
    private int playerCol = 0;

    //tracks if the player currently has the cloak/treasure in their possession
    private boolean hasCloak = false;
    private boolean hasTreasure = false;

    //tracks the current state of the game (if the game is in progress or over)
    private int gameState = STATE_PLAYING;

    //tracks the row/col of the treasure, cloak, and ladder once the Maze graph is constructed
    private int treasureRow, treasureCol;
    private int cloakRow, cloakCol;
    private int ladderRow, ladderCol;    

    //tracks number of steps the player has taken
    private int stepsCounter = 0;
    
    //tracks the last returned "shortest path" when toggled by the player
    private String lastPath;



    //Expects the name of the maze file to load in for the game
    public MazeWindow(String mazeFile) {
        super();

        //Load in the images and hitboxes for arrow controls
        generateUIAssets();
        //Create a 2D array of GUICell objects for the map window graphics
        generateMazeGUIMatrix(mazeFile);
        //initialize the text areas and their respective graphics
        initDialogueText();
    }



    //Loads in the source image assets for various UI elements (arrow buttons, key, etc)
    //Also initializes invisible hitboxes (which repurpose GUICell objects) to react to button presses
    private void generateUIAssets(){
        try{
            //enabled and disabled arrow buttons and hitboxes
            arrowImg[0] = ImageIO.read(new File(SRC_LOC + ARROW_SRC[0]));
            arrowDisImg[0] = ImageIO.read(new File(SRC_LOC + ARROW_DIS_SRC[0]));
            int arrow_h = arrowImg[0].getHeight();
            int arrow_w = arrowImg[0].getWidth();
            arrow_hitboxes[0] = new GUICell(ARROWS_X + arrow_w, ARROWS_Y, arrow_w);
            for (int i = 1; i < arrowImg.length; i++){
                arrowImg[i] = ImageIO.read(new File(SRC_LOC + ARROW_SRC[i]));
                arrowDisImg[i] = ImageIO.read(new File(SRC_LOC + ARROW_DIS_SRC[i]));
                arrow_hitboxes[i] = new GUICell(ARROWS_X + arrow_w * (i-1), ARROWS_Y + arrow_h, arrow_w);
            }
            //"Key" showing description of differnet colors
            legendImg = ImageIO.read(new File(SRC_LOC + LEGEND_SRC));
            //enabled and disabled "toggle shortest path" button and hitbox
            pathButtonImg = ImageIO.read(new File(SRC_LOC + PATH_BUTTON_SRC));
            pathButtonDisabledImg = ImageIO.read(new File(SRC_LOC + PATH_BUTTON_DISABLED_SRC));
            pathHitbox = new GUICell(ARROWS_X + (2 * CELL_PADDING), ARROWS_Y + (2 * arrow_h) + CELL_PADDING, pathButtonImg.getWidth(), pathButtonImg.getHeight());
            //images for cloak and treasure inventory icons
            cloakImg = ImageIO.read(new File(SRC_LOC + CLOAK_SRC));
            treasureImg = ImageIO.read(new File(SRC_LOC + TREASURE_SRC));
        }
        catch(IOException e){
            System.err.println("Unable to read assets from src folder!  Ending game!");
            System.out.println(e);
            System.exit(1);
        }
    }


    //Generates a matrix of GUICell objects which construct the graphical grid
    //for the map window in the game's window
    private void generateMazeGUIMatrix(String mazeFilename){
        //first, parse the data out of the specified maze file and convert to 2D String array
        ArrayList<String[]> mazeData = parseMazeFile(mazeFilename);
        String[][] mazeDataArr = convertToArr(mazeData);
        removeEmptyCells(mazeDataArr); //converts any empty Strings to null cells
        //make sure the maze has a start cell
        if (mazeDataArr[0][0] == null)
            terminateWithError("There must be a room at row: 0, col:0!");

        //Uses Student's function to create the graph of Room objects given the maze file's data
        Room origin = MazeLogic.buildMazeGraph(mazeDataArr);
        if (origin == null)
            terminateWithError("createMazeGraph returned null!");

        //use a exhaustive recursive algorithm to populate the matrix of graphical elements
        //for the game's map window
        cellsGUI = new GUICell[mazeData.size()][mazeData.get(0).length];
        HashSet<Room> visited = new HashSet<Room>();
        makeMapCell(ladderRow, ladderCol, origin, visited);

        //set the player location, and link any doors together
        //(game only draws South and East doors, so link any N/W doors to the adjacent cell's S/E doors)
        cellsGUI[ladderRow][ladderCol].setHasPlayer(true);
        playerRow = ladderRow;
        playerCol = ladderCol;
        linkDoors();
    }


    //Parses a maze .txt file, and also verifies the contents to ensure it meets all the formatting rules
    //Returns an ArrayList of String arrays, where each String[] in the AL represents one row of the maze
    //Each index of the String[] represents one cell in the row
    private ArrayList<String[]> parseMazeFile(String mazeFilename){
        ArrayList<String[]> mazeData = new ArrayList<String[]>();
        try{
            Scanner scan = new Scanner(new File(mazeFilename));
            int prevRowLen = -1;
            int rowNum = 0;
            while (scan.hasNextLine()){
                String[] row = (scan.nextLine().toUpperCase() + SPACE).split(FILE_SEP + "");
                if (row.length != prevRowLen && prevRowLen != -1)//Ensure all rows have same numebr of cols
                    terminateWithError("All rows of maze file must be same length! Mismatch on row #" + rowNum);
                prevRowLen = row.length;
                mazeData.add(row);
            }
            if (prevRowLen < MIN_CELLS_DIM || mazeData.size() < MIN_CELLS_DIM)
                terminateWithError("Maze row dimensions must be a minimum of: " + MIN_CELLS_DIM + " x " + MIN_CELLS_DIM);
        }
        catch(FileNotFoundException fnfe){
            terminateWithError("Unable to open maze file: \"" + mazeFilename +"\"");
        }
        verifyMazeData(mazeData);
        return mazeData;
    }

    //Verifies the contents of a parsed maze .txt file
    //Accepts an ArrayList of String arrays as formatted by parseMazeFile()
    private void verifyMazeData(ArrayList<String[]> mazeData){
        int cols = -1;
        for (int i = 0; i < mazeData.size(); i++){
            String[] row = mazeData.get(i);
            if (row == null)
                continue;
            cols = row.length;
            for (int col = 0; col < cols; col++){
                if (row[col] == null)
                    continue;
                else if (hasInvalidChars(row[col]))//check for invalid chars
                    terminateWithError("Invalid characters found in cell at row: " + i + " col: " + col + ", \"" + row[col] + "\"");
                else if (hasTooManyContents(row[col]))//check if room has multiple occupants
                    terminateWithError("Cell at row: " + i + " col: " + col + " has too many things in it! \"" + row[col] + "\"");
            }
        }
        verifyDoors(mazeData);
        verifyNecessaryOccupants(mazeData);
    }


    //Given an ArrayList of maze data (as formatted by parseMazeFile()), ensure that
    //All doors in rooms have reciprocated door in the appropriate adjacent cell.
    //For example, if the cell at row:3 column:5 has a door to the EAST, this function
    //ensures that the room at row:3 column:6 has a door to the WEST
    private static void verifyDoors(ArrayList<String[]> mazeData){
        checkDoorsOOB(mazeData);
        for (int row = 0; row < mazeData.size(); row++){
            for (int col = 0; col < mazeData.get(row).length; col++){
                String doors = mazeData.get(row)[col];
                if (hasChar(doors, MazeLogic.NORTH) && !hasChar(mazeData.get(row-1)[col], MazeLogic.SOUTH))
                    terminateWithError("Door mismatch! Room at row: " + row + " col: " + col + " has " + MazeLogic.NORTH + " door but corresponding cell has no " + MazeLogic.SOUTH + " door!");
                if (hasChar(doors, MazeLogic.SOUTH) && !hasChar(mazeData.get(row+1)[col], MazeLogic.NORTH))
                    terminateWithError("Door mismatch! Room at row: " + row + " col: " + col + " has " + MazeLogic.SOUTH + " door but corresponding cell has no " + MazeLogic.NORTH + " door!");
                if (hasChar(doors, MazeLogic.EAST) && !hasChar(mazeData.get(row)[col+1], MazeLogic.WEST))
                    terminateWithError("Door mismatch! Room at row: " + row + " col: " + col + " has " + MazeLogic.EAST + " door but corresponding cell has no " + MazeLogic.WEST + " door!");
                if (hasChar(doors, MazeLogic.WEST) && !hasChar(mazeData.get(row)[col-1], MazeLogic.EAST))
                    terminateWithError("Door mismatch! Room at row: " + row + " col: " + col + " has " + MazeLogic.WEST + " door but corresponding cell has no " + MazeLogic.EAST + " door!");
            }
        }
    }


    //Given an ArrayList of maze data (as formatted by parseMazeFile()), ensure that
    //no room has a door that leads out of bounds of the maze (ex, ensure no room in row 0
    //has a door to the NORTH).
    private static void checkDoorsOOB(ArrayList<String[]> mazeData){
        //Checks for doors to rooms at negative col or col > maze width
        for (int row = 0; row < mazeData.size(); row++){
            String[] wholeRow = mazeData.get(row);
            if (hasChar(wholeRow[0], MazeLogic.WEST))
                generateOOBDoorError(row, 0, MazeLogic.WEST);
            if (hasChar(wholeRow[wholeRow.length-1], MazeLogic.EAST))
                generateOOBDoorError(row, 0, MazeLogic.EAST);
        }
        String[] topRow = mazeData.get(0);
        String[] botRow = mazeData.get(mazeData.size()-1);
        //Checks for doors to rooms at negative row or row > maze height
        for (int col = 0; col < topRow.length; col++){
            if (hasChar(topRow[col], MazeLogic.NORTH))
                generateOOBDoorError(0, col, MazeLogic.NORTH);
            if (hasChar(botRow[col], MazeLogic.SOUTH))
                generateOOBDoorError(mazeData.size()-1, col, MazeLogic.SOUTH);
        }
    }

    //Ensures the maze only has exactly one room with a cloak and one room with a treasure
    private void verifyNecessaryOccupants(ArrayList<String[]> mazeData){
        int treasureCount = 0;
        int cloakCount = 0;
        int ladderCount = 0;
        for (int row = 0; row < mazeData.size(); row++){
            for (int col = 0; col < mazeData.get(row).length; col++){
                String cell = mazeData.get(row)[col];
                if (cell == null)
                    continue;
                else if (hasChar(cell, MazeLogic.TREASURE)){
                    treasureCount++;
                    treasureRow = row;
                    treasureCol = col;
                }
                else if (hasChar(cell, MazeLogic.CLOAK)){
                    cloakCount++;
                    cloakRow = row;
                    cloakCol = col;
                }
                else if (hasChar(cell, MazeLogic.LADDER)){
                    ladderCount++;                
                    ladderRow = row;
                    ladderCol = col;
                }
            }
        }
        if (treasureCount > 1)
            terminateWithError("Maze file has multiple cells with treasure (maze can only have one treasure!)");
        else if (treasureCount == 0)
            terminateWithError("Maze file has no treasure!  Maze must have exactly one cell with a treasure!");
        else if (cloakCount > 1)
            terminateWithError("Maze file has multiple cells with cloak (maze can only have one cloak!)");
        else if (cloakCount == 0)
            terminateWithError("Maze file has no cloak!  Maze must have exactly one cell with a cloak!");
        else if (ladderCount > 1)
            terminateWithError("Maze file has multiple cells with ladder (maze can only have one ladder!)");
        else if (ladderCount == 0)
            terminateWithError("Maze file has no ladder!  Maze must have exactly one cell with a ladder!");        
    }

    //Checks to see if a cell has any invalid characters
    //ie char thats not a valid direction or occupant
    private boolean hasInvalidChars(String cellData){
        for (int i = 0; i < cellData.length(); i++){
            if (MazeLogic.indexOfChar(VALID_CHARS, cellData.charAt(i)) < 0)
                return true;
        }
        return false;
    }


    //Checks to see if an individual room of the maze has too many things in it
    //ie too many occupants, such as a cloak and a manticore
    private boolean hasTooManyContents(String cellData){
        boolean hasItem = false;
        for (int i = 0; i < cellData.length(); i++){
            if (MazeLogic.indexOfChar(MazeLogic.OCCUPANTS, cellData.charAt(i)) >= 0){
                if (hasItem)
                    return true;
                hasItem = true;
            }
        }
        return false;
    }

    //eliminate any white spaces from cells
    //if a cell has no doors/occupants, convert it to null in the array
    private void removeEmptyCells(String[][] arr){
        for (int row = 0; row < arr.length; row++){
            for (int col = 0; col < arr[0].length; col++){
                arr[row][col] = arr[row][col].replaceAll(SPACE + "", "");
                if (arr[row][col].length() == 0)
                    arr[row][col] = null;
            }
        }
    }

    //Links the doors of adjacent cells in teh GUICell matrix
    //Links the West/North doors to another GUICell objects' East/South door respectively.
    //Done to avoid double painting overlapping doors
    private void linkDoors(){
        for (int row = 0; row < cellsGUI.length; row++){
            for (int col = cellsGUI[row].length-1; col >= 0; col--){
                if (cellsGUI[row][col] == null)
                    continue;
                if (cellsGUI[row][col].hasDoor(MazeLogic.WEST))
                    cellsGUI[row][col].linkWestDoor(cellsGUI[row][col-1]);
                if (cellsGUI[row][col].hasDoor(MazeLogic.NORTH))
                    cellsGUI[row][col].linkNorthDoor(cellsGUI[row-1][col]);
            }
        }
    }

    //recursive algorithm that traverses and graph and populates the
    //uses a hashset to track which vertices it has already visited
    private void makeMapCell(int row, int col, Room current, HashSet<Room> visited){
        //row/col num deterine x/y coordinates to draw graphic in the map window
        cellsGUI[row][col] = new GUICell(col*(CELL_SIZE+CELL_PADDING) + CELL_PADDING, row*(CELL_SIZE+CELL_PADDING)+CELL_PADDING, CELL_SIZE, current);
        visited.add(current);
     
        //recursively search all the doors to rooms not already searched
        for (Character dir : current.getAllDoorDirections()){
            if (visited.contains(current.go(dir)))
                continue;
            if (dir == MazeLogic.NORTH)
                makeMapCell(row-1,col,current.go(dir),visited);
            else if (dir == MazeLogic.SOUTH)
                makeMapCell(row+1,col,current.go(dir),visited);
            else if (dir == MazeLogic.EAST)
                makeMapCell(row,col+1,current.go(dir),visited);
            else
                makeMapCell(row,col-1,current.go(dir),visited);
        }
        if ((row == ladderRow && col == ladderCol) || GameLauncher.DEBUG_MODE)
            cellsGUI[row][col].setVisibility(true);
    }



    //Set the initial text for the dialogue box
    private void initDialogueText(){
        dialogueLine1 = TXT_ROOM_START;
        int manticoreDist = MazeLogic.distanceToNearestManticore(cellsGUI[playerRow][playerCol].getLinkedRoom(), MAX_MANTICORE_DIST);
        setManticoreText(MazeLogic.NO_OCCUPANT, manticoreDist);
    }



    //Move the player in the argument direction
    private void movePlayer(int dir){
        dialogueLine1 = TXT_ROOM_MOVE + DIR_DESC[dir];
        //Is there a wall there?
        if (!cellsGUI[playerRow][playerCol].hasDoor(DIR_IDX[dir]))
            dialogueLine1 += TXT_ROOM_WALL;
        else {
            boolean isPathOn = (lastPath != null);
            if (isPathOn) //erase any shortest path if one is currently enabled
                togglePath();
            cellsGUI[playerRow][playerCol].setHasPlayer(false);
            if (dir == UP_IDX)
                playerRow--;
            else if (dir == DOWN_IDX)
                playerRow++;
            else if (dir == LEFT_IDX)
                playerCol--;
            else
                playerCol++;
            stepsCounter++;
            setDialogueText();
            cellsGUI[playerRow][playerCol].setHasPlayer(true);
            cellsGUI[playerRow][playerCol].setVisibility(true);
            
            if (isPathOn)
                togglePath();            
        }
    }

    //Update the dialogue text (called when player moves to a new room)
    private void setDialogueText(){
        char occupant = cellsGUI[playerRow][playerCol].getOccupant();
        //find the closest manticore
        int manticoreDist = MazeLogic.distanceToNearestManticore(cellsGUI[playerRow][playerCol].getLinkedRoom(), MAX_MANTICORE_DIST);
        if (GameLauncher.DEBUG_MODE)
            System.out.println("**DEBUG: call to MazeLogic.distanceToNearestManticore(" + MAX_MANTICORE_DIST +") returned: " + manticoreDist);
        setRoomText(occupant, manticoreDist);
        setManticoreText(occupant, manticoreDist);
    }


    //Sets the top line of dialogue, typically describing the contents of the room
    private void setRoomText(char occupant, int manticoreDist){
        //player at maze entrance
        if (playerRow == ladderRow && playerCol == ladderCol){
            if (hasTreasure){
                dialogueLine1 += TXT_ROOM_ENTRANCE;
            }
            else
                dialogueLine1 += TXT_ROOM_ENTRANCE_NO_TREASURE;
        }
        //player in room w/ manticore
        else if(occupant == MazeLogic.MANTICORE)
            dialogueLine1 += TXT_ROOM_MANTICORE;
        //player in room w/ treasure
        else if(occupant == MazeLogic.TREASURE){
            if (hasCloak && !hasTreasure){
                dialogueLine1 += TXT_ROOM_TREASURE;
                hasTreasure = true;
            }
            else if(!hasCloak)
                dialogueLine1 += TXT_ROOM_TREASURE_NO_CLOAK;
            else if(hasTreasure)
                dialogueLine1 += TXT_ROOM_EMPTY;
        }
        //player in room w/ cloak
        else if(occupant == MazeLogic.CLOAK){
            if (!hasCloak){
                dialogueLine1 += TXT_ROOM_CLOAK;
                hasCloak = true;
            }
            else
                dialogueLine1 += TXT_ROOM_EMPTY;
        }
        //player in empty room
        else
            dialogueLine1 += TXT_ROOM_EMPTY;
    }


    //Sets bottom line of dialogue, typically describing any manticores in the room/area
    private void setManticoreText(char occupant, int manticoreDist){
        //if the player has escaped the maze with the treasure, they won!
        if (playerRow == ladderRow && playerCol == ladderCol && hasTreasure){
            dialogueLine2 = TXT_MANTICORE_ESCAPE + stepsCounter + " steps!";
            gameState = STATE_WON;
        }
        //if player is in room w/ manticore
        else if (manticoreDist == 0){
            if  (hasCloak)
                dialogueLine2 = TXT_MANTICORE_HERE;
            else{
                dialogueLine2 = TXT_MANTICORE_HERE_NO_CLOAK;
                gameState = STATE_LOST; //if no cloak, they lose!
            }
        }
        //If there's a manticore anywhere in proximity
        else if (manticoreDist > 0)
            dialogueLine2 = TXT_MANTICORE_DIST[manticoreDist];
        //otherwise, the room is silent!
        else
            dialogueLine2 = TXT_MANTICORE_NONE;
    }


    //Draws or erases the "shortest path" in the game's map window
    private void setPath(String path, boolean enable){
        //if path is null, there is no viable path to the next objective
        //thus, display the error message text under the "toggle path" button
        if (path == null){
            pathTxt = TXT_NO_PATH;
            return;
        }
        int row = playerRow;
        int col = playerCol;
        for (int i = 0; i < path.length(); i++){
            char dir = path.charAt(i);
            cellsGUI[row][col].setDoorPath(dir, enable);
            cellsGUI[row][col].setRoomPath(enable);
            int[] offset = DIR_OFFSET[MazeLogic.indexOfChar(DIR_IDX, dir)];
            row += offset[ROW_IDX];
            col += offset[COL_IDX];
            cellsGUI[row][col].setDoorPath(MazeLogic.getOppositeDir(dir),enable);
        }
        cellsGUI[row][col].setRoomPath(enable);
        if(enable)
            lastPath = path;
        else
            lastPath = null;
    }



    //Validates dialogue text, truncating it if it is too many characters long
    private void validateDialogue(){
        if (dialogueLine1.length() > MAX_DIALOGUE_LEN)
            dialogueLine1 = dialogueLine1.substring(0, MAX_DIALOGUE_LEN);
        if (dialogueLine2.length() > MAX_DIALOGUE_LEN)
            dialogueLine2 = dialogueLine2.substring(0, MAX_DIALOGUE_LEN);
    }


    //Checks to see if any of the buttons were clicked in the game window.
    public void mousePressed(MouseEvent event) {

        int lastKnownMouseX = event.getX();
        int lastKnownMouseY = event.getY();
        //Only check for button presses if the game is not over and not in cooldown
        if (cooldown > 0 || gameState != STATE_PLAYING)
            return;
        //check if the user clicked an arrow button
        for (int i = 0; i < arrow_hitboxes.length; i++){
            if (arrow_hitboxes[i].contains(lastKnownMouseX, lastKnownMouseY)){
                cooldown = COOLDOWN_TICKS;
                movePlayer(i);
                return;
            }
        }
        //check if the user clicked the "toggle path" button
        if (pathHitbox.contains(lastKnownMouseX, lastKnownMouseY)){
            togglePath();

        }
    }

    //toggle the path to next objective
    private void togglePath(){
        //If no path is being drawn go find the shortest path
        if (lastPath == null){
            Room start = getRoom(playerRow, playerCol);
            Room end;
            //determining the player's next target
            if (hasTreasure)
                end = getRoom(ladderRow,ladderCol);
            else if (hasCloak)
                end = getRoom(treasureRow, treasureCol);
            else
                end = getRoom(cloakRow, cloakCol);
            String path = MazeLogic.findShortestPathToObjective(start, hasCloak, hasTreasure);
            if (GameLauncher.DEBUG_MODE)
                System.out.println("**DEBUG: call to MazeLogic.findShortestPathToObjective returned: \"" + path + "\"");
            setPath(path, true);
        }
        //if a path is already being drawn, erase it
        else
            setPath(lastPath, false);        
        
    }

    //Reacts to user pressing a key on the keyboard
    public void keyPressed(KeyEvent e) {

        int keyCode = e.getKeyCode();
        //'Esc' quits the program
        if (e.getKeyCode()==KeyEvent.VK_ESCAPE)
            System.exit(1);
        //Only check for other button presses if the game is not over and not in cooldown        
        if (cooldown > 0 || gameState != STATE_PLAYING)
            return;        
        else if (e.getKeyCode()==KeyEvent.VK_P)
            togglePath();
        else if (e.getKeyCode()==KeyEvent.VK_UP)
            movePlayer(UP_IDX);
        else if (e.getKeyCode()==KeyEvent.VK_DOWN)
            movePlayer(DOWN_IDX);
        else if (e.getKeyCode()==KeyEvent.VK_LEFT)
            movePlayer(LEFT_IDX);
        else if (e.getKeyCode()==KeyEvent.VK_RIGHT)
            movePlayer(RIGHT_IDX);        
        
    }


    //Initializes and displays our window (represented by a JFrame object)
    public void launchWindow() {

        JFrame window = new JFrame("Mazes and Manticores!");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        window.add(this);

        this.setBackground(Color.WHITE);
        this.setOpaque(true);
        window.setVisible(true);
        window.setResizable(false);
        //Fix Windows listener bug(?)
        this.setFocusable(true);
        //window.requestFocus();
        this.requestFocusInWindow();

        //Lets the window know that the methods to react to keyboard/mouse actions
        //are implemented in this class
        addKeyListener(this);
        addMouseListener(this);


        //Initialize and start the timer
        Timer timer = new Timer(0, this);
        timer.setDelay(REPAINT_INTERVAL);
        timer.start();
    }


    //Redraws everything in the game window
    //This gets called everytime repaint() is called!
    public void paintComponent(Graphics g) {
        Graphics2D g2D = (Graphics2D)g;
        initCanvas(g); //create a blank window
        drawText(g, g2D); //draw the dialogue text and text box
        drawMap(g); //draw the maze map
        drawUI(g); //draw the UI and button controls
    }


    //initializes a blank canvas for our window, to draw things on
    private void initCanvas(Graphics g){
        super.paintComponent(g);
        Graphics2D g2D = (Graphics2D)g;
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);
        g2D.setColor(getBackground());
        g2D.fillRect(0, 0, getWidth(), getHeight());
    }

    //Draws any dialogue text or text related graphics (like the box surrounding the text area)
    private void drawText(Graphics g, Graphics2D g2D){
        g2D.setColor(DIALOGUE_FONT_COLOR);
        g2D.setStroke(new BasicStroke(TEXT_BOX_BORDER_WIDTH));
        g2D.setFont(FONT_DIALOGUE);
        
        //draw player steps taken
        g2D.drawString("Steps Taken: " + stepsCounter, pathHitbox.getShapeX() - CELL_PADDING/2, pathHitbox.getShapeY() + (2 * pathButtonImg.getHeight()) + CELL_PADDING);
        
      
        //changes the text box to green/red when the game is over (if won/lost)
        if (gameState != STATE_PLAYING){
            if (gameState == STATE_WON)
                g2D.setPaint(TEXT_BOX_WON_COLOR);
            else
                g2D.setPaint(TEXT_BOX_LOST_COLOR);
            g2D.fill(TEXT_BOX);
            g2D.setColor(GAMEOVER_FONT_COLOR);
        }


        g2D.drawString(dialogueLine1, TEXT_AREA_X, TEXT_AREA_TOP_Y);
        g2D.drawString(dialogueLine2, TEXT_AREA_X, TEXT_AREA_BOT_Y);
        g2D.setPaint(Color.BLACK);
        g2D.draw(TEXT_BOX);         
        if (GameLauncher.DEBUG_MODE){
            g2D.setColor(DEBUG_FONT_COLOR);
            g2D.setFont(FONT_DEBUG);
            g2D.drawString(TXT_DEBUG_ENABLED, arrow_hitboxes[1].getShapeX() + CELL_PADDING, CELL_PADDING);
        }
        //display a message if the path toggle determines there's no viable path to the next objective
        if (pathTxt != null){
            g2D.setColor(DEBUG_FONT_COLOR);
            g2D.setFont(FONT_DEBUG);
            g2D.drawString(pathTxt, pathHitbox.getShapeX() - 3*HALF_PADDING, pathHitbox.getShapeY() + pathButtonImg.getHeight() + CELL_PADDING);
        }
        

    }

    //Draws the matrix of GUI cells, creating the visual grid for the map window
    private void drawMap(Graphics g){
        for (int row = 0; row < cellsGUI.length; row++){
            for (int col = 0; col < cellsGUI[0].length; col++){
                if (cellsGUI[row][col] != null) //null means no room there
                    cellsGUI[row][col].paintComponent(g);
            }
        }
    }

    //Draws all UI elements, including images, buttons, and their respective hitboxes
    private void drawUI(Graphics g){
        drawArrows(g);
        //legend
        g.drawImage(legendImg, arrow_hitboxes[1].getShapeX(), HALF_PADDING*3, null);
        //invisible hitbox for toggle path button
        pathHitbox.paintComponent(g);
        //path button
        if (lastPath == null)
            g.drawImage(pathButtonImg, pathHitbox.getShapeX(), pathHitbox.getShapeY(), null);
        else
            g.drawImage(pathButtonDisabledImg, pathHitbox.getShapeX(), pathHitbox.getShapeY(), null);
        //inventory contents
        if (hasCloak)
            g.drawImage(cloakImg, arrow_hitboxes[1].getShapeX(), (2 * CELL_PADDING) + legendImg.getHeight(), null);
        if (hasTreasure)
            g.drawImage(treasureImg, arrow_hitboxes[3].getShapeX()-CELL_PADDING,  (2 * CELL_PADDING) + legendImg.getHeight(), null);
    }

    //Draws the arrow (ie movement) images and their respective hitboxes
    private void drawArrows(Graphics g){
        for (int i = 0; i < arrowImg.length; i++){
            arrow_hitboxes[i].paintComponent(g);
            if (cooldown > 0 || gameState != STATE_PLAYING) //draws disabled buttons if game is over or in cooldown
                g.drawImage(arrowDisImg[i], arrow_hitboxes[i].getShapeX(), arrow_hitboxes[i].getShapeY(), null);
            else
                g.drawImage(arrowImg[i], arrow_hitboxes[i].getShapeX(), arrow_hitboxes[i].getShapeY(), null);
        }
    }

    //Gets called everytime the timer "ticks"
    //Redraws everything to the game window
    public void actionPerformed(ActionEvent ae){
        //each tick subtracts a cooldown, so player will be able to move again after short wait
        if (cooldown > 0)
            cooldown--;
        repaint();
    }


    //Used when it is determined that the maze file has an out of bound door
    //Constructs and appropriate error message identifying the problem room/door
    private static void generateOOBDoorError(int row, int col, char dir){
        terminateWithError("Maze has out of bounds door! direction: " + dir + " at row: " + row + ", col: " + col);
    }

    //Terminates the game with an IllegalStateException, used by the validation code.
    private static void terminateWithError(String errorMsg){
        errorMsg = "ERROR! " + errorMsg + " Terminating game!";
        throw new IllegalStateException(errorMsg);
    }


    //retrives the room object at a specified row and column
    private Room getRoom(int row, int col){
        return cellsGUI[row][col].getLinkedRoom();
    }

    //Converts an ArrayList of String[] to a 2D String array
    private String[][] convertToArr(ArrayList<String[]> list){
        String[][] arr = new String[list.size()][list.get(0).length];
        for (int i = 0; i < list.size(); i++){
            for (int j = 0; j < list.get(0).length; j++){
                arr[i][j] = list.get(i)[j];
            }
        }
        return arr;
    }


    //Checks to see if an argument String contains an argument character
    private static boolean hasChar(String s, char c){
        for (int i = 0; i < s.length(); i++){
            if (s.charAt(i) == c)
                return true;
        }
        return false;
    }




    //These functions are required by various interfaces, but are not used
    public void mouseReleased(MouseEvent event) { }

    public void mouseClicked(MouseEvent event) { }

    public void mouseEntered(MouseEvent event) { }

    public void mouseExited(MouseEvent event) { }

    public void mouseMoved(MouseEvent event) { }

    public void keyReleased(KeyEvent event) {  }

    public void keyTyped(KeyEvent event) {  }



}
