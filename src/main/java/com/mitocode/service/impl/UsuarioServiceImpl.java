package com.mitocode.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mitocode.document.Usuario;
import com.mitocode.repo.IUsuarioRepo;
import com.mitocode.service.IUsuarioService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UsuarioServiceImpl implements IUsuarioService {

	@Autowired
	private IUsuarioRepo repo;
	
	@Override
	public Mono<Usuario> registrar(Usuario t) {
		return repo.save(t);
	}

	@Override
	public Mono<Usuario> modificar(Usuario t) {		
		return repo.save(t);
	}

	@Override
	public Flux<Usuario> listar() {
		return repo.findAll();
	}
	
	@Override
	public Mono<Usuario> listarPorId(String v) {
		return repo.findById(v);
	}

	@Override
	public Mono<Void> eliminar(String v) {
		return repo.deleteById(v);
	}
}
