import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class Puzzle {
    private Board foundGoal = null;
    private Board interactiveBoard;
    private int maxNodes = Integer.MAX_VALUE;
    private long SEED = 1234;
    // Seeding the random number generator so that it always returns the same stream of random numbers
    private Random randomNumGen = new Random(SEED);
    private boolean expExceedMax = false;

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
    private HashMap<Board, Board> closed = new HashMap<>();

    public Puzzle() {
        this.interactiveBoard = new Board();
    }

    public Puzzle(Board b) {
        this.interactiveBoard = b;
        b.clearData();
    }

    public void setMaxNodes(int num) {
        maxNodes = num;
    }

    public int getMaxNodes() {
        return maxNodes;
    }

    public Board getFoundGoal() {
        return this.foundGoal;
    }

    public void setInteractiveBoard(Board b) {
        this.interactiveBoard = b;
    }

    public void setFoundGoal(Board b) {
        this.foundGoal = b;
    }

    public boolean getExceedMax() {
        return this.expExceedMax;
    }

    public void setExpExceedMax(boolean b) {
        this.expExceedMax = b;
    }

    /**
     * Solve the puzzle using the A* algorithm
     * @return solved puzzle board
     */
    public Board solvePuzzleAStar(String heuristicType) {
        queue.clear();
        closed.clear();
        getInteractiveBoard().setAlgorithmType("astar");
        getInteractiveBoard().setHeuristicType(heuristicType);
        Board board = getInteractiveBoard();
        board.setG(0);
        board.setH(board.computeSumOfManhattan());

        queue.offer(board);
        Board solutionBoard = null;
        int nodesVisited = 0;

        while(!queue.isEmpty()) {
            Board current = queue.poll();

            if(nodesVisited > getMaxNodes()) {
//                System.out.println("ERROR: Algorithm has considered " + Integer.toString(nodesVisited) + " this exceeds" +
//                        " the amount specified in maxNodes.");
//                System.exit(0);
                return null;
            }

            nodesVisited++;
            if(current.computeHeuristic() == 0) {
                solutionBoard = current;
                break;
            }

            // get all the next possible board positions
            ArrayList<Board> children = current.getValidChildren();
            for(Board child : children) {
                boolean addToQueue = true;

//                // if this board position has benn encountered already with a lower cost estimate,
//                // skip this board
//                if(queue.contains(child)) {
//                    Board duplicateBoard = queue.remove();
//                    if(duplicateBoard.getF() < child.getF()) {
//                        queue.offer(duplicateBoard);
//                        addToQueue = false;
//                    }
//                }


                if(closed.containsKey(child)) {
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
//        System.out.print("Nodes considered " + String.valueOf(nodesVisited));
        return solutionBoard;
    }

    public Board beamSearch(int k) {
        getInteractiveBoard().setAlgorithmType("beam");
        getInteractiveBoard().setHeuristicType("h2");
//        getInteractiveBoard().printBoard();
        queue.clear();
        Board goal = null;
        ArrayList<Board> bestBoards;
        ArrayList<Board> children = getInteractiveBoard().getValidChildren();


        if(children.size() < k) {
            bestBoards = children;
        }
        else {
            bestBoards = getKBestBoards(k, queue, children);
        }

        while(!bestBoards.isEmpty()) {
            ArrayList<Board> allSuccessors = generateAllSuccessors(bestBoards);
            if(getExceedMax()) {
                return null;
            }

            if(getFoundGoal() != null) {
                goal = getFoundGoal();
                break;
            }
            bestBoards = getKBestBoards(k, queue, allSuccessors);
//            System.out.println("Size of best board: "  + String.valueOf(bestBoards.size()));
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
        int nodesVisited = 0;
        ArrayList<Board> allBoards = new ArrayList<>();
        for(Board b : oldBest) {
            if(nodesVisited > getMaxNodes()) {
//                System.out.println("ERROR: Algorithm has considered " + Integer.toString(nodesVisited) + " this exceeds" +
//                        " the amount specified in maxNodes.");
//                System.exit(0);
                setExpExceedMax(true);
            }

            nodesVisited++;
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
    public void readCommandsFromFile(String fileName) throws IOException {
        Stream<String> stream = Files.lines(Paths.get(fileName));
            stream.forEach(System.out::println);
    }

    public void generateNRandomMoves(int numMoves) {
        String[] directions = {"left", "right", "up", "down"};
        int count = numMoves;
        int max = 4;
        int randomIndex;
        randomNumGen.setSeed(numMoves);

        while (count > 0) {
            randomIndex = randomNumGen.nextInt(max);
            String direction = directions[randomIndex];

            if(getInteractiveBoard().isLegalMove(direction)) {
                setInteractiveBoard(getInteractiveBoard().move(direction));
                count--;
            }
        }
    }

    public void printSolution(Board solvedBoard) {
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
        getInteractiveBoard().setState(state);
    }

    public Board getInteractiveBoard() {
        return this.interactiveBoard;
    }

    public void checkForNullBoard() {
        if(getInteractiveBoard() == null) {
            System.out.println("You have not set the board state yet.");
            System.exit(0);
        }
    }

    public ArrayList<Double> expAstarH1() {
        double numSolved = 0;
        double NUM_MOVES = 50;
        ArrayList<Double> fractions = new ArrayList<>();

        for(int maxNodes = 1; maxNodes < 1050; maxNodes = maxNodes * 2) {
            for(int numMoves = 1; numMoves < NUM_MOVES + 1; numMoves++) {
                setMaxNodes(maxNodes);
                setInteractiveBoard(new Board(Board.GOAL));
                getInteractiveBoard().clearData();
                generateNRandomMoves(numMoves);
                Board s = solvePuzzleAStar("h1");
                if(s != null) {
                    numSolved++;
                }
            }
            fractions.add((numSolved/NUM_MOVES));
            numSolved = 0;
        }

        return fractions;
    }

    public ArrayList<Double> expAstarH2() {
        double numSolved = 0;
        double NUM_MOVES = 50;
        ArrayList<Double> fractions = new ArrayList<>();

        for(int maxNodes = 1; maxNodes < 1050; maxNodes = maxNodes * 2) {
            for(int numMoves = 1; numMoves < NUM_MOVES + 1; numMoves++) {
                setMaxNodes(maxNodes);
                setInteractiveBoard(new Board(Board.GOAL));
                getInteractiveBoard().clearData();
                generateNRandomMoves(numMoves);
                Board s = solvePuzzleAStar("h2");
                if(s != null) {
                    numSolved++;
                }
            }
            fractions.add((numSolved/NUM_MOVES));
            numSolved = 0;
        }

        return fractions;
    }

    public ArrayList<Double> expBeam() {
        double numSolved = 0;
        double NUM_MOVES = 100;
        ArrayList<Double> fractions = new ArrayList<>();
        for(int maxNodes = 1; maxNodes < 1050; maxNodes = maxNodes * 2) {
            for(int numMoves = 1; numMoves < NUM_MOVES + 1; numMoves++) {
                setExpExceedMax(false);
                setMaxNodes(maxNodes);
                getInteractiveBoard().clearData();
                setInteractiveBoard(new Board(Board.GOAL));

                setFoundGoal(null);
                generateNRandomMoves(numMoves);
                Board s = beamSearch(10000);
                if(s != null) {
                    numSolved++;
                }
            }
            fractions.add((numSolved/NUM_MOVES));
            numSolved = 0;
        }

        return fractions;
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
                            p.setFoundGoal(null);
                            p = new Puzzle(p.getInteractiveBoard());
                            int k = Integer.parseInt(arg);
                            p.checkForNullBoard();
                            System.out.println("Solving puzzle using Beam search algorithm....");
                            solution = p.beamSearch(k);
                            p.printSolution(solution);
                            break;
                        case "A-star":
                            p = new Puzzle(p.getInteractiveBoard());
                            String heuristic = arg;
                            p.checkForNullBoard();
                            System.out.println("Solving puzzle using A* algorithm....");
                            solution = p.solvePuzzleAStar(heuristic);
                            p.printSolution(solution);
                            break;
                        default:
                            System.out.println("Search algorithm not recognized. Check spelling.");
                            System.exit(0);
                            break;
                    }
                    break;
                case "setState":
                    String state = inputs[1] + " " + inputs[2] + " " + inputs[3];
                    if(state.length() != 11) {
                        System.out.println("Board state is invalid. Check state again.");
                        System.exit(0);
                    }
                    p.setBoardState(state);
                    break;
                case "printState":
                    p.checkForNullBoard();
                    p.interactiveBoard.printBoard();
                    break;
                case "move":
                    String direction = inputs[1];
                    p.checkForNullBoard();
                    p.setInteractiveBoard(p.getInteractiveBoard().move(direction));
                    break;
                case "maxNodes":
                    p.checkForNullBoard();
                    int maxNodes = Integer.parseInt(inputs[1]);
                    p.setMaxNodes(maxNodes);
                    break;
                case "randomizeState":
                    if(inputs.length < 2) {
                        System.out.println("Missing argument.");
                    }
                    int numMoves = Integer.parseInt(inputs[1]);
                    p.setInteractiveBoard(new Board(Board.GOAL));
                    p.generateNRandomMoves(numMoves);
                    break;
                case "exit":
                    System.exit(1);
                    break;
                case "exp":
                    ArrayList<Double> result = p.expAstarH1();
                    ArrayList<Double> result2 = p.expAstarH2();
                    ArrayList<Double> result3 = p.expBeam();
                    System.out.println("A* H1: " + result.toString());
                    System.out.println("A* H2: " + result2.toString());
                    System.out.println("Beam: " + result3.toString());
                    break;
                default:
                    System.out.println("Command not recognized.");
                    break;
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
