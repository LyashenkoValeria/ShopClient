import lombok.SneakyThrows;

public class Main {
    private static final int PORT = 8832;
    private static final String HOST = "localhost";

    @SneakyThrows
    public static void main(String[] args) {
        new Client(PORT,HOST).launch();
    }
}
