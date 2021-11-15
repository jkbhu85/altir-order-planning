package com.jk.orderplanning;

import lombok.*;
import lombok.experimental.*;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Warehouse
{

    int warehouseId;
    String warehouseName;
    int posx;
    int posy;

}
