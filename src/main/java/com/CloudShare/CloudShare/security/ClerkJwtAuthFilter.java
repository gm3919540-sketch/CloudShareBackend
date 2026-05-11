package com.CloudShare.CloudShare.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClerkJwtAuthFilter extends OncePerRequestFilter {
    @Value("${clerk.issuer}")
    private  String clerkIssuer;
    private  final  ClerksJwksProvider jwksProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(request.getRequestURI().contains("/webhooks") || request.getRequestURI().contains("/public") || request.getRequestURI().contains("/download") ){
            filterChain.doFilter(request,response);
            return;
        }
        log.info("inside do filter");

       String authHeader = request.getHeader("Authorization");
        log.info("authheader"+ authHeader);
        if(authHeader==null || !authHeader.startsWith("Bearer ")){
            response.sendError(HttpServletResponse.SC_FORBIDDEN,"Authorization header missing or invalid");
            return;
        }
        try{
            String token = authHeader.substring(7);
           String[] chunks= token.split("\\.");
           if(chunks.length<3){
               response.sendError(HttpServletResponse.SC_FORBIDDEN,"Invalid JWT TKEN FORMET");
               return;
           }

        String headerJson=   new String(Base64.getUrlDecoder().decode(chunks[0]));
            System.out.println(headerJson);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode headerNode = mapper.readTree(headerJson);
            if(!headerNode.has("kid")){
                response.sendError(HttpServletResponse.SC_FORBIDDEN,"token header is missing");
                return;
            }
            String kid =headerNode.get("kid").asText();
            PublicKey publicKey = jwksProvider.getPublicKey(kid);
            //verify the token
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .requireIssuer(clerkIssuer)
                    .setAllowedClockSkewSeconds(60)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            System.out.println("JWT VERIFIED SUCCESSFULLY");

            System.out.println(claims);


            String clerkId =claims.getSubject();
            System.out.println("CLERK ID: " + clerkId);
            UsernamePasswordAuthenticationToken authenticationToken= new UsernamePasswordAuthenticationToken(clerkId,null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            filterChain.doFilter(request,response);

        }catch (Exception e){

            System.out.println("JWT ERROR");

            e.printStackTrace();

            System.out.println("ERROR MESSAGE:");
            System.out.println(e.getMessage());

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Invalid JWT token " + e.getMessage()
            );

            return;
        }


    }
}
