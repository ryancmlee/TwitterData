import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


import LocationMapper.LocationMapper;
import LocationMapper.Log;

import com.google.gson.Gson;

import twitter4j.*;



public class TwitterData  implements Serializable
{

	public static Object loadSerializedData(String primPath, String optionName, boolean compressed)
	{
		String fileLocation = primPath;
		if(optionName != null && optionName.equals("") == false)
			fileLocation += "/" + optionName;
		
		
		Object data = null;
		try
		{
			if(compressed)
			{
				FileInputStream fileIn = new FileInputStream(fileLocation);
				GZIPInputStream gzipIn = new GZIPInputStream(fileIn);
				ObjectInputStream in = new ObjectInputStream(gzipIn);
				data = in.readObject();
				in.close();
				gzipIn.close();
				fileIn.close();
			}
			else
			{
				FileInputStream fileIn = new FileInputStream(fileLocation);
				ObjectInputStream in = new ObjectInputStream(fileIn);
				data = in.readObject();
				in.close();
				fileIn.close();
			}
		}
		catch (Exception e)
		{
			System.out.println("loadSerializedData failed: " + e + ".  returning null...");
			return null;
		}

		return data;
	}
	public static boolean saveDataSerialized(Object data, String primPath, String optionName, boolean compressed)
	{
		String fileLocation = primPath;
		
		if(fileLocation.endsWith("/"))
			fileLocation = fileLocation.substring(0, fileLocation.length() - 1);
		if(optionName != null && optionName.equals("") == false)
			fileLocation += "/" + optionName;
		
		
		try
		{
			if(compressed)
			{
				FileOutputStream fileOut = new FileOutputStream(fileLocation);
				GZIPOutputStream gzipOut = new GZIPOutputStream(fileOut);
				ObjectOutputStream objectOut = new ObjectOutputStream(gzipOut);

				objectOut.writeObject(data);
				objectOut.close();
				gzipOut.close();
				fileOut.close();
			}
			else
			{
				FileOutputStream fileOut = new FileOutputStream(fileLocation);
				ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);

				objectOut.writeObject(data);
				objectOut.close();
				fileOut.close();
			}
		}
		catch (Exception e)
		{
			System.out.println(e);
			return false;
		}
		return true;
	}

	
	public static ArrayList<String> LoadTextFile(String primDir, String optionDir, boolean skipFirstLine)
	{
		ArrayList<String> data = new ArrayList<String>();

		String fileLocation = primDir;
		if(optionDir != null && optionDir.equals("") == false)
			fileLocation += "/" + optionDir;

		try
		{
			FileInputStream inStream = new FileInputStream(fileLocation);
			DataInputStream in = new DataInputStream(inStream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));


			if(skipFirstLine)
				br.readLine();

			String string;
			while ((string = br.readLine()) != null)   
			{
				if(string.startsWith("//") || string.trim().equals(""))  //wont be null cause of   while ((string = br.readLine()) != null) 
					continue;

				data.add(string);
			}

			br.close();
			in.close();
			inStream.close();
		}
		catch (Exception e)
		{
			System.out.println("ERROR in LoadTextFile " + e);
		}

		return data;
	}
	public static boolean writeText(String primDir, String optionDir, ArrayList<String> data, boolean append)
	{

		String fileLocation = primDir;

		if(optionDir != null && optionDir.equals("") == false)
			fileLocation += "/" + optionDir;
		try
		{

			BufferedWriter out = new BufferedWriter(new FileWriter(new File(fileLocation), append));

			for(String line : data)
			{
				out.write(line);
				out.newLine();
			}


			out.close();
		}
		catch (Exception e)
		{
			System.out.println("Failed to write out " + fileLocation  + " |  " + e);
			return false;
		}

		return true; 
	}

	
	final int batchCount = 20000;
	
	long totalGeoTweets = 0;
	long totalRawTweets = 0;
	

	ArrayList<TweetData> dataBuffer = new ArrayList<TweetData>(batchCount);


	LocationMapper locMapper;
	TwitterStream twitterStream;
	Gson gson = new Gson();
	
	
	public TwitterData(String[] args)
	{

		locMapper = new LocationMapper(args);
		
		totalGeoTweets = locMapper.TEMP_totalGeoTweets;
		totalRawTweets = locMapper.TEMP_totalRawTweets;
		
		

		
		  StatusListener listener = new StatusListener()
		  {
		        public void onStatus(Status status) 
		        {
		        	doOnStatus(status);
		        }
		        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}
		        public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}
		        public void onException(Exception ex) 
		        {
		            ex.printStackTrace();
		        }
				@Override
				public void onScrubGeo(long arg0, long arg1) {}
				@Override
				public void onStallWarning(StallWarning arg0) {}
		    };
		    
		twitterStream = new TwitterStreamFactory().getInstance();
	    twitterStream.addListener(listener);
	    twitterStream.sample();//start the stream

	}
	



	void writeHits(String dirName, HashMap<String, Long> map)
	{
		ArrayList<String> temp = new ArrayList<String>();
		for(String key : map.keySet())
		{
			long value = map.get(key);
			temp.add(key + "=" + value);
		}
		
		Collections.sort(temp);
		
		temp.add(0, "totalGeoTweets=" + totalGeoTweets);
		temp.add(0, "totalRawTweets=" + totalRawTweets);

		
		TwitterData.writeText(dirName, null, temp, false);
	}
	
	
	ArrayList<String> jsonDatas = new ArrayList<String>(batchCount);
	ArrayList<TweetData> writeBuffer;
	private void addTweet(TweetData tweetData)
	{
		synchronized (dataBuffer)
		{
			dataBuffer.add(tweetData);
		}
		
		locMapper.ProcessAndUpdate(0, (float)tweetData.lat, (float)tweetData.lon, tweetData.location, tweetData.lang);
		
		
		if(dataBuffer.size() >= batchCount)	//flush
		{
			
			synchronized (dataBuffer)
			{
				writeBuffer = new ArrayList<TweetData>(dataBuffer); //copy to avoid adding data while iterating
				dataBuffer.clear();
			}
			for(TweetData tdata : writeBuffer)
			{
				String jsonData = gson.toJson(tdata);
				jsonDatas.add(jsonData);
			}
			writeText("out/rawData.json","", jsonDatas, true);
			jsonDatas.clear();
			
			writeHits("out/hits.txt", LocationMapper.hits);
			saveDataSerialized(LocationMapper.users, "out/users.dat", null, true);
			
			System.out.println("[" + new Date() + "]runningTotal=" + totalGeoTweets + ", totalRawTweets=" + totalRawTweets);

		}
	}

	public void doOnStatus(Status status) 
    {
    	User user = status.getUser();
    	
     	String loc = user.getLocation();
     	String lang = user.getLang();
     	String name = user.getName();
     	long id = user.getId();
     	
     	GeoLocation geoLoc = status.getGeoLocation();
     	Place place = status.getPlace();
     	Date date = status.getCreatedAt();

 
     	String userKey = id + "";
     	if(geoLoc != null || loc.equals("") == false)
     	{
     		totalGeoTweets++;
     		
     		if(LocationMapper.users.containsKey(userKey + "_GeoTrue") == false)
    		{
     			TweetData temp = new TweetData(geoLoc, place, loc, lang, id, name, date);
	     		userKey += "_GeoTrue";
	     		addTweet(temp);
    		}
     	}

     	
     	if(LocationMapper.users.containsKey(userKey))
		{
			int count = LocationMapper.users.get(userKey);
			LocationMapper.users.put(userKey, ++count);
		}
		else
			LocationMapper.users.put(userKey, 1);
		
     	
     	totalRawTweets++;
    }
	
	
	
	
	public static void main(String[] args) throws TwitterException, IOException
	{
			
		new TwitterData(args);
	}

}
