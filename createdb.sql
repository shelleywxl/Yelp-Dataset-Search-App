/*
    createdb.sql
    Tables created:
        Business
        Business_Hours
        Business_Neighborhoods
        Business_Attributes
        Business_Category
        Users
        Review
*/


-- Business Table
-- Separate tables for category, hours, neighborhoods, attributes
CREATE TABLE Business(
                        business_id VARCHAR2(100) PRIMARY KEY,
                        -- encrypted business id
                        full_address VARCHAR2(200) NOT NULL,
                        -- localized address
                        open VARCHAR2(5),
                        -- true/false if the business is open/closed
                        city VARCHAR2(50) NOT NULL,
                        state VARCHAR2 (50) NOT NULL,
                        latitude NUMBER NOT NULL,
                        longitude NUMBER NOT NULL,
                        review_count NUMBER,
                        business_name VARCHAR2(100),
                        stars NUMBER,
                        -- star rating, rounded to half-stars
                        type VARCHAR2(30)
                        -- 'business'
                        );

-- Business Hours Table
CREATE TABLE Business_Hours(
                            business_id VARCHAR2(100),
                            business_day VARCHAR2(20),
                            -- the days of the week
                            open_time TIMESTAMP,
                            close_time TIMESTAMP,
                            PRIMARY KEY (business_id, business_day),
                            FOREIGN KEY (business_id) REFERENCES Business(business_id) ON DELETE CASCADE
                            );

-- Business Neighborhoods Table
CREATE TABLE Business_Neighborhoods(
                                    business_id VARCHAR2(100),
                                    neighborhoods VARCHAR2(100),
                                    -- hood names
                                    PRIMARY KEY (business_id, neighborhoods),
                                    FOREIGN KEY (business_id) REFERENCES Business(business_id) ON DELETE CASCADE
                                    );

-- Business Attributes Table
CREATE TABLE Business_Attributes(
                                    business_id VARCHAR2(100),
                                    attributes VARCHAR2(500),
                                    -- business properties
                                    PRIMARY KEY (business_id, attributes),
                                    FOREIGN KEY (business_id) REFERENCES Business(business_id) ON DELETE CASCADE
                                );

-- Business Category Table
CREATE TABLE Business_Category(
                                business_id VARCHAR2(100),
                                main_category VARCHAR2(100) NOT NULL,
                                sub_category VARCHAR2(100),
                                FOREIGN KEY (business_id) REFERENCES Business(business_id) ON DELETE CASCADE
);


-- User Table
CREATE TABLE Users(
                    yelping_since DATE NOT NULL,
                    useful_votes NUMBER,
                    funny_votes NUMBER,
                    cool_votes NUMBER,
                    review_count NUMBER,
                    user_name VARCHAR2(100) NOT NULL,
                    user_id VARCHAR2(100) PRIMARY KEY,
                    --friends -- "friends": ["abcdeedgs", "dsaggagrtrtgr", ...]
                    number_of_friends NUMBER,
                    fans NUMBER,
                    average_stars NUMBER,
                    type VARCHAR2(30)
                    --compliments -- "compliments": {"note": 1, "photo": 2, ...}
                    --elite -- "elite": [2012, 2013, 2014]
                    );


-- Review Table
CREATE TABLE Review(
                    useful_votes NUMBER,
                    funny_votes NUMBER,
                    cool_votes NUMBER,
                    votes NUMBER,
                    user_id VARCHAR2(100),
                    review_id VARCHAR2(100) PRIMARY KEY,
                    stars NUMBER,
                    review_date DATE,
                    review_text CLOB,
                    type VARCHAR2(30),
                    business_id VARCHAR2(100),
                    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
                    FOREIGN KEY (business_id) REFERENCES Business(business_id) ON DELETE CASCADE
                    );


CREATE INDEX bc_idx ON Business_Category(main_category, sub_category);
CREATE INDEX ba_idx ON Business_Attributes(attributes);
CREATE INDEX user_idx ON Users(yelping_since, useful_votes, funny_votes, cool_votes, review_count, number_of_friends, average_stars);
CREATE INDEX review_idx ON Review(votes, user_id, stars, review_date, business_id);