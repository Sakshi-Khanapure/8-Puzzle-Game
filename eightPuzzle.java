import java.util.Iterator;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Scanner;


enum Move {

    UP, DOWN, LEFT, RIGHT;    //boxes can move up, down, left or right relative to the blank space
}

//--------------------------------------------------------------------------------------------------------------------

class State {
    Move action;   //move that led to this State
    int[][] board = new int[3][3]; //resulting state of the board

    public static final int Empty_space = 0;   //denotes the blank box on the board

    State create(State state, Move move) {
        State nextState = new State();

        //copy the board configuration of `state` to `nextState`
        int i, j;        //used for traversing the 3x3 arrays
        int r=9, c=9;    //coordinates of the blank space char

        for(i = 0; i < 3; i++) {
            for(j = 0; j < 3; j++) {
                if(state.board[i][j] == Empty_space) {    //search for the Empty_space
                    r = i;
                    c = j;
                }
                nextState.board[i][j] = state.board[i][j];
            }
        }

        //check if the coordinates are valid and make changes in the new board configuration to show the new move made
        //completing the actions and changing the board accordingly
        if(move == Move.UP && (r - 1) >=0 && r<3 && c<3) {
            int temp = nextState.board[r - 1][c];
            nextState.board[r - 1][c] = Empty_space;
            nextState.board[r][c] = temp;
            nextState.action = Move.UP;
        }

        else if(move == Move.DOWN  && r + 1 < 3) {
            int temp = nextState.board[r + 1][c];
            nextState.board[r + 1][c] = Empty_space;
            nextState.board[r][c] = temp;
            nextState.action = Move.DOWN;
        }

        else if(move == Move.LEFT  && c - 1 >= 0 && r<3 && c<3) {
            int temp = nextState.board[r][c - 1];
            nextState.board[r][c - 1] = Empty_space;
            nextState.board[r][c] = temp;
            nextState.action = Move.LEFT;
        }

        else if(move == Move.RIGHT && c + 1 < 3) {
            int temp = nextState.board[r][c + 1];
            nextState.board[r][c + 1] = Empty_space;
            nextState.board[r][c] = temp;
            nextState.action = Move.RIGHT;
        }
        return nextState;      //returns the new state or the same state if a move is invalid
    }


   
    public int manhattanDist(State curr, State goal) {
        int x1, x2;       //coordinates in curr board
        int y1, y2;       //corresponding coordinates in goal board
        int hztdist, vertdist;
        int sum = 0;
        int newSum = 0;

        //for coordinates (x1, y1) in curr, find corresponding coordinates (x2,y2) in goal board
        for(x1=0; x1<3; x1++)  {
            for(y1=0; y1<3; y1++)  {
                for(x2=0; x2<3; x2++)  {
                    for(y2=0; y2<3; y2++)  {
                        if(curr.board[x1][y1] == goal.board[x2][y2])   {
                            //find distance between r coord and cumn coord
                            hztdist = java.lang.Math.abs(x1-x2);
                            vertdist = java.lang.Math.abs(y1-y2);

                            sum = sum+(hztdist + vertdist);
                            newSum = 1;
                        }
                    }
                }
            }
        }
        if(newSum==1)
            return sum;
        else
            return 999;
    }

    
    boolean statesMatch(State CheckState, State goalState) {
        for(int i=0; i<3; i++) {
            for(int j=0; j<3; j++) {
                if(CheckState.board[i][j] != goalState.board[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

}

//-----------------------------------------------------------------------------------------------------

class Node extends State {
    int depth; //level of the Node/ depth from root
    int hCost; //heuristic cost of the node    - we have used Manhattan distance to compute the heuristic cost
    State state;       //state designated to a node
    Node parent;       //parent node
    LinkedList<Node> children; //list of child nodes

    Node() {
        depth = 0;
        hCost = 0;
        state = null;
        parent = null;
        children = null;
    }

    Node(int depth, int hCost, State state, Node parent) {
        this.depth = depth;
        this.hCost = hCost;
        this.state = state;
        this.parent = parent;
        this.children = new LinkedList<>();
    }



    LinkedList<Node> generateChildren(Node parent, State goalState)
    {
        LinkedList<Node> childrenPtr = new LinkedList<>();
        State CheckState = null;
        Node child = null;


        //attempt to create states for each moves, and add to the list of children if true
        if(parent.state.action != Move.DOWN)
        {
            CheckState = create(parent.state, Move.UP);
            if(!statesMatch(CheckState, parent.state)) {
                child = new Node(parent.depth + 1, manhattanDist(CheckState, goalState), CheckState, parent);
                parent.children.add(child);
                childrenPtr.add(child);
            }
        }

        if(parent.state.action != Move.UP)
        {
            CheckState = create(parent.state, Move.DOWN);
            if(!statesMatch(CheckState, parent.state)) {
                child = new Node(parent.depth + 1, manhattanDist(CheckState, goalState), CheckState, parent);
                parent.children.add(child);
                childrenPtr.add(child);
            }
        }

        if(parent.state.action != Move.RIGHT) {
            CheckState = create(parent.state, Move.LEFT);
            if(!statesMatch(CheckState, parent.state)) {
                child = new Node(parent.depth + 1, manhattanDist(CheckState, goalState), CheckState, parent);
                parent.children.add(child);
                childrenPtr.add(child);
            }
        }

        if(parent.state.action != Move.LEFT)
        {
            CheckState = create(parent.state, Move.RIGHT);
            if(!statesMatch(CheckState, parent.state)) {
                child = new Node(parent.depth + 1, manhattanDist(CheckState, goalState), CheckState, parent);
                parent.children.add(child);
                childrenPtr.add(child);
            }
        }

        return childrenPtr;
    }
}

//----------------------------------------------------------------------------------------------------------------

public class eightPuzzle extends Node {
    static int nodesGenerated;    //counts total number of nodes generated
    static int nodesExpanded;     //counts number of nodes expanded every time
    static int totalMoves;       //total number of moves needed to reach the solution

    static long runTime;         //computes runtime of the BFS algorithm


    ArrayList BFS (State initial, State goal) {

        nodesGenerated = 0;
        nodesExpanded = 0;
        totalMoves = 0;
        runTime = 0;

        LinkedList<Node> queue = new LinkedList<>();   //holds the nodes
        //System.out.println("Created children linked list: "+children);
        Node node = null;

        long start = System.currentTimeMillis();      //start time

        int mdist = manhattanDist(initial, goal);
        queue.add(new Node(0, mdist,initial, null));  //add first node
        while(!queue.isEmpty()) {     //while queue has nodes, do the following:
            node = queue.removeFirst();    //get the last node

            if(statesMatch(node.state, goal))     //if their states match, break
            {
                break;
            }

            if(!generateChildren(node, goal).isEmpty()) {
                nodesGenerated += node.children.size();
                nodesExpanded++;
            }

            if(!node.children.isEmpty())
            {
                queue.addAll(node.children);      //add all the child nodes to the queue
            }



        }

        long finish = System.currentTimeMillis();  //finish time
        runTime = finish - start;


        ArrayList<Node> pathHead = new ArrayList<>();
        while(node!=null)  {
            pathHead.add(node);
            ++totalMoves;
            node = node.parent;
        }

        --totalMoves;

        return pathHead;
    }


    //This displays the '8-Puzzle Solver' ASCII art to the screen
    void welcomeUser()
    {
        System.out.println("8 Puzzle Game welcomes you");
    }

    //DESCRIPTION: This displays the input instructions for the user to read
    void printInstructions()
    {
        System.out.println
                (
                        "***************************************************************************\n"+
                                "Instructions:\n"+
                                "  Enter the start and goal state of the 8-puzzle board. Input\n"+
                                "  numbers between 0-8, (0 represents the empty space ),\n"+
                                "***************************************************************************\n"
                );
    }

   /*getting the state from the user (non-repeated integer between 0-9)
   Called twice from main, one forinitial and one for final state*/

    void inputState(State state)
    {
        Scanner sc= new Scanner(System.in);
        int r, c;
        int num;

        // flags for input validation
        int Numtaken[] = new int[9];
        for(int i=0; i<9; i++)
        {
            Numtaken[i]=0;
        }

        //taking input for the board, one element at a time
        for(r = 0; r < 3; r++)
        {
            for(c = 0; c < 3; c++)
            {
                System.out.print("board["+r+"]["+c+"]: ");

                // to prevent scanning newline from the input stream
                num=sc.nextInt();

                // check if input is a blank character or is a number greater than 0 and less than 9
                if(num >= 0 && num < 9)
                {
                    // check if input is repeated
                    if(Numtaken[num]==0)
                    {
                        state.board[r][c] = num;
                        Numtaken[num] = 1;
                    }

                    else
                    {
                        System.out.print(num+" is already used. Enter again.\n");
                        c--;
                    }
                }
                else
                {
                    System.out.print(" Invalid number!!!!!! Enter a number from 0 to 8.\n");
                    c--;
                }
            }
        }
        System.out.println();
    }

    //Standard output display
    void DisplayBoard(int board[][])
    {
        int r, c;

        for(r = 0; r < 3; r++)
        {
            System.out.print("*..*..*..*\n");
            for(c = 0; c < 3; c++)
            {
                System.out.print("| "+board[r][c]);
            }
            System.out.print("|\n");
        }
        System.out.print("*..*..*..*\n");
    }

    /* This function interprets numerical instructions of the move to make, to it's verbal counterpart to be displayed to the screen.
    Here, path is the solution path consisting of a list of nodes from the root to the goal */
    void GameOutput( ArrayList path)
    {
        //check if solution exists
        if(path.isEmpty())
        {
            System.out.print("No solution found.\n");
            return;
        }

        //if theinitial state is already the goal state
        else if(path.size()==1)
        {
            System.out.print("The initial state matches the goal state.\n");
            return;
        }

        else
        {
            Iterator<Node> itr = path.iterator();
            System.out.println();
            while(itr.hasNext()) {
                System.out.print("----------->"+itr.next().state.action);
            }
        }

        System.out.print("\n\nSOLUTION:\n");

        System.out.print(
                "DETAILS:\n - Solution length : "+ totalMoves + "\n"+" - Nodes expanded  : " +
                        nodesExpanded+ "\n" + " - Nodes generated : " + nodesGenerated+
                        " \n- Runtime        : "+ runTime + "\n"); //only counting allocated `Node`s
    }

    public static void main(String args[])
    {
        eightPuzzle obj= new eightPuzzle();
        obj.welcomeUser();         //display welcome message
        obj.printInstructions();   //display instructions

        State initial=new State();         //initial board state
        State goalState=new State();       //goal board configuration

        //solution path of each search method
        ArrayList bfs;

        //inputinitial board state
        System.out.print("INITIAL STATE:\n");
        obj.inputState(initial);

        //input the goal state
        System.out.print("\nGOAL STATE:\n");
        obj.inputState(goalState);

        System.out.print("INITIAL BOARD STATE:\n");
        obj.DisplayBoard(initial.board);

        System.out.print("GOAL BOARD STATE:\n");
        obj.DisplayBoard(goalState.board);

        //perform breadth-first search
        bfs = obj.BFS(initial, goalState);
        System.out.println("\nSolution Path\n");
        obj.GameOutput(bfs);
    }
}




/*    ~ OUTPUT ~
____________________________________________________________________________________________

 Test Case 1
 Same initial and goal state 
 Conclusion :Success
 
 
 8 Puzzle Game welcomes you
***************************************************************************
Instructions:
  Enter the start and goal state of the 8-puzzle board. Input
  numbers between 0-8, (0 represents the empty space ),
***************************************************************************

INITIAL STATE:
board[0][0]: 1
board[0][1]: 2
board[0][2]: 3
board[1][0]: 4
board[1][1]: 5
board[1][2]: 6
board[2][0]: 7
board[2][1]: 8
board[2][2]: 0


GOAL STATE:
board[0][0]: 1
board[0][1]: 2
board[0][2]: 3
board[1][0]: 4
board[1][1]: 5
board[1][2]: 6
board[2][0]: 7
board[2][1]: 8
board[2][2]: 0

INITIAL BOARD STATE:
*..*..*..*
| 1| 2| 3|
*..*..*..*
| 4| 5| 6|
*..*..*..*
| 7| 8| 0|
*..*..*..*
GOAL BOARD STATE:
*..*..*..*
| 1| 2| 3|
*..*..*..*
| 4| 5| 6|
*..*..*..*
| 7| 8| 0|
*..*..*..*

Solution Path

The initial state matches the goal state.
_________________________________________________________________________________________
Test Case 2
 Only one move needed 
 Conclusion :Success
 
 8 Puzzle Game welcomes you
***************************************************************************
Instructions:
  Enter the start and goal state of the 8-puzzle board. Input
  numbers between 0-8, (0 represents the empty space ),
***************************************************************************

INITIAL STATE:
board[0][0]: 1
board[0][1]: 2
board[0][2]: 3
board[1][0]: 4
board[1][1]: 5
board[1][2]: 0
board[2][0]: 7
board[2][1]: 8
board[2][2]: 6


GOAL STATE:
board[0][0]: 1
board[0][1]: 2
board[0][2]: 3
board[1][0]: 4
board[1][1]: 5
board[1][2]: 6
board[2][0]: 7
board[2][1]: 8
board[2][2]: 0

INITIAL BOARD STATE:
*..*..*..*
| 1| 2| 3|
*..*..*..*
| 4| 5| 0|
*..*..*..*
| 7| 8| 6|
*..*..*..*
GOAL BOARD STATE:
*..*..*..*
| 1| 2| 3|
*..*..*..*
| 4| 5| 6|
*..*..*..*
| 7| 8| 0|
*..*..*..*

Solution Path


----------->DOWN----------->null

SOLUTION:
DETAILS:
 - Solution length : 1
 - Nodes expanded  : 2
 - Nodes generated : 4 
- Runtime        : 4
_________________________________________________________________________________________
Test Case 3
 Two moves needed to reach the goal state 
 Conclusion :Success
 
 
 8 Puzzle Game welcomes you
***************************************************************************
Instructions:
  Enter the start and goal state of the 8-puzzle board. Input
  numbers between 0-8, (0 represents the empty space ),
***************************************************************************

INITIAL STATE:
board[0][0]: 1
board[0][1]: 2
board[0][2]: 0
board[1][0]: 4
board[1][1]: 5
board[1][2]: 3
board[2][0]: 7
board[2][1]: 8
board[2][2]: 6


GOAL STATE:
board[0][0]: 1
board[0][1]: 2
board[0][2]: 3
board[1][0]: 4
board[1][1]: 5
board[1][2]: 6
board[2][0]: 7
board[2][1]: 8
board[2][2]: 0

INITIAL BOARD STATE:
*..*..*..*
| 1| 2| 0|
*..*..*..*
| 4| 5| 3|
*..*..*..*
| 7| 8| 6|
*..*..*..*
GOAL BOARD STATE:
*..*..*..*
| 1| 2| 3|
*..*..*..*
| 4| 5| 6|
*..*..*..*
| 7| 8| 0|
*..*..*..*

Solution Path


----------->DOWN----------->DOWN----------->null

SOLUTION:
DETAILS:
 - Solution length : 2
 - Nodes expanded  : 3
 - Nodes generated : 6 
- Runtime        : 6

_______________________________________________________________________________
Test Case 4
11 moves needed to reach the goal state 
 Conclusion :Success
 
 
 8 Puzzle Game welcomes you
***************************************************************************
Instructions:
  Enter the start and goal state of the 8-puzzle board. Input
  numbers between 0-8, (0 represents the empty space ),
***************************************************************************

INITIAL STATE:
board[0][0]: 1
board[0][1]: 2
board[0][2]: 3
board[1][0]: 0
board[1][1]: 5
board[1][2]: 6
board[2][0]: 7
board[2][1]: 8
board[2][2]: 4


GOAL STATE:
board[0][0]: 1
board[0][1]: 2
board[0][2]: 3
board[1][0]: 4
board[1][1]: 5
board[1][2]: 6
board[2][0]: 7
board[2][1]: 8
board[2][2]: 0

INITIAL BOARD STATE:
*..*..*..*
| 1| 2| 3|
*..*..*..*
| 0| 5| 6|
*..*..*..*
| 7| 8| 4|
*..*..*..*
GOAL BOARD STATE:
*..*..*..*
| 1| 2| 3|
*..*..*..*
| 4| 5| 6|
*..*..*..*
| 7| 8| 0|
*..*..*..*

Solution Path


----------->DOWN----------->RIGHT----------->RIGHT----------->UP----------->LEFT----------->DOWN----------->LEFT----------->UP----------->RIGHT----------->RIGHT----------->DOWN----------->null

SOLUTION:
DETAILS:
 - Solution length : 11
 - Nodes expanded  : 1320
 - Nodes generated : 2317 
- Runtime        : 51

_____________________________________________________________________________________________________________________________
Test Case 5
Random Input 
Conclusion :No Result  .BFS is an inefficient algorithm for difficult problem state due to very high space and time complexity.


8 Puzzle Game welcomes you
***************************************************************************
Instructions:
  Enter the start and goal state of the 8-puzzle board. Input
  numbers between 0-8, (0 represents the empty space ),
***************************************************************************

INITIAL STATE:
board[0][0]: 2
board[0][1]: 1
board[0][2]: 3
board[1][0]: 4
board[1][1]: 5
board[1][2]: 8
board[2][0]: 6
board[2][1]: 7
board[2][2]: 0


GOAL STATE:
board[0][0]: 1
board[0][1]: 2
board[0][2]: 3
board[1][0]: 4
board[1][1]: 5
board[1][2]: 6
board[2][0]: 7
board[2][1]: 8
board[2][2]: 0

INITIAL BOARD STATE:
*..*..*..*
| 2| 1| 3|
*..*..*..*
| 4| 5| 8|
*..*..*..*
| 6| 7| 0|
*..*..*..*
GOAL BOARD STATE:
*..*..*..*
| 1| 2| 3|
*..*..*..*
| 4| 5| 6|
*..*..*..*
| 7| 8| 0|
*..*..*..*



 */
