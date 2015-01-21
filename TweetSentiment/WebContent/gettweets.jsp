<%@ page language="java" contentType="text/html; charset=US-ASCII"
    pageEncoding="US-ASCII"%>
<%@page import="org.json.simple.JSONObject"%>
<%@page import="org.json.simple.JSONArray"%>
<%@ page import="java.sql.DriverManager" %>
<%@ page import="java.sql.Statement" %>
<%@ page import="java.sql.ResultSet" %>

<%! @SuppressWarnings("unchecked") %>
<%
String keyword;
String id;
String coordinates;
Double lon;
Double lat;

//get the keyword
System.out.println("jsp file running");
keyword = request.getParameter("keyword");
System.out.println("here"+keyword);


// This is needed to use Connector/J. It basically creates a new instance
// of the Connector/J jdbc driver.
Class.forName("com.mysql.jdbc.Driver").newInstance();

// Open new connection.
java.sql.Connection conn;
/* To connect to the database, you need to use a JDBC url with the following 
   format ([xxx] denotes optional url components):
   jdbc:mysql://[hostname][:port]/[dbname][?param1=value1][&param2=value2]... 
   By default MySQL's hostname is "localhost." The database used here is 
   called "mydb" and MySQL's default user is "root". If we had a database 
   password we would add "&password=xxx" to the end of the url.
*/

conn = DriverManager.getConnection("<db-url>");
Statement sqlStatement = conn.createStatement();

// Generate the SQL query.
String query = "SELECT * FROM "+keyword;

// Get the query results and display them.
ResultSet sqlResult = sqlStatement.executeQuery(query);

JSONArray data = new JSONArray();

while(sqlResult.next()) {
	id = sqlResult.getString("tweet_id");
	coordinates = sqlResult.getString("coordinates");
	lon = Double.valueOf(coordinates.split(",")[0]);
	lat = Double.valueOf(coordinates.split(",")[1]);
	
	JSONObject tweet = new JSONObject();
	
	tweet.put("id", id);
	tweet.put("lon", lon);
	tweet.put("lat", lat);
	
	data.add(tweet);
}

//Sent the data back to the client
out.print(data);
out.flush();

// Close the connection.
sqlResult.close();
sqlStatement.close();
conn.close();
%>
