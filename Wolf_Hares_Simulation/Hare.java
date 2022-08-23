public class Hare implements Runnable {
    int positionX;
    int positionY;
    Grid grid;
    SimulationFrame simFrame;
    boolean isAlive;
    
    public Hare(int posX, int posY, Grid grid, SimulationFrame simFrame)
    {
        this.positionX = posX;
        this.positionY = posY;
        this.grid = grid;
        this.simFrame = simFrame;
        this.isAlive = true;
    }
    

    @Override
    public void run() {
        while(isAlive) {
            synchronized (grid) {
                try {
                    int milliseconds = (int) (simFrame.msNumMin + grid.rand.nextDouble() * (simFrame.msNumMax - simFrame.msNumMin));
                    grid.wait(milliseconds);
                    
                    if(isAlive && grid.wolf != null) {
                        moveHare();
                    }
                    
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    

    public int getPositionX() {
        return positionX;
    }

    public int getPositionY() {
        return positionY;
    }
    

    public void moveHare() {
        int[] moveCoordinates = grid.findHareNextMove(positionX, positionY);
        
        if(moveCoordinates[0] != -1) {
            grid.board[moveCoordinates[0]][moveCoordinates[1]] = this; 
            grid.board[positionX][positionY] = null;
            positionX = moveCoordinates[0];
            positionY = moveCoordinates[1];
            simFrame.changeField();
        }

    }


    public void killHare() {
        this.isAlive = false;
        grid.board[positionX][positionY] = null;
    }
}
