-- Fix behavioral change with decimal declarations in Hive 0.11 and 0.12
UPDATE COLUMNS_V2 SET TYPE_NAME='decimal(38,10)'
WHERE TYPE_NAME='decimal';