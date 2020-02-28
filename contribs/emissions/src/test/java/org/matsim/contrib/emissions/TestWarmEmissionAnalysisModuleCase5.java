/* *********************************************************************** *
 * project: org.matsim.*                                                   *
 * TestEmission.java                                                       *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
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

package org.matsim.contrib.emissions;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;
import org.matsim.contrib.emissions.utils.EmissionsConfigGroup;
import org.matsim.contrib.emissions.utils.EmissionsConfigGroup.DetailedVsAverageLookupBehavior;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.network.NetworkUtils;
import org.matsim.testcases.MatsimTestUtils;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;
import org.matsim.vehicles.VehiclesFactory;

import java.util.*;

import static org.matsim.contrib.emissions.Pollutant.NO2;
import static org.matsim.contrib.emissions.Pollutant.PM;

/**
 * @author julia


/*
 * test for playground.vsp.emissions.WarmEmissionAnalysisModule
 *
 * WarmEmissionAnalysisModule (weam) 
 * public methods and corresponding tests: 
 * weamParameter - testWarmEmissionAnalysisParameter
 * throw warm EmissionEvent - testCheckVehicleInfoAndCalculateWarmEmissions_and_throwWarmEmissionEvent*, testCheckVehicleInfoAndCalculateWarmEmissions_and_throwWarmEmissionEvent_Exceptions
 * check vehicle info and calculate warm emissions -testCheckVehicleInfoAndCalculateWarmEmissions_and_throwWarmEmissionEvent*, testCheckVehicleInfoAndCalculateWarmEmissions_and_throwWarmEmissionEvent_Exceptions
 * get free flow occurences - testCounters*()
 * get fraction occurences - testCounters*()
 * get stop go occurences - testCounters*()
 * get km counter - testCounters*()
 * get free flow km counter - testCounters*()
 * get top go km couter - testCounters*()
 * get warm emission event counter - testCounters*()
 *
 * private methods and corresponding tests: 
 * rescale warm emissions - rescaleWarmEmissionsTest()
 * calculate warm emissions - implicitly tested
 * convert string 2 tuple - implicitly tested
 *
 * in all cases the needed tables are created manually by the setUp() method
 * see test methods for details on the particular test cases
 **/

@RunWith(Parameterized.class)
public class TestWarmEmissionAnalysisModuleCase5{
	// This used to be one large test class, which had separate table entries for each test, but put them all into the same table.  The result was
	// difficult if not impossible to debug, and the resulting detailed table was inconsistent in the sense that it did not contain all combinations of
	// entries. -- I have now pulled this apart into 6 different test classes, this one here plus "Case1" to "Case5".  Things look ok, but given that the
	// single class before was so large that I could not fully comprehend it, there may now be errors in the ripped-apart classes.  Hopefully, over time,
	// this will help to sort things out.  kai, feb'20

	//Old list of pollutants
//	private final Set<String> pollutants = new HashSet<>(Arrays.asList(CO, CO2_TOTAL, FC, HC, NMHC, NOx, NO2,PM, SO2));
	private static final Set<Pollutant> pollutants = new HashSet<>( Arrays.asList( Pollutant.values() ));
	static final String HBEFA_ROAD_CATEGORY = "URB";
	private static final int leaveTime = 0;
	private final EmissionsConfigGroup.EmissionsComputationMethod emissionsComputationMethod;
	private boolean excep =false;
	private static final String PASSENGER_CAR = "PASSENGER_CAR";

	private WarmEmissionAnalysisModule emissionsModule;
	private Map<Pollutant, Double> warmEmissions;

	// emission factors for tables - no duplicates!
	private static final Double DETAILED_PETROL_FACTOR_FF = .1;
	private static final Double DETAILED_ZERO_FACTOR_FF =  .0011;
	private static final Double DETAILED_SGFF_FACTOR_FF =   .000011;
	private static final Double DETAILED_SGFF_FACTOR_SG = 	.0000011;
	private static final Double AVG_PC_FACTOR_FF = 1.;
	private static final Double AVG_PC_FACTOR_SG = 10.;

