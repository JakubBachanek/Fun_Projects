import java.util.ArrayList;
import java.util.Random;


public class Grid {
    Random rand = new Random();
    private int height, width;
    Wolf wolf;
    public Object[][] board;
    
    public Grid(int height, int width, int haresCount, SimulationFrame simFrame) {
        this.height = height;
        this.width = width;

        board = new Object[height][width];
        
        for(int i = 0; i < haresCount; i++) {
            int temp = 0;

            while(temp == 0) {
                int posX = rand.nextInt(height);
                int posY = rand.nextInt(width);

                if(board[posX][posY] == null) {
                    board[posX][posY] = new Hare(posX, posY, this, simFrame);
                    Thread t = new Thread((Hare) board[posX][posY]);
                    t.start();
                    temp = 1;
                }
            }
        }
        
        
        int temp = 0;

        while(temp == 0) {
            int posX = rand.nextInt(height);
            int posY = rand.nextInt(width);

            if(board[posX][posY] == null) {
                board[posX][posY] = new Wolf(posX, posY, this, simFrame);
                wolf = (Wolf) board[posX][posY];
                Thread t = new Thread(wolf);
                t.start();
                temp = 1;
            }
        }
    }


    public int getObjectAt(int col, int row) {
        if(board[col][row] instanceof Hare) {
            return 1;
        } else if(board[col][row] instanceof Wolf) {
            return 2;
        } else {
            return 0;
        }
    }

    public int getHeight() {
        return height;
    }


    public int getWidth() {
        return width;
    }
    

    public double calcDistance(int posWolfX, int posWolfY, int posHareX, int posHareY )
    {
        double distance = Math.sqrt((posWolfX - posHareX) * (posWolfX - posHareX) + (posWolfY - posHareY) * (posWolfY - posHareY));
        return distance;
    }
    

    public ArrayList<Hare> findNearbyHares() {
        double shortestDistance = 111;
        
        for (int col = 0; col < height; col++) {
            for (int row = 0; row < width; row++) {
                if(board[col][row] instanceof Hare) {
                    double distance = calcDistance(((Wolf) wolf).getPositionX(), ((Wolf) wolf).getPositionY(), ((Hare) board[col][row]).getPositionX(), ((Hare) board[col][row]).getPositionY());
                    
                    if(distance < shortestDistance) {
                        shortestDistance = distance;
                    }
                }
            }
        }
        
        ArrayList<Hare> nearbyHares = new ArrayList<Hare>();
        
        for (int col = 0; col < height; col++) {
            for (int row = 0; row < width; row++) {
                if(board[col][row] instanceof Hare && calcDistance(((Wolf) wolf).getPositionX(), ((Wolf) wolf).getPositionY(), ((Hare) board[col][row]).getPositionX(), ((Hare) board[col][row]).getPositionY()) == shortestDistance) {
                    nearbyHares.add((Hare) board[col][row]);
                }
            }
        }

        return nearbyHares;
    }
    
    

    public int[] findWolfNextMove() {
        double shortestDistance = 111;
        int WolfPosX = wolf.getPositionX();
        int WolfPosY = wolf.getPositionY();
        int newWolfFields[][] = { { WolfPosX - 1, WolfPosY - 1 }, { WolfPosX - 1, WolfPosY }, { WolfPosX - 1, WolfPosY + 1 }, { WolfPosX, WolfPosY - 1 },
        { WolfPosX, WolfPosY + 1 }, { WolfPosX + 1, WolfPosY - 1 }, { WolfPosX + 1, WolfPosY }, { WolfPosX + 1, WolfPosY + 1 } };
        
        for(int i = 0; i < 8; i++) {
            if(newWolfFields[i][0] >= 0 && newWolfFields[i][0] < height &&  newWolfFields[i][1] >= 0 && newWolfFields[i][1] < width) {
                double distance = calcDistance(newWolfFields[i][0] , newWolfFields[i][1] , wolf.targetHare.getPositionX(), wolf.targetHare.getPositionY());
                if(distance < shortestDistance) {
                    shortestDistance = distance;
                }
            }    		
        }
         
        ArrayList<int[]> possibleMoveFields = new ArrayList<int[]>();
        int wolfTargetField[] = new int[2];
        
        for(int i = 0; i < 8; i++) {
            if(newWolfFields[i][0] >= 0 && newWolfFields[i][0] < height &&  newWolfFields[i][1] >= 0 && newWolfFields[i][1] < width && calcDistance(newWolfFields[i][0] , newWolfFields[i][1] , wolf.targetHare.getPositionX(), wolf.targetHare.getPositionY()) == shortestDistance) {
                possibleMoveFields.add(new int[]{newWolfFields[i][0], newWolfFields[i][1]});
            }
        }
                
        int selection = rand.nextInt(possibleMoveFields.size());
        wolfTargetField = possibleMoveFields.get(selection);

        return wolfTargetField;
    }
    

