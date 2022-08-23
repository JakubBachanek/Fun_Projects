import java.awt.Color;

public class ColorBlock implements Runnable {
    int positionX;
    int positionY;
    Grid grid;
    SimulationFrame simFrame;
    int red;
    int green;
    int blue;
    Color colorOfBlock;

    public ColorBlock(int x, int y, Grid grid, SimulationFrame simFrame, int redRand, int greenRand, int blueRand) {
        this.positionX = x;
        this.positionY = y;
        this.grid = grid;
        this.simFrame = simFrame;
        this.red = redRand;
        this.green = greenRand;
        this.blue = blueRand;
        this.colorOfBlock = new Color(red, green, blue);
        simFrame.buttons[positionX][positionY].setBackground(colorOfBlock);
    }
    
    @Override
    public void run() {
        int test = 1;

        while(true) {
            synchronized (grid) {
                try {
                    if(test == 1) {
                        grid.wait(1500);
                        test = 0;
                    }
                    
                    int milliseconds = (int) (simFrame.msNumMin + grid.rand.nextDouble() * (simFrame.msNumMax - simFrame.msNumMin));
                    grid.wait(milliseconds);
                    changeColor();
                    simFrame.buttons[positionX][positionY].setBackground(colorOfBlock);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public void changeColor() {
        double randDouble = grid.rand.nextDouble();

        if(randDouble <= grid.probability) {
            int redRand = grid.rand.nextInt(256);
            int greenRand = grid.rand.nextInt(256);
            int blueRand = grid.rand.nextInt(256);
            this.red = redRand;
            this.green = greenRand;
            this.blue = blueRand;
            this.colorOfBlock = new Color(red, green, blue);	
        } else {
            int posLeft;
            int posTop;
            
            if(this.positionY == 0) {
                posLeft = grid.getWidth() - 1;
            } else {
                posLeft = this.positionY - 1;
            }
            
            if(this.positionX == 0) {
                posTop = grid.getHeight() - 1;
            } else {
                posTop = this.positionX - 1;
            }
            
            int posRight = (this.positionY + 1) % grid.getWidth();
            int posBottom = (positionX + 1) % grid.getHeight();
            
            int nextRed = (int) ( grid.board[posTop][positionY].red + grid.board[posBottom][positionY].red + grid.board[positionX][posLeft].red + grid.board[positionX][posRight].red) / 4;
            int nextGreen = (int) ( grid.board[posTop][positionY].green + grid.board[posBottom][positionY].green + grid.board[positionX][posLeft].green + grid.board[positionX][posRight].green) / 4;
            int nextBlue = (int) ( grid.board[posTop][positionY].blue + grid.board[posBottom][positionY].blue + grid.board[positionX][posLeft].blue + grid.board[positionX][posRight].blue) / 4;
            
            this.red = nextRed;
            this.green = nextGreen;
            this.blue = nextBlue;
            this.colorOfBlock = new Color(red, green, blue);
        }
    }
}
