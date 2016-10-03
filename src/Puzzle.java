import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class Puzzle {
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

    public Puzzle() {
        interactiveBoard = new Board();
    }

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
        interactiveBoard.setAlgorithmType("astar");
        interactiveBoard.setHeuristicType(heuristicType);
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
        interactiveBoard.setAlgorithmType("beam");
        interactiveBoard.setHeuristicType("h2");
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

    public static String generateRandomDirection() {
        String[] directions = {"left", "right", "up", "down"};
        int min = 0;
        int max = 3;
        int randomIndex = min + (int)(Math.random() * ((max - min) + 1));
        return directions[randomIndex];
    }

    public static void printSolution(Board solvedBoard) {
        ArrayList<String> directions = new ArrayList<>();

        while(solvedBoard != null) {
            if(solvedBoard.getParent() != null) {
                directions.add(solvedBoard.getDirectionMoved());
            }
            solvedBoard = solvedBoard.getParent();
        }

        Collections.reverse(directions);
        int numMoves = directions.size();
        System.out.println();
        System.out.println("Number of moves made to solve: " + String.valueOf(numMoves));
        for(String d : directions) {
            System.out.println(d);
        }


        // print the board states

//        System.out.println();
//        System.out.println();
//        while(solvedBoard != null) {
//            solvedBoard.printBoard();
//            System.out.println();
//            solvedBoard = solvedBoard.getParent();
//        }
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
        Scanner s = new Scanner(System.in);
        while(true) {
            System.out.println("Enter a command:");
            String inputString = s.nextLine();
            String[] inputs = inputString.split(" ");
            String command = inputs[0];

            switch(command) {
                case "solve":
                    String algorithm = inputs[1];
                    String arg = inputs[2];
                    switch (algorithm) {
                        case "beam":
                            int k = Integer.parseInt(arg);
                            checkForNullBoard();
                            System.out.println("Solving puzzle using Beam search algorithm....");
                            solution = p.beamSearch(k);
                            printSolution(solution);
                            break;
                        case "A-star":
                            String heuristic = arg;
                            checkForNullBoard();
                            System.out.println("Solving puzzle using A* algorithm....");
                            solution = p.solvePuzzleAStar(heuristic);
                            printSolution(solution);
                            break;
                        default:
                            System.out.println("Search algorithm not recognized. Check spelling.");
                            System.exit(0);
                            break;
                    }
                    break;
                case "setState": {
                    String state = inputs[1] + " " + inputs[2] + " " + inputs[3];
                    if(state.length() != 11) {
                        System.out.println("Board state is invalid. Check state again.");
                        System.exit(0);
                    }
                    p.setBoardState(state);
                    break;
                }
                case "printState": {
                    checkForNullBoard();
                    interactiveBoard.printBoard();
                    break;
                }
                case "move": {
                    String direction = inputs[1];
                    checkForNullBoard();
                    interactiveBoard = interactiveBoard.move(direction);
                    break;
                }
                case "exit": {
                    System.exit(1);
                    break;
                }
                default: {
                    System.out.println("Command not recognized.");
                    break;
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
