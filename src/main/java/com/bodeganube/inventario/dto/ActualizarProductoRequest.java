package com.bodeganube.inventario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Datos editables de un producto. El SKU no se cambia (es la clave de negocio que usan las ordenes)
 * y el stock reservado solo se mueve con reservar/descontar.
 */
public record ActualizarProductoRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
        String nombre,

        @NotNull(message = "El stock disponible es obligatorio")
        @PositiveOrZero(message = "El stock disponible no puede ser negativo")
        Integer stockDisponible
) {
}
