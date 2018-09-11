package tddc17;


import aima.core.environment.liuvacuum.*;
import aima.core.agent.Action;
import aima.core.agent.AgentProgram;
import aima.core.agent.Percept;
import aima.core.agent.impl.*;

import java.util.Random;
import java.util.HashSet;
import java.util.AbstractMap.SimpleEntry;

class MyAgentState
{
	public int[][] world = new int[30][30];
	public int initialized = 0;
	final int UNKNOWN 	= 0;
	final int WALL 		= 1;
	final int CLEAR 	= 2;
	final int DIRT		= 3;
	final int HOME		= 4;
	final int ACTION_NONE 			= 0;
	final int ACTION_MOVE_FORWARD 	= 1;
	final int ACTION_TURN_RIGHT 	= 2;
	final int ACTION_TURN_LEFT 		= 3;
	final int ACTION_SUCK	 		= 4;
	
	final int SEARCH 			= 0; // Searching for the next unknown cell 
	final int MOVE		 		= 1; // Moving to chosen unknown cell
	final int RETURN 			= 2; // Returning to home cell

	public int agent_mode 		= 0; // Starting in search mode
									 //	where we find our next target unknown cell
	public int agent_x_position = 1;
	public int agent_y_position = 1;
	public int agent_last_action = ACTION_NONE;

	public static final int NORTH = 0;
	public static final int EAST = 1;
	public static final int SOUTH = 2;
	public static final int WEST = 3;
	public int agent_direction = EAST;

	MyAgentState()
	{
		for (int i=0; i < world.length; i++)
			for (int j=0; j < world[i].length ; j++)
				world[i][j] = UNKNOWN;
		world[1][1] = HOME;
		agent_last_action = ACTION_NONE;
	}
	// Based on the last action and the received percept updates the x & y agent position
	public void updatePosition(DynamicPercept p)
	{
		Boolean bump = (Boolean)p.getAttribute("bump");

		if (agent_last_action==ACTION_MOVE_FORWARD && !bump)
	    {
			switch (agent_direction) {
			case MyAgentState.NORTH:
				agent_y_position--;
				break;
			case MyAgentState.EAST:
				agent_x_position++;
				break;
			case MyAgentState.SOUTH:
				agent_y_position++;
				break;
			case MyAgentState.WEST:
				agent_x_position--;
				break;
			}
	    }

	}

	public void updateWorld(int x_position, int y_position, int info)
	{
		if(x_position < world.length && x_position > -1 && y_position < world.length && y_position > -1){
		world[x_position][y_position] = info;
		}
		else
		{
			System.out.println("x_position : " + x_position + ", y_position : " + y_position);
		}
	}

	public void printWorldDebug()
	{
		for (int i=0; i < world.length; i++)
		{
			for (int j=0; j < world[i].length ; j++)
			{
				if (world[j][i]==UNKNOWN)
					System.out.print(" ? ");
				if (world[j][i]==WALL)
					System.out.print(" # ");
				if (world[j][i]==CLEAR)
					System.out.print(" . ");
				if (world[j][i]==DIRT)
					System.out.print(" D ");
				if (world[j][i]==HOME)
					System.out.print(" H ");
			}
			System.out.println("");
		}
	}
}

class MyAgentProgram implements AgentProgram {

	private int initnialRandomActions = 10;
	private Random random_generator = new Random();

	// Here you can define your variables!
	public int iterationCounter = 100;
	// Could use a TreeSet with a comparator for sorting if it's required.
	public HashSet<SimpleEntry<Integer, Integer>> unknownSet= new HashSet<SimpleEntry<Integer, Integer>>();
	public MyAgentState state = new MyAgentState();

