-- 1. Primary key definition and any other constraint or index suggestion
-- Primary keys to unique identify a record on a given table (e.g - item, loc)
-- On item_loc_soh primary key is a composite key because of the relation between item and loc
-- and respective foreign keys to item and loc tables.
--
-- Since department is used very often as search parameter a single index on the column is created to improve performance (the same applies for location)
-- Moreover, the search with more fields (access is per store - location) can be useful so its created the composite search key as composite index for <loc, dept> pair

ALTER TABLE item
ADD CONSTRAINT pk_item PRIMARY KEY (item);

ALTER TABLE loc
ADD CONSTRAINT pk_loc PRIMARY KEY (loc);

ALTER TABLE item_loc_soh
ADD CONSTRAINT pk_item_loc_soh PRIMARY KEY (item, loc),
ADD CONSTRAINT fk_item FOREIGN KEY (item) REFERENCES item(item),
ADD CONSTRAINT fk_loc FOREIGN KEY (loc) REFERENCES loc(loc);

CREATE INDEX idx_item_loc_soh_dept ON item_loc_soh(dept);
CREATE INDEX idx_item_loc_soh_loc ON item_loc_soh(loc);
CREATE INDEX idx_item_loc_soh_loc_dept ON item_loc_soh(loc, dept);


-- 2. Your suggestion for table data management and data access considering the application usage, for example, partition...
-- By partitioning a dataset horizontally to several disks enables the reading and writing in parallel
-- The choosen algorithim between round-robin, range, hash and list was hash.
-- Reason: Since I dont know by hand the range of the values from the loc column I think spreading evenly the records are a suiatble approach.

CREATE TABLE item_loc_soh (
    item VARCHAR2(25) NOT NULL,
    loc NUMBER(10) NOT NULL,
    dept NUMBER(4) NOT NULL,
    unit_cost NUMBER(20,4) NOT NULL,
    stock_on_hand NUMBER(12,4) NOT NULL,
    CONSTRAINT pk_item_loc_soh_new PRIMARY KEY (item, loc),
 	CONSTRAINT fk_item FOREIGN KEY (item) REFERENCES item(item),
 	CONSTRAINT fk_loc FOREIGN KEY (loc) REFERENCES loc(loc);
)
PARTITION BY HASH (loc)
PARTITIONS 4; -- considering 4 processors


-- 3. Your suggestion to avoid row contention at table level parameter because of high level of concurrency
-- 

-- 4. Create a view that can be used at screen level to show only the required fields
-- Did not quite understand the meaning of required since the fields are all required for item_loc_soh
-- Assuming users only want items description and dept, store name, units and cost

CREATE VIEW item_loc_stock_view AS
SELECT il.item, i.item_desc, l.loc_desc, il.unit_cost, il.stock_on_hand
FROM item_loc_soh il
INNER JOIN item i ON il.item = i.item
INNER JOIN loc l ON il.loc = l.loc;

-- 5. Create a new table that associates user to existing dept(s)
-- In order to fulffil the requirements of having an uuser per department the user information is stored on a new table and than it is created the table that defines the relation between the users and the departments
-- 
CREATE TABLE users (
    user_email VARCHAR2(50) NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (email)
);

CREATE TABLE users_dept (
    user_email VARCHAR2(50) NOT NULL,
    dept NUMBER(4) NOT NULL,
    CONSTRAINT pk_users_dept PRIMARY KEY (user_email, dept),
    CONSTRAINT fk_users FOREIGN KEY (user_email) REFERENCES users(user_email),
    CONSTRAINT fk_dept FOREIGN KEY (dept) REFERENCES item(dept)
);

-- 6. Create a package with procedure or function that can be invoked by store or all stores to save the item_loc_soh to a new table that will contain the same information plus the stock value per item/loc (unit_cost*stock_on_hand)
-- 
-- Creation of a new table where the last column will have the total cost of all units for an item
-- New procedure with 1 input argument corresponding to location/store. In case of null value for the location/store the calculation is done on all records of the table, if not only the location/store provided as input will have all item units total cost updated.

CREATE TABLE item_loc_soh_accm (
    item VARCHAR2(25) NOT NULL,
    loc NUMBER(10) NOT NULL,
    dept NUMBER(4) NOT NULL,
    unit_cost NUMBER(20,4) NOT NULL,
    stock_on_hand NUMBER(12,4) NOT NULL,
    stock_cost NUMBER(28,4) NOT NULL,
    CONSTRAINT pk_item_loc_soh_accm PRIMARY KEY (item, loc),
    CONSTRAINT fk_item_accm FOREIGN KEY (item) REFERENCES item(item),
    CONSTRAINT fk_loc_accm FOREIGN KEY (loc) REFERENCES loc(loc)
);

