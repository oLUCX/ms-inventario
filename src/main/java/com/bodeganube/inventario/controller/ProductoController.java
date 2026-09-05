package com.bodeganube.inventario.controller;

import com.bodeganube.inventario.dto.ReservaRequest;
import com.bodeganube.inventario.dto.ReservaResponse;
import com.bodeganube.inventario.model.Producto;
import com.bodeganube.inventario.repository.ProductoRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * RF-07: gestion centralizada del catalogo de productos e inventario.
 * RF-04: reserva/descuento de stock (invocado por ms-ordenes, protegido alla con Circuit Breaker).
 */
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoRepository productoRepository;

    public ProductoController(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @GetMapping
    public List<Producto> listarCatalogo() {
        return productoRepository.findAll();
    }

    @PostMapping("/{sku}/reservar")
    public ResponseEntity<ReservaResponse> reservarStock(@PathVariable String sku,
                                                           @Valid @RequestBody ReservaRequest request) {
        Producto producto = productoRepository.findBySku(sku)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        if (producto.getStockDisponible() < request.cantidad()) {
            return ResponseEntity.ok(new ReservaResponse(false, "Stock insuficiente"));
        }

        producto.setStockDisponible(producto.getStockDisponible() - request.cantidad());
        producto.setStockReservado(producto.getStockReservado() + request.cantidad());
        productoRepository.save(producto);

        return ResponseEntity.ok(new ReservaResponse(true, "Stock reservado correctamente"));
    }

    @PostMapping("/{sku}/descontar")
    public ResponseEntity<Producto> descontarReserva(@PathVariable String sku,
                                                       @Valid @RequestBody ReservaRequest request) {
        Producto producto = productoRepository.findBySku(sku)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        producto.setStockReservado(Math.max(0, producto.getStockReservado() - request.cantidad()));
        return ResponseEntity.ok(productoRepository.save(producto));
    }
}
