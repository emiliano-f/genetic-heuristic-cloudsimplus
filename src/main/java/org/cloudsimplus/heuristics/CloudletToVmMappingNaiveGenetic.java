/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.cloudsimplus.heuristics;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.IntStream;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.distributions.ContinuousDistribution;
import org.cloudsimplus.vms.Vm;

/**
 *
 * @author emiliano
 */

@Accessors
public class CloudletToVmMappingNaiveGenetic 
	extends NaiveGeneticAbstract<CloudletToVmMappingSolution> 
	implements CloudletToVmMappingHeuristic {
	
	/**
     	* Number of {@link CloudletToVmMappingSolution} created so far.
     	*/
    	private static int solutions = 0;

    	private CloudletToVmMappingSolution initialSolution;

    	@Getter @Setter @NonNull
    	private List<Vm> vmList;

    	@Getter @Setter @NonNull
    	private List<Cloudlet> cloudletList;

    	/**
     	* Creates a new Simulated Annealing Heuristic for solving Cloudlets to Vm's mapping.
     	*
     	* @param initialTemperature the system initial temperature
     	* @param random a random number generator
     	* @see #setColdTemperature(double)
     	* @see #setCoolingRate(double)
     	*/
    	public CloudletToVmMappingNaiveGenetic(final double initialTemperature, final ContinuousDistribution random) {
        	super(random, CloudletToVmMappingSolution.class);
	    		setCurrentTemperature(initialTemperature);
        	initialSolution = new CloudletToVmMappingSolution(this, ++solutions);
    	}

    	private CloudletToVmMappingSolution generateRandomSolution() {
        	final var solution = new CloudletToVmMappingSolution(this, ++solutions);
        	cloudletList.forEach(cloudlet -> solution.bindCloudletToVm(cloudlet, getRandomVm()));
        	return solution;
    	}

    	private boolean isReadToGenerateInitialSolution(){
        	return !cloudletList.isEmpty() && !vmList.isEmpty();
    	}

    	private boolean isThereInitialSolution(){
        	return !initialSolution.getResult().isEmpty();
    	}

    	@Override
    	public CloudletToVmMappingSolution getInitialSolution() {
        	if(!isThereInitialSolution() && isReadToGenerateInitialSolution()) {
            		initialSolution = generateRandomSolution();
        	}

        	return initialSolution;
    	}

    	/**
     	* @return a random Vm from the {@link #getVmList() available Vm's list}.
     	*/
    	private Vm getRandomVm() {
        	final int idx = getRandomValue(vmList.size());
        	return vmList.get(idx);
    	}

    	@Override
    	public CloudletToVmMappingSolution createNeighbor(final CloudletToVmMappingSolution source) {
        	final var clone = new CloudletToVmMappingSolution(source, ++solutions);
        	clone.swapVmsOfTwoRandomSelectedMapEntries();
        	return clone;
    	}

	/**
     	* {@return the number of solutions created so far}
     	* At the end of the simulations, it indicates the total number of solutions created.
     	*/
    	public static int getSolutions() {
        	return solutions;
    	}	

	/**
	 * Genetic algorithm implementation
	 */

	@Getter
	private int population;
	@Getter
	private int parents;
	@Getter
	private int mutations;
	private int generations;
	private double mutationProbability;

	/**
	 * Initial population (several solutions)
	 */
	private ArrayList<CloudletToVmMappingSolution> individualsList;

	/**
	 * Parents (next generation)
	 */
	private ArrayList<CloudletToVmMappingSolution> parentsList;

	/**
	 * Temps attributes
	 */
	private boolean flag = true;
	private double avgPunctuation = 0;
	private int mutationsApplied = 0;

	/**
	 * 
	 * @param random
	 * @param population
	 * @param parents
	 * @param mutations
	 * @param generations
	 * @param mutationProbability 
	 */
	public CloudletToVmMappingNaiveGenetic(
					  final double initialTemperature,
					  final ContinuousDistribution random,
					  final int population,
					  final int parents,
					  final int mutations,
					  final int generations,
					  final double mutationProbability) {
		this(initialTemperature, random);
		this.population = population;
		this.parents = parents;
		this.mutations = mutations;
		this.generations = generations;
		this.mutationProbability = mutationProbability;
		this.individualsList = new ArrayList<>(population);
		this.parentsList = new ArrayList<>(parents);
		firstGeneration();
	}

	/**
	 * Generate first population
	 */
	private void firstGeneration() {
		IntStream.range(0, getPopulation())
			 .forEach(idx -> individualsList.add(idx, new CloudletToVmMappingSolution(this, ++solutions)));
	}	

	/**
	 * Select parents (PARENTS)
	 */
	private void selectParents() {
		IntStream.range(0, getParents())
         		 .forEach(idx -> parentsList.add(idx, individualsList.get(getRandomValue(getPopulation()))));
	}
	
	/**
	 * Obtain all descendants
	 * @return descendants
	 */
	private Stack<CloudletToVmMappingSolution> crossoverParents() {
		//Map<Cloudlet, Vm> descendants = new HashMap<>();
		Stack<CloudletToVmMappingSolution> descendants = new Stack<>();
		IntStream.range(0, (int) (getParents()/2))
			 .forEach(idx -> descendants.push(getDescendant(parentsList.get(idx*2), parentsList.get(idx*2+1))));
		return descendants;
	}

	/**
	 * Calculate descendant
	 * Multiple points: odd cloudlets come from parent one and even cloudlets from parent two
	 * @param one
	 * @param two
	 */
	private CloudletToVmMappingSolution getDescendant(final CloudletToVmMappingSolution one,
							  final CloudletToVmMappingSolution two) {
		CloudletToVmMappingSolution descendant = new CloudletToVmMappingSolution(this, ++solutions);
		cloudletList
			.forEach(cloudlet -> descendant.bindCloudletToVm(cloudlet, getBool() ? one.getVm(cloudlet) : two.getVm(cloudlet)));
		return descendant;
	}
	
	/**
	 * Iterates flag value
	 * @return 
	 */
	private boolean getBool() {
		return flag = !flag;
	}

	/**
	 * Replaces individuals with higher cost than descendant
	 * @param descendants 
	 */
	private void replacement(final Stack<CloudletToVmMappingSolution> descendants) {
		descendants.forEach(descendant -> {
			for (int idx=0; idx<getPopulation(); idx++) {
				if (individualsList.get(idx).getCost() >= descendant.getCost()) {
					individualsList.add(idx, descendant);
					break;
				}
			}
		});
	}

	/**
	 * Calculates costs
	 * @return 
	 */
	private double calculateAverage() {
		return avgPunctuation = individualsList
						.stream()
						.mapToDouble(CloudletToVmMappingSolution::getCost)
						.sum()
						/ getPopulation();
	}

	/**
	 * Mutation of individuals if their cost is higher than average 
	 * punctuation
	 */
	private void mutation() {
		calculateAverage();
		individualsList
			.forEach(individual -> {
				if (individual.getCost() > avgPunctuation)
					mutate(individual);
				});
	}

	private void mutate(CloudletToVmMappingSolution individual) {
		if (mutationsApplied < getMutations()) {
			IntStream
				.range(0, getRandomValue(cloudletList.size()))
				.forEach(idx ->individual.swapVmsOfTwoRandomSelectedMapEntries());
			mutationsApplied++;
		}	
	}

	public CloudletToVmMappingSolution getBestIndividual() {
		return individualsList.get(0);
	}
}
