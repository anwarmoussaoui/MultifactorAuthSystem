package com.example.auth.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class EmailVerificationService {
    @Autowired
    private EmailService emailService;


    public String generateVerificationCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    public String sendVerificationCode(String email) {

        String code = generateVerificationCode();
        emailService.sendEmail(email, "Verification Code", "Please use the following code to complete your login: " + code);
        return code;
    }

    public boolean verifyCode(String userEnteredCode, String sentCode) {
        return sentCode.equals(userEnteredCode);
    }
}