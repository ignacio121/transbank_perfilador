package cl.transbank.domain.utils;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PasswordGenerator {

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()-_+=<>?";
    
    private static final SecureRandom random = new SecureRandom();

    public static String generatePassword(int length) {
        if (length < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        }

        List<Character> passwordChars = new ArrayList<>();

        // Garantizar que contiene al menos uno de cada tipo
        passwordChars.add(randomChar(UPPER));
        passwordChars.add(randomChar(LOWER));
        passwordChars.add(randomChar(DIGITS));
        passwordChars.add(randomChar(SPECIAL));

        String allAllowed = UPPER + LOWER + DIGITS + SPECIAL;

        // Completar el resto de la contraseña
        for (int i = 4; i < length; i++) {
            passwordChars.add(randomChar(allAllowed));
        }

        // Mezclar los caracteres para evitar patrones
        Collections.shuffle(passwordChars);

        // Convertir a String
        StringBuilder password = new StringBuilder();
        for (char c : passwordChars) {
            password.append(c);
        }

        return password.toString();
    }

    private static char randomChar(String chars) {
        return chars.charAt(random.nextInt(chars.length()));
    }
}