    public int[] findHareNextMove(int posX, int posY) {
        double longestDistance = -1;
                
        ArrayList<int[]> hareDestinationFields = new ArrayList<int[]>();
        int hareDestinationField[] = new int[] {-1,-1};
        
        boolean isEdgeNeighbour = false;
        
        int harePosX = posX;
        int harePosY = posY;
        
        int possibleMoveFields[][] = { { harePosX - 1, harePosY - 1 }, { harePosX - 1, harePosY }, { harePosX - 1, harePosY + 1 }, { harePosX, harePosY - 1 },
        { harePosX, harePosY + 1 }, { harePosX + 1, harePosY - 1 }, { harePosX + 1, harePosY }, { harePosX + 1, harePosY + 1 } };
        
        if(((possibleMoveFields[0][0] < 0 ) && (possibleMoveFields[1][0] < 0) && (possibleMoveFields[2][0] < 0))
                || ((possibleMoveFields[0][1] < 0) && (possibleMoveFields[3][1] < 0 ) && (possibleMoveFields[5][1] < 0))
                || ((possibleMoveFields[2][1] >= width) && (possibleMoveFields[4][1] >= width ) && (possibleMoveFields[7][1] >= width))
                || ((possibleMoveFields[5][0] >= height) && (possibleMoveFields[6][0] >= height ) && (possibleMoveFields[7][0] >= height))) {
            isEdgeNeighbour = true;
        }


        if(!isEdgeNeighbour) {
            for(int i = 0; i < 8; i++) {
                if(possibleMoveFields[i][0] >= 0 && possibleMoveFields[i][0] < height &&  possibleMoveFields[i][1] >= 0 && possibleMoveFields[i][1] < width) {
                    double distance = calcDistance(possibleMoveFields[i][0] , possibleMoveFields[i][1] , wolf.getPositionX(), wolf.getPositionY());

                    if(distance > longestDistance) {
                        longestDistance = distance;
                    }
                }
            }
            

            
            for(int i = 0; i < 8; i++) {
                if(possibleMoveFields[i][0] >= 0 && possibleMoveFields[i][0] < height &&  possibleMoveFields[i][1] >= 0 && possibleMoveFields[i][1] < width && calcDistance(possibleMoveFields[i][0] , possibleMoveFields[i][1] , wolf.getPositionX(), wolf.getPositionY()) == longestDistance) {
                    if(board[possibleMoveFields[i][0]][possibleMoveFields[i][1]] == null) {
                        int[] temp = new int[] {possibleMoveFields[i][0], possibleMoveFields[i][1]};
                        hareDestinationFields.add(temp);
                    }
                }
            }
        } else {
            for(int i = 0; i < 8; i++) {
                if(possibleMoveFields[i][0] >= 0 && possibleMoveFields[i][0] < height &&  possibleMoveFields[i][1] >= 0 && possibleMoveFields[i][1] < width) {
                    if(board[possibleMoveFields[i][0]][possibleMoveFields[i][1]] == null) {
                        int[] temp = new int[] {possibleMoveFields[i][0], possibleMoveFields[i][1] };
                        hareDestinationFields.add(temp);
                    }
                }
            }
        }
        
        
        if(hareDestinationFields.size() > 0) {
            int i = rand.nextInt(hareDestinationFields.size());
            hareDestinationField = new int[]{hareDestinationFields.get(i)[0], hareDestinationFields.get(i)[1]};
        }
                
        return hareDestinationField;
    }
}
