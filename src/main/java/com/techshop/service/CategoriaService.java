package com.techshop.service;

import java.util.List;

import com.techshop.model.Categoria;

public interface CategoriaService {

	List<Categoria> listarTodas();
    Categoria buscarPorId(Integer id);
}
