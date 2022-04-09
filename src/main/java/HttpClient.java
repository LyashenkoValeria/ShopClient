import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.SneakyThrows;
import okhttp3.*;
import java.util.*;

public class HttpClient {
    private String url;
    OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");


    public HttpClient(String url) {
        this.url = url;
    }


    @SneakyThrows
    public ResponseCode authorization(String name) {
        Product user = getUser(name);
        Products products = new Products();
        products.getProducts().add(user);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String jsonString = ow.writeValueAsString(products);
        RequestBody formBody = RequestBody.create(jsonString, JSON);
        Request request = new Request.Builder()
                .url(url + "/auth")
                .header("Content-Type", "application/json")
                .post(formBody)
                .build();
        Response response = client.newCall(request).execute();

        ObjectMapper mapper = new ObjectMapper();
        Code code = mapper.readValue(response.body().string(), Code.class);
        return code.getCode();
    }

    @SneakyThrows
    public List<Product> getList() {
        ObjectMapper mapper = new ObjectMapper();
        Request request = new Request.Builder()
                .url(url + "/list")
                .build();
        Response response = client.newCall(request).execute();
        String json = Objects.requireNonNull(response.body()).string();
        response.close();

        TypeFactory typeFactory = mapper.getTypeFactory();
        MapType mapType = typeFactory.constructMapType(HashMap.class, String.class, Product[].class);
        Map<String, Product[]> map = mapper.readValue(json, mapType);
        List<Product> list = Arrays.asList(map.get("products"));

        return list;
    }

    @SneakyThrows
    public ResponseCode addProduct(Product product) {
        Products products = new Products();
        products.getProducts().add(product);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String jsonString = ow.writeValueAsString(products);
        RequestBody formBody = RequestBody.create(jsonString, JSON);
        Request request = new Request.Builder()
                .url(url + "/add")
                .header("Content-Type", "application/json")
                .post(formBody)
                .build();
        Response response = client.newCall(request).execute();

        ObjectMapper mapper = new ObjectMapper();
        Code code = mapper.readValue(response.body().string(), Code.class);
        return code.getCode();
    }

    @SneakyThrows
    public ResponseCode buyProduct(List<Product> basket) {
        Products products = new Products();
        products.setProducts(basket);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        RequestBody formBody = RequestBody.create(ow.writeValueAsString(products), JSON);

        Request request = new Request.Builder()
                .url(url + "/buy")
                .post(formBody)
                .build();
        Response response = client.newCall(request).execute();

        ObjectMapper mapper = new ObjectMapper();
        Code code = mapper.readValue(response.body().string(), Code.class);
        return code.getCode();
    }

    @SneakyThrows
    public ResponseCode disconnect(String name) {
        Product user = getUser(name);
        Products products = new Products();
        products.getProducts().add(user);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String jsonString = ow.writeValueAsString(products);
        RequestBody formBody = RequestBody.create(jsonString, JSON);
        Request request = new Request.Builder()
                .url(url + "/disconnect")
                .header("Content-Type", "application/json")
                .post(formBody)
                .build();
        Response response = client.newCall(request).execute();

        ObjectMapper mapper = new ObjectMapper();
        Code code = mapper.readValue(response.body().string(), Code.class);
        return code.getCode();
    }

    private Product getUser(String name){
        Product user = new Product();
        user.setUsername(name);
        return user;
    }

}
