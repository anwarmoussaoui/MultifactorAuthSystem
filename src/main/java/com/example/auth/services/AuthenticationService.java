package com.example.auth.services;


import com.example.auth.config.JwtService;
import com.example.auth.controllers.AuthenticationRequest;
import com.example.auth.controllers.AuthenticationResponse;
import com.example.auth.controllers.RegisterRequest;
import com.example.auth.entities.Role;
import com.example.auth.entities.User;
import com.example.auth.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository repos;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final AuthenticationManager authenticationManager;
    private final EmailVerificationService emailService;

    public String register(RegisterRequest request) {
        Optional<User> optionalUser = repos.findUtilisateurByEmail(request.getEmail());

        if (!optionalUser.isPresent()){
        var user= User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .lastName(request.getLastName())
                .password(passwordEncoder.encode( request.getPassword()))
                .role(Role.User)
                .build();
        repos.save(user);
        var jwtToken =  jwtService.generateToken(user);
        return "email has been created successfully";}
        else {
            return "email already exist";
        }
    }

    public AuthenticationResponse login(AuthenticationRequest request) throws InterruptedException {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = repos.findUtilisateurByEmail(request.getEmail());
        if(user.isPresent()){
            User user1= user.get();

            String code = emailService.sendVerificationCode(request.getEmail());
            user1.setCode(code);
            repos.save(user1);
            executorService.submit(() -> clearCodeAfterDelay(user1));

        } return null;


    }
    @Async
    public void clearCodeAfterDelay(User user1) {
        try {
            Thread.sleep(30000);  // Delay for 30 seconds
            user1.setCode(null);  // Remove the code after delay
            repos.save(user1);  // Save the user with the code set to null
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public AuthenticationResponse verify(String email,String code){
        Optional<User> user= repos.findUtilisateurByEmail(email);
        if(user.isPresent()){
            User user1=user.get();
           if(emailService.verifyCode(code,user1.getCode())) {
        var jwtToken =  jwtService.generateToken(user1);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }else return null;
        }else  return  null;
    }
    public String changePassword(AuthenticationRequest req) {
        Optional<User> optionalUser = repos.findUtilisateurByEmail(req.getEmail());

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (passwordEncoder.matches(req.getPassword(), user.getPassword())) {
                user.setPassword(passwordEncoder.encode(req.getNewPassword()));
                repos.save(user);
                return "password changed";
            } else {
                throw new IllegalArgumentException("Incorrect current password");
            }
        } else {
            throw new IllegalArgumentException("User with email " + req.getEmail() + " not found");
        }
    }


}
