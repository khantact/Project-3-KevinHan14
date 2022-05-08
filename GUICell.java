import java.awt.*;
import java.util.*;
import java.awt.geom.*;
import javax.swing.JPanel;





//************************************************************************************
//*                                                                                  *
//*                                                                                  *
//*                    YOU ARE NOT ALLOWED TO MODIFY THIS CLASS                      *
//*           YOU ALSO DON'T NEED TO READ/TRACE/CALL ANY METHODS IN HERE             *
//*         (But feel free to take a look if you're feeling extra curious!)          *
//*                                                                                  *
//*                                                                                  *
//************************************************************************************




//A cell and its respective doors to be painted in the map window
//area of the Mazes and Manticores game window

public class GUICell extends JPanel {

    //Dimensions for door rectangles
    private static final int DOOR_W = 6;
    private static final int DOOR_L = MazeWindow.CELL_PADDING;  
    
    //Default colors for rooms/doors
    private static final Color DEFAULT_CELL_COLOR = Color.GRAY;
    private static final Color DEFAULT_DOOR_COLOR = Color.GRAY;
    private static final Color PATH_COLOR = Color.MAGENTA;
    
    //Indices corresponding to the different directions for the array storing the cell's associated doors
    private static final char[] DOOR_IDX_DIR = {MazeLogic.SOUTH, MazeLogic.EAST, MazeLogic.NORTH, MazeLogic.WEST};
    private static final int SOUTH_IDX = 0;
    private static final int EAST_IDX = 1;
    private static final int NORTH_IDX = 2;
    private static final int WEST_IDX = 3;
    private static final int NONE_IDX = -1;
    
    //Border thickness for when cell is outlined as part of shortest path
    private static final int BORDER_THICKNESS = 3;
    
    //Tracks the square's color and border
    //Square alternates between fillColor1 and 2 when painted
    private Color fillColor1 = DEFAULT_CELL_COLOR;
    private Color fillColor2 = DEFAULT_CELL_COLOR;
    private Color outlineColor = null;    
    
    //Used to create square's color flashing effect
    private int tick = 0;
    //Determines if the cell is drawn to the window or not (doesn't effect shortest path)
    private boolean isVisible = false;
    //Tracks if the player is currently at this cell.
    private boolean playerHere = false;
    
    //Links this square to its respective Room object in the Maze    
    private Room linkedRoom;
    
    //The geometric rectangles drawn for the room and its collection of doors
    private RectangularShape room;
    private RectangularShape[] doors = new RectangularShape[4];    
    //Tracks which doors, if any, need to be highlighted as part of "shortest path"
    private boolean[] pathDoors = new boolean[DOOR_IDX_DIR.length];
    
    
    //Constructors accepting a position, height/width (or size if square)
    //As well as a Room object to link to this object if applicable.
    public GUICell(double x, double y, double size){
        this(x, y, size, size, null);
    }    
    
    public GUICell(double x, double y, double size, Room linkedRoom){
        this(x, y, size, size, linkedRoom);
    }      
    
    public GUICell(double x, double y, double h, double w){
        this(x, y, h, w, null);
    }  
    
    public GUICell(double x, double y, double h, double w, Room linkedRoom){
        super();
        room = new Rectangle2D.Double(x, y, h, w);
        this.linkedRoom = linkedRoom;
        initDoors();
        initColors();
    }    
    
    
    //initializes the colors for the various components to be painted to the window
    public void initColors(){
        if (linkedRoom != null){
            char occupant = linkedRoom.getOccupant();
            if (occupant == MazeLogic.MANTICORE)
                fillColor2 = MazeWindow.MANTICORE_COLOR;
            else if (occupant == MazeLogic.CLOAK)
                fillColor2 = MazeWindow.CLOAK_COLOR;   
            else if (occupant == MazeLogic.TREASURE)
                fillColor2 = MazeWindow.TREASURE_COLOR; 
            else if (occupant == MazeLogic.LADDER)
                fillColor2 = MazeWindow.LADDER_COLOR;             
        }
    }
    
