import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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


	private ArrayList<TweetData> data = new ArrayList<TweetData>();
	
	
	private final int batchCount = 10000;
	private int runningTotal = 0;

	
	
	private void addTweet(TweetData tweetData)
	{
		data.add(tweetData);
		runningTotal++;
		
		if(data.size() >= batchCount)
		{
			String fileName = new Date().toString().replaceAll("[: ]", ".");
			saveDataSerialized(data, "data", fileName, true);
			data.clear();
			System.out.println(new Date() + ": runningTotal=" + runningTotal);
		}
	}

	
	public TwitterData()
	{
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
		    
		TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
	    twitterStream.addListener(listener);
	    twitterStream.sample();

	}
	
	
	
	
	
	public static void main(String[] args) throws TwitterException, IOException
	{
		new TwitterData();
	}

}
