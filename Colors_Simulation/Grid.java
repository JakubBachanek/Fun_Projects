import java.util.Random;


public class Grid {
    Random rand = new Random();
    private int height, width;
    double probability;
    public ColorBlock[][] board;
    
    public Grid(int height, int width, double probability, SimulationFrame simFrame) {
        this.height = height;
        this.width = width;
        this.probability = probability;
        
        board = new ColorBlock[height][width];
        
        for(int i = 0; i < height; i++) {
            for(int j = 0; j < width; j++) {
                int redRand = rand.nextInt(256);
                int greenRand = rand.nextInt(256);
                int blueRand = rand.nextInt(256);
                
                board[i][j] = new ColorBlock(i, j, this, simFrame, redRand, greenRand, blueRand);
                Thread t = new Thread(board[i][j]);
                t.start();		
            }
        }
    }


    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }
}
