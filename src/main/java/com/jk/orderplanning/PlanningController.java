package com.jk.orderplanning;

import static org.springframework.http.MediaType.*;

import java.util.*;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import lombok.*;

@RequiredArgsConstructor
@RestController
public class PlanningController
{

    private final PlanningService planningService;

    @PostMapping(value = "/customers/add", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public void addUser(@RequestBody Customer customer)
    {
        planningService.saveCustomer(customer);
    }

    @PostMapping(value = "/orders/create", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createOrder(@RequestBody OrderRequest req)
    {
        Object response;
        try
        {
            response = planningService.createOrder(req);
        }
        catch (NoWarehouseHavingProductException ignred)
        {
            response = Map.of("message",
                "Order could not be created. Currently, none of the warehouses have the product.");
        }

        return ResponseEntity.ok(response);
    }

}
