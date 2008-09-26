/* *********************************************************************** *
 * project: org.matsim.*
 * SimplifyPersons.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

/**
 * 
 */
package playground.johannes.socialnets;

import java.util.LinkedList;
import java.util.List;

import org.matsim.config.Config;
import org.matsim.controler.ScenarioData;
import org.matsim.gbl.Gbl;
import org.matsim.population.Person;
import org.matsim.population.Plan;
import org.matsim.population.Population;
import org.matsim.population.PopulationWriter;
import org.matsim.utils.geometry.Coord;

/**
 * @author illenberger
 *
 */
public class SimplifyPersons {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Config config = Gbl.createConfig(new String[]{args[0]});
		
		ScenarioData data = new ScenarioData(config);


		Population pop = data.getPopulation();
		List<Person> remove = new LinkedList<Person>();
		for(Person p : pop) {
			for(int i = 1; i < p.getPlans().size(); i = 1)
				p.getPlans().remove(i);
			
			Plan selected = p.getSelectedPlan();
			for(int i = 1; i < selected.getActsLegs().size(); i = 1) {
				selected.getActsLegs().remove(i);
			}
			Coord c = p.getPlans().get(0).getFirstActivity().getCoord();
			if(!(c.getX() >= 668000 && c.getX() <= 698000 && c.getY() >= 232000 && c.getY() <= 262000))
				remove.add(p);
		}
		
		for(Person p : remove)
			pop.getPersons().remove(p.getId());
		
		PopulationWriter writer = new PopulationWriter(pop, args[1], "v4", 100);
		writer.write();
	}

}
