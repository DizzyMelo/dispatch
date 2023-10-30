package com.dicedev.dispatch.message;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDispatched {
    
    UUID id;
    UUID processedById;
    String notes;
}
