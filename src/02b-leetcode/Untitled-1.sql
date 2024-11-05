SELECT
    vehicle_type_name AS `Vehicle Type`,
    IFNULL(FORMAT(AVG(CASE WHEN condition = 'New' THEN purchase_price END), 2), '0') AS `New`,
    IFNULL(FORMAT(AVG(CASE WHEN condition = 'New' THEN purchase_price END), 2), '0') AS `New`,
    IFNULL(FORMAT(AVG(CASE WHEN condition = 'New' THEN purchase_price END), 2), '0') AS `New`,
FROM
    vehicle_info
GROUP BY
    vehicle_type_name
ORDER BY
    vehicle_type_name;