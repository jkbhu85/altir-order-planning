package com.jk.orderplanning;

import lombok.*;

@AllArgsConstructor(staticName = "of")
@Getter
public class WarehouseDistance
{

    private final String warehouseName;
    private final int distance;

}
