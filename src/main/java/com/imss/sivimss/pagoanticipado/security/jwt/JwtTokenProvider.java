package com.imss.sivimss.pagoanticipado.security.jwt;

import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.imss.sivimss.pagoanticipado.util.AppConstantes;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

@Component
public class JwtTokenProvider {

	private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

	@Value("${jwt.secretkey-flujo}")
	private String jwtSecret;
	@Value("${jwt.secretkey-dominios}")
	private String jwtSecretDominios;

	@Value("${jwt.expiration-milliseconds}")
	private String expiration;

	public String createToken(String subject) {
		Map<String, Object> claims = Jwts.claims().setSubject(subject);
		Date now = new Date();
		Date exp = new Date(now.getTime() + Long.parseLong(expiration) * 1000);
		return Jwts.builder().setHeaderParam("sistema", "sivimss").setClaims(claims).setIssuedAt(now).setExpiration(exp)
				.signWith(SignatureAlgorithm.HS512, jwtSecretDominios).compact();
	}

	public Long getUserIdFromJWT(String token) {
		Claims claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();

		return Long.parseLong(claims.getSubject());
	}

	public String getUsernameFromToken(String token) {
		Claims claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();

		return claims.getSubject();
	}

	public boolean validateToken(String authToken, HttpServletRequest request) {
		try {
			// Jwt token has not been tampered with
			Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
			return true;
		} catch (MalformedJwtException e) {
			request.setAttribute(AppConstantes.STATUSEXCEPTION, AppConstantes.MALFORMEDJWTEXCEPTION);
			return false;
		} catch (UnsupportedJwtException e) {
			logger.error("token no soportado");
			request.setAttribute(AppConstantes.STATUSEXCEPTION, AppConstantes.UNSUPPORTEDJWTEXCEPTION);
			return false;
		} catch (ExpiredJwtException e) {
			logger.error("token expirado");
			request.setAttribute(AppConstantes.STATUSEXCEPTION, AppConstantes.EXPIREDJWTEXCEPTION);
			return false;
		} catch (IllegalArgumentException e) {
			logger.error("token vacío {}", e.getMessage());
			request.setAttribute(AppConstantes.STATUSEXCEPTION, AppConstantes.ILLEGALARGUMENTEXCEPTION);
			return false;
		} catch (SignatureException e) {
			request.setAttribute(AppConstantes.STATUSEXCEPTION, AppConstantes.SIGNATUREEXCEPTION);
			return false;
		} catch (Exception e) {
			request.setAttribute(AppConstantes.STATUSEXCEPTION, AppConstantes.FORBIDDENEXCEPTION);
			return false;
		}
	}

}