	// moves the Agent to a random start position
	// uses percepts to update the Agent position - only the position, other percepts are ignored
	// returns a random action
	private Action moveToRandomStartPosition(DynamicPercept percept) {
		int action = random_generator.nextInt(6);
		initnialRandomActions--;
		state.updatePosition(percept);
		if(action==0) {
		    state.agent_direction = ((state.agent_direction-1) % 4);
		    if (state.agent_direction<0)
		    	state.agent_direction +=4;
		    state.agent_last_action = state.ACTION_TURN_LEFT;
			return LIUVacuumEnvironment.ACTION_TURN_LEFT;
		} else if (action==1) {
			state.agent_direction = ((state.agent_direction+1) % 4);
		    state.agent_last_action = state.ACTION_TURN_RIGHT;
		    return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
		}
		state.agent_last_action=state.ACTION_MOVE_FORWARD;
		return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
	}


	@Override
	public Action execute(Percept percept) {

		// DO NOT REMOVE this if condition!!!
    	if (initnialRandomActions>0) {
    		return moveToRandomStartPosition((DynamicPercept) percept);
    	} else if (initnialRandomActions==0) {
    		// process percept for the last step of the initial random actions
    		initnialRandomActions--;
    		state.updatePosition((DynamicPercept) percept);
			System.out.println("Processing percepts after the last execution of moveToRandomStartPosition()");
			state.agent_last_action=state.ACTION_SUCK;
	    	return LIUVacuumEnvironment.ACTION_SUCK;
    	}

    	// This example agent program will update the internal agent state while only moving forward.
    	// START HERE - code below should be modified!

    	System.out.println("x=" + state.agent_x_position);
    	System.out.println("y=" + state.agent_y_position);
    	System.out.println("dir=" + state.agent_direction);


	    iterationCounter--;

	    if (iterationCounter==0)
	    	return NoOpAction.NO_OP;

	    DynamicPercept p = (DynamicPercept) percept;
	    Boolean bump = (Boolean)p.getAttribute("bump");
	    Boolean dirt = (Boolean)p.getAttribute("dirt");
	    Boolean home = (Boolean)p.getAttribute("home");
	    System.out.println("percept: " + p);


	    // State update based on the percept value and the last action
	    state.updatePosition((DynamicPercept)percept);
	    if (bump) {
			switch (state.agent_direction) {
			case MyAgentState.NORTH:
				state.updateWorld(state.agent_x_position,state.agent_y_position-1,state.WALL);
				unknownSet.remove(new SimpleEntry<Integer, Integer>(state.agent_x_position, state.agent_y_position-1));
				break;
			case MyAgentState.EAST:
				state.updateWorld(state.agent_x_position+1,state.agent_y_position,state.WALL);
				unknownSet.remove(new SimpleEntry<Integer, Integer>(state.agent_x_position+1, state.agent_y_position));
				break;
			case MyAgentState.SOUTH:
				state.updateWorld(state.agent_x_position,state.agent_y_position+1,state.WALL);
				unknownSet.remove(new SimpleEntry<Integer, Integer>(state.agent_x_position, state.agent_y_position+1));
				break;
			case MyAgentState.WEST:
				state.updateWorld(state.agent_x_position-1,state.agent_y_position,state.WALL);
				unknownSet.remove(new SimpleEntry<Integer, Integer>(state.agent_x_position-1, state.agent_y_position));
				break;
			}
	    }
	    if (dirt){
	    	state.updateWorld(state.agent_x_position,state.agent_y_position,state.DIRT);
	    }
	    else
	    {
	    	state.updateWorld(state.agent_x_position,state.agent_y_position,state.CLEAR);
			unknownSet.remove(new SimpleEntry<Integer, Integer>(state.agent_x_position, state.agent_y_position));
	    }

	    state.printWorldDebug();
	    
	    updateUnknownSet();
	    
	    System.out.println("X=Y");
	    for(SimpleEntry<Integer, Integer> set : unknownSet )
	    {
	    	System.out.println(set);
	    }
	    
	    // Next action selection based on the percept value
	    if (dirt)
	    {
	    	System.out.println("DIRT -> choosing SUCK action!");
	    	state.agent_last_action=state.ACTION_SUCK;
	    	return LIUVacuumEnvironment.ACTION_SUCK;
	    }
	    else
	    {
				// 1. Lägga in alla unknowns vi går förbi i en lista.
				// 1. Välja den närmaste unknown i listan.
				// 2. Hitta en väg så att man kan gå dit, lista med [(x,y), (x+1, y), (x+1, y+1)]
				// 3. Röra sig mot nästa position i listan.
				// 4. -> 3 om inte framme vid unknown då -> Suck eller 1.
				//


	    	if (bump)
	    	{
				System.out.println("BUMP -> choosing TURN_RIGHT action");
	    		state.agent_last_action=state.ACTION_TURN_RIGHT;
				updateDirection(state.ACTION_TURN_RIGHT);
		    	return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
	    	}
	    	else
	    	{
				System.out.println("Nothing -> choosing MOVE_FORWARD action");
	    		state.agent_last_action=state.ACTION_MOVE_FORWARD;
	    		return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
	    	}

	    }
	}

