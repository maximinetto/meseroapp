package com.mitocode;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.mitocode.document.Usuario;
import com.mitocode.repo.IUsuarioRepo;

import reactor.core.publisher.Mono;

@Service
public class UserDetailsServiceImpl implements ReactiveUserDetailsService{

	@Autowired
	private IUsuarioRepo repo;
	
	@Override
	public Mono<UserDetails> findByUsername(String username) {
		
		Mono<Usuario> usuarioMono = repo.findOneByUsuario(username);
		
		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();		

		return usuarioMono.doOnNext(u -> {			
			u.getRoles().forEach(role -> authorities.add(new SimpleGrantedAuthority(role.getNombre())));			
		}).flatMap(u -> {
			return Mono.just(new User(u.getUsuario(), u.getClave(), u.getEstado(), true, true, true, authorities));
		});
	}

}
