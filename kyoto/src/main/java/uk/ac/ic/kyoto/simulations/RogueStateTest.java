package uk.ac.ic.kyoto.simulations;

import java.util.HashSet;
import java.util.Set;
import uk.ac.ic.kyoto.CarbonData1990;
import uk.ac.ic.kyoto.actions.AddRemoveFromMonitorHandler;
import uk.ac.ic.kyoto.actions.AddToCarbonTargetHandler;
import uk.ac.ic.kyoto.actions.ApplyMonitorTaxHandler;
import uk.ac.ic.kyoto.actions.QueryEmissionsTargetHandler;
import uk.ac.ic.kyoto.actions.SubmitCarbonEmissionReportHandler;
import uk.ac.ic.kyoto.countries.CarbonTarget;
import uk.ac.ic.kyoto.countries.Monitor;
import uk.ac.ic.kyoto.countries.testCountries.DoNothing;
import uk.ac.ic.kyoto.countries.testCountries.GDPTestCount;
import uk.ac.ic.kyoto.nonannexone.NonAnnexOne;
import uk.ac.ic.kyoto.roguestates.USAgent;
import uk.ac.ic.kyoto.services.CarbonReportingService;
import uk.ac.ic.kyoto.services.Economy;
import uk.ac.ic.kyoto.services.GlobalTimeService;
import uk.ac.ic.kyoto.services.ParticipantCarbonReportingService;
import uk.ac.ic.kyoto.services.ParticipantTimeService;
import uk.ac.imperial.presage2.core.simulator.InjectedSimulation;
import uk.ac.imperial.presage2.core.simulator.Parameter;
import uk.ac.imperial.presage2.core.simulator.Scenario;
import uk.ac.imperial.presage2.core.util.random.Random;
import uk.ac.imperial.presage2.util.environment.AbstractEnvironmentModule;
import uk.ac.imperial.presage2.util.network.NetworkModule;
import uk.ac.imperial.presage2.util.participant.AbstractParticipant;
import com.google.inject.AbstractModule;

/**
 * 
 *Sits through multiple years and watches its cash do something
 * 
 * @author ct
 *
 */

public class RogueStateTest extends InjectedSimulation {
	
//	@Parameter(name="countries")
//	public int countries;

	@Override
	protected Set<AbstractModule> getModules() {
		
		Set<AbstractModule> modules = new HashSet<AbstractModule>();
		
		modules.add(new AbstractEnvironmentModule()
			.addGlobalEnvironmentService(GlobalTimeService.class)
			.addParticipantEnvironmentService(ParticipantTimeService.class)
			.addParticipantEnvironmentService(Economy.class)
/* No carbon targets in this sim, no Kyoto agreement at all! */
			.addParticipantEnvironmentService(ParticipantCarbonReportingService.class)
			.addActionHandler(SubmitCarbonEmissionReportHandler.class)
			.addActionHandler(AddToCarbonTargetHandler.class)
			.addActionHandler(QueryEmissionsTargetHandler.class)
			.addActionHandler(AddRemoveFromMonitorHandler.class)
			.addActionHandler(ApplyMonitorTaxHandler.class)
			.addGlobalEnvironmentService(CarbonReportingService.class)
			.addGlobalEnvironmentService(Monitor.class)
			.addGlobalEnvironmentService(CarbonTarget.class)
			);
		
		modules.add(NetworkModule.fullyConnectedNetworkModule().withNodeDiscovery());
		
		return modules;
	}

	public RogueStateTest(Set<AbstractModule> modules) {
		super(modules);
	}
	
	@Override
	protected void addToScenario(Scenario s) {
		
		// Germany Stats
		final double landArea = 348672;
		final double arableLandArea = 115698;
		final double GDP = 1976272728895.0;
		final double energyOutput = 1054601548;
		final double carbonOutput = 895533000;
		final double GDPRate = -0.0606;
		
			String name1 = "Russia";
			String ISO1 = "RU";
			AbstractParticipant p1 = new NonAnnexOne(Random.randomUUID(), name1, ISO1, landArea, arableLandArea, GDP, GDPRate, energyOutput, carbonOutput);
			s.addParticipant(p1);
			CarbonData1990.addCountry(ISO1, 928327000);
			
			String name2 = "US";
			String ISO2 = "US";
			AbstractParticipant p2 = new USAgent(Random.randomUUID(), name2, ISO2, landArea, arableLandArea, GDP, GDPRate, energyOutput, carbonOutput);
			s.addParticipant(p2);
			CarbonData1990.addCountry(ISO2, 131245789);
		
	}
}

