import java.io.Serializable;
import java.util.Date;

import org.joda.time.DateTime;

import twitter4j.*;



public class TweetData implements Serializable
{
	private static final long serialVersionUID = -6116080419193521353L;
	
	
	long id;
	String dateTime;
	double lat = 0;
	double lon = 0;
	String lang;
	String name;
 	String location;
 			
 	//Place place;
 	
 	
 	
 	
 	
 	public TweetData(GeoLocation geoLoc, Place place, String location, String lang, long id, String name, Date date)
 	{
 		if(geoLoc != null)
 		{
	 		this.lat = geoLoc.getLatitude();
	 		this.lon = geoLoc.getLongitude();
 		}
 		
 		//this.place = place;
 		this.location = location;
 		this.lang = lang;
 		this.id = id;
 		this.name = name;
 		this.dateTime = new DateTime(date).toString();
 	}

 	
 	@Override 
 	public String toString()
 	{
 		return id + "|" + lat + "|" + lon +  "|" + name + "|" + location + "|" + lang;
 	}
 	
}
