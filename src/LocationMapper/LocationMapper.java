package LocationMapper;


import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.joda.time.DateTime;

import LatLongParser.Column;
import LatLongParser.LatLongParser;
import TextParser.Location;
import TextParser.PartManager;
import TextParser.TextParser;


public class LocationMapper 
{
	public static String workDir = "";
	public static String logDir = "";
	public static String shapeDataDir = "./../../persistant/location_data";// /home/ryan/base/bin/LocationMapperV3/data"; //D:\\data; //./../../persistant/location_data
	//public static String textDir = "text";
	
	public DateTime startDateTime;

//	public SQLConnection sqlConnectionIN;
//	public SQLConnection sqlConnectionOUT;
	
	
	public LatLongParser latLongParser;
	//public TextParser textParser;
	public PartManager partManager;
	
	
	public static HashMap<String, Long> hits = new HashMap<String, Long>();
	public static HashMap<String, Integer> users = new HashMap<String, Integer>();
	
	
//	public static HashMap<String, Integer> states = new HashMap<String, Integer>();
//	public static HashMap<String, Integer> cities = new HashMap<String, Integer>();
//	public static HashMap<String, Integer> postals = new HashMap<String, Integer>();
//	
	
//	public void parseTweetData(float lat, float lon, String loc, String lan)
//	{
//		
//
//		float latitude = lat;
//		float longitude =  lon;
//		String twitter_user_location = loc;
//		String twitter_user_lang = lan;
//		
//	
//		this.ProcessAndUpdate(0, latitude, longitude, twitter_user_location, twitter_user_lang);
//
//	}
	
	public long TEMP_totalGeoTweets = 0;
	public long TEMP_totalRawTweets = 0;
	

