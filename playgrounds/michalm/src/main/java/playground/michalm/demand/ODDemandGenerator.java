/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2012 by the members listed in the COPYING,        *
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

package playground.michalm.demand;

import java.util.Map;

import org.matsim.api.core.v01.*;
import org.matsim.api.core.v01.population.*;
import org.matsim.matrices.*;

import pl.poznan.put.util.random.*;
import playground.michalm.zone.Zone;
import playground.michalm.zone.util.MatrixUtils;


public class ODDemandGenerator
{
    private final UniformRandom uniform = RandomUtils.getGlobalUniform();

    private final Scenario scenario;
    private final ActivityCreator activityCreator;
    private final PersonCreator personCreator;
    private final Map<Id, Zone> zones;
    private final PopulationFactory pf;


    public ODDemandGenerator(Scenario scenario, Map<Id, Zone> zones)
    {
        this(scenario, new DefaultActivityCreator(scenario), new DefaultPersonCreator(scenario),
                zones);
    }


    public ODDemandGenerator(Scenario scenario, ActivityCreator activityCreator,
            PersonCreator personCreator, Map<Id, Zone> zones)
    {
        this.scenario = scenario;
        this.activityCreator = activityCreator;
        this.personCreator = personCreator;
        this.zones = zones;
        pf = scenario.getPopulation().getFactory();
    }


    public void generateSinglePeriod(Matrix matrix, String fromActivityType, String toActivityType,
            String mode, double startTime, double duration, double flowCoeff)//TransportMode.car
    {
        Iterable<Entry> entryIter = MatrixUtils.createEntryIterable(matrix);

        for (Entry e : entryIter) {
            Zone fromZone = zones.get(e.getFromLocation());
            Zone toZone = zones.get(e.getToLocation());
            int trips = (int)uniform.floorOrCeil(flowCoeff * e.getValue());

            for (int k = 0; k < trips; k++) {
                Plan plan = pf.createPlan();

                // act0
                Activity startAct = activityCreator.createActivity(fromZone, fromActivityType);
                startAct.setEndTime((int)uniform.nextDouble(startTime, startTime + duration));
                plan.addActivity(startAct);

                // leg
                plan.addLeg(pf.createLeg(mode));

                // act1
                plan.addActivity(activityCreator.createActivity(toZone, toActivityType));

                Person person = personCreator.createPerson(plan, fromZone, toZone);
                person.addPlan(plan);
                scenario.getPopulation().addPerson(person);
            }
        }
    }


    public void generateMultiplePeriods(Matrix matrix, String fromActivityType,
            String toActivityType, String mode, double startTime, double duration,
            double[] flowCoeffs)
    {
        for (int i = 0; i < flowCoeffs.length; i++) {
            generateSinglePeriod(matrix, fromActivityType, toActivityType, mode, startTime,
                    duration, flowCoeffs[i]);
            startTime += duration;
        }
    }


    public void generateMultiplePeriods(Matrix[] matrices, String fromActivityType,
            String toActivityType, String mode, double startTime, double duration, double flowCoeffs)
    {
        for (int i = 0; i < matrices.length; i++) {
            generateSinglePeriod(matrices[i], fromActivityType, toActivityType, mode, startTime,
                    duration, flowCoeffs);
            startTime += duration;
        }
    }


    public void write(String plansFile)
    {
        new PopulationWriter(scenario.getPopulation(), scenario.getNetwork()).write(plansFile);
        System.out.println("Generated population written to: " + plansFile);
    }
}