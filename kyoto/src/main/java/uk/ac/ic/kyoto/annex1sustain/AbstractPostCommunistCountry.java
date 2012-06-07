package uk.ac.ic.kyoto.annex1sustain;

import java.util.ArrayList;
import java.util.UUID;
import java.util.List;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.ic.kyoto.countries.NotEnoughCarbonOutputException;
import uk.ac.ic.kyoto.countries.NotEnoughCashException;
import uk.ac.ic.kyoto.countries.NotEnoughLandException;
import uk.ac.ic.kyoto.market.Economy;
import uk.ac.ic.kyoto.market.FossilPrices;
import uk.ac.ic.kyoto.services.TimeService;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.simulator.EndOfTimeCycle;

/**
 * 
 * @author Adam, Piotr
 */
public class AbstractPostCommunistCountry extends AbstractCountry {
	
	//================================================================================
    // PrivateFields
    //================================================================================
	
	// TODO add comments
	protected long	 		internalPrice;					//
	protected List<Double> 	uncommittedTransactionsCosts;	//
	protected List<Double> 	committedTransactionsCosts;		//
	protected long 			creditsToSell;					//
	protected long 			creditsToSellTarget;			//
	protected long			absorptionInvestmentTarget;		//
	protected long			reductionInvestmentTarget;		//
	protected double		lastYearFactor;					// wtf is factor?
		
	//================================================================================
    // Constructors
    //================================================================================
	
	public AbstractPostCommunistCountry(UUID id, String name, String ISO,
			double landArea, double arableLandArea, double GDP, double GDPRate,
			long availiableToSpend, long emissionsTarget, long carbonOffset, long energyOutput)
	{
		super(id, name, ISO, landArea, arableLandArea, GDP, GDPRate, emissionsTarget,
				carbonOffset, energyOutput, energyOutput, energyOutput);
		
		// TODO make sure the initialisation is fine
		this.internalPrice = Long.MAX_VALUE;
		this.uncommittedTransactionsCosts = new ArrayList<Double>();
		this.committedTransactionsCosts = new ArrayList<Double>();
		this.creditsToSell = 0;
		this.creditsToSellTarget = 0;
		this.absorptionInvestmentTarget = Constants.MINIMAL_INVESTMENT;
		this.reductionInvestmentTarget = Constants.MINIMAL_INVESTMENT;
		this.lastYearFactor = 1;
		
	}
	
	//================================================================================
    // Overridden functions
    //================================================================================
	
	@Override
	protected void processInput(Input input) {
		// TODO Auto-generated method stub
	}

	@Override
	public void YearlyFunction() {
		calculateLastYearFactor();
		calculateNewSellingTarget();
		logger.info("Internal Yearly Data of Post-Communist Country " + this.getName() + " was updated");
	}

	@Override
	public void SessionFunction() {
		
	}
	
	//================================================================================
    // Public methods to update data
    //================================================================================
	
	/**
	 * Updates the internal data that is supposed to change every tick
	 *  
	 * @param e
	 * The event that is called every simulation tick
	 */
	@EventListener
	public void updateTickData(EndOfTimeCycle e) {
		updateUncommittedTransactions();
		updateCommittedTransactions();
		updateInternalPrice();
		logger.info("Internal Data of Post-Communist Country " + this.getName() + " was updated");
		makeInvestments();
	}
	
	//================================================================================
    // Private functions called every tick
    //================================================================================
	
	private void updateInternalPrice() {
		internalPrice   = 	(long)
							(
							calculateMarketPrice() * 
							calculateEndOfRoundFactor() * 
							lastYearFactor
							);
	}

	private double calculateMarketPrice() {
		double marketPrice;
		double maximumCommittedPrice = 0;
		double minimumUncommittedPrice = Double.MAX_VALUE;
		
		try {
			// Find maximum price of the committed transactions
			for (double price : committedTransactionsCosts) {
				if (price > maximumCommittedPrice)
					maximumCommittedPrice = price;
			}
			
			// Find minimum price of the uncommitted transactions
			for (double price : uncommittedTransactionsCosts) {
				if (price < minimumUncommittedPrice)
					minimumUncommittedPrice = price;
			}
			marketPrice = (maximumCommittedPrice + minimumUncommittedPrice) / 2;
		}
		catch (Exception e) {
			logger.warn("Problem calculating marketPrice: " + e);
			marketPrice = 0;
		}
		
		return marketPrice;
	}
	
	private double calculateEndOfRoundFactor() {
		double endOfRoundFactor = 1;
		try {
			// get ticksToEndOfRound from Time service
			TimeService timeService = getEnvironmentService(TimeService.class);
			int ticksToEndOfRound = timeService.getTicksInYear() - timeService.getCurrentTick();
			
			if(ticksToEndOfRound < Constants.END_OF_ROUND_MINIMUM_NUMBER_OF_TICKS)
				endOfRoundFactor = 	Constants.END_OF_ROUND_FACTOR_SLOPE *
									(
										Constants.NUMBER_OF_TICKS_IN_ROUND
										- Constants.END_OF_ROUND_MINIMUM_NUMBER_OF_TICKS
										- ticksToEndOfRound
									);
		}
		catch (Exception e) {
			logger.warn("Problem calculating endOfRoundFactor: " + e);
			endOfRoundFactor = 1;
		}
		return endOfRoundFactor;
	}
	
