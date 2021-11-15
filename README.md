# Order Planning
A project that lets the customer create an order where the the ordered product would be shipped from the closest warehouse from their location.

There are two key aspects to solve this problem.
1. Choosing a positioning system for customers and warehouses so that distance can be calculated.
2. Pre-calculating distances between the customer and warehouses so that we don't have to this when the order is actually being created.

Here, simple XY coordinate system has been chosen. A point on earth can be taken as origin (0,0) and the location of customers and warehouses can be a point on this XY plain.

When a new customer is added to the system, the distance of the customer from each warehouse is calculated right then and stored in the database. This information is then used at the time of placing the order.

## Database Design
Below tables are used.
**CUSTOMERS**
Stores information of customers.
Name of the column | Description
--- | ---
`CUSTOMER_ID` |  Primary key of the table.
`CUSTOMER_NAME` | Name of the customer. Allows only unique values.
`POX_X` | X-coordinate part in the position of the customer
`POS_Y` | Y-coordinate part in the position of the customer

**WAREHOUSES**
Stores information of warehouses.
Name of the column | Description
--- | ---
`WAREHOUSE_ID` |  Primary key of the table.
`WAREHOUSE_NAME` | Name of the warehouse. Allows only unique values.
`POX_X` | X-coordinate part in the position of the warehouse
`POS_Y` | Y-coordinate part in the position of the warehouse

**DISTANCE_MAP**
Name of the column | Description
--- | ---
`CUSTOMER_ID` | Reference to the primary key of CUSTOMERS table
`WAREHOUSE_ID` | Reference to the primary key of the WAREHOUSES table
`DISTANCE` | Distance between this customer and this warehouse

**PRODUCTS**
All the products. A product has an ID and unique name.

**WAREHOUSE_PRODUCTS**
Products stored in a warehouse (contains warehouse id and product id).

## Description of Endpoints
As per the requirement two endpoints have been provided. `/customers/add` to add a new customer to the system and `/orders/create` to create an order.

**`/customers/add`**
Request format
```json
{
  "customerName": "SACHIN",
  "posx": -800,
  "posy": 100
}
```
If the request is successful, HTTP status 200 is returned.

**`/orders/create`**
Order can be created by passing the name of the customer and name of the product.
Request format
```json
{
  "customerName": "SACHIN",
  "productName": "PRODUCT_9"
}
```

Order creation success response. Contains, name of the warehouse from which the product would be delivered, distance of the warehouse from the customer and estimated time of delivery. Time of delivery is calculated by assuming that the goods can be moved at an average speed of 40 KM/h.
```json
{
  "warehouseName": "WAREHOUSE_40",
  "distance": "802 Kilometers",
  "estimatedDeliveryTime": "21 Hours"
}
```

Order creation failure response
```json
{
  "message": "Order could not be created. Currently, none of the warehouses have the product."
}
```

## Using the System
The system is represented by a Spring Boot application with Java 11. The project is ready to build and deploy.

**Once the project is running and deployed test data can either be setup using `data.sql` file in the resources or by calling an endpoint that would set an arbitrary amount of data (preferred approach).**

**`/createTestData`**
This is GET endpoint. Calling this endpoint sets below data.
- 50 products
- 50 warehouses
- 20000 customers

Products are named as `PRODUCT_1` and so on. Customers are named as `CUSTOMER_1` and so on. Warehouses are named as `WAREHOUSE_` and so on. A random number of products are stored in each warehouse at the time of data setup.

Note: These numbers of different items to be setup are defined in the class `TestDataGenerator`. These values can take any arbitrary values.

**Once the data has been setup, order can be placed by using the endpoint to create the order. Further, any new customers can be added to the system if needed.**


## Conclusion
The provided solution is scalable to large amounts of data. Tests show that the system handles 50 products, 50 warehouses and 20000 customers quite fast.

The performance of this system depends on the **number of warehouses**. Which is a good factor since the number of warehouses would not increase to very high number in a system.
