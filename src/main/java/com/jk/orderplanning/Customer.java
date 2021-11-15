package com.jk.orderplanning;

import lombok.*;
import lombok.experimental.*;
import lombok.extern.jackson.*;

@Getter
@Builder
@Jacksonized
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Customer
{

    @With
    int customerId;
    String customerName;
    int posx;
    int posy;

}
