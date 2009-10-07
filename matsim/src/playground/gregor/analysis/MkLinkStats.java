package playground.gregor.analysis;

import org.matsim.analysis.CalcLinkStats;
import org.matsim.analysis.VolumesAnalyzer;
import org.matsim.core.config.Config;
import org.matsim.core.events.EventsImpl;
import org.matsim.core.events.EventsReaderTXTv1;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.network.NetworkLayer;
import org.matsim.core.trafficmonitoring.TravelTimeCalculator;
import org.matsim.core.trafficmonitoring.TravelTimeCalculatorFactoryImpl;

public class MkLinkStats {
	
	public static void main(String [] args) {
		String outDir = "../../../arbeit/svn/runs-svn/run316/stage2/output";
		String it = "201";
		String net = outDir + "/output_network.xml.gz";
		String eventsFile = outDir + "/ITERS/it." + it + "/" + it + ".events.txt.gz";
		String stats =  outDir + "/ITERS/it." + it + "/" + it + ".linkstats.txt.gz";
		
		 Config c = Gbl.createConfig(new String [] {outDir + "/output_config.xml.gz"});
		
		NetworkLayer netzzz = new NetworkLayer();
		new MatsimNetworkReader(netzzz).readFile(net);
		
		EventsImpl events = new EventsImpl();
		VolumesAnalyzer h = new VolumesAnalyzer(60,5*3600,netzzz);
		CalcLinkStats ls = new CalcLinkStats(netzzz);
		events.addHandler(h);
		
		TravelTimeCalculator tt = new TravelTimeCalculatorFactoryImpl().createTravelTimeCalculator(netzzz, c.travelTimeCalculator());
		events.addHandler(tt);
		new EventsReaderTXTv1(events).readFile(eventsFile);
		ls.addData(h, tt);
		ls.writeFile(stats);
		
		
	}

}
