package com.mitocode.service.impl;

import java.util.stream.Collectors;

import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.ReactiveTransaction;
import org.springframework.transaction.reactive.TransactionCallback;
import org.springframework.transaction.reactive.TransactionalOperator;

import com.mitocode.document.Factura;
import com.mitocode.document.Plato;
import com.mitocode.pagination.PageSupport;
import com.mitocode.repo.IFacturaRepo;
import com.mitocode.repo.IPlatoRepo;
import com.mitocode.service.IFacturaService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class FacturaServiceImpl implements IFacturaService{

	@Autowired
	private TransactionalOperator txo;	
	
	@Autowired
	private IFacturaRepo repo;
	
	@Autowired
	private IPlatoRepo pRepo;	
	
	@Override
	public Mono<Factura> registrar(Factura t) {
		return repo.save(t);
	}

	@Override
	public Mono<Factura> modificar(Factura t) {		
		return repo.save(t);
	}

	@Override
	public Flux<Factura> listar() {
		return repo.findAll();
	}
	
	public Mono<Factura> listarPorId(String v) {
		return repo.findById(v);
	}

	@Override
	public Mono<Void> eliminar(String v) {
		return repo.deleteById(v);
	}

	@Override
	public Mono<PageSupport<Factura>> listarPagina(Pageable page) {
		return repo.findAll()
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

	@Override
	public Mono<Factura> registrarTransaccional(Factura f) throws InterruptedException {

		Plato p = new Plato();		
		p.setEstado(true);
		p.setNombre("CHAUFA");
		p.setPrecio(25);
		
		Plato p2 = new Plato();		
		p2.setEstado(true);
		p2.setNombre("CECINA");
		p2.setPrecio(27);
		
		
		/*this.txo.execute(new TransactionCallback<Factura>() {
			@Override
			public Publisher<Factura> doInTransaction(ReactiveTransaction status) {
				return repo.save(f);
			}
		});*/
		
		return this.txo.execute(status -> pRepo.save(p)).then(pRepo.save(p2)).then(repo.save(f));
		
		//throw new InterruptedException("FALLO");
	}
}
