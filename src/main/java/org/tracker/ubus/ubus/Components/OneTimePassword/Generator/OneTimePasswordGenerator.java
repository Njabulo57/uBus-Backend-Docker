package org.tracker.ubus.ubus.Components.OneTimePassword.Generator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserRole;

import java.security.SecureRandom;


@Component
@RequiredArgsConstructor
public final class OneTimePasswordGenerator {

    @Value("${otp.length}")
    private int defaultLength;

    @Value("${otp.admin.length}")
    private int adminLength;

    @Value("${otp.numeric}")
    private String numeric;

    @Value("${otp.alphabets}")
    private String alphabets;

    private final SecureRandom secureRandom;


    /**
     * Generates a random alphanumeric string of the specified defaultLength.
     * the default defaultLength is 6
     * @return A random alphanumeric string of the specified defaultLength.
     */
    public String generateOTP(UserRole role) throws UnsupportedOperationException {

        if(role == UserRole.DRIVER)
            throw new UnsupportedOperationException("Driver OTP generation is not supported");

        var length = 0;
        var otp = "";

        if(role == UserRole.STAFF || role == UserRole.STUDENT) {
            length = defaultLength;
            otp = generateAlphaNumeric(length);
        }

        else {
            length = adminLength;
            otp = generateAlphaNumeric(length); // generate the otp
            otp = formatAdminOTP(otp); //format the otp for admin users
        }
       return otp;
    }

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


    /**
     * Formats the given OTP (One-Time Password) for admin users by inserting
     * hyphens at fixed intervals to enhance readability.
     *
     * @param otp The original OTP string, expected to be a 12-character alphanumeric sequence.
     * @return A formatted OTP string in the pattern "XXXX-XXXX-XXXX", where
     *         each segment consists of four characters from the input OTP.
     * @throws StringIndexOutOfBoundsException if the input OTP is shorter than 12 characters.
     */
    private String formatAdminOTP(String otp) {
        return otp.substring(0, 4) + "-" + otp.substring(4, 8) + "-" + otp.substring(8, 12);
    }


}

