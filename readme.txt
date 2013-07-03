 


3 output files:
hits.txt --	a map with location and the number of times hit for location + some other metrics
			totalRawTweets=#long#  		is total tweets recived from twitter (same handleID counted MULTIPLE times)
			totalGeoTweets=#long#		is total tweets that have geoLocation data	(same handleID counted MULTIPLE times)
			_._._=#long#				is total tweets that have geoLocationData but was not able to be mapped to a normalized location  (same handleID counted ONE time)
			ae.01.abu dhabi=#long#		is total tweets that are mapped to a location  (same handleID counted ONE time)
										and on

rawData.json -- raw data of tweets that have geolocation (one entry per handleID)

users.dat 	 -- serialized object of type java.util.HashMap<String, Integer> Where String is (handleID) or (handleID" + _Geo")[depending if the tweet has geo data]  and Interger it hit count
				note its also gziped.  



 
To Compile:
javac @sources.txt  -cp ./libs/twitter4j-3.0.3/lib/twitter4j-core-3.0.3.jar:./libs/twitter4j-3.0.3/lib/twitter4j-stream-3.0.3.jar:./libs/joda-time-2.2.jar:./libs/postgresql-9.2-1002.jdbc4.jar:./libs/diewald_shapeFileReader.jar:./libs/google-gson-2.2.4/gson-2.2.4.jar

To Run:  --note that there is one optional argument.  it is the path to the shapefiles directory.  the default value is ./../../persistant/location_data
java -cp ./libs/twitter4j-3.0.3/lib/twitter4j-core-3.0.3.jar:./libs/twitter4j-3.0.3/lib/twitter4j-stream-3.0.3.jar:./libs/joda-time-2.2.jar:./libs/postgresql-9.2-1002.jdbc4.jar:./libs/diewald_shapeFileReader.jar:./libs/google-gson-2.2.4/gson-2.2.4.jar:./src TwitterData

	or with the optional argument:

java -cp ./libs/twitter4j-3.0.3/lib/twitter4j-core-3.0.3.jar:./libs/twitter4j-3.0.3/lib/twitter4j-stream-3.0.3.jar:./libs/joda-time-2.2.jar:./libs/postgresql-9.2-1002.jdbc4.jar:./libs/diewald_shapeFileReader.jar:./libs/google-gson-2.2.4/gson-2.2.4.jar:./src TwitterData ./this/is/a/path/to/location_data