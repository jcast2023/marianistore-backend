package com.techshop.util;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;

@Component
public class Token {
    
	
	    @Value("${app.jwt.secret}")  
	    private String tokenFirma;

	    @Value("${app.jwt.expiration:3600}")  
	    private Long tokenDuracion;
    
    public String crearToken(Integer idUsuario, String user, String email, List<String> roles) {
        long expiracionTiempo = tokenDuracion * 1_000;
        Date expiracionFecha = new Date(System.currentTimeMillis() + expiracionTiempo);
        
        Map<String, Object> map = new HashMap<>();
        map.put("idUsuario", idUsuario); 
        map.put("nombre", user);
        map.put("roles", roles != null ? roles : Collections.emptyList());

        return Jwts.builder()
                .setSubject(email)
                .setExpiration(expiracionFecha)
                .addClaims(map)
                .signWith(Keys.hmacShaKeyFor(tokenFirma.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
    
    public  UsernamePasswordAuthenticationToken getAuth(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(tokenFirma.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            String email = claims.getSubject();
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) claims.get("roles");
            
            
            List<GrantedAuthority> authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            return new UsernamePasswordAuthenticationToken(email, null, authorities);
        } catch (Exception e) {
            return null;
        }
    }
}