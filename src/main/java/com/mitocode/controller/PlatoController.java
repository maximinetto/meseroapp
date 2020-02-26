package com.mitocode.controller;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

import com.mitocode.document.Plato;
import com.mitocode.service.IPlatoService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/platos")
@SessionAttributes("plato")
public class PlatoController {

	private static final Logger log = LoggerFactory.getLogger(PlatoController.class);
	
	@Autowired
	private IPlatoService service;
	
	@GetMapping("/listar")
	public Mono<String> listar(Model model) {
		
		Flux<Plato> platos = service.listar();
		
		platos.doOnNext(p -> log.info(p.getNombre())).subscribe();
		
		model.addAttribute("platos", platos);
		model.addAttribute("titulo", "Listado de Platos");
		return Mono.just("platos/listar");
	}
	
	@GetMapping("/form")
	public Mono<String> crear(Model model){
		model.addAttribute("plato", new Plato());
		model.addAttribute("titulo", "Formulario de plato");
		model.addAttribute("boton", "Crear");
		return Mono.just("platos/form");
	}
	
	@GetMapping("/form/{id}")
	public Mono<String> editar(@PathVariable String id, Model model){
		Mono<Plato> platoMono = service.listarPorId(id)
									.doOnNext(p -> log.info("Plato: " + p.getNombre()))
									.defaultIfEmpty(new Plato());
		
		/*platoMono.subscribe( data -> {
			System.out.println(data.getNombre());
		});*/		
		
		model.addAttribute("titulo", "Editar Plato");
		model.addAttribute("boton", "Editar");
		model.addAttribute("plato", platoMono);
		
		return Mono.just("platos/form");
	}
	
	@PostMapping("/operar")
	public Mono<String> operar(@Valid Plato plato, BindingResult validaciones, Model model, SessionStatus status) {
		
		if(validaciones.hasErrors()) {
			validaciones.reject("ERR780", "Error de validacion de formulario");
			model.addAttribute("titulo", "Errores en formulario de platos");
			model.addAttribute("boton", "Guardar");
			
			return Mono.just("platos/form");
		}else {
			//Limpiar variables de sesion
			status.setComplete();
			
			return service.registrar(plato)
					.doOnNext(p -> log.info("Plato guardado: " + p.getNombre() + " Id: " + p.getId()))
					.thenReturn("redirect:/platos/listar?success=plato+guardado");
		}				
	}
	
	@GetMapping("/eliminar/{id}")
	public Mono<String> eliminar(@PathVariable String id){
		return service.listarPorId(id)				
				//.then(Mono.just("redirect:/platos/listar")) //si el then esta aqui, flujo termina en ese momento
				.defaultIfEmpty(new Plato()) //Mono<Plato>
				.flatMap(p -> {
					if(p.getId() == null){
						return Mono.error(new InterruptedException("No existe id")); //Mono<Object>
					}
					return Mono.just(p); //Mono<Plato>
				})
				.flatMap(p -> {
					log.info("A punto de eliminar el plato: " + p.getNombre());
					return service.eliminar(p.getId()); //Mono<Void>
				})
				.then(Mono.just("redirect:/platos/listar"))
				.onErrorResume(ex -> Mono.just("redirect:/platos/listar?error=Error%20Interno"));
	}
	
	@GetMapping(value = "/cargarplatos/{term}", produces = { "application/json" })
	public @ResponseBody Flux<Plato> buscarPorNombre(@PathVariable String term) {
		Flux<Plato> platos = service.buscarPorNombre(term);		
		platos.doOnNext(p -> {
			log.info("INFO" + p.getNombre());
		});
		return platos;
	}
}
