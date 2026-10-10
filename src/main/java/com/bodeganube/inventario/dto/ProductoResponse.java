package com.bodeganube.inventario.dto;

import com.bodeganube.inventario.model.Producto;

/** Lo que la API devuelve de un producto. Desacopla el JSON de la entidad JPA. */
public record ProductoResponse(
        Long id,
        String sku,
        String nombre,
        Integer stockDisponible,
        Integer stockReservado
) {
    public static ProductoResponse de(Producto producto) {
        return new ProductoResponse(producto.getId(), producto.getSku(), producto.getNombre(),
                producto.getStockDisponible(), producto.getStockReservado());
    }
}