CREATE PACKAGE item_loc_soh_pkg IS
	PROCEDURE calculate_sum_item_cost(in_loc IN NUMBER DEFAULT NULL);	
END item_loc_soh_pkg;

CREATE PACKAGE BODY item_loc_soh_pkg IS
	PROCEDURE calculate_sum_item_cost(in_loc IN NUMBER DEFAULT NULL) IS
	BEGIN
		IF in_loc IS NULL THEN
			DELETE ALL FROM item_loc_soh;
			INSERT INTO item_loc_soh_accm(item, loc, dept, unit_cost, stock_on_hand, stock_cost)
			SELECT item, loc, dept, unit_cost, stock_on_hand, unit_cost * stock_on_hand
			FROM item_loc_soh;
		ELSE
			INSERT INTO item_loc_soh_accm(item, loc, dept, unit_cost, stock_on_hand, stock_cost)
			SELECT item, loc, dept, unit_cost, stock_on_hand, unit_cost * stock_on_hand
			FROM item_loc_soh
			WHERE loc = in_loc;
		END IF;
	END calculate_sum_item_cost;
END item_loc_soh_pkg;

-- 7. Create a data filter mechanism that can be used at screen level to filter out the data that user can see accordingly to dept association (created previously)
-- 
-- Creation of a view where items will be filtered accordingly with the users_depts records, i,e only the departments listed per user on user_depts will filter the items per that departments on item_loc_soh tbale.
-- 

CREATE VIEW item_loc_soh_dept_user_view AS
SELECT ud.user_email, ils.dept, ils.item, ils.loc, ils.stock_on_hand, ils.unit_cost
FROM item_loc_soh ils
JOIN users_dept ud ON ils.dept = ud.dept

-- Example
-- SELECT 
--    dept, 
--    item, 
--    loc, 
--    stock_on_hand, 
--    unit_cost
--FROM 
--    item_loc_soh_dept_user_view
--WHERE 
--    user_email = 'user@mail.com' 
-- ORDER BY 
--    dept, 
--    item;
-- 


-- 8. Create a pipeline function to be used in the location list of values (drop down)
-- 
-- First it is the definition of the type for location row (loc plus description). Then a schema-level nested table with that type is created.
-- In the end an implementation of the PTF (piplined table function) is done returning back each row data of the new type.
-- 

CREATE OR REPLACE TYPE loc_type AS OBJECT (
    loc NUMBER(10),
    loc_desc VARCHAR2(25)
);

CREATE OR REPLACE TYPE loc_lov AS TABLE OF loc_type;

CREATE OR REPLACE FUNCTION get_loc_rows RETURN loc_lov PIPELINED IS
BEGIN
    FOR r IN (SELECT loc, loc_desc FROM loc ORDER BY loc) LOOP
        PIPE ROW (loc_type(r.loc, r.loc_desc));
    END LOOP;
    RETURN;
END;

-- 9. Looking into the following explain plan what should be your recommendation and implementation to improve the existing data model. Please share your solution in sql and the corresponding explain plan of that solution. Please take in consideration the way that user will use the app.
-- 
-- From the analysis of the explain plan a full scan was made on the table item_loc_soh when filtering by LOC=652 and DEPT=68 which means that no index is created for these columns. However, in 1. answer I've created a composite index with <loc,dept> pair already taken into account this situation which is:
-- Users look for each store/warehouse on the application
-- Most of the searches includes dept attribute
--
-- The before and after explain plan will be at docs folder on git could not export from APEX workspace)
-- Results analysis (see images on docs - 9_before_without_index and 9_after_with_composite_index)
-- The number of rows processed were signficantly low (Rows) and the cost/number of the I/O opertaions decreased (Cost/Bytes).
-- From the explain image the time for executing the query maintained but it was due to the small set of the table. However with index was 0,02s and without 0,03s the execution of the query 

CREATE INDEX idx_item_loc_soh_loc_dept ON item_loc_soh(loc, dept);

-- 10. Run the previous method that was created on 6. for all the stores from item_loc_soh to the history table. The entire migration should not take more than 10s to run (don't use parallel hint to solve it :)) 
--
-- The use of append on INSERTs of the procedure and disable the default index for primary key
--  See docs folder on github (images 10_before and 10_after_with_appends_disabling_pk). This execution was for 10000 records of item_loc_soh


-- 11. Please have a look into the AWR report (AWR.html) in attachment and let us know what is the problem that the AWR is highlighting and potential solution.


-- SQL ID :4crxsfs3f1x8n
-- Analysis: From SQL statistics it seems that the statement runs for 112,508.74 s and has 60,85% of Total elapsed DB time.
-- The query statement represents an access information for some LOV regarding the final price of each items basket.



