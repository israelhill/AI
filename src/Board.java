import java.util.ArrayList;
import java.util.Arrays;

public class Board {
    private int h;
    private int g;
    private int f;
    private char[][] boardState;
    private Board parent;
    private String directionMoved;
    private int blankRow;
    private int blankColumn;
    private String heuristicType;
    private String algorithmType;
    public static final char[][] GOAL = {{'b', '1', '2'}, {'3', '4', '5'}, {'6', '7', '8'}};

    public Board(char[][] state) {
        this.boardState = state;
        this.f = 0;
        this.h = 0;
        this.g = 0;
    }

    public Board() {
        this.boardState = new char[3][3];
        this.f = 0;
        this.h = 0;
        this.g = 0;
    }

    public Board(char[][] state, int g, Board parent) {
        this.boardState = state;
        setHeuristicType(parent.getHeuristicType());
        setAlgorithmType(parent.getAlgorithmType());
        this.g = g;
        this.h = computeHeuristic();
        setF(g, h);
        this.parent = parent;
        directionMoved = null;
    }

    public void setDirectionMoved(String direction) {
        this.directionMoved = direction;
    }

    public String getDirectionMoved() {
        return this.directionMoved;
    }

    public void setHeuristicType(String heuristicType) {
        this.heuristicType = heuristicType;
    }

    public String getHeuristicType() {
        return this.heuristicType;
    }

    public void setAlgorithmType(String algorithmType) {
        this.algorithmType = algorithmType;
    }

    public String getAlgorithmType() {
        return this.algorithmType;
    }

