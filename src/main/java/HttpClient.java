import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.SneakyThrows;
import okhttp3.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HttpClient {
    private String url;
    OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");


    public HttpClient(String url) {
        this.url = url;
    }


    @SneakyThrows
    public int authorization(String name) {
        Product user = getUser(name);
        Products products = new Products();
        products.getProducts().add(user);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String test = ow.writeValueAsString(products);
        RequestBody formBody = RequestBody.create(ow.writeValueAsString(test), JSON);
        Request request = new Request.Builder()
                .url(url + "/auth")
                .post(formBody)
                .build();
        Response response = client.newCall(request).execute();
        return response.code();
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
        return mapper.readValue(json, new TypeReference<>() {
        });
    }

    @SneakyThrows
    public int addProduct(Product product) {
        Products products = new Products();
        products.getProducts().add(product);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String test = ow.writeValueAsString(products);
        RequestBody formBody = RequestBody.create(ow.writeValueAsString(test), JSON);
        Request request = new Request.Builder()
                .url(url + "/add")
                .post(formBody)
                .build();
        Response response = client.newCall(request).execute();
        return response.code();
    }

    @SneakyThrows
    public Response buyProduct(List<Product> basket) {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        RequestBody formBody = RequestBody.create(ow.writeValueAsString(basket), JSON);
        Request request = new Request.Builder()
                .url(url + "/buy")
                .post(formBody)
                .build();
        Response response = client.newCall(request).execute();
        return response;
    }

    @SneakyThrows
    public int disconnect(String name) {
        Product user = getUser(name);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        RequestBody formBody = RequestBody.create(ow.writeValueAsString(user), JSON);
        Request request = new Request.Builder()
                .url(url + "/disconnect")
                .post(formBody)
                .build();
        Response response = client.newCall(request).execute();
        return response.code();
    }

    private Product getUser(String name){
        Product user = new Product();
        user.setUsername(name);
        return user;
    }

}
