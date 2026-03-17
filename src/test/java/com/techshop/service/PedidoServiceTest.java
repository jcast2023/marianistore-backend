package com.techshop.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.techshop.model.Pedido;
import com.techshop.model.Usuario;
import com.techshop.dto.PedidoDTO;
import com.techshop.repository.PedidoRepository;
import com.techshop.serviceImplement.PedidoServiceImplement;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @InjectMocks
    private PedidoServiceImplement pedidoService;

    @Test
    @DisplayName("Debería cambiar el estado del pedido a PAGADO exitosamente")
    void pagarPedidoExitoso() {
        // 1. Preparación (Given)
        Integer idPedido = 1;
        String metodoPago = "TARJETA_CREDITO"; // ← AGREGAR
        
        Usuario usuarioSimulado = new Usuario();
        usuarioSimulado.setIdUsuario(100);

        Pedido pedidoSimulado = new Pedido();
        pedidoSimulado.setIdPedido(idPedido);
        pedidoSimulado.setEstado("PENDIENTE");
        pedidoSimulado.setUsuario(usuarioSimulado);

        when(pedidoRepository.findById(idPedido)).thenReturn(Optional.of(pedidoSimulado));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoSimulado);

        // 2. Ejecución (When) - ← PASAR 2 PARÁMETROS
        PedidoDTO resultado = pedidoService.pagarPedido(idPedido, metodoPago);

        // 3. Verificación (Then)
        assertNotNull(resultado);
        assertEquals("PAGADO", pedidoSimulado.getEstado());
        assertEquals("TARJETA_CREDITO", pedidoSimulado.getMetodoPago()); // ← VERIFICAR MÉTODO DE PAGO
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Debería lanzar una excepción cuando el pedido no existe")
    void pagarPedidoNoEncontrado() {
        // 1. Preparación (Given)
        Integer idInexistente = 999;
        String metodoPago = "PAYPAL"; // ← AGREGAR

        // Simulamos que el repositorio devuelve un Optional vacío (no encontró nada)
        when(pedidoRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // 2. Ejecución y Verificación (When & Then)
        // assertThrows verifica que la lógica lance una excepción de tipo RuntimeException
        assertThrows(RuntimeException.class, () -> {
            pedidoService.pagarPedido(idInexistente, metodoPago); // ← PASAR 2 PARÁMETROS
        });

        // Verificación de seguridad: confirmamos que el método save NUNCA se llamó
        // Esto garantiza que no se intentó guardar datos corruptos o nulos
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }
    
    // ← AGREGAR NUEVO TEST: Verificar que el método de pago se guarde correctamente
    @Test
    @DisplayName("Debería guardar el método de pago correctamente")
    void pagarPedidoConMetodoPago() {
        // 1. Preparación (Given)
        Integer idPedido = 2;
        String metodoPago = "PAYPAL";
        
        Usuario usuarioSimulado = new Usuario();
        usuarioSimulado.setIdUsuario(200);

        Pedido pedidoSimulado = new Pedido();
        pedidoSimulado.setIdPedido(idPedido);
        pedidoSimulado.setEstado("PENDIENTE");
        pedidoSimulado.setUsuario(usuarioSimulado);

        when(pedidoRepository.findById(idPedido)).thenReturn(Optional.of(pedidoSimulado));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoSimulado);

        // 2. Ejecución (When)
        PedidoDTO resultado = pedidoService.pagarPedido(idPedido, metodoPago);

        // 3. Verificación (Then)
        assertNotNull(resultado);
        assertEquals("PAGADO", pedidoSimulado.getEstado());
        assertEquals("PAYPAL", pedidoSimulado.getMetodoPago());
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }
    
    // ← AGREGAR NUEVO TEST: Verificar que funciona sin método de pago
    @Test
    @DisplayName("Debería cambiar estado a PAGADO aunque no se envíe método de pago")
    void pagarPedidoSinMetodoPago() {
        // 1. Preparación (Given)
        Integer idPedido = 3;
        String metodoPago = null; // ← Sin método de pago
        
        Usuario usuarioSimulado = new Usuario();
        usuarioSimulado.setIdUsuario(300);

        Pedido pedidoSimulado = new Pedido();
        pedidoSimulado.setIdPedido(idPedido);
        pedidoSimulado.setEstado("PENDIENTE");
        pedidoSimulado.setUsuario(usuarioSimulado);

        when(pedidoRepository.findById(idPedido)).thenReturn(Optional.of(pedidoSimulado));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoSimulado);

        // 2. Ejecución (When)
        PedidoDTO resultado = pedidoService.pagarPedido(idPedido, metodoPago);

        // 3. Verificación (Then)
        assertNotNull(resultado);
        assertEquals("PAGADO", pedidoSimulado.getEstado());
        assertNull(pedidoSimulado.getMetodoPago()); // ← No debe tener método de pago
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }
}