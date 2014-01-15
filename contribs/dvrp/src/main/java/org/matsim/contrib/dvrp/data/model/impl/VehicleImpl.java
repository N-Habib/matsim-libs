/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2013 by the members listed in the COPYING,        *
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

package org.matsim.contrib.dvrp.data.model.impl;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.dvrp.data.model.*;
import org.matsim.contrib.dvrp.data.schedule.Schedule;
import org.matsim.contrib.dvrp.data.schedule.impl.*;


public class VehicleImpl
    implements Vehicle
{
    private final Id id;
    private final String name;
    private final Depot depot;

    private final double capacity;

    // TW for vehicle
    private final double t0;
    private final double t1;

    // max time outside the depot
    private final double timeLimit;

    private final Schedule<? extends AbstractTask> schedule;


    public VehicleImpl(Id id, String name, Depot depot, double capacity, double t0, double t1,
            double timeLimit)
    {
        this.id = id;
        this.name = name;
        this.depot = depot;
        this.capacity = capacity;
        this.t0 = t0;
        this.t1 = t1;
        this.timeLimit = timeLimit;

        schedule = new ScheduleImpl<AbstractTask>(this);
    }


    @Override
    public Id getId()
    {
        return id;
    }


    @Override
    public String getName()
    {
        return name;
    }


    @Override
    public Depot getDepot()
    {
        return depot;
    }


    @Override
    public double getCapacity()
    {
        return capacity;
    }


    @Override
    public double getT0()
    {
        return t0;
    }


    @Override
    public double getT1()
    {
        return t1;
    }


    @Override
    public double getTimeLimit()
    {
        return timeLimit;
    }


    @Override
    public Schedule<? extends AbstractTask> getSchedule()
    {
        return schedule;
    }


    @Override
    public String toString()
    {
        return "Vehicle_" + id;
    }
}
