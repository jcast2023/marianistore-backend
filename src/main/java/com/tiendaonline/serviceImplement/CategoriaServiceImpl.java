package com.tiendaonline.serviceImplement;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tiendaonline.model.Categoria;
import com.tiendaonline.repository.CategoriaRepository;
import com.tiendaonline.service.CategoriaService;

@Service
public class CategoriaServiceImpl implements CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Override
    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    @Override
    public Categoria buscarPorId(Integer id) {
        return categoriaRepository.findById(id).orElse(null);
    }
}
