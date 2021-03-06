package com.CometDatabaseHandler;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.CometDatabaseHandlerDAO.*;

/**
 * @author Students This class inserts data from a .json response into the comet
 *         AWS Oracle SQL database
 */
@SuppressWarnings("unused")
public class SQLDataInsertDAO extends ConnectionAbstractDAO implements SQLDataInsertDAOI {

	@Override
	public int createAccount(String username, String password, String email) {
		int autoGeneratedKey = -1;
		try {
			this.connect();
			ps = conn.prepareStatement(SQL.INSERT_ACCOUNT_DATA.getQuery(), new String[] { "account_id" });
			ps.setString(1, username);
			ps.setString(2, password);
			ps.setString(3, email);
			ps.setString(4, "");
			ps.setString(5, "");
			ps.execute();
			rs = ps.getGeneratedKeys();
			if (rs.next())
				autoGeneratedKey = rs.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			this.dispose();
		}
		return autoGeneratedKey;
	}

	@Override
	public int createAccount(String username, String password, String email, String fullName, String country) {
		int autoGeneratedKey = -1;
		try {
			this.connect();
			ps = conn.prepareStatement(SQL.INSERT_ACCOUNT_DATA.getQuery(), new String[] { "account_id" });
			ps.setString(1, username);
			ps.setString(2, password);
			ps.setString(3, email);
			ps.setString(4, fullName);
			ps.setString(5, country);
			ps.execute();
			rs = ps.getGeneratedKeys();
			if (rs.next())
				autoGeneratedKey = rs.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			this.dispose();
		}
		return autoGeneratedKey;
	}

	@Override
	public JSONObject connectToHistoricalFile(int fileNumber, String coin) throws Exception {
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(new FileReader("C:\\Users\\Students\\SourceControl\\comet\\CryptoCollector\\data\\"
				+ coin + "\\" + coin + "HistDaily" + String.valueOf(fileNumber) + ".json"));
		JSONObject jsonObject = (JSONObject) obj;
		if (!jsonObject.get("Response").equals("Success"))
			return null;

		return jsonObject;
	}

	public JSONObject connectToAllCoinsFile() throws Exception {
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(
				new FileReader("C:\\Users\\Students\\SourceControl\\comet\\CryptoCollector\\data\\AllCoins.json"));
		JSONObject jsonObject = (JSONObject) obj;
		if (!jsonObject.get("Response").equals("Success"))
			return null;

		return jsonObject;
	}

	@Override
	public boolean parseHistCoinData(String coin) throws Exception {
		// check for amount of files in folder, from 0 to max-1 (not including basic
		// info file)
		try {
			int length = new File("C:\\Users\\Students\\SourceControl\\comet\\CryptoCollector\\data\\" + coin)
					.list().length;
			for (int i = 0; i < length; i++) {
				JSONObject jsonObject = connectToHistoricalFile(i, coin);
				if (jsonObject != null) {

					// Initialize/Instantiate Json data values
					JSONArray coinData = (JSONArray) jsonObject.get("Data");
					long timestamp;
					int coinID = getCoinID(coin);
					double open, close, high, low, volumeTo, volumeFrom;

					// coin is not found in database
					if (coinID < 0)
						return false;

					@SuppressWarnings("unchecked")
					Iterator<JSONObject> iterator = coinData.iterator();
					while (iterator.hasNext()) {
						// iterates through entire array and inserts into historical table
						JSONObject o = (JSONObject) iterator.next();
						timestamp = (Long) o.get("time");
						volumeTo = ((Number) o.get("volumeto")).doubleValue();
						volumeFrom = ((Number) o.get("volumefrom")).doubleValue();
						open = ((Number) o.get("open")).doubleValue();
						close = ((Number) o.get("close")).doubleValue();
						high = ((Number) o.get("high")).doubleValue();
						low = ((Number) o.get("low")).doubleValue();
						insertCoinHistoricalData(coinID, timestamp, open, close, high, low, volumeTo, volumeFrom);
					}
				}
			}
			return true;
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (SQLIntegrityConstraintViolationException e) {
			System.out.println("ERR: Coin in database");
		} catch (IOException e) {
			System.out.println("ERR: File not found");
		}
		return false;
	}

	@Override
	public boolean parseBasicCoinData(String coin) throws Exception {
		JSONObject jsonObject = connectToAllCoinsFile();

		if (jsonObject != null) {
			try {
				JSONObject allCoins = (JSONObject) jsonObject.get("Data");
				JSONObject coinData = (JSONObject) allCoins.get(coin);

				String symbol = (String) coinData.get("Symbol");
				String coinName = (String) coinData.get("CoinName");
				double maxSupply = Double.valueOf((String) coinData.get("TotalCoinSupply"));
				insertCoinBasicData(symbol, coinName, maxSupply);
				return true;
			} catch (NullPointerException e) {
				System.out.println("ERR: Coin not found");
			} catch (SQLIntegrityConstraintViolationException e) {
				System.out.println("ERR: Coin in database");
			}

		}

		return false;
	}

	@Override
	public boolean insertAccountData() throws SQLException {
		try {
			this.connect();
			ps = conn.prepareStatement(SQL.INSERT_COIN_BASIC_DATA.getQuery());

			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			this.dispose();
		}
		return false;
	}

	@Override
	public boolean insertCoinBasicData(String symbol, String coinName, double maxSupply) throws SQLException {
		try {
			this.connect();
			ps = conn.prepareStatement(SQL.INSERT_COIN_BASIC_DATA.getQuery());
			ps.setString(1, symbol);
			ps.setString(2, coinName);
			ps.setDouble(3, maxSupply);
			ps.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			this.dispose();
		}
		return false;
	}

	// TODO test - need to refactor - preparing statements.
	@Override
	public boolean insertCoinHistoricalData(int coinID, long timestamp, double open, double close, double high,
			double low, double volumeTo, double volumeFrom) throws SQLException {
		try {
			this.connect();
			ps = conn.prepareStatement(SQL.INSERT_COIN_HISTORICAL_DATA.getQuery());
			ps.setInt(1, coinID);
			ps.setLong(2, timestamp);
			ps.setDouble(3, open);
			ps.setDouble(4, close);
			ps.setDouble(5, high);
			ps.setDouble(6, low);
			ps.setDouble(7, volumeTo);
			ps.setDouble(8, volumeFrom);
			ps.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			this.dispose();
		}
		return false;
	}

	// TODO implement
	@Override
	public boolean assignUserCoin(String symbol) throws SQLException {
		try {
			this.connect();
			if (getCoinID(symbol) == -1)
				return false;
			else {

			}
			ps = conn.prepareStatement(SQL.INSERT_COIN_BASIC_DATA.getQuery());
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			this.dispose();
		}
		return false;
	}

	@Override
	public int getCoinID(String symbol) throws SQLException {
		try {
			this.connect();
			ps = conn.prepareStatement(SQL.LOOK_FOR_COIN.getQuery());
			ps.setString(1, symbol);
			rs = ps.executeQuery();
			if (rs.next())
				return rs.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			this.dispose();
		}
		return -1;
	}
}