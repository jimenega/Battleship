package battleship;

import java.io.IOException;
import java.util.*;

class Grid {
    static int rows = 10;
    static int columns = 10;
    private char[][] grid;
    String gridMode;
    boolean showGridTitle = false;

    Grid(String gridMode) {
        this.gridMode = gridMode;
    }

    void populateGrid() {
        grid = new char[rows][columns];
        for (int i = 0; i < 10; i++) {
            for ( int j = 0; j < 10; j++) {
                grid[i][j] = '~';
            }
        }
    }

    String formatString(int row) {
        return Arrays.toString(grid[row])
                .replace(",", "")  //remove commas
                .replace("[", "")  //remove right bracket
                .replace("]", "")  //remove left bracket
                .trim(); //remove trailing spaces
    }

    void showGrid() {
        int gridRow = 0;
        if (showGridTitle) { System.out.printf("%n%s%n", gridMode);}
        System.out.printf("%s", "  ");

        for (int columnHeading = 1; columnHeading <= columns; columnHeading++ ) {
            System.out.printf("%d ", columnHeading);
        }
        System.out.println();

        // ASCII 65 - 74 for rowHeading A - J
        for (int rowHeading = 65; rowHeading <= 74; rowHeading++ ) {
            System.out.printf("%c %s%n", rowHeading, formatString(gridRow));
            gridRow++;
        }
    }

    void setPoint(int x, int y, char c) {
        grid[x][y] = c;
    }

    void setPoint(int x, int y) {
        grid[x][y] = 'O';
    }

    void setPoint(String p, char c) {
        int[] pI = Utils.findCorInMap(p);
        int pIx = pI[0];
        int pIy = pI[1];
        setPoint(pIx, pIy, c);
    }

    void drawLine(int[] line) {
        for(int i = 0; i < line.length; i += 2) {
            grid[line[i]][line[i + 1]] = 'O';
        }
    }

    char getPoint(int x, int y) {
        return grid[x][y];
    }

    boolean isPointSet(String p) {
        boolean pointSet = false;
        int[] pI = Utils.findCorInMap(p);
        int pIx = pI[0];
        int pIy = pI[1];
        if ( getPoint(pIx, pIy) == 'X') pointSet = true;
        return pointSet;
    }
}

class Ship {
    String name;
    int size;
    Coordinates coordinates;
    Coordinates xcoordinates;
    private int hits;
    private boolean sunk = false;

    Ship(String name, int size, List<Ship> objSlist) {
        this.name = name;
        this.size = size;
        coordinates = new Coordinates();
        xcoordinates = new Coordinates();
        objSlist.add(this);
    }

    String getShip() {
        return this.name;
    }

    public void addHit() {
        this.hits++;
    }

    public int getHits() {
        return hits;
    }

    public void setSunk() {
        if ( size <= hits) {
            sunk = true;
        }
    }

    public boolean isShipSunk() {
        return sunk;
    }
}

class ShipBuilder {

    List<Ship> objSlist;

    ShipBuilder(List<Ship> objSlist) {
        this.objSlist = objSlist;
    }

    enum Ships {
        Carrier (5),
        Battleship (4),
        Submarine (3),
        Cruiser (3),
        Destroyer (2)
        ;

        private final int size;

        Ships(int i) {
            this.size = i;
        }

        int getShipSize() {
            return this.size;
        }
    }

    void buildAll(String player) {
        Ship ship;

        for (Ships s : Ships.values()) {
            ship = new Ship(s.name(),s.size, objSlist);
            Utils.coordsList.add(ship.coordinates);
            if (player.equals("player1")) {
                Utils.coordsMap1.put(s.name(), ship.coordinates);
            } else if (player.equals("player2")) {
                Utils.coordsMap2.put(s.name(), ship.coordinates);
            }
        }
    }
}

class Player {
    Grid PrimeGrid, TrackingGrid;
    String name;
    List<Ship> objSlist = new ArrayList<>();

    Player(String name) {
        this.name = name;
        PrimeGrid = new Grid(name + " Primary Grid");
        PrimeGrid.populateGrid();
        TrackingGrid = new Grid(name + " Tracking Grid");
        TrackingGrid.populateGrid();
        ShipBuilder fleet1 = new ShipBuilder(objSlist);
        fleet1.buildAll(name);
    }
}

class Shooter {
    Player player;
    private int sunkCount;
    private Boolean win;
    private final List<Ship> objShipList;

    Set<Map.Entry<String, Coordinates>> coordsMapEset;
    List<String> seamapList = Arrays.asList(Utils.smrowmajor);
    String str1 = "You hit a ship!";
    String str2 = "You sank a ship! Specify a new target:";
    String str3 = "You sank the last ship. You won. Congratulations!";

