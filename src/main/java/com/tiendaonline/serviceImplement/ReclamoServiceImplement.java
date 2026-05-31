package com.tiendaonline.serviceImplement;

import com.tiendaonline.dto.ReclamoDTO;
import com.tiendaonline.model.Reclamo;
import com.tiendaonline.repository.ReclamoRepository;
import com.tiendaonline.service.EmailService;
import com.tiendaonline.service.ReclamoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReclamoServiceImplement implements ReclamoService {

    private final ReclamoRepository reclamoRepository;
    private final EmailService emailService;

    public ReclamoServiceImplement(ReclamoRepository reclamoRepository, EmailService emailService) {
        this.reclamoRepository = reclamoRepository;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public Reclamo registrarReclamo(ReclamoDTO dto) {
        Reclamo reclamo = new Reclamo();

        // Mapeo de datos del DTO a la Entidad
        reclamo.setNombres(dto.getNombres());
        reclamo.setApellidos(dto.getApellidos());
        reclamo.setTipoDocumento(dto.getTipoDocumento());
        reclamo.setNumDocumento(dto.getNumDocumento());
        reclamo.setEsMenor(dto.getEsMenor());
        reclamo.setDireccion(dto.getDireccion());
        reclamo.setDepartamento(dto.getDepartamento());
        reclamo.setProvincia(dto.getProvincia());
        reclamo.setDistrito(dto.getDistrito());
        reclamo.setEmail(dto.getEmail());
        reclamo.setTelefono(dto.getTelefono());

        reclamo.setTipoBien(dto.getTipoBien());
        reclamo.setMontoReclamado(dto.getMontoReclamado());
        reclamo.setPedido(dto.getPedido());
        reclamo.setDescripcion(dto.getDescripcion());

        reclamo.setQuejaReclamo(dto.getQuejaReclamo());
        reclamo.setDetalle(dto.getDetalle());
        reclamo.setPedidoConsumidor(dto.getPedidoConsumidor());

        // Lógica de negocio protegida en el Back-end
        reclamo.setFechaRegistro(LocalDateTime.now());
        reclamo.setEstado("PENDIENTE");

        String codigoCorto = UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        reclamo.setCodigoReclamo("REC-" + LocalDateTime.now().getYear() + "-" + codigoCorto);

        // 1. Guardamos el reclamo en la base de datos MySQL
        Reclamo reclamoGuardado = reclamoRepository.save(reclamo);

        // 2. 🔥 Enviamos el correo de confirmación de manera asíncrona
        emailService.enviarCorreoReclamo(reclamoGuardado);

        // 3. Retornamos la entidad guardada para la respuesta de la API
        return reclamoGuardado;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reclamo> listarTodos() {
        return reclamoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Reclamo> buscarPorCodigo(String codigoReclamo) {
        return reclamoRepository.findByCodigoReclamo(codigoReclamo);
    }
}