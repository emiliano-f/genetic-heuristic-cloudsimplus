/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.cloudsimplus.heuristics;

import java.util.List;
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
}
