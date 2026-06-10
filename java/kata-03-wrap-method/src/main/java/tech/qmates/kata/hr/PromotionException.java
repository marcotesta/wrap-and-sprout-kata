package tech.qmates.kata.hr;

/**
 * Raised when a promotion (or demotion) violates one of the HR business rules.
 */
public class PromotionException extends Exception {

    public PromotionException(String message) {
        super(message);
    }
}
