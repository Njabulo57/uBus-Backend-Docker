package org.tracker.ubus.ubus.Components.OneTimePassword.Generator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.SecureRandom;


@Component
@RequiredArgsConstructor
public final class OneTimePasswordGenerator {

    @Value("${otp.length}")
    private int length;

    @Value("${otp.numeric}")
    private String numeric;

    @Value("${otp.alphabets}")
    private String alphabets;

    private final SecureRandom secureRandom;

    /**
     * Generates a random alphanumeric string of the specified length.
     * @return A random alphanumeric string of the specified length.
     */
    public String generateOTP() {
        return generateAlphaNumeric(length);
    }

    /**
     * Generates a random alphanumeric string of the specified length.
     * @param length The length of the string (default = 8)
     * @return A random alphanumeric string of the specified length.
     */
    private String generateAlphaNumeric(int length) {


        var stringBuilder = new StringBuilder();
        for(int i = 0; i < length; i++) {

            int randomIndex = 0;
            int choice = secureRandom.nextInt(2);

            char charToAppendInBuilder = '\0';

            if(choice ==1) { //we go for numeric values
                randomIndex = secureRandom.nextInt(numeric.length());
                charToAppendInBuilder = numeric.charAt(randomIndex); //initialize the numeric char
            }else { // we go for alphabetic values

                randomIndex = secureRandom.nextInt(alphabets.length());
                //initialize the alphabet with a random case
                charToAppendInBuilder = getRandomLetterCase(randomIndex);

            }
            stringBuilder.append(charToAppendInBuilder);
        }

        return stringBuilder.toString();
    }

    /**
     * Converts the letter at the specified index of the alphabet string to a random case
     * (either uppercase or lowercase) and returns the resulting character.
     *
     * @param randomIndex The index of the letter in the alphabet string to be converted.
     * @return A character representing either the uppercase or lowercase form of the letter
     *         at the given index in the alphabet string.
     */
    private char getRandomLetterCase(int randomIndex) {

        int letterCase = secureRandom.nextInt(2); //determine the case by dice roll

        char letterToAppend;
        char letterReference;
        letterReference = alphabets.charAt(randomIndex);
        if(letterCase == 1) //small letter at 1
            letterToAppend = Character.toLowerCase(letterReference);

        else // upper case
            letterToAppend =  Character.toUpperCase(letterReference);

        return letterToAppend;
    }



}

