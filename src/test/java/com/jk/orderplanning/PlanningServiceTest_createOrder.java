package com.jk.orderplanning;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.mockito.quality.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class PlanningServiceTest_createOrder
{

    @Mock
    PlanningRepository planningRepository;

    @InjectMocks
    PlanningService planningService;

    @Test
    void whenNoWarehouseWithProduct_thenExpectException()
    {
        OrderRequest orderRequest = new OrderRequest();

        // setup
        when(planningRepository.findClosestWarehouseHavingProduct(orderRequest))
            .thenReturn(null);

        // execute code
        assertThrows(NoWarehouseHavingProductException.class,
            () -> planningService.createOrder(orderRequest));
    }

    @Test
    void whenAWarehouseWithProductExists_thenSucceed_1()
        throws NoWarehouseHavingProductException
    {
        WarehouseDistance wd = WarehouseDistance.of("warehouse_1", 10);
        OrderRequest orderRequest = new OrderRequest();

        // setup
        when(planningRepository.findClosestWarehouseHavingProduct(orderRequest))
            .thenReturn(wd);

        // execute code
        OrderResponse orderResponse = planningService.createOrder(orderRequest);

        // verify
        assertThat(orderResponse.getWarehouseName()).isEqualTo("warehouse_1");
        assertThat(orderResponse.getEstimatedDeliveryTime()).isEqualTo(
            "1 Hours");
        assertThat(orderResponse.getDistance()).isEqualTo("10 Kilometers");
    }

    @Test
    void whenAWarehouseWithProductExists_thenSucceed_2()
        throws NoWarehouseHavingProductException
    {
        WarehouseDistance wd = WarehouseDistance.of("warehouse_2", 1000);
        OrderRequest orderRequest = new OrderRequest();

        // setup
        when(planningRepository.findClosestWarehouseHavingProduct(orderRequest))
            .thenReturn(wd);

        // execute code
        OrderResponse orderResponse = planningService.createOrder(orderRequest);

        // verify
        assertThat(orderResponse.getWarehouseName()).isEqualTo("warehouse_2");
        assertThat(orderResponse.getEstimatedDeliveryTime()).isEqualTo(
            "26 Hours");
        assertThat(orderResponse.getDistance()).isEqualTo("1000 Kilometers");
    }

}
