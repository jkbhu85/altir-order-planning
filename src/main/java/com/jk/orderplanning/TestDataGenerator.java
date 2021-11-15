package com.jk.orderplanning;

import java.util.*;
import java.util.stream.*;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import lombok.*;
import lombok.extern.slf4j.*;

@Slf4j
@RequiredArgsConstructor
@RestController
public class TestDataGenerator
{

    private static final int NUMBER_OF_PRODUCTS = 50;
    private static final int NUMBER_OF_WAREHOUSES = 50;
    private static final int NUMBER_OF_CUSTOMERS = 20000;

    private final PlanningRepository planningRepository;

    @GetMapping(value = "/createTestData", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createTestData()
    {
        setupData();

        Map<String, Integer> dataCreatedDetails = Map.of(
            "products", NUMBER_OF_PRODUCTS,
            "warehouses", NUMBER_OF_WAREHOUSES,
            "customers", NUMBER_OF_CUSTOMERS);
        return ResponseEntity.ok(dataCreatedDetails);
    }

    private void setupData()
    {
        log.info("Setting up products");
        generateProducts();
        log.info("Setting up warehouses");
        generateWarehouses();
        log.info("Storing products in warehouses");
        storeProductsInWarehouses();
        log.info("Creating customers");
        generateCustomers();
        log.info("Data setup complete");
    }

    private void generateProducts()
    {
        List<Product> products = new ArrayList<>(NUMBER_OF_PRODUCTS);
        for (int i = 0; i < NUMBER_OF_PRODUCTS; i++)
            products.add(Product.of(0, "PRODUCT_" + (i + 1)));

        planningRepository.saveAllProducts(products);
    }

    private void generateWarehouses()
    {
        final int bound = 1500;
        Random random = new Random();

        List<Warehouse> warehouses = new ArrayList<>(NUMBER_OF_WAREHOUSES);
        for (int i = 0; i < NUMBER_OF_WAREHOUSES; i++)
            warehouses.add(Warehouse.builder()
                .warehouseName("WAREHOUSE_" + (i + 1))
                .posx(random.nextInt(bound))
                .posy(random.nextInt(bound))
                .build());

        planningRepository.saveAllWarehouses(warehouses);
    }

    private void storeProductsInWarehouses()
    {
        List<Warehouse> warehouses = planningRepository.findAllWarehouses();
        List<Product> products = planningRepository.findAllProducts();
        List<WarehouseProduct> warehouseProducts = new ArrayList<>();
        for (Warehouse warehouse : warehouses)
            warehouseProducts.addAll(putProductsInWarehouse(warehouse,
                products));

        planningRepository.storeProductsInWarehouses(warehouseProducts);
    }

    private static List<WarehouseProduct> putProductsInWarehouse(
        Warehouse warehouse,
        List<Product> products)
    {

        Set<Product> selectedProducts = selectProductsToStore(products);
        return toWarehouseProducts(warehouse, selectedProducts);
    }

    private static List<WarehouseProduct> toWarehouseProducts(
        Warehouse warehouse,
        Collection<Product> selectedProducts)
    {
        return selectedProducts.stream()
            .map(product -> WarehouseProduct.builder()
                .productId(product.getProductId())
                .warehouseId(warehouse.getWarehouseId())
                .build())
            .collect(Collectors.toList());
    }

    private static Set<Product> selectProductsToStore(List<Product> products)
    {
        // store 1 to n products in each warehouse
        // choose number of products to be stored randomly

        final int numberOfProducts = products.size();
        Random random = new Random();
        int numberOfProductsToStore = random.nextInt(numberOfProducts);
        
        if (numberOfProductsToStore < 1)
            numberOfProductsToStore = 1;
        
        if (numberOfProductsToStore == numberOfProducts)
            return Set.copyOf(products);

        Set<Product> selectedProducts = new HashSet<>(numberOfProducts);
        while (selectedProducts.size() < numberOfProductsToStore)
        {
            int productIndex = random.nextInt(numberOfProducts);
            selectedProducts.add(products.get(productIndex));
        }

        return selectedProducts;
    }

    private void generateCustomers()
    {
        final int bound = 1500;
        Random random = new Random();

        List<Customer> customers = new ArrayList<>(
            NUMBER_OF_CUSTOMERS);
        for (int i = 0; i < NUMBER_OF_CUSTOMERS; i++)
            customers.add(Customer.builder()
                .customerName("CUSTOMER_" + (i + 1))
                .posx(random.nextInt(bound))
                .posy(random.nextInt(bound))
                .build());

        planningRepository.saveAllCustomers(customers);
        calculateDistancesFromWarehousesForAllCustomersAndSave();
    }

    private void calculateDistancesFromWarehousesForAllCustomersAndSave()
    {
        log.info("Pre calculating distances b/w customers and warehouses");
        List<Customer> customers = planningRepository.findAllCustomers();
        List<Warehouse> warehouses = planningRepository.findAllWarehouses();

        List<CustomerWarehouseDistance> cwDistances = new ArrayList<>(customers
            .size() * warehouses.size());

        int i = 0;
        for (Customer customer : customers) {
            cwDistances.addAll(PlanningService.customerWarehousesDistances(
                customer, warehouses));
            i++;
            if (i % 10000 == 0) {
                log.info("Completed calculation for {} customers", i);
            }
        }

        log.info("Storing pre-calculated distances in database");
        saveAllDistances(cwDistances);
        log.info("Pre calculated distances have been saved successfully.");
    }
    
    private void saveAllDistances(List<CustomerWarehouseDistance> cwDistances) {
        final int totalSize = cwDistances.size();
        final int chunkSize = 10000;
        int start = 0;
        int end = 0;
        do {
            start = end;
            end = start + chunkSize <= totalSize ? start + chunkSize : totalSize;
            log.info("Storing customer-warehouse distances. start: {}, end: {}", start, end);
            planningRepository.saveAllCustomerWarehouseDistances(cwDistances.subList(start, end)); 
        }
        while (end < totalSize);
    }

}
