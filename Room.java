import java.util.*;



//************************************************************************************
//*                                                                                  *
//*                                                                                  *
//*                    YOU ARE NOT ALLOWED TO MODIFY THIS CLASS                      *
//*                 But WILL need to instantiate / use methods in here!              *
//*                                                                                  *
//*                                                                                  *
//************************************************************************************




//class representing a single room in the maze, along with its respective doors (to other Rooms)
//and any object residing inside the room.
public class Room{
    
    //A collection of all doors (ie exits to other Rooms) the maze has
    private HashMap<Character, Room> doors;    
    //what occupant (if any) the Room has
    private char occupant;
    
    
    
    public Room(){
        doors = new HashMap<Character, Room>();
        occupant = MazeLogic.NO_OCCUPANT;        
    }
    
    //Adds a door at the specified direction to the Room's collection of exits
    public void addDoor(char dir, Room room){
        doors.put(dir,room);
    }
    
    //checks to see if there is a door in the specified direction
    public boolean hasDoor(char dir){
        return doors.containsKey(dir);
    }
    

    //Sets the occupant of this room to the argument value
    public void setOccupant(char occupant){
        if (MazeLogic.indexOfChar(MazeLogic.OCCUPANTS, occupant) < 0)
            throw new IllegalStateException("Error! Invalid character specified as Maze Room occupant: " + occupant);
        this.occupant = occupant;
    }
    
    
    //Given a particular cardinal direction, returns the linked Room object that this room has a door to
    public Room go(char dir){
        if (!hasDoor(dir))
            throw new IllegalStateException("Error! Room has no door for specified direction: " + dir);
        return doors.get(dir);
    }
    

    //Returns the room's occupant
    public char getOccupant(){
        return occupant;
    }
    
    //Returns all rooms connected to this room
    public Collection<Room> getAllConnectedRooms(){
        return doors.values();
    }
    
    //Returns all directions that the room has doors in
    public Set<Character> getAllDoorDirections(){
        return doors.keySet();
    }
    
    
    //2 Rooms are equal if and only if they reference the exact same object
    //(ie both references point to the same object in memory).    
    public boolean equals(Object other){
        return this == other;
    }
    
    

    

    
}