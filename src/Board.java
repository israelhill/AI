import java.util.ArrayList;
import java.util.Arrays;

public class Board {
    private int h;
    private int g;
    private int f;
    private char[][] boardState;
    private Board parent;
    private int blankRow;
    private int blankColumn;
    private final char[][] GOAL = {{'b', '1', '2'}, {'3', '4', '5'}, {'6', '7', '8'}};

    public Board(String state) {
        this.boardState = new char[3][3];
        setState(state);
    }

    public Board(char[][] state, int g, Board parent) {
        this.boardState = state;
        this.g = g;
        this.h = goalOffset();
        setF(g, h);
        this.parent = parent;
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

    public int goalOffset() {
        int offset = 0;
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                if(boardState[i][j] != GOAL[i][j]) {
                    offset++;
                }
            }
        }
        return offset;
    }

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

    public Board moveUp() {
        findBlank();
        char[][] boardCopy = copyArray(boardState);
        char temp = boardCopy[blankRow - 1][blankColumn];
        boardCopy[blankRow - 1][blankColumn] = boardCopy[blankRow][blankColumn];
        boardCopy[blankRow][blankColumn] = temp;
        return new Board(boardCopy, this.g + 1, this);
    }

    public Board moveDown() {
        findBlank();
        char[][] boardCopy = copyArray(boardState);
        char temp = boardCopy[blankRow + 1][blankColumn];
        boardCopy[blankRow + 1][blankColumn] = boardCopy[blankRow][blankColumn];
        boardCopy[blankRow][blankColumn] = temp;
        return new Board(boardCopy, this.g + 1, this);
    }

    public Board moveLeft() {
        findBlank();
        char[][] boardCopy = copyArray(boardState);
        char temp = boardCopy[blankRow][blankColumn - 1];
        boardCopy[blankRow][blankColumn - 1] = boardCopy[blankRow][blankColumn];
        boardCopy[blankRow][blankColumn] = temp;
        return new Board(boardCopy, this.g + 1, this);
    }

    public Board moveRight() {
        findBlank();
        char[][] boardCopy = copyArray(boardState);
        char temp = boardCopy[blankRow][blankColumn + 1];
        boardCopy[blankRow][blankColumn  + 1] = boardCopy[blankRow][blankColumn];
        boardCopy[blankRow][blankColumn] = temp;
        return new Board(boardCopy, this.g + 1, this);
    }

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

    public void printBoard() {
        for(int i = 0; i < 3; i++) {
            for(int j = 0 ; j < 3; j++) {
                if(boardState[i][j] == 'b') {
                    System.out.print("_ ");
                }
                else {
                    System.out.print(boardState[i][j] + " ");
                }
            }
            System.out.println();
        }
    }
}
