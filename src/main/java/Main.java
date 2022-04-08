import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final int PORT = 8832;
    private static final String HOST = "localhost";

    @SneakyThrows
    public static void main(String[] args) {
        new Client(PORT,HOST).launch();
        //List<Product> list = new ArrayList<>();
//        Products list = new Products();
//        Product p1 = new Product("user", "apple", 1, null);
//        Product p2 = new Product("user", "cheese", 2, null);
//        list.getProducts().add(p1);
//        list.getProducts().add(p2);
//        ObjectWriter mapper = new ObjectMapper().writer().;
//        String json = mapper.writeValueAsString(list);
//        System.out.println(json);
    }
}
