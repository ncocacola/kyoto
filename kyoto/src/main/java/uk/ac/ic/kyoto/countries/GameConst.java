package uk.ac.ic.kyoto.countries;

public final class GameConst {
	
	// GDP Growth Values
	public static final double GROWTH_MARKET_STATE = 0.05;
	public static final double STABLE_MARKET_STATE = 0.02;
	public static final double RECESSION_MARKET_STATE = 0.03;
	
	public static final double GROWTH_MARKET_CHANCE = 0.1;
	public static final double STABLE_MARKET_CHANCE = 0.8;
	public static final double RECESSION_MARKET_CHANCE = 0.1;
	
	@Deprecated
	public static final int ECONOMIC_OUTPUT_REDUCTION = 1; // Deprecated constant
	
	/**
	 * The cost of investment in one extra ton of carbon per year
	 */
	public static final double CARBON_INVESTMENT_PRICE = 1;
	
	/**
	 * Normalises value of GDP rate increase
	 */
	public static final double GROWTH_SCALER = 0.001;
	
	/**
	 * Percentage of GDP available to spend on development
	 */
	public static final double PERCENTAGE_OF_GDP = 0.1;
	
	
	/**
	 * The price of reducing carbon reduction by one unit when we have 100% dirty industry
	 */
	public static double CARBON_REDUCTION_PRICE_MIN = 100;
	/**
	 * The price of reducing carbon reduction by one unit when we have 0% dirty industry
	 */
	public static double CARBON_REDUCTION_PRICE_MAX = 2000;
	
	/**
	 * Coefficient to calculate the cost of carbon absorption
	 */
	public static float CARBON_ABSORPTION_COEFF = 1;
	
	/**
	 * Size of a block of arable land
	 */
	public static float FOREST_BLOCK_SIZE = 100;
	
	/**
	 * Amount of carbon (in tons) that you offset with a single unit of forest PER YEAR
	 */
	public static long FOREST_CARBON_ABSORPTION = 10;
	
	/**
	 * Percentage of GDP that is levied from availiableToSpend for monitoring
	 */
	public static double MONITOR_COST_PERCENTAGE = 0.005;
	
	/**
	 * Percentage of GDP taken from country if they fail to meet their targets.  
	 * Increases in linear amounts for each transgression.
	 */
	public static double SANCTION_RATE = 0.05;
	
	/**
	 * Price for each monitor
	 */
	public static double MONITORING_PRICE = 5000;
}
