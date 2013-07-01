import java.io.Serializable;

import twitter4j.*;



public class TweetData implements Serializable
{
	private static final long serialVersionUID = -6116080419193521353L;
	

	double lat = 0;
	double lon = 0;
	//Place place;
 	String location;
 	String lang;
 	
 	
 	
 	public TweetData(GeoLocation geoLoc, Place place, String location, String lang)
 	{
 		if(geoLoc != null)
 		{
	 		this.lat = geoLoc.getLatitude();
	 		this.lon = geoLoc.getLongitude();
 		}
 		//this.place = place;
 		this.location = location;
 		this.lang = lang;
 	}

 	
 	@Override 
 	public String toString()
 	{
 		return lat + "|" + lon +  "|" + location + "|" + lang;
 	}
 	
}
