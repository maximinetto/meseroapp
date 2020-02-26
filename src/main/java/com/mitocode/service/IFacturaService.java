package com.mitocode.service;

import org.springframework.data.domain.Pageable;

import com.mitocode.document.Factura;
import com.mitocode.pagination.PageSupport;

import reactor.core.publisher.Mono;

public interface IFacturaService extends ICRUD<Factura, String> {

	Mono<PageSupport<Factura>> listarPagina(Pageable page);
	Mono<Factura> registrarTransaccional(Factura f) throws InterruptedException;

}
