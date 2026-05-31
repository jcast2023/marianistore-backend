package com.tiendaonline.dto;

import lombok.Data;

@Data
public class PreferenciaResponseDTO {
    private String preferenceId;
    private String initPoint;     // URL de pago real
    private String sandboxUrl;    // URL de pago test
}
