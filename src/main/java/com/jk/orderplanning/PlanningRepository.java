package com.jk.orderplanning;

import java.sql.*;
import java.util.*;

import javax.sql.*;

import org.springframework.jdbc.core.*;
import org.springframework.jdbc.support.*;
import org.springframework.stereotype.*;

@Repository
public class PlanningRepository
{

    private final JdbcTemplate jdbcTemplate;

    public PlanningRepository(DataSource dataSource)
    {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void saveAllProducts(List<Product> products)
    {
        jdbcTemplate.batchUpdate(
            "INSERT INTO PRODUCTS(PRODUCT_NAME) VALUES (?)",
            new BatchPreparedStatementSetter() {

                @Override
                public void setValues(PreparedStatement ps, int i)
                    throws SQLException
                {
                    Product pd = products.get(i);
                    ps.setString(1, pd.getProductName());
                }

                @Override
                public int getBatchSize()
                {
                    return products.size();
                }

            });
    }

    public void saveAllWarehouses(List<Warehouse> warehouses)
    {
        jdbcTemplate.batchUpdate(
            "INSERT INTO WAREHOUSES (WAREHOUSE_NAME, POS_X, POS_Y) VALUES (?, ?, ?)",
            new BatchPreparedStatementSetter() {

                @Override
                public void setValues(PreparedStatement ps, int i)
                    throws SQLException
                {
                    Warehouse w = warehouses.get(i);
                    ps.setString(1, w.getWarehouseName());
                    ps.setInt(2, w.getPosx());
                    ps.setInt(3, w.getPosy());
                }

                @Override
                public int getBatchSize()
                {
                    return warehouses.size();
                }

            });
    }

    public void storeProductsInWarehouses(
        List<WarehouseProduct> warehouseProducts)
    {
        jdbcTemplate.batchUpdate(
            "INSERT INTO WAREHOUSE_PRODUCTS (WAREHOUSE_ID, PRODUCT_ID) VALUES (?, ?)",
            new BatchPreparedStatementSetter() {

                @Override
                public void setValues(PreparedStatement ps, int i)
                    throws SQLException
                {
                    WarehouseProduct wp = warehouseProducts.get(i);
                    ps.setInt(1, wp.getWarehouseId());
                    ps.setInt(2, wp.getProductId());
                }

                @Override
                public int getBatchSize()
                {
                    return warehouseProducts.size();
                }

            });
    }

    public void saveAllCustomers(List<Customer> customers)
    {

        jdbcTemplate.batchUpdate(
            "INSERT INTO CUSTOMERS (CUSTOMER_NAME, POS_X, POS_Y) VALUES (?, ?, ?)",
            new BatchPreparedStatementSetter() {

                @Override
                public void setValues(PreparedStatement ps, int i)
                    throws SQLException
                {
                    Customer c = customers.get(i);
                    int col = 1;
                    ps.setString(col++, c.getCustomerName());
                    ps.setInt(col++, c.getPosx());
                    ps.setInt(col++, c.getPosy());
                }

                @Override
                public int getBatchSize()
                {
                    return customers.size();
                }

            });
    }

    public List<Customer> findAllCustomers()
    {
        return jdbcTemplate.query(
            "SELECT * FROM CUSTOMERS",
            (rs, rowNum) -> Customer.builder()
                .customerId(rs.getInt("CUSTOMER_ID"))
                .customerName(rs.getString("CUSTOMER_NAME"))
                .posx(rs.getInt("POS_X"))
                .posy(rs.getInt("POS_Y"))
                .build());
    }

    public List<Product> findAllProducts()
    {
        return jdbcTemplate.query(
            "SELECT * FROM PRODUCTS",
            (rs, rowNum) -> Product.of(rs.getInt("PRODUCT_ID"), rs.getString(
                "PRODUCT_NAME")));
    }

    public List<Warehouse> findAllWarehouses()
    {
        return jdbcTemplate.query(
            "SELECT * FROM WAREHOUSES",
            (rs, rowNum) -> Warehouse.builder()
                .warehouseId(rs.getInt("WAREHOUSE_ID"))
                .warehouseName(rs.getString("WAREHOUSE_NAME"))
                .posx(rs.getInt("POS_X"))
                .posy(rs.getInt("POS_Y"))
                .build());
    }


    public Customer saveCustomer(Customer customer)
    {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
            con -> {
                PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO CUSTOMERS (CUSTOMER_NAME, POS_X, POS_Y) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
                int col = 1;
                ps.setString(col++, customer.getCustomerName());
                ps.setInt(col++, customer.getPosx());
                ps.setInt(col++, customer.getPosy());
                return ps;
            }, keyHolder);

        return customer.withCustomerId(keyHolder.getKey().intValue());
    }

    public void saveAllCustomerWarehouseDistances(List<CustomerWarehouseDistance> cwDistances)
    {
        jdbcTemplate.batchUpdate(
            "INSERT INTO DISTANCE_MAP (CUSTOMER_ID, WAREHOUSE_ID, DISTANCE) VALUES (?, ?, ?)",
            new BatchPreparedStatementSetter() {

                @Override
                public void setValues(PreparedStatement ps, int i)
                    throws SQLException
                {
                    CustomerWarehouseDistance cwd = cwDistances.get(i);
                    int col = 1;
                    ps.setInt(col++, cwd.getCustomerId());
                    ps.setInt(col++, cwd.getWarehouseId());
                    ps.setInt(col++, cwd.getDistance());
                }

                @Override
                public int getBatchSize()
                {
                    return cwDistances.size();
                }

            });
    }

    private static final String SQL_FIND_WAREHOUSES_HAVING_PRODUCTS = ""
        + " SELECT "
        + "     W.WAREHOUSE_NAME, "
        + "     D.DISTANCE "
        + " FROM "
        + "     DISTANCE_MAP D "
        + "     INNER JOIN WAREHOUSES W "
        + "         ON D.WAREHOUSE_ID = W.WAREHOUSE_ID "
        + " WHERE "
        + "     W.WAREHOUSE_ID IN ( "
        + "         SELECT "
        + "             W.WAREHOUSE_ID "
        + "         FROM "
        + "             WAREHOUSES W "
        + "             INNER JOIN WAREHOUSE_PRODUCTS PM "
        + "                 ON PM.WAREHOUSE_ID = W.WAREHOUSE_ID "
        + "             INNER JOIN PRODUCTS P "
        + "                 ON P.PRODUCT_ID = PM.PRODUCT_ID "
        + "         WHERE "
        + "             P.PRODUCT_NAME = ? "
        + "     ) "
        + "     AND D.CUSTOMER_ID = (SELECT CUSTOMER_ID FROM CUSTOMERS WHERE CUSTOMER_NAME = ?) "
        + " ORDER BY "
        + "     D.DISTANCE ASC ";

    public WarehouseDistance findClosestWarehouseHavingProduct(OrderRequest orderRequest)
    {
        List<WarehouseDistance> list = jdbcTemplate.query(
            SQL_FIND_WAREHOUSES_HAVING_PRODUCTS,
            ps -> {
                ps.setString(1, orderRequest.getProductName());
                ps.setString(2, orderRequest.getCustomerName());
            },
            (rs, rowNum) -> WarehouseDistance.of(rs.getString("WAREHOUSE_NAME"),
                rs.getInt("DISTANCE")));

        return list.size() > 0 ? list.get(0) : null;
    }

}
