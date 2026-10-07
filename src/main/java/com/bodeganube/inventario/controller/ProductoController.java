package com.bodeganube.inventario.controller;

import com.bodeganube.inventario.dto.ReservaRequest;
import com.bodeganube.inventario.dto.ReservaResponse;
import com.bodeganube.inventario.model.Producto;
import com.bodeganube.inventario.service.ProductoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * RF-07: gestion centralizada del catalogo de productos e inventario.
 * RF-04: reserva/descuento de stock (invocado por ms-ordenes, protegido alla con Circuit Breaker).
 */
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public List<Producto> listarCatalogo() {
        return productoService.listar();
    }

    @PostMapping("/{sku}/reservar")
    public ReservaResponse reservarStock(@PathVariable String sku, @Valid @RequestBody ReservaRequest request) {
        return productoService.reservar(sku, request.cantidad());
    }

    @PostMapping("/{sku}/descontar")
    public ReservaResponse descontarReserva(@PathVariable String sku, @Valid @RequestBody ReservaRequest request) {
        return productoService.descontar(sku, request.cantidad());
    }
}
