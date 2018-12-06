package com.CometDatabaseHandlerDAO;

import java.sql.SQLException;

import org.json.simple.JSONObject;

/**
 * @author Michael Sy
 *
 */
public interface SQLDataInsertDAOI {
	enum SQL{
		// TABLE DATA INSERTION
		INSERT_ACCOUNT_DATA("INSERT INTO accounts VALUES (?, ?, ?, ?, ?, ?"),
		INSERT_COIN_BASIC_DATA("INSERT INTO coinbasicinfo"
				+ "(SYMBOL, COIN_NAME, MAX_SUPPLY) "
				+ "VALUES (?, ?, ?)"),
		INSERT_COIN_HISTORICAL_DATA("INSERT INTO coinhistoricalinfo"
				+ "(COIN_ID, COIN_TIMESTAMP, OPEN_PRICE, CLOSE_PRICE, HIGH_PRICE, LOW_PRICE, VOLUMETO, VOLUMEFROM)"
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"),
		ASSIGN_USER_COINS("INSERT INTO usercoins VALUES (?,?)"),
		
		// USED TO VERIFY COIN FOR ASSIGNING USER COINS
		LOOK_FOR_COIN("SELECT coin_id FROM coinbasicinfo where symbol = ?");
		
		private String query;
		private SQL(String s) {
			this.query = s;
		}
		public String getQuery() {
			return query;
		}
	}
	
	/** Connects to a particular JSON response file. Helper method for parseHistoricalCoinData.
	 * @return false if unable to connect to a file, true if connects to a file.
	 */
	JSONObject connectToHistoricalFile(int fileNumber, String coin) throws Exception;
	
	
	/** Helper method for insertBasicCoinInfo; gets basic information from mined data
	 * @param coin
	 * @return JSONObject for parsing data
	 * @throws Exception
	 */
	JSONObject connectToAllCoinsFile()throws Exception;
	
	/** Connects to a series of JSON files in a particular coin's folder.
	 * @return false if unable to connect to a file, true if connects to a file.
	 */
	boolean parseHistCoinData(String coin) throws Exception;
	
	/** Connects to a JSON file containing basic information of a particular coin
	 * @return false if unable to connect to a file, true if connects to a file.
	 */
	boolean parseBasicCoinData(String coin) throws Exception;
	
	/** Takes the information from the JSON response and inserts the data to Accounts table in the database.
	 * @return false if unable to insert data, true if data is inserted.
	 */
	boolean insertAccountData() throws SQLException;
	
	/**Takes the information from the JSON response and inserts the data to CoinBasicInfo table in the database.
	 * @param symbol
	 * @param coinName
	 * @param maxSupply
	 * @return false if unable to insert data, true if data is inserted.
	 * @throws SQLException
	 */
	boolean insertCoinBasicData(String symbol, String coinName, double maxSupply) throws SQLException;
	
	/**Takes the information from the JSON response and inserts the data to CoinHistoricalInfo table in the database.
	 * @param coinID
	 * @param timestamp
	 * @param open
	 * @param close
	 * @param high
	 * @param low
	 * @param volumeTo
	 * @param volumeFrom
	 * @return false if unable to insert data, true if data is inserted.
	 * @throws SQLException
	 */
	boolean insertCoinHistoricalData(int coinID, long timestamp, double open, double close, double high, double low, double volumeTo, double volumeFrom) throws SQLException;
	
	/** Links a user to a coin by inserting a values into UserCoins
	 * @return false if unable to insert data, true if data is inserted.
	 */
	boolean assignUserCoin(String symbol) throws SQLException;
	
	
	/** Queries the database for the coin id. Returns -1 if not found.
	 * @param symbol
	 * @return the coin id from the database
	 * @throws SQLException
	 */
	int getCoinID(String symbol) throws SQLException;

}
