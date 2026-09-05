package com.bodeganube.inventario.dto;

import jakarta.validation.constraints.Positive;

public record ReservaRequest(@Positive Integer cantidad) {
}
