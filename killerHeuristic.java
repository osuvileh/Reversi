import reversi.*;
import java.util.*;
import static java.lang.System.out;
public class killerHeuristic implements ReversiAlgorithm
{
	// Constants
	public final static int DEPTH_LIMIT = 4; 
	// Variables
	boolean initialized;
	volatile boolean running; // Note: volatile for synchronization issues.
	GameController controller;
	GameState initialState;
	public Move selectedMove;
	int myIndex;
	int aiIndex;
	int nodes;

	public killerHeuristic() {} //the constructor

	public void requestMove(GameController requester)
	{
		running = false;
		requester.doMove(selectedMove);
	}

	public void init(GameController game, GameState state, int playerIndex, int turnLength)
	{
		initialState = state;
		myIndex = playerIndex;
		if (myIndex == 1)
			aiIndex = 0;
		else
			aiIndex = 1;
		controller = game;
		initialized = true;
	}

	public String getName() { return "killerHeuristic"; }

	public void cleanup() {}
	public void run()
	{
		//implementation of the actual algorithm
		System.out.println("----------------------------");
		System.out.println(getName() + " starting search.");
		System.out.println("myIndex: " + myIndex);
		while(!initialized);
		initialized = false;
		running = true;
		Move selectedMove = null;
		Node selectedNode = null;
		Node bestNode = null;
		int depth = 1;
		nodes = 0; //init node counter to 0 at the start of turn
		while(running){
			
			selectedNode = searchToDepth(depth++);
			//if no valid move exists
			if (selectedNode==null)
				break;
			//Get move and coordinates from node for validity check
			Move move = selectedNode.getMove();
			int x=move.getX();
			int y=move.getY();
			int player = move.getPlayer();
			//Check that move is for the right player that the move is valid
			if (player==myIndex && initialState.isPossibleMove(x,y,myIndex)){
				if (bestNode == null){//set bestNode if null
					bestNode = selectedNode;
					}
				//Select the node with biggest score
				if (selectedNode.getScore() > bestNode.getScore()){

						bestNode = selectedNode; //set bestNode if better node was found
				}
			}
			if(depth > DEPTH_LIMIT)
				break;

		}
		if (bestNode!=null)
			selectedMove = bestNode.getMove();
		//info printout
		if (selectedMove!=null){
			System.out.println("selected move "+selectedMove.toString()+" inspected nodes  "+ nodes);
			System.out.println("selected score: "+bestNode.getScore());
		}
		else
			System.out.println("No valid move available");
		//execute the move
		if(running)
			controller.doMove(selectedMove);
		
	}


	double maxPlayer(Node node, int myIndex, int depth){
		nodes++;
		double maxScore = Integer.MIN_VALUE;
		double bestScore = Integer.MIN_VALUE;

		Vector moves = node.getState().getPossibleMoves(myIndex);
		//Skip turn if there are no possible moves for this player
		if(moves.size() == 0)
			return  0;
		//return score if time has run out
		if(!running)
			return bestScore;
		if (myIndex == 1)
			aiIndex = 0;
		else
			aiIndex = 1;
		//Return evaluated score after leave scores have been propagated to root
		if (depth<=0)
			return evaluate(node);
		//Standard minimax run algorithm
		for (int i = 0; i < moves.size(); i++){
			Move move = (Move)moves.elementAt(i);
			double score = minPlayer(new Node(node.getState().getNewInstance(move), move), aiIndex, depth -1);
			bestScore = Math.max(score, bestScore);
			//maxScore = bestScore;
			
		}
		return bestScore;
		
	}

	double minPlayer(Node node, int aiIndex, int depth){
		nodes++;
		double minScore = Integer.MAX_VALUE;
		double bestScore = Integer.MAX_VALUE;
		//get moves from node
		Vector moves = node.getState().getPossibleMoves(aiIndex);
		//Skip turn if there are no possible moves for this player
		if(moves.size() == 0)
			return 0;

		//return score if time has run out
		if(!running)
			return bestScore;
		if (aiIndex  == 1)
			myIndex = 0;
		else
			myIndex = 1;
		//Return evaluated score after leave scores have been propagated to root
		if (depth<=0)
			return evaluate(node);
		//Standard minimax run algorithm
		for (int i = 0; i < moves.size(); i++){
			Move move = (Move)moves.elementAt(i);
			double score = maxPlayer(new Node(node.getState().getNewInstance(move), move), myIndex,  depth -1);
			bestScore = Math.min(score, bestScore);
			//minScore = bestScore;
		}
		return bestScore;
	}
	
	Node searchToDepth(int depth)
	{
		Move parentMove;
		Move childMove;
		Move checkMove;
		//Initialize selectedNodee
		Node selectedNode = null;
		
		double bestScore = Integer.MIN_VALUE;
		//get all possible root moves
		Vector moves = initialState.getPossibleMoves(myIndex);
		//Return null if no possible moves exist
		if (moves.size()==0)
			return selectedNode;

		for (int i = 0; i < moves.size(); i++){
			Move move = (Move)moves.elementAt(i);
			//create a new node with current move
			Node node = new Node(initialState.getNewInstance(move), move);
			//propagate scores to root
			node.setScore(minPlayer(node, aiIndex, depth-1));
			//select node with biggest score
			if (node.getScore() > bestScore){
				selectedNode = node;
				bestScore = node.getScore();
			}

		}
		return selectedNode;

	}


	int evaluate(Node node)
    {
    	// Evaluates the leaf node based on amount of marks and position on board
        int score;
        int maximize;
        int minimize;
        int pstate;
        //checks which player's move it is
        GameState state = node.getState();
        Move move = node.getMove();
        pstate = move.getPlayer();
        int x;
        int y;
        x = move.getX();
        y = move.getY();
       
        if(pstate == myIndex){
        	maximize = state.getMarkCount(myIndex);
        	minimize = state.getMarkCount(aiIndex);
        	
        }
        else{
        	maximize = state.getMarkCount(aiIndex);
        	//System.out.println("pstate!=myindex & maximize "+maximize);
        	minimize = state.getMarkCount(myIndex);
        	//System.out.println("pstate!=myindex & min "+minimize);
        }



        int [][] scores = {
            {20,   -7,  7,  5,  5,  7,  -7,  20},
            {-7,   -7,  0,  0,  0,  0,  -7, -20},
            {7,     0,  0,  0,  0,  0,   0,   7},
            {3,     0,  0,  0,  0,  0,   0,   3},
            {3,     0,  0,  0,  0,  0,   0,   3},
            {7,     0,  0,  0,  0,  0,   0,   7},
            {-7,   -7,  0,  0,  0,  0,  -7,  -7},
            {20,   -7,  7,  3,  3,  7,  -7,  20}
        };

        //positional player's evaluation
        //maximizes scores on each move
        

        if (state.getMarkAt(x,y) == myIndex && pstate == myIndex){
            maximize += scores[x][y];
            minimize -= scores[x][y];
        }
        else{
            //scores reduced if opponent gets the square
            minimize += scores[x][y];
            maximize -= scores[x][y];
    	}
    	score = maximize-minimize;
        //Return score for evaluated move
        return score;
    }

    
}