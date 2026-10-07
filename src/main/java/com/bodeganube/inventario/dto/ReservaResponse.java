package com.bodeganube.inventario.dto;

import com.bodeganube.inventario.model.Producto;

/** Resultado de reservar o descontar stock: muestra como quedo el producto despues del movimiento. */
public record ReservaResponse(
        String sku,
        int cantidad,
        int stockDisponible,
        int stockReservado,
        String mensaje
) {
    public static ReservaResponse de(Producto producto, int cantidad, String mensaje) {
        return new ReservaResponse(producto.getSku(), cantidad,
                producto.getStockDisponible(), producto.getStockReservado(), mensaje);
    }
}