    //Instantiates rectangles for any doors that the Room associated with this object has
    public void initDoors(){
        if (this.linkedRoom == null)
            return;
        Set<Character> doorDirs = linkedRoom.getAllDoorDirections();
        if (doorDirs.contains(MazeLogic.SOUTH))
            doors[SOUTH_IDX] = new Rectangle2D.Double(this.getShapeX() + (MazeWindow.CELL_SIZE/2)-(DOOR_W/2), this.getShapeY()+MazeWindow.CELL_SIZE,DOOR_W,DOOR_L);
        if (doorDirs.contains(MazeLogic.EAST))
            doors[EAST_IDX] = new Rectangle2D.Double(this.getShapeX() + MazeWindow.CELL_SIZE, this.getShapeY()+(MazeWindow.CELL_SIZE / 2)-(DOOR_W/2),DOOR_L,DOOR_W);    
    }      
    
    
    //Checks to see if this object's Rectangle contains the argument x,y coord     
    public boolean contains(int x, int y) {
        return room.contains(x, y);
    }  
    
    
    //Links the West/North doors to another GUICell objects East/South door respectively.
    //Done to avoid double painting overlapping doors
    public void linkWestDoor(GUICell other){
        doors[WEST_IDX] = other.doors[EAST_IDX];       
    }
    
    public void linkNorthDoor(GUICell other){
        doors[NORTH_IDX] = other.doors[SOUTH_IDX];    
    }
    
    //Checks if this cell has a door in the argument direction
    public boolean hasDoor(char dir){
        return linkedRoom.hasDoor(dir);
    }
    
    
    //Updates the status of whether the player is currently at this location
    public void setHasPlayer(boolean isHere){
        Color toCheck = DEFAULT_CELL_COLOR;
        Color toSet = MazeWindow.PLAYER_COLOR;
        if (!isHere){
            toCheck = MazeWindow.PLAYER_COLOR;       
            toSet = DEFAULT_CELL_COLOR;                    
        }
        if (fillColor2 == toCheck)
            fillColor2 = toSet;
        else if (fillColor1 == toCheck)
            fillColor1 = toSet;
    }
    
    //Designates components of this cell as being part of the "shortest path"
    //meaning they will be colored/outlined in magenta
    public void setDoorPath(char dir, boolean enable){
        pathDoors[MazeLogic.indexOfChar(DOOR_IDX_DIR, dir)] = enable;
    }
    
    public void setRoomPath(boolean enable){
        if (enable)
            outlineColor = PATH_COLOR;
        else
            outlineColor = null;
    }      
    
    //toggles the visibility of this cell on the window
    public void setVisibility(boolean isVisible){
        this.isVisible = isVisible;
    }
    
    
    //This overrides the paintComponent method of JPanel
    //Draws the cell and its associated doors to the window
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        paintRoom(g2d);
        paintDoors(g2d);
        
    }    
    
    //Paints the room (ie square)
    private void paintRoom(Graphics2D g2d){
        if (++tick % 2 == 0)
            g2d.setPaint(fillColor1);
        else
            g2d.setPaint(fillColor2);
        if (isVisible)
            g2d.fill(room);
        if (outlineColor != null) {
            g2d.setStroke(new BasicStroke(BORDER_THICKNESS));
            g2d.setPaint(outlineColor);
            g2d.draw(room);
        }           
    }
    
    //Paints any doors associated iwth the cell
    private void paintDoors(Graphics2D g2d){
        for (int i = 0; i < doors.length; i++){
            if (doors[i] != null){
                if (pathDoors[i])
                    g2d.setPaint(PATH_COLOR);
                else
                    g2d.setPaint(DEFAULT_DOOR_COLOR);
                if (isVisible || pathDoors[i])
                    g2d.fill(doors[i]);        
            }            
        }
    }  
    
    
    //Accessor methods (uses linked Room object if applicable)
    public  int getShapeX() {
        return (int)room.getX();
    }
    
    public  int getShapeY() {
        return (int)room.getY();
    }
    
    public Room getLinkedRoom(){
        return linkedRoom;
    }  
    
    public char getOccupant(){
        return linkedRoom.getOccupant();
    }    
    
}
