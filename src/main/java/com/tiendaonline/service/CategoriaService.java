package com.tiendaonline.service;

import java.util.List;

import com.tiendaonline.model.Categoria;

public interface CategoriaService {

	List<Categoria> listarTodas();
    Categoria buscarPorId(Integer id);
}
