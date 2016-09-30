import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.stream.Stream;

public class Puzzle {
    public static String HEURISTIC_TYPE;
    public static String ALGORITHM_TYPE;
    private Board foundGoal = null;
    private static Board interactiveBoard;
    private int maxNodes = Integer.MAX_VALUE;

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

    public void setMaxNodes(int num) {
        this.maxNodes = num;
    }

    public int getMaxNodes() {
        return this.maxNodes;
    }

    /**
     * Solve the puzzle using the A* algorithm
     * @return solved puzzle board
     */
    public Board solvePuzzleAStar(String heuristicType) {
        queue.clear();
        setAlgorithmType("astar");
        setHeuristicType(heuristicType);
        Board board = getInteractiveBoard();
        board.setG(0);
        board.setH(board.computeSumOfManhattan());

        queue.offer(board);
        Board solutionBoard = null;
        int nodesVisited = 0;

        while(!queue.isEmpty()) {
            Board current = queue.poll();
            nodesVisited++;

            if(nodesVisited > maxNodes) {
                System.out.println("ERROR: Algorithm has considered " + Integer.toString(nodesVisited) + " this exceeds" +
                        " the amount specified in maxNodes.");
                System.exit(0);
            }

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

    public Board beamSearch(int k) {
        setAlgorithmType("beam");
        setHeuristicType("h2");
        queue.clear();
        int nodesVisited = 0;
        Board goal = null;
        ArrayList<Board> bestBoards;
        Board board = getInteractiveBoard();
        ArrayList<Board> children = board.getValidChildren();


        if(children.size() < k) {
            bestBoards = children;
        }
        else {
            bestBoards = getKBestBoards(k, queue, children);
        }

        nodesVisited += bestBoards.size();


        while(!bestBoards.isEmpty()) {
            if(nodesVisited > maxNodes) {
                System.out.println("ERROR: Algorithm has considered " + Integer.toString(nodesVisited) + " this exceeds" +
                        " the amount specified in maxNodes.");
                System.exit(0);
            }

            ArrayList<Board> allSuccessors = generateAllSuccessors(bestBoards);
            if(foundGoal != null) {
                goal = foundGoal;
                break;
            }
            bestBoards = getKBestBoards(k, queue, allSuccessors);
            nodesVisited += bestBoards.size();
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

    public static void printSolution(Board solvedBoard) {
        while(solvedBoard.getParent() != null) {
            solvedBoard.getParent().printBoard();
            System.out.println();
            solvedBoard = solvedBoard.getParent();
        }
    }

    public void setBoardState(String state) {
        interactiveBoard.setState(state);
    }

    public static Board getInteractiveBoard() {
        return interactiveBoard;
    }

    public static void checkForNullBoard() {
        if(getInteractiveBoard() == null) {
            System.out.println("You have not set the board state yet.");
            System.exit(0);
        }
    }




    public static void main(String[] args) {
        Puzzle p = new Puzzle();
        Board solution;
        while(true) {
            Scanner s = new Scanner(System.in);
            String inputString = s.nextLine();
            String[] inputs = inputString.split(" ");
            String command = inputs[0];

            switch(command) {
                case "solve": {
                    String algorithm = inputs[1];
                    String arg = inputs[2];
                    if(algorithm.equals("beam")) {
                        int k = Integer.parseInt(arg);
                        checkForNullBoard();
                        System.out.println("Solving puzzle using Beam search algorithm....");
                        solution = p.beamSearch(k);
                    }
                    else if(algorithm.equals("A-star")){
                        String heuristic = arg;
                        checkForNullBoard();
                        System.out.println("Solving puzzle using A* algorithm....");
                        solution = p.solvePuzzleAStar(heuristic);
                    }
                    else {
                        System.out.println("Search algorithm not recognized. Check spelling.");
                        System.exit(0);
                    }
                }
                case "setState": {
                    String state = inputs[1];
                    if(state.length() != 11) {
                        System.out.println("Board state is invalid. Check state again.");
                        System.exit(0);
                    }
                    p.setBoardState(state);
                }
                case "printState": {
                    checkForNullBoard();
                    interactiveBoard.printBoard();
                }
                case "move": {
                    String direction = inputs[1];
                    checkForNullBoard();
                    interactiveBoard = interactiveBoard.move(direction);
                }
            }
        }




//        Board solution = p.solvePuzzleAStar("h2", "b12 643 785");
//        Board solution = p.beamSearch(500, "b12 643 785");
//        solution.printBoard();
//        System.out.println();
//
//        Board backtrace = solution;
//        printSolution(backtrace);


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
