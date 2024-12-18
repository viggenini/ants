package org.evensen.ants;

import java.util.*;

public class MyAntWorld implements AntWorld {
    private final int width;
    private final int height;
    private final Set<Position> obstacles = new HashSet<>();
    private final List<FoodSource> foodSourcesList = new ArrayList<>();
    private final Position homeSource;
    private final float[][] foragingPheromones;
    private final float[][] foodPheromones;
    private final boolean[][] foodMatrix;
    private final float[][] tmP;
    private final float[][][] pheromoneMatrices;
    private final int foodConstant = 2000;



    public MyAntWorld(int width, int height, int foodSourcesCount) {
        this.width = width;
        this.height = height;
        this.homeSource = new Position(width - 10, height / 2); // Hemposition

        this.foragingPheromones = new float[width][height];
        this.foodPheromones = new float[width][height];
        this.foodMatrix = new boolean[width][height]; // Initiera matrisen

        this.tmP = new float[width][height];
        this.pheromoneMatrices = new float[2][this.width][this.height];

        Random random = new Random();
        for (int i = 0; i < foodSourcesCount; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            FoodSource foodSource = new FoodSource(new Position(x, y), 10, (this.foodConstant)); // Radie = 10, Mat = 50
            this.foodSourcesList.add(foodSource);
            updateFoodMatrix(foodSource, true); // Uppdatera matrisen med den nya matkällan
        }
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public int getFoodSources() {
        return this.foodSourcesList.size();
    }

    @Override
    public boolean containsFood(Position p) {
        int x = (int) p.getX();
        int y = (int) p.getY();
        if (isWithinBounds(x, y)) {
            return this.foodMatrix[x][y]; // Kontrollera direkt i matrisen
        }
        return false;
    }

    @Override
    public void pickUpFood(Position p) {
        List<FoodSource> copyList;

        synchronized (this.foodSourcesList) {
            copyList = new ArrayList<>(this.foodSourcesList); // Skapa en kopia för iteration
        }

        for (FoodSource foodSource : copyList) {
            if (foodSource.containsFood(p)) {
                boolean foodTaken = foodSource.takeFood();

                if (!foodTaken) { // Om matkällan är tom
                    synchronized (this.foodSourcesList) {
                        this.foodSourcesList.remove(foodSource); // Ta bort från listan
                    }
                    updateFoodMatrix(foodSource, false); // Uppdatera matrisen
                    addNewFoodSource(); // Skapa ny matkällan
                } else {
                    updateFoodMatrix(foodSource, true); // Uppdatera matrisen
                }
                return; // Avsluta direkt när mat plockats
            }
        }

    }

    @Override
    public long getFoodCount() {
        long totalFood = 0;
        for (FoodSource foodSource : this.foodSourcesList) {
            totalFood += foodSource.getFoodAmount();
        }
        return totalFood;
    }

    @Override
    public boolean isHome(Position p) {
        int homeRadius = 10;
        return this.homeSource.isWithinRadius(p, homeRadius);
    }

    @Override
    public void dropForagingPheromone(Position p, float amount) {
        int x = (int) p.getX();
        int y = (int) p.getY();
        if (isWithinBounds(x, y)) {
            this.foragingPheromones[x][y] = Math.min(1.0f, this.foragingPheromones[x][y] + amount);
            //System.out.println("foraging fermoner :" + getForagingStrength(p));
        }
    }

    @Override
    public void dropFoodPheromone(Position p, float amount) {
        int x = (int) p.getX();
        int y = (int) p.getY();
        if (isWithinBounds(x, y)) {
            this.foodPheromones[x][y] = Math.min(1.0f, this.foodPheromones[x][y] + amount);
            //System.out.println("foraging food :" + getFoodStrength(p));
        }
    }





    @Override
    public float getForagingStrength(Position p) {
        int x = (int) p.getX();
        int y = (int) p.getY();
        if (isWithinBounds(x, y)) {
            return this.foragingPheromones[x][y];
        }
        return 0;
    }

    @Override
    public float getFoodStrength(Position p) {
        int x = (int) p.getX();
        int y = (int) p.getY();
        if (isWithinBounds(x, y)) {
            return this.foodPheromones[x][y];
        }
        return 0;

    }


    @Override
    public void dispersePheromones() {
        float k = 0.5f;
        float f = 0.95f;
        // 1 Iterera över feromonarray
        for (int t = 0; t < this.pheromoneMatrices.length; t++) {
            // 2 Temporär feromonmatris
            float[][] tmpP = new float[this.width][this.height];

            ///3 Iterera över världens positioner
            for (int x = 0; x < this.width; x++) {
                for (int y = 0; y < this.height; y++) {
                    // 4 Initiera NPL(summan av neighbors' pheromone levels)
                    float npl = 0.0f;

                    //5 Skapa ett positionsobjekt för nuvarande koordinater (x, y)
                    Position p = new Position(x, y);

                    // 6 Kolla efter obstacle
                    if (!isObstacle(p)) {
                        // 7 Iterera genom alla 8 grannceller, och repetera kantvärden
                        for (int dx = -1; dx <= 1; dx++) {
                            for (int dy = -1; dy <= 1; dy++) {
                                if (dx == 0 && dy == 0) {
                                    continue; // Skippa center av 3x3 grid
                                }

                                int nx = x + dx;
                                int ny = y + dy;

                                // Hantera kantfall, om utanför världen repetera kantvärdet
                                if (nx < 0) nx = 0;
                                if (ny < 0) ny = 0;
                                if (nx >= this.width) nx = this.width - 1;
                                if (ny >= this.height) ny = this.height - 1;

                                // 8 Lägg till feromonnivån på grannposition npl
                                npl += this.pheromoneMatrices[t][nx][ny];

                            }
                        }

                        // 9 Updatera npl med keep-konstanten k=0.5 och feromonnivån på nuvarande position
                        npl = (float) (((1 - k) * npl) / 8.0f + (k * this.pheromoneMatrices[t][x][y]));


                        // 10 Lagra den uppdaterade feromonnivån i tmP
                        tmpP[x][y] = npl * f;
                    }
                }
            }

            //11 Kopiera tmPs innehåll och lägg in i pheromonematrices
            for (int x = 0; x < this.width; x++) {
                for (int y = 0; y < this.height; y++) {
                    this.pheromoneMatrices[t][x][y] = tmpP[x][y];
                }
            }
        }

        for (FoodSource foodSource : this.foodSourcesList) {

            int mx = (int) foodSource.getPosition().getX();
            int my = (int) foodSource.getPosition().getY();

            this.dropFoodPheromone(new Position(mx, my), 1);

            //matkällans radie radius-1

            //   System.out.println("Fermon droppad på matkälla" + (new Position(mx,my)));
            //   System.out.println("Antal mat på" + this.getFoodStrength(new Position(mx,my)));
        }
        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                this.foragingPheromones[x][y] *= 0.95f; // Förångning av letarferomon
                this.foodPheromones[x][y] *= 0.95f;     // Förångning av matferomon
            }
        }

    }



    public void selfContainedDisperse(){
        for (FoodSource foodSource : this.foodSourcesList) {

            int mx = (int) foodSource.getPosition().getX();
            int my = (int) foodSource.getPosition().getY();

            this.dropFoodPheromone(new Position(mx, my), 1);

            //matkällans radie radius-1

            //   System.out.println("Fermon droppad på matkälla" + (new Position(mx,my)));
            //   System.out.println("Antal mat på" + this.getFoodStrength(new Position(mx,my)));
        }
        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                this.foragingPheromones[x][y] *= 0.95f; // Förångning av letarferomon
                this.foodPheromones[x][y] *= 0.95f;     // Förångning av matferomon
            }
        }
    }
    @Override
    public boolean isObstacle(Position p) {
        int x = (int) p.getX();
        int y = (int) p.getY();
        return x < 0 || y < 0 || x >= this.width || y >= this.height || this.obstacles.contains(p);
    }

    @Override
    public void setObstacle(Position p, boolean add) {
        if (add) {
            this.obstacles.add(p);
        } else {
            this.obstacles.remove(p);
        }
    }

    @Override
    public void hitObstacle(Position p, float strength) {
        // Ej implementerat
    }

    @Override
    public void dropFood(Position p) {
        // Ej implementerat
    }

    @Override
    public float getDeadAntCount(Position p) {
        return 0; // Ej implementerat
    }

    private void addNewFoodSource() {
        Random random = new Random();
        Position newPosition;
        do {
            int x = random.nextInt(this.width);
            int y = random.nextInt(this.height);
            newPosition = new Position(x, y);
        } while (isObstacle(newPosition)); // Kontrollera att platsen inte är ett hinder

        FoodSource newFoodSource = new FoodSource(newPosition, 10, this.foodConstant); // Radie = 10, Mat = 50
        this.foodSourcesList.add(newFoodSource);
        updateFoodMatrix(newFoodSource, true); // Uppdatera matrisen

        // Ta bort koordinaterna på foodsource, och lägg nya koordinaten på newfoodsource.


    }

    private void updateFoodMatrix(FoodSource foodSource, boolean hasFood) {
        int x = (int) foodSource.getPosition().getX();
        int y = (int) foodSource.getPosition().getY();
        int radius = foodSource.getRadius();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                int nx = x + dx;
                int ny = y + dy;
                if (isWithinBounds(nx, ny) && Math.sqrt(dx * dx + dy * dy) <= radius-1) {
                    this.foodMatrix[nx][ny] = hasFood;
                }

            }
        }
    }

    private boolean isWithinBounds(int x, int y) {
        return x >= 0 && x < this.width && y >= 0 && y < this.height;
    }
}


