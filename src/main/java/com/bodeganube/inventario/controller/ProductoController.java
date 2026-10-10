package com.bodeganube.inventario.controller;

import com.bodeganube.inventario.dto.ActualizarProductoRequest;
import com.bodeganube.inventario.dto.ProductoRequest;
import com.bodeganube.inventario.dto.ProductoResponse;
import com.bodeganube.inventario.dto.ReservaRequest;
import com.bodeganube.inventario.dto.ReservaResponse;
import com.bodeganube.inventario.service.ProductoService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * RF-07: gestion centralizada del catalogo de productos e inventario (CRUD).
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
    public List<ProductoResponse> listar() {
        return productoService.listar();
    }

    @GetMapping("/{sku}")
    public ProductoResponse obtener(@PathVariable String sku) {
        return productoService.obtener(sku);
    }

    @PostMapping
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest request) {
        ProductoResponse creado = productoService.crear(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{sku}")
                .buildAndExpand(creado.sku())
                .toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @PutMapping("/{sku}")
    public ProductoResponse actualizar(@PathVariable String sku,
                                       @Valid @RequestBody ActualizarProductoRequest request) {
        return productoService.actualizar(sku, request);
    }

    @DeleteMapping("/{sku}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String sku) {
        productoService.eliminar(sku);
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
