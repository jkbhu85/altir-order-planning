package com.jk.orderplanning;

import java.util.*;
import java.util.stream.*;

import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

import lombok.*;

@RequiredArgsConstructor
@Service
public class PlanningService
{

    private static final int AVERAGE_MOVEMENT_PER_HOUR = 40;

    private final PlanningRepository planningRepository;

    private static int distanceBetween(Customer req, Warehouse w)
    {
        int dx = req.getPosx() - w.getPosx();
        int dy = req.getPosy() - w.getPosy();
        int x2 = dx * dx;
        int y2 = dy * dy;

        return (int) Math.sqrt(x2 + y2);
    }

    @Transactional
    public void saveCustomer(Customer customer)
    {
        List<Warehouse> warehouses = planningRepository.findAllWarehouses();
        Customer savedCustomer = planningRepository.saveCustomer(customer);

        List<CustomerWarehouseDistance> distances =
            customerWarehousesDistances(savedCustomer, warehouses);

        planningRepository.saveAllCustomerWarehouseDistances(distances);
    }
    
    static List<CustomerWarehouseDistance> customerWarehousesDistances(
        Customer customer,
        List<Warehouse> warehouses)
    {
        return warehouses.stream()
            .map(warehouse -> CustomerWarehouseDistance.builder()
                .warehouseId(warehouse.getWarehouseId())
                .customerId(customer.getCustomerId())
                .distance(distanceBetween(customer, warehouse))
                .build())
            .collect(Collectors.toList());
    }

    public OrderResponse createOrder(OrderRequest orderRequest)
        throws NoWarehouseHavingProductException
    {
        WarehouseDistance wd = planningRepository
            .findClosestWarehouseHavingProduct(orderRequest);

        if (wd == null)
            throw new NoWarehouseHavingProductException();

        return toOrderResponse(wd);
    }

    static OrderResponse toOrderResponse(WarehouseDistance wd)
    {
        int hours = (wd.getDistance() / AVERAGE_MOVEMENT_PER_HOUR) + 1;
        return OrderResponse.builder()
            .warehouseName(wd.getWarehouseName())
            .distance(wd.getDistance() + " Kilometers")
            .estimatedDeliveryTime(hours + " Hours")
            .build();
    }

}
