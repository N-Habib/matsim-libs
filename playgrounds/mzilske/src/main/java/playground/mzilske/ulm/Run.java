package playground.mzilske.ulm;


import org.matsim.api.core.v01.Scenario;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.OTFVisConfigGroup.ColoringScheme;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.config.groups.StrategyConfigGroup.StrategySettings;
import org.matsim.core.controler.Controler;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioImpl;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.pt.router.TransitRouter;
import org.matsim.pt.router.TransitRouterFactory;
import org.matsim.pt.transitSchedule.api.TransitScheduleReader;
import org.matsim.vehicles.VehicleReaderV1;

import playground.mzilske.vbb.OTPTripRouterFactory;
import d4d.AltPopulationReaderMatsimV5;

public class Run {
	
	public static void main(String[] args) {

		

		
		Config config = ConfigUtils.createConfig();
		config.scenario().setUseVehicles(true);
		config.scenario().setUseTransit(true);
		config.controler().setMobsim("qsim");
		config.controler().setLastIteration(1);
		config.controler().setOutputDirectory("/Users/vspuser/gtfs-ulm/output");
		config.addQSimConfigGroup(new QSimConfigGroup());
		config.getQSimConfigGroup().setSnapshotStyle("queue");
		config.getQSimConfigGroup().setSnapshotPeriod(1);
		config.getQSimConfigGroup().setRemoveStuckVehicles(false);
		config.otfVis().setColoringScheme(ColoringScheme.gtfs);
		config.otfVis().setDrawTransitFacilities(false);
		config.transitRouter().setMaxBeelineWalkConnectionDistance(1.0);
		
		config.network().setInputFile("/Users/vspuser/gtfs-ulm/network.xml");
		config.plans().setInputFile("/Users/vspuser/gtfs-ulm/population.xml");
		config.transit().setTransitScheduleFile("/Users/vspuser/gtfs-ulm/transit-schedule.xml");
		config.transit().setVehiclesFile("/Users/vspuser/gtfs-ulm/transit-vehicles.xml");
		
		StrategySettings best = new StrategySettings(new IdImpl(1));
		best.setModuleName("ChangeExpBeta");
		best.setProbability(0.5);
		
		StrategySettings reRoute = new StrategySettings(new IdImpl(2));
		reRoute.setModuleName("ReRoute");
		reRoute.setProbability(0.5);
		
		
		
		config.strategy().addStrategySettings(best);
		config.strategy().addStrategySettings(reRoute);
		
		
		ActivityParams home = new ActivityParams("home");
		home.setTypicalDuration(12*60*60);
		config.planCalcScore().addActivityParams(home);
		ActivityParams work = new ActivityParams("work");
		work.setTypicalDuration(8*60*60);
		config.planCalcScore().addActivityParams(work);
		config.planCalcScore().setWriteExperiencedPlans(true);
		
		Scenario scenario = ScenarioUtils.createScenario(config);
		
		new MatsimNetworkReader(scenario).readFile(config.network().getInputFile());
		new VehicleReaderV1(((ScenarioImpl) scenario).getVehicles()).readFile(config.transit().getVehiclesFile());
		new TransitScheduleReader(scenario).readFile(config.transit().getTransitScheduleFile());
		new AltPopulationReaderMatsimV5(scenario).readFile(config.plans().getInputFile());
		
		
		Controler controler = new Controler(scenario);
		controler.setOverwriteFiles(true);
		controler.setTransitRouterFactory(new TransitRouterFactory() {

			@Override
			public TransitRouter createTransitRouter() {
				throw new RuntimeException();
			}
			
		});
		controler.setTripRouterFactory(new OTPTripRouterFactory(scenario.getTransitSchedule(), TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84_UTM35S, TransformationFactory.WGS84)));		
		controler.run();


	}

}
