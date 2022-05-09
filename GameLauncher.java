// Driver class
public class GameLauncher {

    // Toggles Debug Mode
    // **This is the only line you need to modify in this file!**
    public static final boolean DEBUG_MODE = true;

    public static void main(String[] args) {
        args = new String[] { "maze_normal.txt" };
        if (args.length < 1)
            throw new IllegalArgumentException(
                    "Error! Game Launcher requires a maze '.txt' filename command line argument!");

        // Create and display the graphical window for our game
        MazeWindow gameWindow = new MazeWindow(args[0]);
        gameWindow.launchWindow();

    }
}
