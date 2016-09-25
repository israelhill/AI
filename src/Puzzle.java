import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.stream.Stream;

public class Puzzle {
    public static String HEURISTIC_TYPE;
    public static String ALGORITHM_TYPE;
    private Board foundGoal = null;

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

    /**
     * Solve the puzzle using the A* algorithm
     * @return solved puzzle board
     */
    public Board solvePuzzleAStar(String heuristicType, String boardState) {
        queue.clear();
        setHeuristicType(heuristicType);
        Board board = new Board(boardState);
        board.setG(0);
        board.setH(board.computeSumOfManhattan());

        queue.offer(board);
        Board solutionBoard = null;

        while(!queue.isEmpty()) {
            Board current = queue.poll();

            if(current.computeHeuristic() == 0) {
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

    public Board beamSearch(int k, String boardState) {
        setAlgorithmType("beam");
        setHeuristicType("h2");
        queue.clear();
        Board goal = null;
        ArrayList<Board> bestBoards;
        Board board = new Board(boardState);
        ArrayList<Board> children = board.getValidChildren();

        if(children.size() < k) {
            bestBoards = children;
        }
        else {
            bestBoards = getKBestBoards(k, queue, children);
        }

        while(!bestBoards.isEmpty()) {
            ArrayList<Board> allSuccessors = generateAllSuccessors(bestBoards);
            if(foundGoal != null) {
                goal = foundGoal;
                break;
            }
            bestBoards = getKBestBoards(k, queue, allSuccessors);
        }

        return goal;
    }

    public ArrayList<Board> getKBestBoards(int k, PriorityQueue<Board> q, ArrayList<Board> boards) {
        ArrayList<Board> best = new ArrayList<>();
        for(Board b : boards) {
            q.offer(b);
        }

        int count = 0;
        while(count < k && !q.isEmpty()) {
            Board current = q.poll();
            best.add(current);
            count++;
        }
        return best;
    }

    public ArrayList<Board> generateAllSuccessors(ArrayList<Board> oldBest) {
        ArrayList<Board> allBoards = new ArrayList<>();
        for(Board b : oldBest) {
            if(b.computeHeuristic() == 0) {
                foundGoal = b;
                break;
            }
            ArrayList<Board> children = b.getValidChildren();
            allBoards.addAll(children);
        }
        return allBoards;
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

    public static void setHeuristicType(String heuristicType) {
        HEURISTIC_TYPE = heuristicType;
    }

    public static void setAlgorithmType(String algorithmType) {
        ALGORITHM_TYPE = algorithmType;
    }

    public static String generateRandomDirection() {
        String[] directions = {"left", "right", "up", "down"};
        int min = 0;
        int max = 3;
        int randomIndex = min + (int)(Math.random() * ((max - min) + 1));
        return directions[randomIndex];
    }

    public static void main(String[] args) {
        Puzzle p = new Puzzle();
//        Board solution = p.solvePuzzleAStar("h2", "724 5b6 831");
        Board solution = p.beamSearch(500, "724 5b6 831");
        solution.printBoard();
        System.out.println();

        Board backtrace = solution;
        while(backtrace.getParent() != null) {
            backtrace.getParent().printBoard();
            System.out.println();
            backtrace = backtrace.getParent();
        }

//        if(args[0].equals("readFile")) {
//            String fileName = args[1];
//            try {
//                readCommandsFromFile(fileName);
//            }
//            catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }
}
