package com.bodeganube.inventario.service;

import com.bodeganube.inventario.dto.ReservaResponse;
import com.bodeganube.inventario.exception.RecursoNoEncontradoException;
import com.bodeganube.inventario.exception.ReglaNegocioException;
import com.bodeganube.inventario.model.Producto;
import com.bodeganube.inventario.repository.ProductoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Logica de negocio del inventario. El controller solo traduce HTTP y delega aqui;
 * el acceso a datos queda en ProductoRepository.
 */
@Service
@Transactional(readOnly = true)
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<Producto> listar() {
        return productoRepository.findAll();
    }

    /**
     * RF-04: reserva stock si hay disponibilidad suficiente. Mueve unidades de "disponible"
     * a "reservado" para que otra orden no pueda tomarlas.
     */
    @Transactional
    public ReservaResponse reservar(String sku, int cantidad) {
        Producto producto = buscarPorSku(sku);
        if (producto.getStockDisponible() < cantidad) {
            throw new ReglaNegocioException("Stock insuficiente para " + sku + ": disponible "
                    + producto.getStockDisponible() + ", solicitado " + cantidad);
        }
        producto.setStockDisponible(producto.getStockDisponible() - cantidad);
        producto.setStockReservado(producto.getStockReservado() + cantidad);
        return ReservaResponse.de(productoRepository.save(producto), cantidad, "Stock reservado correctamente");
    }

    /** Descuenta definitivamente unidades ya reservadas, cuando se confirma el despacho. */
    @Transactional
    public ReservaResponse descontar(String sku, int cantidad) {
        Producto producto = buscarPorSku(sku);
        if (producto.getStockReservado() < cantidad) {
            throw new ReglaNegocioException("No se pueden descontar " + cantidad + " unidades de " + sku
                    + ": solo hay " + producto.getStockReservado() + " reservadas");
        }
        producto.setStockReservado(producto.getStockReservado() - cantidad);
        return ReservaResponse.de(productoRepository.save(producto), cantidad, "Reserva descontada del inventario");
    }

    private Producto buscarPorSku(String sku) {
        return productoRepository.findBySku(sku)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un producto con SKU " + sku));
    }
}