    Shooter(Player player, List<Ship> objSlist) {
        this.player = player;
        this.win = false;
        this.sunkCount = 0;
        this.objShipList = objSlist;

        if (Objects.equals(this.player.name, "player1")) {
            coordsMapEset = Utils.coordsMap1.entrySet();
        } else if (Objects.equals(this.player.name, "player2")) {
            coordsMapEset = Utils.coordsMap2.entrySet();
        }
    }

    void setWin() {
        win = true;
    }

    void increaseSunkCount() {
        sunkCount++;
    }

    int getSunkCount() {
        return sunkCount;
    }

    boolean isWin() {
        return win;
    }

    void setHit(int shipCount) {
        objShipList.get(shipCount).addHit();  // add a hit to this ship
        objShipList.get(shipCount).setSunk();  // if all coords hit, set sunk
        if (objShipList.get(shipCount).isShipSunk()) {
            increaseSunkCount();
        }
        if (sunkCount == 5) {
            setWin();
        }
        if (objShipList.get(shipCount).isShipSunk() && isWin()) {
            System.out.println(str3);
            Seabattle.gameOver = true;
            System.exit(0);
        } else  if (objShipList.get(shipCount).isShipSunk() && !isWin()) {
            System.out.println(str2);
        } else {
            System.out.println(str1);
        }
    }

    void markHit(String p) {
        player.PrimeGrid.setPoint(p, 'X');
        player.TrackingGrid.setPoint(p,'X');
    }

    void markMiss(String p) {
        player.PrimeGrid.setPoint(p, 'M');
        player.TrackingGrid.setPoint(p,'M');
        System.out.println("You missed!");
    }

    void takeShot() {
        int shipCount = 0;
        boolean isShot = false;

        String uis = uiGet();

        for ( Map.Entry<String, Coordinates> kv : coordsMapEset) {
            if (isShot) break;
            if(kv.getValue().getCoords().contains(uis)) {
                markHit(uis);
                setHit(shipCount);
                kv.getValue().getXcoords().add(uis);
                kv.getValue().getCoords().remove(uis);
                isShot = true;
            }
            shipCount++;
        }
        if (!isShot) {
            if (!player.TrackingGrid.isPointSet(uis)) {
                markMiss(uis);
            } else {
                System.out.println("You hit a ship!");
            }
        }
    }

    String uiGet() {
        Scanner scanner = new Scanner(System.in);
        String shot = "";
        boolean shotOK = false;

        while (!shotOK) {
            shot = scanner.next();
            if (shot.length() > 3 || shot.length() < 2) {
                System.out.println("\nError! Incorrect Entry! Try again:\n");
                continue;
            }
            if (!(seamapList.contains(shot))) {
                System.out.println("\nError! You entered the wrong coordinates! Try again:\n");
                continue;
            }
            shotOK = true;
            System.out.println();
        }
        return shot;
    }
}

class Coordinates {
    private final List<String> coords = new ArrayList<>();
    private final List<String> xcoords = new ArrayList<>();
    List<String> getCoords() {
        return coords;
    }
    List<String> getXcoords() {
        return xcoords;
    }

    void showCoordinates() {
        System.out.printf("%s %s%n", "showCoordinates: ", coords);
    }

    void addAll(List<String> inPoints) {
        coords.addAll(inPoints);
    }

     public void addSingle(String tpoint) {
        coords.add(tpoint);
    }
}

class Point {
    String sPoint;
    int[] cPoint;
    int x, y;

    Point(String sPoint) {
        this.sPoint = sPoint;
        this.cPoint = Utils.findCorInMap(sPoint);
        this.x = cPoint[0];
        this.y = cPoint[1];
    }

    boolean isTouching(Point other) {
        double xdiff = x - other.x;
        double ydiff = y - other.y;
        int distance = (int) Math.sqrt(xdiff * xdiff + ydiff * ydiff);
        return (distance <= 1 && Utils.currentPlayer.PrimeGrid.getPoint(other.x, other.y) == 'O');
    }
}

class Utils {
    static String[] smrowmajor;
    static String[] smcolumnmajor;
    static String[][] seamap;
    static String[] r = new String[] {"A","B","C","D","E","F","G","H","I","J"};
    static String[] c = new String[] {"1","2","3","4","5","6","7","8","9","10"};
    enum orientation {VERTICAL, HORIZONTAL, DIAGONAL}
    static orientation currentOrientation = null;
    static int currentSize = 0;
    static String currentShip = null;
    static Player currentPlayer = null;
    static List<Coordinates> coordsList = new ArrayList<>();
    static LinkedHashMap<String, Coordinates> coordsMap1 = new LinkedHashMap<>(); // testing
    static LinkedHashMap<String, Coordinates> coordsMap2 = new LinkedHashMap<>(); //testing
    static int cycle;

