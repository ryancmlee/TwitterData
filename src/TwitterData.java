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
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


import LocationMapper.LocationMapper;

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

	
	final int batchCount = 1000;

	
	ArrayList<TweetData> datas = new ArrayList<TweetData>(batchCount);
//	ArrayList<TweetData> datas2 = new ArrayList<TweetData>(batchCount);
//	ArrayList<TweetData> curDatas = datas1;
	
	ArrayList<String> jsonDatas = new ArrayList<String>(1000);	
	
	
	
	
	int runningTotal = 0;
	Gson gson = new Gson();
	LocationMapper locMapper;
	TwitterStream twitterStream;
	
	
	
	public TwitterData()
	{
		locMapper = new LocationMapper();
		
		
		
		  StatusListener listener = new StatusListener()
		  {
		        public void onStatus(Status status) 
		        {
		        	User user = status.getUser();
		    		
		         	GeoLocation geoLoc = status.getGeoLocation();
		         	Place place = status.getPlace();
		         	String loc = user.getLocation();
		         	String lang = user.getLang();
		         	

		         	if(geoLoc != null || place != null || loc.equals("") == false)
		         	{
		         		TweetData temp = new TweetData(geoLoc, place, loc, lang);
		         		
		         		
		         		
		         		
		         		//System.out.println(temp.toString());
		         		addTweet(temp);
		         	}
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
	


	
	
	void writeOutMap(String dirName, HashMap<String, Integer> map)
	{
		ArrayList<String> temp = new ArrayList<String>();
		for(String key : map.keySet())
		{
			int value = map.get(key);
			temp.add(key + "= " + value);
		}
		
		Collections.sort(temp);
		
		temp.add(0, "Total Tweets= " + runningTotal);
		
		TwitterData.writeText(dirName, null, temp, false);
	}
	
	private void addTweet(TweetData tweetData)
	{
		datas.add(tweetData);
		runningTotal++;
		
		locMapper.ProcessAndUpdate(0, (float)tweetData.lat, (float)tweetData.lon, tweetData.location, tweetData.lang);
		
		if(datas.size() >= batchCount)				//flush
		{
			//twitterStream.cleanUp();
			
			
			writeOutMap("data/hits.txt", LocationMapper.hits);
			
			for(TweetData tdata : datas)
			{
				String jsonData = gson.toJson(tdata);
				jsonDatas.add(jsonData);
			}
			
			writeText("data/rawData.json","", jsonDatas, true);
			
			datas.clear();
			jsonDatas.clear();
			System.out.println(new Date() + ": runningTotal=" + runningTotal);
			//System.gc();
			
			//twitterStream.sample();
		}
	}

	
	public static void main(String[] args) throws TwitterException, IOException
	{
			
		new TwitterData();
	}

}
