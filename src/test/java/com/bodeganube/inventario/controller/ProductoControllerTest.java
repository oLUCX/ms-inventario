package com.bodeganube.inventario.controller;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bodeganube.inventario.dto.ProductoRequest;
import com.bodeganube.inventario.dto.ProductoResponse;
import com.bodeganube.inventario.exception.RecursoNoEncontradoException;
import com.bodeganube.inventario.exception.ReglaNegocioException;
import com.bodeganube.inventario.service.ProductoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Prueba la capa web aislada: codigos HTTP, validaciones y formato de error.
 * El service es un mock, asi que no se necesita base de datos.
 */
@WebMvcTest(ProductoController.class)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductoService productoService;

    @Test
    void obtenerProductoExistenteDevuelve200() throws Exception {
        when(productoService.obtener("SKU-1")).thenReturn(new ProductoResponse(1L, "SKU-1", "Polera talla M", 10, 0));

        mockMvc.perform(get("/api/productos/SKU-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("SKU-1"))
                .andExpect(jsonPath("$.stockDisponible").value(10));
    }

    @Test
    void obtenerProductoInexistenteDevuelve404ConMensaje() throws Exception {
        when(productoService.obtener("NO-EXISTE"))
                .thenThrow(new RecursoNoEncontradoException("No existe un producto con SKU NO-EXISTE"));

        mockMvc.perform(get("/api/productos/NO-EXISTE"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensaje").value("No existe un producto con SKU NO-EXISTE"));
    }

    @Test
    void crearProductoValidoDevuelve201ConLocation() throws Exception {
        when(productoService.crear(any(ProductoRequest.class)))
                .thenReturn(new ProductoResponse(1L, "SKU-1", "Polera talla M", 10, 0));

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"SKU-1\",\"nombre\":\"Polera talla M\",\"stockDisponible\":10}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/productos/SKU-1")))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void crearProductoInvalidoDevuelve400ConDetallePorCampo() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"SKU-1\",\"stockDisponible\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles.nombre").exists())
                .andExpect(jsonPath("$.detalles.stockDisponible").exists());
    }

    @Test
    void reservarSinStockDevuelve409() throws Exception {
        when(productoService.reservar("SKU-1", 50)).thenThrow(new ReglaNegocioException("Stock insuficiente"));

        mockMvc.perform(post("/api/productos/SKU-1/reservar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cantidad\":50}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("Stock insuficiente"));
    }

    @Test
    void eliminarDevuelve204() throws Exception {
        mockMvc.perform(delete("/api/productos/SKU-1"))
                .andExpect(status().isNoContent());

        verify(productoService).eliminar("SKU-1");
    }
}