    static void populateMap() {
        int k = 0;
        int rows = 10, columns = 10;
        seamap = new String[rows][columns];
        smrowmajor = new String[100];
        smcolumnmajor = new String[100];

        for (int i = 0; i < rows; i++) {
            for ( int j = 0; j < columns; j++, k++) {
                seamap[i][j] = String.format("%s%s", r[i],c[j]);
                smrowmajor[k] = String.format("%s%s", r[i],c[j]);
                smcolumnmajor[k] = String.format("%s%s", r[j],c[i]);
            }
        }
    }

    static String getSqInMap(int x, int y) {
        return seamap[x][y];
    }

    static int[] findCorInMap(String cell) {
        boolean found = false;
        int[] cellcoords = new int[] {-1,-1};
        for (int i = 0; i < seamap.length; i++) {
            if (found) break;
            for (int j = 0; j < seamap[i].length; j++) {
                if (found) break;
                if (cell.equals(seamap[i][j])) {
                    cellcoords = new int[] {i,j};
                    found = true;
                }
            }
        }
        return cellcoords;
    }

    // Natural selection
    static String[] sort(String[] range) {
        String[] sorted = new String[] {range[0], range[1]};
        int r1 = 0;
        int r2 = 0;

        for (int i = 0; i < 100; i++) {
            if (smrowmajor[i].equals(range[0])) { r1 = i;}
            if (smrowmajor[i].equals(range[1])) { r2 = i;}
            if (r1 != 0 && r2 != 0) { break;}
        }
        if (r1 > r2) {
            sorted[0] = range[1];
            sorted[1] = range[0];
        }
        return sorted;
    }

    static int getCount(int x1, int y1, int x2, int y2){
        double xdiff = x1 - x2;
        double ydiff = y1 - y2;
        int distance = (int) Math.sqrt(xdiff * xdiff + ydiff * ydiff);
        return distance + 1;
    }

    static void setOrientation(int x1, int y1, int x2, int y2){
        if (x1 == x2) {
            currentOrientation = orientation.HORIZONTAL;
        }
        else if (y1 == y2) {
            currentOrientation = orientation.VERTICAL;
        }
        else {
            currentOrientation = orientation.DIAGONAL;
        }
    }

    static boolean isTouchingTest (String ui) {
        Point cp = new Point(ui);
        Point tp;
        String tpS;
        int cx = cp.x;  // central point
        int cy = cp.y;
        int tx = cx;    //test point
        int ty = cy;
        boolean touching = false;

        for(int i = 1; i <= 8; i++) {
            if (touching) break;
            switch (i) {
                case 1:
                    tx -= 1; ty -= 1;
                    if (tx < 0 || tx > 9 || ty < 0 || ty > 9) break;
                    tpS = getSqInMap(tx, ty);
                    tp = new Point(tpS);
                    if (cp.isTouching(tp)) {
                        touching = true;
                    }
                    break;
                case 2:
                case 3:
                    ty += 1;
                    if (tx < 0 || tx > 9 || ty < 0 || ty > 9) break;
                    tpS = getSqInMap(tx, ty);
                    tp = new Point(tpS);
                    if (cp.isTouching(tp)) {
                        touching = true;
                    }
                    break;
                case 4:
                case 5:
                    tx += 1;
                    if (tx < 0 || tx > 9 || ty < 0 || ty > 9) break;
                    tpS = getSqInMap(tx, ty);
                    tp = new Point(tpS);
                    if (cp.isTouching(tp)) {
                        touching = true;
                    }
                    break;
                case 6:
                case 7:
                    ty -= 1;
                    if (tx < 0 || tx > 9 || ty < 0 || ty > 9) break;
                    tpS = getSqInMap(tx, ty);
                    tp = new Point(tpS);
                    if (cp.isTouching(tp)) {
                        touching = true;
                    }
                    break;
                case 8:
                    tx -= 1;
                    if (tx < 0 || tx > 9 || ty < 0 || ty > 9) break;
                    tpS = getSqInMap(tx, ty);
                    tp = new Point(tpS);
                    if (cp.isTouching(tp)) {
                        touching = true;
                    }
                    break;
            }
        }
        return touching;
    }