	private void updateUncommittedTransactions() {
		// TODO implement
	}
	
	private void updateCommittedTransactions() {
		// TODO implement
	}
	
	private void increaseAbsorptionInvestmentTarget() {
		absorptionInvestmentTarget = (long) (absorptionInvestmentTarget * Constants.INVESTMENT_SCALING);
		if (absorptionInvestmentTarget > Constants.MAXIMAL_INVESTMENT) {
			absorptionInvestmentTarget = Constants.MAXIMAL_INVESTMENT;
		}
	}
	
	private void decreaseAbsorptionInvestmentTarget() {
		absorptionInvestmentTarget = (long) (absorptionInvestmentTarget / Constants.INVESTMENT_SCALING);
		if (absorptionInvestmentTarget < Constants.MINIMAL_INVESTMENT) {
			absorptionInvestmentTarget = Constants.MINIMAL_INVESTMENT;
		}
	}
	
	private void increaseReductionInvestmentTarget() {
		reductionInvestmentTarget = (long) (reductionInvestmentTarget * Constants.INVESTMENT_SCALING);
		if (reductionInvestmentTarget > Constants.MAXIMAL_INVESTMENT) {
			reductionInvestmentTarget = Constants.MAXIMAL_INVESTMENT;
		}
	}
	
	private void decreaseReductionInvestmentTarget() {
		reductionInvestmentTarget = (long) (reductionInvestmentTarget / Constants.INVESTMENT_SCALING);
		if (reductionInvestmentTarget < Constants.MINIMAL_INVESTMENT) {
			reductionInvestmentTarget = Constants.MINIMAL_INVESTMENT;
		}
	}
	
	private void carbonAbsorptionInvestment () {
		long investmentCost;
		long potentialProfit;
		
		try {
			investmentCost = carbonAbsorptionHandler.getCost(Constants.MINIMAL_INVESTMENT);
			potentialProfit = Constants.MINIMAL_INVESTMENT * internalPrice;
			
			if (potentialProfit > investmentCost) {
				carbonAbsorptionHandler.invest(investmentCost);
				increaseAbsorptionInvestmentTarget();
				logger.info("Post-Communist Country " + this.getName() + " invested " + String.valueOf(investmentCost) + " in carbon absorption");
			}
			else {
				decreaseAbsorptionInvestmentTarget();
				logger.info("Post-Communist Country " + this.getName() + " deemed carbon absorption not profitable");
			}
		}
		catch (NotEnoughCashException e) {
			decreaseAbsorptionInvestmentTarget();
			logger.info("Post-Communist Country " + this.getName() + " has insufficient funds for carbon absorption");
		}
		catch (NotEnoughLandException e) {
			decreaseAbsorptionInvestmentTarget();
			logger.info("Post-Communist Country " + this.getName() + " has insufficient land for carbon absorption");
		}
		catch (Exception e) {
			logger.warn("Problem investing in carbon absorption");
		}
	}
	
	private void carbonReductionInvestment () {
		long investmentCost;
		long potentialProfit;
		
		try {
			investmentCost = carbonReductionHandler.getCost(Constants.MINIMAL_INVESTMENT);
			potentialProfit = Constants.MINIMAL_INVESTMENT * internalPrice;
			
			if (potentialProfit > investmentCost) {
				carbonReductionHandler.invest(investmentCost);
				increaseReductionInvestmentTarget();
				logger.info("Post-Communist Country " + this.getName() + " invested " + String.valueOf(investmentCost) + " in carbon reduction");
			}
			else {
				decreaseReductionInvestmentTarget();
				logger.info("Post-Communist Country " + this.getName() + " deemed carbon reduction not profitable");
			}
		}
		catch (NotEnoughCashException e) {
			decreaseReductionInvestmentTarget();
			logger.info("Post-Communist Country " + this.getName() + " has insufficient funds for carbon reduction");
		}
		catch (NotEnoughCarbonOutputException e) {
			decreaseReductionInvestmentTarget();
			logger.info("Post-Communist Country " + this.getName() + " has insufficient carbon output for carbon reduction");
		}
		catch (Exception e) {
			logger.warn("Problem investing in carbon reduction");
		}
	}
	
	private void otherCountriesInvestment () {
		// TODO implement
		//   There are no handlers for investing in other countries yet
	}
	
	private void makeInvestments() {
		carbonAbsorptionInvestment();
		carbonReductionInvestment();
		otherCountriesInvestment();
	}
	
	//================================================================================
    // Private functions called every year
    //================================================================================
	
