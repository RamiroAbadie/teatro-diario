package io.github.ramiroabadie.backend.identidad.internal.usuario;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Cuenta con perfil público (D3). Interna del módulo Identidad: ningún otro módulo la toca,
 * la referencian por id opaco (D30).
 *
 * <p>El {@code username} va a ser la URL del perfil ({@code /{username}}, HU-03), así que se
 * guarda normalizado a minúsculas: evita que {@code /rama} y {@code /Rama} sean dos perfiles y
 * hace que el índice único alcance para garantizar unicidad. Lo mismo el email.</p>
 *
 * <p>La contraseña nunca se guarda: se guarda su hash BCrypt (D44).</p>
 */
@Entity
@Table(name = "usuario")
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 20)
	private String username;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private RolUsuario rol;

	@Column(name = "creado_en", nullable = false, updatable = false)
	private Instant creadoEn;

	protected Usuario() {
		// requerido por JPA
	}

	Usuario(String username, String email, String passwordHash) {
		this.username = username;
		this.email = email;
		this.passwordHash = passwordHash;
		this.rol = RolUsuario.USUARIO;
		this.creadoEn = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public String getEmail() {
		return email;
	}

	String getPasswordHash() {
		return passwordHash;
	}

	public RolUsuario getRol() {
		return rol;
	}

	public Instant getCreadoEn() {
		return creadoEn;
	}
}
