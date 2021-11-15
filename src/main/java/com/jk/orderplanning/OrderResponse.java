package com.jk.orderplanning;

import lombok.*;
import lombok.experimental.*;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderResponse
{

    String warehouseName;
    String distance;
    String estimatedDeliveryTime;

}
