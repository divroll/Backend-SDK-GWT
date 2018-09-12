package com.divroll.backend.sdk;

import java.util.Date;
import java.util.Random;

public class DataFactory {
    public String getName() {
        return "sample " + randomString();
    }
    public String getEmailAddress() {
        return randomString() + "@sample.com";
    }
    public String getFirstName() {
        return "John Smith";
    }
    public Date getBirthDate() {
        return new Date();
    }
    public String getAddress() {
        return "Sample Address " + randomString();
    }
    public String getRandomWord() {
        return "a random word " + randomString();
    }

    private String randomString() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        String generatedString = buffer.toString();
        return generatedString;
    }

}
