package utils;

import java.util.Random;

/**
 * A utility class for generating and validating simple math-based CAPTCHAs.
 * This provides a basic anti-bot protection for forms.
 */
public class CaptchaGenerator {
    private static final Random random = new Random();
    
    // Store the last generated CAPTCHA answer for validation
    private static int lastAnswer;
    
    /**
     * Generates a simple math CAPTCHA question.
     * @return A string containing a math problem (e.g., "5 + 3 = ?")
     */
    public static String generateMathCaptcha() {
        int num1 = random.nextInt(10) + 1; // 1-10
        int num2 = random.nextInt(10) + 1; // 1-10
        int operation = random.nextInt(3); // 0: addition, 1: subtraction, 2: multiplication
        
        String operationSymbol;
        switch (operation) {
            case 0: // Addition
                operationSymbol = "+";
                lastAnswer = num1 + num2;
                break;
            case 1: // Subtraction (ensure positive result)
                if (num1 < num2) {
                    int temp = num1;
                    num1 = num2;
                    num2 = temp;
                }
                operationSymbol = "-";
                lastAnswer = num1 - num2;
                break;
            case 2: // Multiplication
                operationSymbol = "×";
                lastAnswer = num1 * num2;
                break;
            default:
                operationSymbol = "+";
                lastAnswer = num1 + num2;
        }
        
        return num1 + " " + operationSymbol + " " + num2 + " = ?";
    }
    
    /**
     * Validates the user's answer against the expected result.
     * @param userAnswer The answer provided by the user
     * @return true if the answer is correct, false otherwise
     */
    public static boolean validateCaptcha(String userAnswer) {
        if (userAnswer == null || userAnswer.trim().isEmpty()) {
            return false;
        }
        
        try {
            int answer = Integer.parseInt(userAnswer.trim());
            return answer == lastAnswer;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Gets the expected answer for the last generated CAPTCHA.
     * This is primarily for testing purposes.
     * @return The answer to the last generated CAPTCHA
     */
    public static int getLastAnswer() {
        return lastAnswer;
    }
}