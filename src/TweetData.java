import java.io.Serializable;

import twitter4j.*;



public class TweetData implements Serializable
{
	private static final long serialVersionUID = -6116080419193521353L;
	

	GeoLocation geoLoc;
 	Place place;
 	String location;
 	String lang;
 	
 	
 	
 	public TweetData(GeoLocation geoLoc, Place place, String location, String lang)
 	{
 		this.geoLoc = geoLoc;
 		this.place = place;
 		this.location = location;
 		this.lang = lang;
 	}

 	
 	@Override 
 	public String toString()
 	{
 		return geoLoc + "|" + place + "|" + location + "|" + lang;
 	}
 	
}
