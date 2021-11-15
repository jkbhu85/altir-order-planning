package com.jk.orderplanning;

import lombok.*;
import lombok.experimental.*;

@Getter
@AllArgsConstructor(staticName = "of")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EqualsAndHashCode
public class Product
{
    int productId;
    String productName;
}
