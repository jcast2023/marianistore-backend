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
        Usuario usuarioSimulado = new Usuario();
        usuarioSimulado.setIdUsuario(100);
        
        Pedido pedidoSimulado = new Pedido();
        pedidoSimulado.setIdPedido(idPedido);
        pedidoSimulado.setEstado("PENDIENTE");
        pedidoSimulado.setUsuario(usuarioSimulado);

        when(pedidoRepository.findById(idPedido)).thenReturn(Optional.of(pedidoSimulado));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoSimulado);

        // 2. Ejecución (When)
        PedidoDTO resultado = pedidoService.pagarPedido(idPedido);

        // 3. Verificación (Then)
        assertNotNull(resultado);
        assertEquals("PAGADO", pedidoSimulado.getEstado()); 
        verify(pedidoRepository, times(1)).save(any(Pedido.class)); 
    }

    @Test
    @DisplayName("Debería lanzar una excepción cuando el pedido no existe")
    void pagarPedidoNoEncontrado() {
        // 1. Preparación (Given)
        Integer idInexistente = 999;
        
        // Simulamos que el repositorio devuelve un Optional vacío (no encontró nada)
        when(pedidoRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // 2. Ejecución y Verificación (When & Then)
        // assertThrows verifica que la lógica lance una excepción de tipo RuntimeException
        assertThrows(RuntimeException.class, () -> {
            pedidoService.pagarPedido(idInexistente);
        });

        // Verificación de seguridad: confirmamos que el método save NUNCA se llamó
        // Esto garantiza que no se intentó guardar datos corruptos o nulos
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }
}