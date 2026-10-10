package com.bodeganube.inventario.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bodeganube.inventario.dto.ProductoRequest;
import com.bodeganube.inventario.dto.ProductoResponse;
import com.bodeganube.inventario.dto.ReservaResponse;
import com.bodeganube.inventario.exception.RecursoNoEncontradoException;
import com.bodeganube.inventario.exception.ReglaNegocioException;
import com.bodeganube.inventario.model.Producto;
import com.bodeganube.inventario.repository.ProductoRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Pruebas unitarias de las reglas de negocio: el repositorio se reemplaza por un mock de Mockito. */
@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    @Test
    void reservarMueveUnidadesDeDisponibleAReservado() {
        when(productoRepository.findBySku("SKU-1")).thenReturn(Optional.of(producto("SKU-1", 10, 0)));
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        ReservaResponse respuesta = productoService.reservar("SKU-1", 3);

        assertThat(respuesta.stockDisponible()).isEqualTo(7);
        assertThat(respuesta.stockReservado()).isEqualTo(3);
    }

    @Test
    void reservarSinStockSuficienteLanzaReglaNegocioYNoGuarda() {
        when(productoRepository.findBySku("SKU-1")).thenReturn(Optional.of(producto("SKU-1", 2, 0)));

        assertThatThrownBy(() -> productoService.reservar("SKU-1", 5))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Stock insuficiente");
        verify(productoRepository, never()).save(any());
    }

    @Test
    void reservarProductoInexistenteLanzaRecursoNoEncontrado() {
        when(productoRepository.findBySku("NO-EXISTE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.reservar("NO-EXISTE", 1))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void descontarMasDeLoReservadoLanzaReglaNegocio() {
        when(productoRepository.findBySku("SKU-1")).thenReturn(Optional.of(producto("SKU-1", 5, 1)));

        assertThatThrownBy(() -> productoService.descontar("SKU-1", 2))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void crearGuardaElProductoConStockReservadoEnCero() {
        when(productoRepository.existsBySku("SKU-1")).thenReturn(false);
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        ProductoResponse creado = productoService.crear(new ProductoRequest("SKU-1", "Polera talla M", 20));

        assertThat(creado.sku()).isEqualTo("SKU-1");
        assertThat(creado.stockDisponible()).isEqualTo(20);
        assertThat(creado.stockReservado()).isZero();
    }

    @Test
    void crearConSkuDuplicadoLanzaReglaNegocio() {
        when(productoRepository.existsBySku("SKU-1")).thenReturn(true);

        assertThatThrownBy(() -> productoService.crear(new ProductoRequest("SKU-1", "Polera talla M", 20)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Ya existe");
    }

    @Test
    void eliminarProductoConStockReservadoLanzaReglaNegocio() {
        when(productoRepository.findBySku("SKU-1")).thenReturn(Optional.of(producto("SKU-1", 5, 2)));

        assertThatThrownBy(() -> productoService.eliminar("SKU-1"))
                .isInstanceOf(ReglaNegocioException.class);
        verify(productoRepository, never()).delete(any());
    }

    private static Producto producto(String sku, int disponible, int reservado) {
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setSku(sku);
        producto.setNombre("Producto de prueba");
        producto.setStockDisponible(disponible);
        producto.setStockReservado(reservado);
        return producto;
    }
}
