/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.cloudsimplus.heuristics;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cloudsimplus.distributions.ContinuousDistribution;

/**
 *
 * @author emiliano
 */
@Accessors @Getter
public abstract class NaiveGeneticAbstract<S extends HeuristicSolution<?>> 
	extends HeuristicAbstract<S> {

	/**
	 * The temperature that defines the system is cold enough and solution
	 * search may be stopped.
	 */
	@Setter
	private double coldTemperature;

	/**
	 * The current system temperature that represents the system state at
	 * the time of the method call.
	 */
	private double currentTemperature;

	/**
	 * Percentage rate in which the system will be cooled, in scale from [0
	 * to 1[.
	 */
	@Setter
	private double coolingRate;

	/**
	 * Instantiates a simulated annealing heuristic.
	 *
	 * @param random a pseudo random number generator
	 * @param solutionClass reference to the generic class that will be used
	 * to instantiate heuristic solutions
	 */
	NaiveGeneticAbstract(final ContinuousDistribution random, final Class<S> solutionClass) {
		super(random, solutionClass);
	}	


	/**
	 * Genetic Algorithm Modification
	 */
	
	/**
     	* {@inheritDoc}
     	* <p>It is used the Boltzmann distribution to define the probability
     	* of a worse solution (considering its cost)
     	* to be accepted or not in order to avoid local minima.
     	* The computed Boltzmann factor also ensures that better solutions are always accepted.
     	* </p>
     	*
     	* <p>The Boltzmann Constant has different values depending of the used unit.
     	* In this case, it was used the natural unit of information.</p>
     	*
     	* @return {@inheritDoc}
     	*
     	* @see <a href="http://www.wikiwand.com/en/Boltzmann_distribution">Boltzmann distribution</a>
     	* @see <a href="http://en.wikipedia.org/wiki/Boltzmann_constant">Boltzmann constant</a>
     	*/
    	@Override
    	public double getAcceptanceProbability() {
        	final double boltzmannConstant = 1.0;
        	return Math.exp((getBestSolutionSoFar().getCost() - getNeighborSolution().getCost())
               						/ (boltzmannConstant * currentTemperature));
    	}

    	/**
     	* {@inheritDoc}
     	*
     	* @return true if the system is cold enough and solution search can be
     	* stopped, false otherwise
     	*/
	/*
    	@Override
    	public boolean isToStopSearch() {
        	return currentTemperature <= coldTemperature;
    	}

    	/**
     	* {@inheritDoc}
     	*
     	* Cools the system at a defined {@link #getCoolingRate() cooling rate}.
     	* @see #getCurrentTemperature()
     	*/
	/*
    	@Override
    	public void updateSystemState() {
	    	currentTemperature *= 1 - coolingRate;
	    	LOGGER.debug(
	        "{}: Best solution cost so far is {}, current system temperature is {}",
            System.currentTimeMillis(), getBestSolutionSoFar().getCost(), getCurrentTemperature());
    	}

    	/**
	 * Sets the current system temperature.
	 * @param currentTemperature the temperature to set
	 */
	protected void setCurrentTemperature(final double currentTemperature) {
		this.currentTemperature = currentTemperature;
	}

}