	// vehicle information for regular test cases

	private static final Double PETROL_SPEED_FF = TestWarmEmissionAnalysisModule.AVG_PASSENGER_CAR_SPEED_FF_KMH;
	private static final Double PETROL_SPEED_SG = TestWarmEmissionAnalysisModule.AVG_PASSENGER_CAR_SPEED_SG_KMH;

	// case 5 - data in detailed table, stop go speed zero
	private final String zeroRoadCatgory = "URB_case6";
	private final String zeroTechnology = "zero technology";
	private final String zeroConcept = "zero concept";
	private final String zeroSizeClass = "zero size class";
	private final Double zeroFreeVelocity = TestWarmEmissionAnalysisModule.AVG_PASSENGER_CAR_SPEED_FF_KMH;
	private final Double zeroSgVelocity = 0.;


	@Parameterized.Parameters( name = "{index}: ComputationMethod={0}")
	public static Collection<Object[]> createCombinations() {
		List <Object[]> list = new ArrayList<>();
		list.add( new Object [] {EmissionsConfigGroup.EmissionsComputationMethod.StopAndGoFraction} ) ;
		list.add( new Object [] {EmissionsConfigGroup.EmissionsComputationMethod.AverageSpeed} ) ;
		return list;
	}

	public TestWarmEmissionAnalysisModuleCase5( EmissionsConfigGroup.EmissionsComputationMethod emissionsComputationMethod ) {
		this.emissionsComputationMethod = emissionsComputationMethod;
	}


	@Test
	public void testCheckVehicleInfoAndCalculateWarmEmissions_and_throwWarmEmissionEvent5(){
		//-- set up tables, event handler, parameters, module
		setUp();

		// case 6 - data in detailed table, stop go speed zero
		// use free flow factor to calculate emissions
		Id<Vehicle> zeroVehicleId = Id.create("vehicle zero", Vehicle.class);
		double zeroLinklength = 3000.;
		Link zerolink = TestWarmEmissionAnalysisModule.createMockLink("link zero", zeroLinklength, zeroFreeVelocity / 3.6 );
		Id<Link> lpgLinkId = zerolink.getId();
		EmissionUtils.setHbefaRoadType(zerolink, zeroRoadCatgory);

		Id<VehicleType> zeroVehicleTypeId = Id.create(
				PASSENGER_CAR + ";"+ zeroTechnology + ";" + zeroSizeClass + ";" + zeroConcept, VehicleType.class );
		VehiclesFactory vehFac = VehicleUtils.getFactory();
		Vehicle zeroVehicle = vehFac.createVehicle(zeroVehicleId, vehFac.createVehicleType(zeroVehicleTypeId));

		warmEmissions = emissionsModule.checkVehicleInfoAndCalculateWarmEmissions(zeroVehicle, zerolink, 2*zeroLinklength/(zeroFreeVelocity+zeroSgVelocity)*3.6 );
		Assert.assertEquals( DETAILED_ZERO_FACTOR_FF *zeroLinklength/1000., warmEmissions.get(PM ), MatsimTestUtils.EPSILON );
		HandlerToTestEmissionAnalysisModules.reset();

		emissionsModule.throwWarmEmissionEvent(22., lpgLinkId, zeroVehicleId, warmEmissions );
		Assert.assertEquals( pollutants.size() * DETAILED_ZERO_FACTOR_FF *zeroLinklength/1000., HandlerToTestEmissionAnalysisModules.getSum(), MatsimTestUtils.EPSILON );
		HandlerToTestEmissionAnalysisModules.reset(); warmEmissions.clear();

	}