	@SuppressWarnings("unchecked")
	public LocationMapper(String[] args)
	{
		Log.doConsolePrint = true;
		Log.doLog = false;
		
	
		

		try
		{
			shapeDataDir = args[0];
		}
		catch (Exception e)
		{
			Log.log("No dataDir set, defualting to: " + shapeDataDir);
		}
		
		
		this.logDir = shapeDataDir + "/logs";
		
		
		this.startDateTime = new DateTime();
		
		Log.log("Starting : " + startDateTime);
//		Log.log("address = " + address);
//		Log.log("tableName = " + serverName);
//		Log.log("port = " + port);
//		Log.log("userName = " + userName);
//		Log.log("password = " + password);
		Log.log("shapeDataDir = " + shapeDataDir);
		Log.log(Log.breakString);
		
		
		if (!new File(shapeDataDir).isDirectory())
		{
			Log.log(shapeDataDir + "does not exist!    Exiting(-1)");
			Exit(-1);
		}
		
		if (new File("out/users.dat").isFile() &&  new File("out/hits.txt").isFile() && new File("out/rawData.json").isFile())
		{
			Log.log("loading data from last session");
			Log.log("\tloading users.dat");
			LocationMapper.users = (HashMap<String, Integer>) TextParser.loadSerializedData("out/users.dat", null, true);
			
			Log.log("\tloading hits.txt");
			for(String string : TextParser.LoadTextFile("out/hits.txt", null, false))
			{
				String[] strings = string.split("=");		
				Long tempInt = Long.parseLong(strings[1]);
				hits.put(strings[0], tempInt);
			}
			
			TEMP_totalGeoTweets = hits.get("totalGeoTweets");
			TEMP_totalRawTweets = hits.get("totalRawTweets");
			
			hits.remove("totalGeoTweets");
			hits.remove("totalRawTweets");
			
			
			Log.log("totalRawTweets=" + TEMP_totalRawTweets);
			Log.log("runningLocTotal=" + TEMP_totalGeoTweets);			
		}
		else
			Log.log("No data from perevious session found! FYI");
		
		
////		test connection to server
//		sqlConnectionIN = new SQLConnection(address, serverName, port, userName, password);//(String address, String tableName, String port, String userName, String passwo
//		if(this.sqlConnectionIN.Connect() == false)
//		{
//			Log.log("Unable to connect to to sqlServer IN");
//			Log.log("Exiting");
//			Exit(1);
//			return;
//		}
//		try {
//			sqlConnectionIN.connection.setAutoCommit(false);
//		} catch (SQLException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		
//		
//		sqlConnectionOUT = new SQLConnection(address, serverName, port, userName, password);//(String address, String tableName, String port, String userName, String password)
//		if(this.sqlConnectionOUT.Connect() == false)
//		{
//			Log.log("Unable to connect to to sqlServer OUT");
//			Log.log("Exiting");
//			Exit(1);
//			return;
//		}
		
		
	
		//load textParser
		TextParser textParser = new TextParser();
		textParser.loadText(shapeDataDir);
		textParser.CreateMasterOut(shapeDataDir);
	

		//build partmap
		partManager = new PartManager(textParser.allLoc, "[, \\./\\\\]");
		
		
		

		//load LatLongParser
		latLongParser = new LatLongParser();
		latLongParser.loadData(shapeDataDir);
		
	
		
		//get data from server
//		ResultSet results = sqlConnectionIN.getData();
//		
//		Log.log("Got results!");
//		Log.log("Processing Records");

		//Map Locations

		
		
	
//		try 
//		{
//			Log.log("Total ParseCount = " + sqlConnectionOUT.parseCount);
//			results.close();
//			
//		} 
//		catch (SQLException e)
//		{
//			Log.log("error closing reslutSet", e);
//		}
//		
		
		
		//Exit(0);
	}
	
	
	
	
	public void ProcessAndUpdate(int id, float latitude, float longitude, String twitter_user_location, String twitter_user_lang)
	{

		
		
		
		twitter_user_location = TextParser.makeSuperNice(twitter_user_location);	
		
	
		
//		if(twitter_user_lang.equals("es"))
//		{
//			String temp = twitter_user_location;
//			while(true)
//			{
//				twitter_user_location = twitter_user_location.replaceAll( " de | la | me | en | mi | el | del |el " , " ");
//				
//				if(temp.equals(twitter_user_location))
//					break;
//
//				 temp = twitter_user_location;
//			}
//		}
	
		

		Record record = new Record(id, latitude, longitude, twitter_user_location, twitter_user_lang);
		
		
		latLongParser.setPossableLocations(record);
		partManager.setMatchStrings(record);
		

	
		HashMap<String, Location> cities = new HashMap<String, Location>();
		HashMap<String, Location>  states = new HashMap<String, Location>();
		HashMap<String, Location> countires = new HashMap<String, Location>();
		
		

		for (Location loc : record.possableLocations)
		{
			if(loc.column == Column.city)
			{
				cities.put(loc.getKey(), loc);	
			}
			else if(loc.column == Column.state_province)
			{
				states.put(loc.getKey(), loc);
			}
			else if(loc.column == Column.country)
			{
				countires.put(loc.getKey(), loc);
			}	
		}
		
		boolean hasES = false;
		if(record.twitter_user_lang.equals("es"))
		{
			for ( String string : new ArrayList<String>(cities.keySet()))
			{
				if(cities.get(string).countryCode.equals("es"))
				{
					hasES = true;
							break;
				}
				
			}
			
			if(hasES)
			{
				for ( String string : new ArrayList<String>(cities.keySet()))
				{
					if(cities.get(string).countryCode.equals("es") == false)
					{
						cities.remove(string);
					}
					
				}
				
			}
			else
			{
				for ( String string : new ArrayList<String>(states.keySet()))
				{
					if(states.get(string).countryCode.equals("es"))
					{
						hasES = true;
								break;
					}
					
				}
			}
			
			if(hasES)
			{
				for ( String string : new ArrayList<String>(states.keySet()))
				{
					if(states.get(string).countryCode.equals("es") == false)
					{
						states.remove(string);
					}
					
				}
				for ( String string : new ArrayList<String>(countires.keySet()))
				{
					if(countires.get(string).countryCode.equals("es") == false)
					{
						countires.remove(string);
					}
					
				}
				
			}
			
			
		}
		
//		long pop = -1;
//		Location highestPop = null;
//		for (Location loc : cities.values())
//		{
//			if(loc.population > pop)
//			{
//				highestPop = loc;
//				pop = loc.population;
//			}
//		}
//		record.textData.put(Column.city, highestPop);
		
		
		
		
//		
//		
//		for (Location loc : cities.values())
//		{
//			String key = loc.getStateKey();
//			if(states.containsKey(loc.getStateKey()))// we have a cityLocation that is in a states we also have
//			{
//				loc.hits++;
//				states.get(loc.getStateKey()).hits++;
//			}
//			key = loc.getCountryKey();
//			if(countires.containsKey(loc.getCountryKey()))// we have a countiresLocation that is in a states we also have
//			{
//				loc.hits++;
//				countires.get(loc.getCountryKey()).hits++;
//			}
//		}
//		for (Location loc : states.values())
//		{
//			String key = loc.getCountryKey();
//			if(countires.containsKey(loc.getCountryKey()))// we have a cityLocation that is in a states we also have
//			{
//				loc.hits++;
//				countires.get(loc.getCountryKey()).hits++;
//			}
//		}
//		

		long pop = -1;
		Location highestPop = null;
		for (Location loc : cities.values())
		{
			if(loc.population > pop)
			{
				highestPop = loc;
				pop = loc.population;
			}
			
//			if(loc.getCountryKey().equals("US._._") )
//				record.textData.put(Column.city, loc);
		}
		if(highestPop != null)
			record.textData.put(Column.city, highestPop);
		
		
		pop = -1;
		highestPop = null;
		for (Location loc : states.values())
		{
			if(loc.population > pop)
			{
				highestPop = loc;
				pop = loc.population;
			}
			
//			if(loc.getCountryKey().equals("us._._") )
//				record.textData.put(Column.city, loc);
		}
		if(highestPop != null)
			record.textData.put(Column.state_province, highestPop);
		
		pop = -1;
		highestPop = null;
		for (Location loc : countires.values())
		{
			if(loc.population > pop)
			{
				highestPop = loc;
				pop = loc.population;
			}
		}
		if(highestPop != null)
			record.textData.put(Column.country, highestPop);
		
		
		
		
	
		
		
//		
//		
//		if(record.textData.containsKey(Column.country) == false) //no country 
//		{
//			if(record.textData.containsKey(Column.state_province) == false)  //no state
//			{
//				if(record.textData.containsKey(Column.city) )//&& cities.size() <= 5) // no country or state yes city
//				{
//					String stateKey = record.textData.get(Column.city).getStateKey();
//					Location stateLocation = partManager.allLocations.get(stateKey);
//					
//					if(stateLocation != null) 														//try fill state from city
//						record.textData.put(Column.state_province, stateLocation);
//					else
//					{
//						int asdf = 234;
//					}
//				}
//			}
//			if(record.textData.containsKey(Column.state_province)  && states.size() <= 5)  //yes state
//			{
//				String countryKey = record.textData.get(Column.state_province).getCountryKey();
//				Location countryLocation = partManager.allLocations.get(countryKey);
//				
//				if(countryLocation != null) 														//try fill country from state
//					record.textData.put(Column.country, countryLocation);	
//			}
//		}
		

		record.getUpdateStatement();
		//sqlConnectionOUT.updateRecord(record);
		

	}
	
	

	
	
	
	
	
	
	public static void Exit(int status)
	{
		if(Log.doLog)
			Log.saveLog(logDir, "log_" + new DateTime().toString().replaceAll(":", ".") + ".log", false);
		System.exit(status);
	}
	
	
	
}
