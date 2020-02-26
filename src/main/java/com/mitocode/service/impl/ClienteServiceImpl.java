package com.mitocode.service.impl;

import java.time.Duration;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.mitocode.document.Cliente;
import com.mitocode.pagination.PageSupport;
import com.mitocode.repo.IClienteRepo;
import com.mitocode.service.IClienteService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ClienteServiceImpl implements IClienteService{

	@Autowired
	private IClienteRepo repo;

	@Override
	public Mono<Cliente> registrar(Cliente t) {
		return repo.save(t);
	}

	@Override
	public Mono<Cliente> modificar(Cliente t) {		
		return repo.save(t);
	}

	@Override
	public Flux<Cliente> listar() {
		return repo.findAll();
	}

	@Override
	public Mono<Cliente> listarPorId(String v) {
		return repo.findById(v);
	}

	@Override
	public Mono<Void> eliminar(String v) {
		return repo.deleteById(v);
	}

	@Override
	public Flux<Cliente> listarDemorado() {
		return repo.findAll().delayElements(Duration.ofSeconds(1));
	}

	@Override
	public Flux<Cliente> listarSobrecargado() {
		return repo.findAll().repeat(3000);
	}
	
	@Override
	public Mono<PageSupport<Cliente>> listarPagina(Pageable page) {
		return repo.findAll() //mi query segun filtros
				.collectList()
				.map(lista -> new PageSupport<>(
						lista
						.stream()
						.skip(page.getPageNumber() * page.getPageSize())
						.limit(page.getPageSize())
						.collect(Collectors.toList()),
					page.getPageNumber(), page.getPageSize(), lista.size()
					));
	}

	
	
}