	private void setUp() {

		Map<HbefaWarmEmissionFactorKey, HbefaWarmEmissionFactor> avgHbefaWarmTable = new HashMap<>();
		Map<HbefaWarmEmissionFactorKey, HbefaWarmEmissionFactor> detailedHbefaWarmTable = new HashMap<>();

		TestWarmEmissionAnalysisModule.fillAverageTable( avgHbefaWarmTable );
		fillDetailedTable( detailedHbefaWarmTable );
		Map<HbefaRoadVehicleCategoryKey, Map<HbefaTrafficSituation, Double>> hbefaRoadTrafficSpeeds = EmissionUtils.createHBEFASpeedsTable(
				avgHbefaWarmTable );
		TestWarmEmissionAnalysisModule.addDetailedRecordsToTestSpeedsTable( hbefaRoadTrafficSpeeds, detailedHbefaWarmTable );

		EventsManager emissionEventManager = new HandlerToTestEmissionAnalysisModules();
		EmissionsConfigGroup ecg = new EmissionsConfigGroup();
		ecg.setHbefaVehicleDescriptionSource( EmissionsConfigGroup.HbefaVehicleDescriptionSource.usingVehicleTypeId );
		ecg.setEmissionsComputationMethod( this.emissionsComputationMethod );
		ecg.setDetailedVsAverageLookupBehavior( DetailedVsAverageLookupBehavior.tryDetailedThenTechnologyAverageThenAverageTable );

		emissionsModule = new WarmEmissionAnalysisModule( avgHbefaWarmTable, detailedHbefaWarmTable, hbefaRoadTrafficSpeeds, pollutants, emissionEventManager, ecg );

	}

	private void fillDetailedTable( Map<HbefaWarmEmissionFactorKey, HbefaWarmEmissionFactor> detailedHbefaWarmTable) {

		//entries for zero case
		{
			HbefaVehicleAttributes vehAtt = new HbefaVehicleAttributes();
			vehAtt.setHbefaEmConcept( zeroConcept );
			vehAtt.setHbefaSizeClass( zeroSizeClass );
			vehAtt.setHbefaTechnology( zeroTechnology );

			HbefaWarmEmissionFactor detWarmFactor = new HbefaWarmEmissionFactor();
			detWarmFactor.setWarmEmissionFactor( DETAILED_ZERO_FACTOR_FF );
			detWarmFactor.setSpeed( zeroFreeVelocity );

			for( Pollutant wp : pollutants ){
				HbefaWarmEmissionFactorKey detWarmKey = new HbefaWarmEmissionFactorKey();
				detWarmKey.setHbefaComponent( wp );
				detWarmKey.setHbefaRoadCategory( zeroRoadCatgory );
				detWarmKey.setHbefaTrafficSituation( HbefaTrafficSituation.FREEFLOW );
				detWarmKey.setHbefaVehicleAttributes( vehAtt );
				detWarmKey.setHbefaVehicleCategory( HbefaVehicleCategory.PASSENGER_CAR );
				detailedHbefaWarmTable.put( detWarmKey, detWarmFactor );
			}

			detWarmFactor = new HbefaWarmEmissionFactor();
			double detailedZeroFactorSg = .00011;
			detWarmFactor.setWarmEmissionFactor( detailedZeroFactorSg );
			detWarmFactor.setSpeed( zeroSgVelocity );

			for( Pollutant wp : pollutants ){
				HbefaWarmEmissionFactorKey detWarmKey = new HbefaWarmEmissionFactorKey();
				detWarmKey.setHbefaComponent( wp );
				detWarmKey.setHbefaRoadCategory( zeroRoadCatgory );
				detWarmKey.setHbefaTrafficSituation( HbefaTrafficSituation.STOPANDGO );
				detWarmKey.setHbefaVehicleAttributes( vehAtt );
				detWarmKey.setHbefaVehicleCategory( HbefaVehicleCategory.PASSENGER_CAR );
				detailedHbefaWarmTable.put( detWarmKey, detWarmFactor );
			}
		}

	}



}
	

	

