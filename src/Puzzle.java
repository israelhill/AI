import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.stream.Stream;

public class Puzzle {
    private Board board;
    private static PriorityQueue<Board> queue = new PriorityQueue<>((a, b) -> {
        if(a.getF() == b.getF()) {
            return 0;
        }
        else if(a.getF() > b.getF()) {
            return 1;
        }
        else {
            return -1;
        }
    });
    private static HashMap<Board, Board> closed = new HashMap<>();

    public Puzzle(String state) {
        board = new Board(state);
    }

    /**
     * Solve the puzzle using the A* algorithm
     * @return solved puzzle board
     */
    public Board solvePuzzleAStar() {
        board.setG(0);
        board.setH(board.goalOffset());
        queue.offer(board);
        Board solutionBoard = null;

        while(!queue.isEmpty()) {
            Board current = queue.poll();

            if(current.goalOffset() == 0) {
                solutionBoard = current;
                break;
            }

            // get all the next possible board positions
            ArrayList<Board> children = current.getValidChildren();
            for(Board child : children) {
                boolean addToQueue = true;

                // if this board position has benn encountered already with a lower cost estimate,
                // skip this board
                if(queue.contains(child)) {
                    Board duplicateBoard = queue.remove();
                    if(duplicateBoard.getF() < child.getF()) {
                        queue.offer(duplicateBoard);
                        addToQueue = false;
                    }
                }
                else if(closed.containsKey(child)) {
                    Board duplicateBoard = closed.get(child);
                    if(duplicateBoard.getF() < child.getF()) {
                        addToQueue = false;
                    }
                }

                // if this is our first time encountering this board position,
                // or this board has a lower cost estimate, add it to the queue
                if(addToQueue) {
                    queue.offer(child);
                }
            }
            closed.put(current, current);
        }
        return solutionBoard;
    }

    /**
     * Read commands from a text file
     * @param fileName
     * @throws IOException
     */
    public static void readCommandsFromFile(String fileName) throws IOException {
        Stream<String> stream = Files.lines(Paths.get(fileName));
            stream.forEach(System.out::println);
    }

    public static void main(String[] args) {
//        Puzzle p = new Puzzle("312 6b4 785");
//        Board solution = p.solvePuzzleAStar();
//        solution.printBoard();
//        System.out.println();
//
//        Board backtrace = solution;
//        while(backtrace.getParent() != null) {
//            backtrace.getParent().printBoard();
//            System.out.println();
//            backtrace = backtrace.getParent();
//        }

        if(args[0].equals("readFile")) {
            String fileName = args[1];
            try {
                readCommandsFromFile(fileName);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