	/**
	 * Gets the number of credits available to sell.
	 * Multiplies it by a constant factor and returns it.
	 */
	private double calculateAvailableCreditsFactor() {
		double availableCreditsFactor;
		
		try {
			availableCreditsFactor = carbonOffset * Constants.SELL_AMOUNT_COEFFICIENT;
		}
		catch (Exception e) {
			logger.warn("Problem when calculating availableCreditsFactor " + e);
			availableCreditsFactor = carbonOffset;
		}
		return availableCreditsFactor;
	}
	
	/**
	 * Gets oil and gas prices from Market data.
	 * Calculates a gradient of change, and returns an appropriate factor.
	 */
	private double calculateFossilFuelsFactor() {
		double fossilFuelsFactor;
		
		try {			
			
			// get current year from the Time service
			TimeService timeService = getEnvironmentService(TimeService.class);
			int currentYear = timeService.getCurrentYear();
			
			// get the data from the FossilPrices Service
			FossilPrices fossilPrices = getEnvironmentService(FossilPrices.class);
			double newOilPrice = fossilPrices.getOilPrice(currentYear);
			double oldOilPrice = fossilPrices.getOilPrice(currentYear - 1);
			double newGasPrice = fossilPrices.getGasPrice(currentYear);
			double oldGasPrice = fossilPrices.getGasPrice(currentYear - 1);
			
			// if the data is relevant, calculate the gradients and the coefficient
			if ((newOilPrice != 0) && (oldOilPrice != 0) && (newGasPrice != 0) && (oldGasPrice != 0) ) {
				double oilGradient = (newOilPrice - oldOilPrice) / oldOilPrice;
				double gasGradient = (newGasPrice - oldGasPrice) / oldGasPrice;
				fossilFuelsFactor = Constants.FOSSIL_FUEL_PRICE_COEFFICIENT * (oilGradient + gasGradient) / 2;
			}
			
			// if the data is irrelevant, coefficient becomes irrelevant.
			else
				fossilFuelsFactor = 1;
		}
		catch (UnavailableServiceException e) {
			logger.warn("Unable to reach the fossil fuel service: " + e);
			fossilFuelsFactor = 1;
		}
		catch (Exception e) {
			logger.warn("Problem when calculating fossilFuelsFactor " + e);
			fossilFuelsFactor = 1;
		}
		return fossilFuelsFactor;
	}
	
	/**
	 * Returns a factor based on the current state of economy.
	 */
	private double calculateMarketFactor() {
		double marketFactor;
		
		try {
			Economy economy = getEnvironmentService(Economy.class);
			
			switch (economy.getEconomyState()) {
				case GROWTH:
					marketFactor = 1 + Constants.MARKET_STATE_COEFFICIENT;
					break;
				case RECESSION:
					marketFactor =  1 - Constants.MARKET_STATE_COEFFICIENT;
					break;
				default:
					marketFactor = 1;
					break;
			}
		}
		catch (Exception e) {
			logger.warn("Problem when calculating marketFactor " + e);
			marketFactor = 1;
		}
		return marketFactor;
	}
	
	/**
	 * Returns a new target, which is a multiplication of three factors:
	 * - available credits
	 * - fossil fuels historical prices
	 * - current state of the market
	 * All adjusted with a constant coefficient.
	 */
	protected void calculateNewSellingTarget() {
		long newSellingTarget;
		
		try {
			// Calculate new target based on three factors
			newSellingTarget =	(long) 
						( calculateAvailableCreditsFactor() *
						  calculateFossilFuelsFactor() *
						  calculateMarketFactor() );
			
			// Adjust the new target if out of possible range
			if (newSellingTarget > carbonOffset) {
				newSellingTarget = carbonOffset;
			}
		}
		catch (Exception e) {
			logger.warn("Problem when calculating newTarget " + e);
			newSellingTarget = creditsToSellTarget;
		}
		creditsToSellTarget = newSellingTarget;
	}
	
	/**
	 * Calculates the percentage of credits successfully sold in previous year.
	 * Returns the factor based on that percentage, which is used to set the price we sell at.
	 */
	private void calculateLastYearFactor() {
		double lastYearPercentageSold;
		
		try {
			// Calculate the percentage of successfully sold credits in the last year
			lastYearPercentageSold = (creditsToSellTarget - creditsToSell) / creditsToSellTarget;
			
			// Adjust if out of boundaries
			if (lastYearPercentageSold > 100) {
				logger.warn("The calculated percentage of carbon emission sold exceeded 100%");
				lastYearPercentageSold = 100;
			}
			if (lastYearPercentageSold < 0 ) {
				logger.warn("The calculated percentage of carbon emission sold was lower than 0%");
				lastYearPercentageSold = 0;
			}
		}
		catch (ArithmeticException e) {
			logger.warn("Division by 0 error: " + e);
			lastYearPercentageSold = 0;
		}
		
		try {
			// Calculate the factor
			lastYearFactor = 1 + Constants.LAST_YEAR_FACTOR_SLOPE * (lastYearPercentageSold - Constants.LAST_YEAR_FACTOR_OFFSET);
		}
		catch (Exception e) {
			logger.warn("Problem when calculating lastYearFactor " + e);
			lastYearFactor = 1;
		}
	}
}