    static boolean uiTest(String[] ui) {
        boolean uiOk = true;
        int[] c1 = Utils.findCorInMap(ui[0]);
        int[] c2 = Utils.findCorInMap(ui[1]);
        int uiInCount = getCount(c1[0], c1[1], c2[0], c2[1]);
        setOrientation(c1[0], c1[1], c2[0], c2[1]);  //setOrientation
        boolean touching = isTouchingTest(ui[0]) || isTouchingTest(ui[1]);

        if (!(uiInCount == currentSize)) {
            System.out.println("Error! Wrong length of the " + currentShip
                                + "! Try again:" + '\n');
            uiOk = false;
        }

        if ((c1[0] == -1 || c1[1] == -1 || c2[0] == -1 || c2[1] == -1)) {
            System.out.println("Error! Bad coordinates: Try again:" + '\n');
            uiOk = false;
        }

        if (!(c1[0] == c2[0] || c1[1] == c2[1])) {
            System.out.println("Error! Wrong ship location! Try again:" + '\n');
            uiOk = false;
        }

        if (touching) {
            System.out.println("Error! You placed it too close to another one. Try again:" + '\n');
            uiOk = false;
        }

        return uiOk;
    }

    static String[] getUI() {
        Scanner scanner = new Scanner(System.in);
        String[] ui = {"",""};
        boolean uiOk = false;

        System.out.println("\nEnter the coordinates of the " + currentShip + " (" + currentSize + " cells):" + '\n');
        while (!uiOk) {
            ui = scanner.nextLine().split("\\s+");
            if(ui.length != 2) {
                continue;
            }
            System.out.println();
            ui = Utils.sort(ui);
            uiOk = uiTest(ui);
        }
        return ui;
    }

    static int[] getShipPlacement(String[] sorted) {
        List<String> majorList;
        List<String> subList;
        List<int[]> intArrayList = new ArrayList<>();
        List<Integer> intList = new ArrayList<>();

        if (currentOrientation == orientation.HORIZONTAL) {
            majorList = Arrays.asList(smrowmajor);
        } else {
            majorList = Arrays.asList(smcolumnmajor);
        }
        int listIndexI =  majorList.indexOf(sorted[0]);
        int listIndexJ =  majorList.indexOf(sorted[1]);
        subList = majorList.subList(listIndexI, listIndexJ + 1);

        //used to create return intArray
        int subListIndex = 0;
        for (String ignored : subList) {
            int[] tpoint = findCorInMap(subList.get(subListIndex));
            intArrayList.add(tpoint);
            int[] tmpIntArray = intArrayList.get(subListIndex);
            intList.add(tmpIntArray[0]);
            intList.add(tmpIntArray[1]);
            subListIndex++;
        }

        //conversion: intList to intArray for return
        int[] intArray = new int[intList.size()];
        for(int i  = 0; i < intList.size(); i++) {
            int j = intList.get(i);
            intArray[i] = j;
        }

        // update currentCoord with subList
        Coordinates currentCoord = coordsList.get(cycle);
        cycle++;
        currentCoord.addAll(subList);
        return intArray;
    }

    static void SetUp(Player player) {
        populateMap();
        currentPlayer = player;
        for ( ShipBuilder.Ships s : ShipBuilder.Ships.values()) {
            currentSize = s.getShipSize();
            currentShip = s.name();
            if(currentShip.equals("Carrier")) {  //rename to Aircraft Carrier
                currentShip = "Aircraft Carrier";
            }
            int[] shipLine = getShipPlacement(getUI());
            player.PrimeGrid.drawLine(shipLine);
            player.PrimeGrid.showGrid();
        }
    }

    public static void promptEnterKey() {

        System.out.println("Press Enter and pass the move to another player");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void clearConsole() {
        final String os = System.getProperty("os.name");
        if (os.contains("Windows")) {
            try {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                Runtime.getRuntime().exec("clear");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class Seabattle {
    public static boolean gameOver = false;
    public static void main(String[] args) {

        Player player1 = new Player("player1");
        Player player2 = new Player("player2");
        System.out.println("Player 1, place your ships on the game field\n");
        player1.PrimeGrid.showGrid();
        Utils.SetUp(player1);
        System.out.println();
        Utils.promptEnterKey();
        System.out.println("Player 2, place your ships on the game field\n");
        player2.PrimeGrid.showGrid();
        Utils.SetUp(player2);
        System.out.println(); // used for formatting output
        Shooter shooter1 = new Shooter(player2, player2.objSlist);  //shooter1 shoots at player2
        Shooter shooter2 = new Shooter(player1, player1.objSlist );

        while (!gameOver) {
            Utils.promptEnterKey();
            //Utils.clearConsole();  //works when run on the OS; not in IDE
            player2.TrackingGrid.showGrid();
            System.out.println("---------------------");
            player1.PrimeGrid.showGrid();
            System.out.println("\nPlayer 1, it's your turn:\n");
            shooter1.takeShot();

            Utils.promptEnterKey();
            //Utils.clearConsole();
            player1.TrackingGrid.showGrid();
            System.out.println("---------------------");
            player2.PrimeGrid.showGrid();
            System.out.println("\nPlayer 2, it's your turn:\n");
            shooter2.takeShot();
        }
    }
}