    public void clearData() {
        this.f = 0;
        this.h = 0;
        this.g = 0;
        this.parent = null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Board)) {
            return false;
        }

        Board board = (Board) o;
        return this.hashCode() == board.hashCode();
    }

    @Override
    public int hashCode() {
        return Arrays.deepToString(this.boardState).hashCode();
    }

    public Board getParent() {
        return this.parent;
    }

    public int computeHeuristic() {
        if(heuristicType.equals("h1")) {
            return this.goalOffset();
        }
        else {
            return this.computeSumOfManhattan();
        }
    }

    /**
     * Heuristic h1: compute the number of tiles in incorrect positions
     * @return the number of tiles out of place
     */
    public int goalOffset() {
        int offset = 0;
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                if(boardState[i][j] != GOAL[i][j] && boardState[i][j] != 'b') {
                    offset++;
                }
            }
        }
        return offset;
    }

    /**
     * Heuristic h2: compute the Manhattan distance between two points
     * @return manhattan distance
     */
    public int computeSumOfManhattan() {
        int totalSum = 0;
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                char currentChar = boardState[i][j];
                if(currentChar != 'b') {
                    totalSum += computeManhattanDistance(currentChar, i, j);
                }
            }
        }
        return totalSum;
    }

    /**
     * Compute the sum of all manhattan distances
     * @param num
     * @param x1
     * @param y1
     * @return
     */
    public int computeManhattanDistance(char num, int x1, int y1) {
        int x0 = getX(num);
        int y0 = getY(num);
        return Math.abs(x1 - x0) + Math.abs(y1 - y0);
    }

    /**
     * get the X-Coordinate for a given tile
     * @param num
     * @return
     */
    public int getX(char num) {
        for(int i = 0; i < 3; i++) {
            for(int j = 0 ; j < 3; j++) {
                if(GOAL[i][j] == num) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * get the Y-Coordinate for a given tile
     * @param num
     * @return
     */
    public int getY(char num) {
        for(int i = 0; i < 3; i++) {
            for(int j = 0 ; j < 3; j++) {
                if(GOAL[i][j] == num) {
                    return j;
                }
            }
        }
        return -1;
    }

    /**
     * Given a string representing the board state, create a
     * 2-D array representation of the board.
     * @param state string representation of the board
     */
    public void setState(String state) {
        String stateNoSpaces = state.replace(" ", "");
        char[] characters = stateNoSpaces.toCharArray();

        int currentCharacterIndex = 0;
        for(int r = 0; r < 3; r++) {
            for(int c = 0; c < 3; c++) {
                this.boardState[r][c] = characters[currentCharacterIndex];
                if(this.boardState[r][c] == 'b') {
                    blankRow = r;
                    blankColumn = c;
                }
                currentCharacterIndex++;
            }
        }
    }

    public int getF() {
        return this.f;
    }

    public void setF(int g, int h) {
        this.f = g + h;
    }

    public void setG(int g) {
        this.g = g;
    }

    public int getG() {
        return this.g;
    }

    public void setH(int h) {
        this.h = h;
    }

    public int getH() {
        return this.h;
    }

    /**
     * Locate and set the coordinates of the blank tile
     */
    public void findBlank() {
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                if(boardState[i][j] == 'b') {
                    blankRow = i;
                    blankColumn = j;
                }
            }
        }
    }

    /**
     * Get all the valid board states reachable from the parent board
     * @return a list of valid boards
     */
    public ArrayList<Board> getValidChildren() {
        ArrayList<Board> children = new ArrayList<>();
        if(this.isLegalMove("up")) {
            children.add(this.moveUp());
        }

        if(this.isLegalMove("down")) {
            children.add(this.moveDown());
        }

        if(this.isLegalMove("left")) {
            children.add(this.moveLeft());
        }

        if(this.isLegalMove("right")) {
            children.add(this.moveRight());
        }

        return children;
    }

    public char[][] copyArray(char[][] state) {
        char[][] retVal = new char[3][3];
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                retVal[i][j] = state[i][j];
            }
        }
        return retVal;
    }

    public Board move(String direction) {
        Board retVal = null;
        findBlank();
        if(isLegalMove(direction)) {
            switch(direction) {
                case "up": {
                    retVal = moveUp();
                    break;
                }
                case "down": {
                    retVal = moveDown();
                    break;
                }
                case "left": {
                    retVal = moveLeft();
                    break;
                }
                case "right": {
                    retVal = moveRight();
                    break;
                }
                default: {
                    System.out.println("Direction not recognized.");
                    System.exit(0);
                }
            }

        }
        else {
            System.out.println("This is not a valid move!");
            retVal = this;
        }
        return retVal;
    }

    public Board moveUp() {
        findBlank();
        char[][] boardCopy = copyArray(boardState);
        char temp = boardCopy[blankRow - 1][blankColumn];
        boardCopy[blankRow - 1][blankColumn] = boardCopy[blankRow][blankColumn];
        boardCopy[blankRow][blankColumn] = temp;

        if(algorithmType == null) {
            return new Board(boardCopy);
        }
        else if(algorithmType.equals("beam")) {
            Board b = new Board(boardCopy, 0, this);
            b.setDirectionMoved("up");
            return b;
        }
        else {
            Board b = new Board(boardCopy, this.g + 1, this);
            b.setDirectionMoved("up");
            return b;
        }
    }

    public Board moveDown() {
        findBlank();
        char[][] boardCopy = copyArray(boardState);
        char temp = boardCopy[blankRow + 1][blankColumn];
        boardCopy[blankRow + 1][blankColumn] = boardCopy[blankRow][blankColumn];
        boardCopy[blankRow][blankColumn] = temp;

        if(algorithmType == null) {
            return new Board(boardCopy);
        }
        else if(algorithmType.equals("beam")) {
            Board b = new Board(boardCopy, 0, this);
            b.setDirectionMoved("down");
            return b;
        }
        else {
            Board b = new Board(boardCopy, this.g + 1, this);
            b.setDirectionMoved("down");
            return b;
        }
    }

    public Board moveLeft() {
        findBlank();
        char[][] boardCopy = copyArray(boardState);
        char temp = boardCopy[blankRow][blankColumn - 1];
        boardCopy[blankRow][blankColumn - 1] = boardCopy[blankRow][blankColumn];
        boardCopy[blankRow][blankColumn] = temp;

        if(algorithmType == null) {
            return new Board(boardCopy);
        }
        else if(algorithmType.equals("beam")) {
            Board b = new Board(boardCopy, 0, this);
            b.setDirectionMoved("left");
            return b;
        }
        else {
            Board b = new Board(boardCopy, this.g + 1, this);
            b.setDirectionMoved("left");
            return b;
        }
    }

    public Board moveRight() {
        findBlank();
        char[][] boardCopy = copyArray(boardState);
        char temp = boardCopy[blankRow][blankColumn + 1];
        boardCopy[blankRow][blankColumn  + 1] = boardCopy[blankRow][blankColumn];
        boardCopy[blankRow][blankColumn] = temp;

        if(algorithmType == null) {
            return new Board(boardCopy);
        }
        else if(algorithmType.equals("beam")) {
            Board b = new Board(boardCopy, 0, this);
            b.setDirectionMoved("right");
            return b;
        }
        else {
            Board b = new Board(boardCopy, this.g + 1, this);
            b.setDirectionMoved("right");
            return b;
        }
    }

    /**
     * Given a direction, check if moving in that direction is possible
     * @param direction
     * @return whether or not the move is legal
     */
    public boolean isLegalMove(String direction) {
        boolean isLegal;
        findBlank();
        switch(direction) {
            case "up": isLegal = blankRow != 0;
                break;
            case "down": isLegal = blankRow != 2;
                break;
            case "left": isLegal = blankColumn != 0;
                break;
            case "right": isLegal = blankColumn != 2;
                break;
            default:isLegal = false;
                break;
        }

        return isLegal;
    }

    /**
     * Print a board to standard output
     */
    public void printBoard() {
        for(int i = 0; i < 3; i++) {
            for(int j = 0 ; j < 3; j++) {
                System.out.print(boardState[i][j] + " ");
            }
            System.out.println();
        }
    }
}
