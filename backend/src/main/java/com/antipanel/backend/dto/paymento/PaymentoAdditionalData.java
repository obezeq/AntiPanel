package com.antipanel.backend.dto.paymento;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for additional data in Paymento payment requests.
 * Paymento expects an array of key-value pairs for metadata.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentoAdditionalData {
    private String key;
    private String value;
}
