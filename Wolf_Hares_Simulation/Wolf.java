import java.util.ArrayList;


public class Wolf implements Runnable {
    int positionX;
    int positionY;
    Grid grid;
    SimulationFrame simFrame;
    Hare targetHare;

    public Wolf(int posX, int posY, Grid grid, SimulationFrame simFrame) {
        this.positionX = posX;
        this.positionY = posY;
        this.grid = grid;
        this.simFrame = simFrame;
    }


    @Override
    public void run() {
        boolean isHareKilled = false;

        while(true) {
            synchronized (grid) {
                try {
                    int milliseconds = (int) (simFrame.msNumMin + grid.rand.nextDouble() * (simFrame.msNumMax - simFrame.msNumMin));
                    grid.wait(milliseconds);
                    searchTarget();
                    
                    if(targetHare != null) {
                        isHareKilled = moveWolf();
                        simFrame.changeField();
                    } else {
                        grid.wait(5000);
                        System.exit(0);
                    }

                    if(isHareKilled) {
                        for(int i = 0; i < 4; i++) {
                            milliseconds = (int) (simFrame.msNumMin + grid.rand.nextDouble() * (simFrame.msNumMax - simFrame.msNumMin));
                            grid.wait(milliseconds);
                        }
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


    public void searchTarget() {

        ArrayList<Hare> nearbyHares = grid.findNearbyHares();

        if(nearbyHares.size() > 0) {
            int selectedHare = grid.rand.nextInt(nearbyHares.size());
            targetHare = nearbyHares.get(selectedHare);
        } else {
            targetHare = null;
        }
    }


    public boolean moveWolf() {
        boolean killedHare = false;
        int[] moveCoordinates = grid.findWolfNextMove();

        if(grid.board[moveCoordinates[0]][moveCoordinates[1]] instanceof Hare)
        {
            ((Hare) grid.board[moveCoordinates[0]][moveCoordinates[1]]).killHare();
            killedHare = true;
        }

        grid.board[moveCoordinates[0]][moveCoordinates[1]] = this;

        grid.board[positionX][positionY] = null;

        positionX = moveCoordinates[0];
        positionY = moveCoordinates[1];
        return killedHare;
    }
}
