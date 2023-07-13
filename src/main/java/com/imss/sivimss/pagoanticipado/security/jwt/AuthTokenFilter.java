package com.imss.sivimss.pagoanticipado.security.jwt;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.imss.sivimss.pagoanticipado.util.AppConstantes;

public class AuthTokenFilter extends OncePerRequestFilter {
	@Autowired
	private JwtTokenProvider jwtUtils;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try {
			// == recuperar el token autorization header
			String jwt = parseJwt(request);

			if (jwt != null && jwtUtils.validateToken(jwt, request)) {
				// == regresa un usuario
				UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
						jwtUtils.getUsernameFromToken(jwt), null, new ArrayList<>());

				// == setear el contexto para el usuario logueado
				SecurityContextHolder.getContext().setAuthentication(authenticationToken);

			}
		} catch (Exception e) {
			request.setAttribute(AppConstantes.STATUSEXCEPTION, AppConstantes.FORBIDDENEXCEPTION);

		}

		filterChain.doFilter(request, response);

	}

	private String parseJwt(HttpServletRequest request) {
		String headerAuth = request.getHeader("Authorization");

		if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
			return headerAuth.substring(7, headerAuth.length());
		}
		request.setAttribute(AppConstantes.STATUSEXCEPTION, AppConstantes.FORBIDDENEXCEPTION);
		return null;
	}
}