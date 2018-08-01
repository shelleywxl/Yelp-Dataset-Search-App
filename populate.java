import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class populate {

    // Define the connection URL
    public static final String HOST = "localhost";
    public static final String DBNAME = "ORCL";
    public static final String PORT = "1522";
    public static final String DBURL = "jdbc:oracle:thin:@" + HOST + ":" + PORT + ":" + DBNAME;
    public static final String DBUSERNAME = "hr";
    public static final String DBPASSWORD = "hr";

    // @param args: the command line arguments
    public static void main(String[] args) throws FileNotFoundException {
        // Load the driver
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        }
        catch (Exception e) {
            System.out.println("Unable to load the driver.");
            e.printStackTrace();
        }

        // Get four JSON files put in command line
        if (args.length != 4) {
            System.out.println("Error: Four JSON files are required.");
        }
        else {
        	// Remove previous data in tables
        	removePrevData();
            parseBusinessJson(args[0]);
            parseUserJson(args[3]);
            parseReviewJson(args[1]);
            // ParseCheckinJson(args[2]);
        }
    }

    // Remove previous data in tables
    public static void removePrevData() {
        try {
            Connection connection = DriverManager.getConnection(DBURL, DBUSERNAME, DBPASSWORD);
            Statement deleteTableStatement = connection.createStatement();

            System.out.println("Removing previous data...");

            // Delete from all tables
            deleteTableStatement.executeUpdate("DELETE FROM Review");
            deleteTableStatement.executeUpdate("DELETE FROM Users");
        	deleteTableStatement.executeUpdate("DELETE FROM Business_Category");
            deleteTableStatement.executeUpdate("DELETE FROM Business_Attributes");
            deleteTableStatement.executeUpdate("DELETE FROM Business_Neighborhoods");
            deleteTableStatement.executeUpdate("DELETE FROM Business_Hours");
            deleteTableStatement.executeUpdate("DELETE FROM Business");
            
        	System.out.println("Previous data removed.");
        }
        catch(SQLException e) {
            System.out.println("Error in removing previous data.");
            e.printStackTrace();
        }
    }

    // Parse yelp_business.json file
    public static void parseBusinessJson(String business_json) {
        System.out.println("Parsing yelp_business.json");
        try {
            File business_file = new File(business_json);
            FileReader business_fr = new FileReader(business_file);
            BufferedReader business_bf = new BufferedReader(business_fr);

            JSONObject jsonObject = null;
            JSONParser parser = new JSONParser();
            String business_line;

            // preparedStatement
            Connection connection = DriverManager.getConnection(DBURL, DBUSERNAME, DBPASSWORD);
            // try
            Statement st = connection.createStatement();

            PreparedStatement businessInsertPreparedStatement = connection.prepareStatement("INSERT INTO Business("
                + "business_id, full_address, open, city, state, latitude, longitude, "
                + "review_count, business_name, stars, type) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?)");
            PreparedStatement hoursInsertPreparedStatement = connection.prepareStatement("INSERT INTO Business_Hours("
                + "business_id, business_day, open_time, close_time) "
                + "VALUES (?,?,?,?)");
            PreparedStatement neighborhoodsPreparedStatement = connection.prepareStatement("INSERT INTO Business_Neighborhoods("
                + "business_id, neighborhoods) "
                + "VALUES (?,?)");
            PreparedStatement attributesPreparedStatement = connection.prepareStatement("INSERT INTO Business_Attributes("
                + "business_id, attributes) "
                + "VALUES (?,?)");
            PreparedStatement categoryPreparedStatement = connection.prepareStatement("INSERT INTO Business_Category("
                + "business_id, main_category, sub_category) "
                + "VALUES (?,?,?)");

            // Read each line
            while ((business_line = business_bf.readLine()) != null) {
            	try {
                    jsonObject = (JSONObject) parser.parse(business_line);

                    // Insert into Business Table
                    String business_id = (String) jsonObject.get("business_id");
                    String full_address = (String) jsonObject.get("full_address");
                    String open = jsonObject.get("open").toString();
                    String city = (String) jsonObject.get("city");
                    String state = (String) jsonObject.get("state");
                    Double latitude = (Double) jsonObject.get("latitude");
                    Double longitude = (Double) jsonObject.get("longitude");
                    Long review_count = (Long) jsonObject.get("review_count");
                    String business_name = (String) jsonObject.get("name");
                    Double stars = (Double) jsonObject.get("stars");
                    String type = (String) jsonObject.get("type");

                    businessInsertPreparedStatement.setString(1, business_id);
                    businessInsertPreparedStatement.setString(2, full_address);
                    businessInsertPreparedStatement.setString(3, open);
                    businessInsertPreparedStatement.setString(4, city);
                    businessInsertPreparedStatement.setString(5, state);
                    businessInsertPreparedStatement.setDouble(6, latitude);
                    businessInsertPreparedStatement.setDouble(7, longitude);
                    businessInsertPreparedStatement.setLong(8, review_count);
                    businessInsertPreparedStatement.setString(9, business_name);
                    businessInsertPreparedStatement.setDouble(10, stars);
                    businessInsertPreparedStatement.setString(11, type);
                    businessInsertPreparedStatement.executeUpdate();

                    //System.out.println("Finishing inserting into Business");

                    // Insert into Business_Hours Table (business_id, business_day, open_time, close_time)
                    // {"hours": {"Monday": {"close": "22:00", "open": "11:00"}, "Tuesday": {}, ...}}
                    // hours_jo: {"Monday": {"close": "22:00", "open": "11:00"}, "Tuesday": {}, ...}
                    JSONObject hours_jo = (JSONObject) jsonObject.get("hours");
                    // close_open_time_jo: {"close": "22:00", "open": "11:00"}
                    JSONObject close_open_time_jo;
                    // days: ["Sunday", "Monday" ...]
                    ArrayList<String> days = new ArrayList<String>();
                    days.addAll(hours_jo.keySet());

                    for (int i = 0; i < hours_jo.size(); ++i) {
                        // for a particular day
                        String day = days.get(i);
                        close_open_time_jo = (JSONObject) hours_jo.get(day);
                        String close_time = (String) close_open_time_jo.get("close");
                        String open_time = (String) close_open_time_jo.get("open");
                        DateFormat dateFormat = new SimpleDateFormat("hh:mm");
                        Date close_date = dateFormat.parse(close_time);
                        Timestamp close_timestamp = new Timestamp(close_date.getTime());
                        Date open_date = dateFormat.parse(open_time);
                        Timestamp open_timestamp = new Timestamp(open_date.getTime());

                        hoursInsertPreparedStatement.setString(1, business_id);
                        hoursInsertPreparedStatement.setString(2, day);
                        hoursInsertPreparedStatement.setTimestamp(3, open_timestamp);
                        hoursInsertPreparedStatement.setTimestamp(4, close_timestamp);
                        hoursInsertPreparedStatement.executeUpdate();
                    }

                    //System.out.println("Finishing inserting into Business_Hours");

                    // Insert into Business_Neighborhoods Table (business_id, neighborhoods)
                    // {"neighborhoods": ["Mayfair Park"]}
                    JSONArray neighborhoods_array = (JSONArray) jsonObject.get("neighborhoods");
                    for (int i = 0; i < neighborhoods_array.size(); ++i) {
                        String neighborhood = neighborhoods_array.get(i).toString();
                        neighborhoodsPreparedStatement.setString(1, business_id);
                        neighborhoodsPreparedStatement.setString(2, neighborhood);
                        neighborhoodsPreparedStatement.executeUpdate();
                    }

                    //System.out.println("Finishing inserting into Business_Neighborhoods");

                    // Insert into Business_Attributes Table (business_id, attributes)
                    // attribute_[subattribute_]true/false/variable
                    // {"attributes": {"Take-out": true, "Good For": {"dessert": false, "breakfast": false}, ...}}
                    JSONObject attributes_jo = (JSONObject) jsonObject.get("attributes");
                    ArrayList<String> attribute_names = new ArrayList<String>();
                    attribute_names.addAll(attributes_jo.keySet());

                    for (int i = 0; i < attribute_names.size(); ++i) {
                        String attribute_name = attribute_names.get(i);
                        String value = attributes_jo.get(attribute_name).toString();
                        
                        // value can be "true" or "false" or "some string" or "{" ":} (an object)"
                        if (value.charAt(0) != '{') {
                        	String combine_attr = attribute_name + "_" + value;
                            attributesPreparedStatement.setString(1, business_id);
                            attributesPreparedStatement.setString(2, combine_attr);
                            attributesPreparedStatement.executeUpdate();    
                        }
                        // sub attributes
                        else {
                            JSONObject sub_attributes_jo = (JSONObject) attributes_jo.get(attribute_name);
                            ArrayList<String> sub_attribute_names = new ArrayList<String>();
                            sub_attribute_names.addAll(sub_attributes_jo.keySet());
                            
                            for (int j = 0; j < sub_attribute_names.size(); ++j) {
                                String sub_attribute_name = sub_attribute_names.get(j);
                                String sub_value = sub_attributes_jo.get(sub_attribute_name).toString();
                                
                                // sub_value can be "true" or "false" or "some string"
                                String combine_attr = attribute_name + "_" + sub_attribute_name + "_" + sub_value;
                                attributesPreparedStatement.setString(1, business_id);
                                attributesPreparedStatement.setString(2, combine_attr);
                                attributesPreparedStatement.executeUpdate();
                            }
                        }
                    }
                    //System.out.println("Finishing inserting into Business_Attributes");

                    // Insert into Business_Category Table (business_id, main_category, sub_category)
                    // {"categories": ["Mexican", "Restaurants"]} can have 0 or more than 1 main/sub categories
                    String[] main_business_categories = {"Active Life", "Arts & Entertainment", "Automotive", "Car Rental", "Cafes", "Beauty & Spas", "Convenience Stores", "Dentists", "Doctors", "Drugstores", "Department Stores", "Education", "Event Planning & Services", "Flowers & Gifts", "Food", "Health & Medical", "Home Services", "Home & Garden", "Hospitals", "Hotels & Travel", "Hardware Stores", "Grocery", "Medical Centers", "Nurseries & Gardening", "Nightlife", "Restaurants", "Shopping", "Transportation"};
                    ArrayList<String> main_categories_list = new ArrayList(Arrays.asList(main_business_categories));

                    JSONArray category_array = (JSONArray) jsonObject.get("categories");
                    // For the current line, list of main categories and sub categories
                    ArrayList<String> main_categories = new ArrayList<String>();
                    ArrayList<String> sub_categories = new ArrayList<String>();

                    for (int i = 0; i < category_array.size(); ++i) {
                        String category = category_array.get(i).toString();

                        if (main_categories_list.contains(category)) {
                            main_categories.add(category);
                        }
                        else {
                            sub_categories.add(category);
                        }
                    }

                    // No sub categories, only need to insert main_category
                    if (sub_categories.size() == 0) {
                        for (int m = 0; m < main_categories.size(); ++m) {
                        	categoryPreparedStatement.setString(1, business_id);
                            categoryPreparedStatement.setString(2, main_categories.get(m));
                            categoryPreparedStatement.setString(3, null);
                            categoryPreparedStatement.executeUpdate();
                        }
                    }
                    // Have sub categories
                    else {
                        for (int m = 0; m < main_categories.size(); ++m) {
                            for (int s = 0; s < sub_categories.size(); ++s) {
                                categoryPreparedStatement.setString(1, business_id);
                                categoryPreparedStatement.setString(2, main_categories.get(m));
                                categoryPreparedStatement.setString(3, sub_categories.get(s));
                                categoryPreparedStatement.executeUpdate();
                            }
                        }
                    }
                    main_categories.clear();
                    sub_categories.clear();

                    //System.out.println("Finishing inserting into Business_Category");
                }
                catch (ParseException e) {
                    System.out.println("Error in parsing business.");
                    e.printStackTrace();
                }
            }

            business_bf.close();
            connection.close();
         	System.out.println("Finishing inserting into Business tables");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }


    // Parse yelp_user.json file
    public static void parseUserJson(String user_json) {
        System.out.println("Parsing yelp_user.json");
        try {
            File user_file = new File(user_json);
            FileReader user_fr = new FileReader(user_file);
            BufferedReader user_bf = new BufferedReader(user_fr);

            JSONObject jsonObject = null;
            JSONParser parser = new JSONParser();
            String user_line;

            // preparedStatement
            Connection connection = DriverManager.getConnection(DBURL, DBUSERNAME, DBPASSWORD);
            PreparedStatement userInsertPreparedStatement = connection.prepareStatement("INSERT INTO Users("
                + "yelping_since, useful_votes, funny_votes, cool_votes, review_count, user_name, user_id, "
                + "number_of_friends, fans, average_stars, type) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?)");

            Timestamp yelping_since = null;

            // Read each line
            while ((user_line = user_bf.readLine()) != null) {
                try {
                    jsonObject = (JSONObject) parser.parse(user_line);

                    // Insert into Review Table
                     // {"yelping_since": "2012-02"}
                    String since_str = (String) jsonObject.get("yelping_since");
                    try {
                    	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM");
                   		Date since_date = dateFormat.parse(since_str);
                    	yelping_since = new Timestamp(since_date.getTime());
                    	
                    }
                    catch (Exception e) {
                    	e.printStackTrace();
                    }
                    // {"votes": {"funny": 0, "useful": 2, "cool": 1}}
                    JSONObject votes = (JSONObject) jsonObject.get("votes");
                    Long useful_votes = (Long) votes.get("useful");
                    Long funny_votes = (Long) votes.get("funny");
                    Long cool_votes = (Long) votes.get("cool");
                    Long review_count = (Long) jsonObject.get("review_count");
                    String user_name = (String) jsonObject.get("name");
                    String user_id = (String) jsonObject.get("user_id");
                    JSONArray friends_array = (JSONArray) jsonObject.get("friends");
                    int number_of_friends = friends_array.size();
                    Long fans = (Long) jsonObject.get("fans");
                    Double average_stars = (Double) jsonObject.get("average_stars");
                    String type = (String) jsonObject.get("type");
 					
 					userInsertPreparedStatement.setTimestamp(1, yelping_since);
                    userInsertPreparedStatement.setLong(2, useful_votes);
                    userInsertPreparedStatement.setLong(3, funny_votes);
                    userInsertPreparedStatement.setLong(4, cool_votes);
                    userInsertPreparedStatement.setLong(5, review_count);
                    userInsertPreparedStatement.setString(6, user_name);
                    userInsertPreparedStatement.setString(7, user_id);
                    userInsertPreparedStatement.setInt(8, number_of_friends);
                    userInsertPreparedStatement.setLong(9, fans);
                    userInsertPreparedStatement.setDouble(10, average_stars);
                    userInsertPreparedStatement.setString(11, type);
                    userInsertPreparedStatement.executeUpdate();
                }
                catch (ParseException e) {
                    System.out.println("Error in parsing user.");
                    e.printStackTrace();
                }
            }
            user_bf.close();
            connection.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Finishing inserting into Users");
    }


    // Parse yelp_review.json file
    public static void parseReviewJson(String review_json) {
        System.out.println("Parsing yelp_review.json");
        try {
            File review_file = new File(review_json);
            FileReader review_fr = new FileReader(review_file);
            BufferedReader review_bf = new BufferedReader(review_fr);

            JSONObject jsonObject = null;
            JSONParser parser = new JSONParser();
            String review_line;

            // preparedStatement
            Connection connection = DriverManager.getConnection(DBURL, DBUSERNAME, DBPASSWORD);
            PreparedStatement reviewInsertPreparedStatement = connection.prepareStatement("INSERT INTO Review("
                + "useful_votes, funny_votes, cool_votes, votes, user_id, review_id, stars, "
                + "review_date, review_text, type, business_id) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?)");

            // Read each line
            while ((review_line = review_bf.readLine()) != null) {
                try {
                    jsonObject = (JSONObject) parser.parse(review_line);

                    // Insert into Review Table
                    // {"votes": {"funny": 0, "useful": 2, "cool": 1}}
                    JSONObject votes = (JSONObject) jsonObject.get("votes");
                    Long useful_votes = (Long) votes.get("useful");
                    Long funny_votes = (Long) votes.get("funny");
                    Long cool_votes = (Long) votes.get("cool");
                    Long total_votes = useful_votes + funny_votes + cool_votes;
                    String user_id = (String) jsonObject.get("user_id");
                    String review_id = (String) jsonObject.get("review_id");
                    Long stars = (Long) jsonObject.get("stars");
                    // {"date": "2012-01-08"}
                    String review_date_str = (String) jsonObject.get("date");
                    try {
	                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    	                Date review_date_date = dateFormat.parse(review_date_str);
        	            Timestamp review_date = new Timestamp(review_date_date.getTime());
                    	reviewInsertPreparedStatement.setTimestamp(8, review_date);
                    } 
                    catch(Exception e) {
                    	e.printStackTrace();
                    }
                    String review_text = (String) jsonObject.get("text");
                    String type = (String) jsonObject.get("type");
                    String business_id = (String) jsonObject.get("business_id");

                    reviewInsertPreparedStatement.setLong(1, useful_votes);
                    reviewInsertPreparedStatement.setLong(2, funny_votes);
                    reviewInsertPreparedStatement.setLong(3, cool_votes);
                    reviewInsertPreparedStatement.setLong(4, total_votes);
                    reviewInsertPreparedStatement.setString(5, user_id);
                    reviewInsertPreparedStatement.setString(6, review_id);
                    reviewInsertPreparedStatement.setLong(7, stars);
                    reviewInsertPreparedStatement.setString(9, review_text);
                    reviewInsertPreparedStatement.setString(10, type);
                    reviewInsertPreparedStatement.setString(11, business_id);
                    reviewInsertPreparedStatement.executeUpdate();
                }
                catch (ParseException e) {
                    System.out.println("Error in parsing review.");
                    e.printStackTrace();
                }
            }
            review_bf.close();
            connection.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    	System.out.println("Finishing inserting into Review");
    }


    // Parse yelp_checkin.json file
    public static void parseCheckinJson(String checkin_json) {
        System.out.println("Parsing yelp_checkin.json");
    }

}