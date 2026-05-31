package com.alpha.hotel.controller.api;

import com.alpha.hotel.dto.ApiAuthResponse;
import com.alpha.hotel.dto.LoginRequest;
import com.alpha.hotel.model.Utilisateur;
import com.alpha.hotel.security.JwtService;
import com.alpha.hotel.service.UtilisateurService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UtilisateurService utilisateurService;

    public ApiAuthController(AuthenticationManager authenticationManager,
                             JwtService jwtService,
                             UtilisateurService utilisateurService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.utilisateurService = utilisateurService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiAuthResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getMotDePasse())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Utilisateur utilisateur = utilisateurService.getUtilisateurParEmail(userDetails.getUsername());
        String token = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(new ApiAuthResponse(token, utilisateur.getEmail(), utilisateur.getRole().name()));
    }
}
