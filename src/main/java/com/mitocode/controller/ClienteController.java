package com.mitocode.controller;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.support.SessionStatus;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;

import com.mitocode.document.Cliente;
import com.mitocode.service.IClienteService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/clientes")
public class ClienteController {

	private static final Logger log = LoggerFactory.getLogger(ClienteController.class);
	
	@Autowired
	private IClienteService service;
	
	@Value("${ruta.subida}")
	private String RUTA_SUBIDA;
		
	@GetMapping("/listar")
	public Mono<String> listar(Model model) {

		Flux<Cliente> clientesFlux = service.listar();

		clientesFlux.doOnNext(c -> log.info(c.getNombres()));
		
		model.addAttribute("titulo", "Listado de Clientes");
		model.addAttribute("clientes", clientesFlux);
				
		return Mono.just("clientes/listar");
	}
	
	@GetMapping("/form")
	public Mono<String> crear(Model model) {
		model.addAttribute("cliente", new Cliente());
		model.addAttribute("titulo", "Formulario de cliente");
		model.addAttribute("boton", "Crear");
		
		return Mono.just("clientes/form");
	}
	
	@GetMapping("/form/{id}")
	public Mono<String> editar(@PathVariable String id, Model model) {
		Mono<Cliente> clienteMono = service.listarPorId(id) //Mono.empty() | Mono<Plato | Cliente>(°)
										.doOnNext(c -> log.info("Cliente: " + c.getNombres()))
										.defaultIfEmpty(new Cliente());
		
		model.addAttribute("titulo", "Editar Cliente");
		model.addAttribute("boton", "Editar");
		model.addAttribute("cliente", clienteMono);

		return Mono.just("clientes/form");
	}
	
	@PostMapping("/operar")
	public Mono<String> operar(@Valid Cliente cliente, BindingResult validaciones, Model model, @RequestPart FilePart file, SessionStatus status) {
		
		if(validaciones.hasErrors()) {
			validaciones.reject("ERR780", "Error de validacion de formulario");
			model.addAttribute("titulo", "Errores en formulario de clientes");
			model.addAttribute("boton", "Guardar");
			
			return Mono.just("clientes/form");
		}else {
			if(!file.filename().isEmpty()) {
				//generar un nombre aleatorio para el archivo para guardarlo en una ruta 'x'
				cliente.setUrlFoto(UUID.randomUUID().toString() + "-" + file.filename());
			}
			
			//Limpiar variables de sesion
			status.setComplete();
			
			return service.registrar(cliente)
					.doOnNext(c -> log.info("Cliente guardado: " + c.getNombres() + " Id: " + c.getId()))
					.flatMap(c -> {
						if(!file.filename().isEmpty()) {
							//Mono<Void>
							return file.transferTo(new File(RUTA_SUBIDA + c.getUrlFoto()));
						}
						//Mono<Void>
						return Mono.empty();
					})
					.thenReturn("redirect:/clientes/listar?success=cliente+guardado");
		}				
	}

	@GetMapping("/eliminar/{id}")
	public Mono<String> eliminar(@PathVariable String id){
		return service.listarPorId(id)				
				//.then(Mono.just("redirect:/clientes/listar")) //si el then esta aqui, flujo termina en ese momento
				.defaultIfEmpty(new Cliente()) //Mono<Cliente>
				.flatMap(c -> {
					if(c.getId() == null){
						return Mono.error(new InterruptedException("No existe id")); //Mono<Object>
					}
					return Mono.just(c); //Mono<Cliente>
				})
				.flatMap(c -> {
					log.info("A punto de eliminar el cliente: " + c.getNombres());
					return service.eliminar(c.getId()); //Mono<Void>
				})
				.then(Mono.just("redirect:/clientes/listar"))
				.onErrorResume(ex -> Mono.just("redirect:/clientes/listar?error=Error%20Interno"));
	}
	
	@GetMapping("/subidas/img/{nombreFoto:.+}")
	public Mono<ResponseEntity<Resource>> verFoto(@PathVariable String nombreFoto) throws MalformedURLException{
		Path ruta = Paths.get(RUTA_SUBIDA).resolve(nombreFoto).toAbsolutePath();
		
		Resource imagen = new UrlResource(ruta.toUri());
		
		//CONTROLAR EL RESPONSE HTTP
		return Mono.just(
					ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + imagen.getFilename() + "\"")
					.body(imagen)
				);
	}
	
	@GetMapping("/listar/datadriver")
	public Mono<String> listarDataDriver(Model model){
		int CANTIDAD_ELEMENTOS_MOSTRAR = 1;
		
		Flux<Cliente> clientesFlux = service.listarDemorado();
		
		model.addAttribute("clientes", new ReactiveDataDriverContextVariable(clientesFlux, CANTIDAD_ELEMENTOS_MOSTRAR));
		model.addAttribute("titulo", "Listado de Clientes");
		
		return Mono.just("clientes/listar");
	}
	
	@GetMapping("/listar/full")
	public Mono<String> listarFull(Model model){		
		
		Flux<Cliente> clientesFlux = service.listarSobrecargado();
		
		model.addAttribute("titulo", "Listado de clientes Full");
		model.addAttribute("clientes", clientesFlux);
		
		return Mono.just("clientes/listar");
	}

	@GetMapping("/listar/chunked") 
	public Mono<String> listarChunked(Model model) {
		Flux<Cliente> clientesFlux = service.listarSobrecargado();
		
		model.addAttribute("titulo", "Listado de clientes Chunked");
		model.addAttribute("clientes", clientesFlux);
		
		return Mono.just("clientes/listar-frag");
	}
}
