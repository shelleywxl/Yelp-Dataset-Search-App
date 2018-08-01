# Yelp-Dataset-Search-App

This standalone Java application is designed to run queries on the Yelp data (from Yelp Academic Dataset). 

Users can use it to search for businesses and users that match their search criteria. It implements faceted search. The user can filter the search results using available attributes (i.e. facets) such as category, sub-category, attributes, reviews, stars and votes. Each time the user clicks on a facet value, the set of results is reduced to only the items that have that value. Additional clicks continue to narrow down the search— the previous facet values are remembered and applied again.

Files
- createdb.sql: create tables, including indexes.
- dropdb.sql: drop all tables
- populate.java: get the names of the input files as command line parameters and populate them into the database (Some json files are too large to be uploaded). It is executed as:
>> java populate yelp_business.json yelp_review.json yelp_checkin.json yelp_user.json
- hw3.java: provide a GUI to query the database.
