package files;

public class LoginMessage extends AbstractMessage{

    public String login;
    private String password;
    public String serverIp;
    public int serverPort;

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String directory;

    public LoginMessage() {
    }

    public LoginMessage(String directory) {
        this.login = null;
        this.password = null;
        this.directory = directory;
    }

    public LoginMessage(String password, String login, String ip, int port) {
        this.login = login;
        this.password = password;
        this.serverPort = port;
        this.serverIp = ip;
    }

    public String getPassword() {
        return password;
    }
}
