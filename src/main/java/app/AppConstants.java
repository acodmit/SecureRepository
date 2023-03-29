package app;

public final class AppConstants {
    public static final int MAX_LOGIN_ATTEMPTS = 3;
    public static final long USER_CERT_VALIDITY_PERIOD = 365L * 24 * 60 * 60 * 1000;
    public static final long WEEK = 7L * 24 * 60 * 60 * 1000;
    public static final int SALT_SIZE = 16;
    public static final int ITERATIONS = 1_000;
    public static final int KEY_SIZE = 256;
    public static final String DGST_ALGORITHM = "SHA-256";

    private AppConstants() {
        // prevent instantiation
    }
}
