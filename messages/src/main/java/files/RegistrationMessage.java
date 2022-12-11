package files;

public class RegistrationMessage extends AbstractMessage {

    private String name;
    private String email;
    private String password;
    private boolean regStatus;

    public RegistrationMessage(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public boolean isRegStatus() {
        return regStatus;
    }

    public String getName() {
        return name;
    }

    public RegistrationMessage(boolean regStatus) {
        this.regStatus = regStatus;
    }

    public String getEmail() {
        return email;
    }


    public String getPassword() {
        return password;
    }
}
