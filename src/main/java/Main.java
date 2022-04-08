

public class Main {
    private static final int PORT = 8080;
    private static final String HOST = "localhost";

    public static void main(String[] args) {
        new Client(PORT,HOST).launch();
    }
}