	public boolean inBounds(int x, int y){
		return (x < state.world.length && x > -1 && y < state.world.length && y > -1);
	}
	
	public void updateUnknownSet(){
		// Can be turned into a for-loop
	    // Above
	    if(inBounds(state.agent_x_position, state.agent_y_position -1)
			&& state.world[state.agent_x_position][state.agent_y_position -1] == state.UNKNOWN){
			unknownSet.add(new SimpleEntry<Integer, Integer>(state.agent_x_position , state.agent_y_position -1));
		}
	    // Right	    
	    if(inBounds(state.agent_x_position + 1, state.agent_y_position )
	    	&& state.world[state.agent_x_position + 1][state.agent_y_position] == state.UNKNOWN){
			unknownSet.add(new SimpleEntry<Integer, Integer>(state.agent_x_position + 1, state.agent_y_position));
		}
	    // Left
	    if(inBounds(state.agent_x_position -1, state.agent_y_position)
			&& state.world[state.agent_x_position -1][state.agent_y_position] == state.UNKNOWN){
			unknownSet.add(new SimpleEntry<Integer, Integer>(state.agent_x_position -1, state.agent_y_position));
		}
	    // Below
	    if(inBounds(state.agent_x_position, state.agent_y_position +1)
			&& state.world[state.agent_x_position][state.agent_y_position +1] == state.UNKNOWN){
			unknownSet.add(new SimpleEntry<Integer, Integer>(state.agent_x_position, state.agent_y_position +1));
		}
	}

	public void updateDirection(int action){
		//final int ACTION_NONE 			= 0;
		//final int ACTION_MOVE_FORWARD 	= 1;
		//final int ACTION_TURN_RIGHT 	= 2;
		//final int ACTION_TURN_LEFT 		= 3;
		//final int ACTION_SUCK	 		= 4;
		if (action == 3){ // LEFT
			switch(state.agent_direction){
				case MyAgentState.NORTH:
					state.agent_direction = MyAgentState.WEST;
					break;
				case MyAgentState.EAST:
					state.agent_direction = MyAgentState.NORTH;
					break;
				case MyAgentState.SOUTH:
					state.agent_direction = MyAgentState.EAST;
					break;
				case MyAgentState.WEST:
					state.agent_direction = MyAgentState.SOUTH;
					break;
			}
		}
		else if(action == 2){ //RIGHT
			switch(state.agent_direction){
				case MyAgentState.NORTH:
					state.agent_direction = MyAgentState.EAST;
					break;
				case MyAgentState.EAST:
					state.agent_direction = MyAgentState.SOUTH;
					break;
				case MyAgentState.SOUTH:
					state.agent_direction = MyAgentState.WEST;
					break;
				case MyAgentState.WEST:
					state.agent_direction = MyAgentState.NORTH;
					break;
				}
		}
	}

	/*
	public Tuple(int ,int) closestUnknown(){
	}
	*/
}

public class MyVacuumAgent extends AbstractAgent {
    public MyVacuumAgent() {
    	super(new MyAgentProgram());
	}
}