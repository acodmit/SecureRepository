package app;

import java.io.File;

public final class AppPaths {
    public static final String CERT_RETRIEVAL_VIEW = "cert_retrieval_view.fxml";
    public static final String LOGIN_VIEW = "login_view.fxml";
    public static final String REGISTRATION_VIEW = "registration_view.fxml";
    public static final String WELCOME_VIEW = "welcome_view.fxml";
    public static final String REPOSITORY_VIEW = "repository_view.fxml";
    public static final String RESOURCES_DIR =  "src" + File.separator + "main" + File.separator + "resources";
    public static final String CONFIGURATION_DIR =  RESOURCES_DIR + File.separator + "app" + File.separator + "configuration";
    public static final String CA_DIR =  CONFIGURATION_DIR + File.separator + "ca";
    public static final String CRL_DIR =  CONFIGURATION_DIR + File.separator + "crl";
    public static final String KEYSTORE_DIR =  CONFIGURATION_DIR + File.separator + "keystore";
    public static final String SALT_DIR =  CONFIGURATION_DIR + File.separator + "salt";
    public static final String REPOSITORY_DIR =  RESOURCES_DIR + File.separator + "app" + File.separator + "repository";
    public static final String DGST_DIR =  CONFIGURATION_DIR + File.separator + "dgst";
    public static final String PART_DIR =  REPOSITORY_DIR + File.separator + "part_";
    public static final String CA_KEYSTORE =  CA_DIR + File.separator + "ca.jks";
    public static final String CRL = CRL_DIR + File.separator + "crl";
    public static final String USERS = CONFIGURATION_DIR + File.separator + "users";
    private AppPaths(){
        // private constructor to prevent instantiation of the class object
    }
}
