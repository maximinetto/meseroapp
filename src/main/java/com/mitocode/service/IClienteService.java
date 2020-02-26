package com.mitocode.service;

import org.springframework.data.domain.Pageable;

import com.mitocode.document.Cliente;
import com.mitocode.pagination.PageSupport;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IClienteService extends ICRUD<Cliente, String>{

	Flux<Cliente> listarDemorado();
	Flux<Cliente> listarSobrecargado();	
	Mono<PageSupport<Cliente>> listarPagina(Pageable page);

}
