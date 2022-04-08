import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import okhttp3.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class Client {
    private final static int MIN_NAME_SIZE = 2;
    private final static int MAX_NAME_SIZE = 32;

    private int port;
    private String host;
    private HttpClient httpClient;
    private static BufferedReader reader;
    private String clientName;
    private List<Product> basket = new ArrayList<>();
    private List<Product> shopList = new ArrayList<>();


    public Client(int port, String host) {
        this.port = port;
        this.host = host;
        //this.httpClient = new HttpClient("http://" + host + ":" + port + "/shop");
        this.httpClient = new HttpClient("http://" + host + ":" + port);
    }


    public void launch() {
        reader = new BufferedReader(new InputStreamReader(System.in));
        enterUserName();
        help();
        while (true) {
            try {
                String[] command = reader.readLine().trim().split(" ");
                switch (command[0]) {
                    case "/help" -> help();
                    case "/list" -> list();
                    case "/add" -> {
                        if (command.length == 4) add(command[1], command[2], command[3]);
                        else System.out.println("Недостаточно аргументов для выполнения команды /add");
                    }
                    case "/put" -> {
                        if (command.length == 3) put(command[1], command[2]);
                        else System.out.println("Недостаточно аргументов для выполнения команды /put");
                    }
                    case "/remove" -> {
                        if (command.length == 3) remove(command[1], command[2]);
                        else System.out.println("Недостаточно аргументов для выполнения команды /remove");
                    }
                    case "/removeAll" -> removeAll();
                    case "/checkout" -> checkout();
                    case "/buy" -> buy();
                    case "/quit" -> quit();
                }

            } catch (IOException e) {
                System.out.println("Ошибка при чтении команды");
                System.exit(-1);
            }
        }
    }

    public void enterUserName() {
        try {
            while (true) {
                System.out.print("Введите ваше имя: ");
                clientName = reader.readLine();
                if (clientName.length() < MIN_NAME_SIZE || clientName.length() > MAX_NAME_SIZE || clientName.contains(" ")) {
                    System.out.println("Неверно задано имя. Длина 2-32 символов, без пробелов");
                } else {
                    auth();
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка при введении имени");
            System.exit(-1);
        }
    }

    private void auth(){
        int code = httpClient.authorization(clientName);
        if (code == 200) System.out.println("Вы успешно вошли в магазин!");
        else System.out.println("Ошибка при подключении к серверу. Повторите попытку");
    }

    private void help() {
        System.out.println("Для работы с магазином имеются следующие функции:");
        System.out.println("/help - получить список функций для работы с магазином");
        System.out.println("/list - получить список товаров, которые есть в магазине");
        System.out.println("/add <name> <n> <price> - добавить новый товар <name> в магазин в количестве <n>");
        System.out.println("/put <name> <n> - добавить в корзину товар <name> в количестве <n>");
        System.out.println("/remove <name> <n> - удалить из корзины товар <name> в количестве <n>");
        System.out.println("/removeAll - полностью очистить корзину");
        System.out.println("/checkout - проверить корзину");
        System.out.println("/buy - купить все товары, находящиеся в корзине");
        System.out.println("/quit - завершение работы");
    }

    private void list(){
        shopList = httpClient.getList();
        for (Product product: shopList){
            product.productToString();
        }
    }

    private void add(String name, String n, String p){
        try {
            int count = Integer.parseInt(n);
            int price = Integer.parseInt(p);
            Product newProduct = new Product(clientName, name, count, price);
            int result = httpClient.addProduct(newProduct);

            if (result == 201) System.out.println("В магазин был успешно добавлен товар " + name);
            else System.out.println("Ошибка при отправке на сервер. Повторите попытку");
        } catch (NumberFormatException e) {
            System.out.println("Некорректно заданы аргументы");
        }
        System.out.println("Команда /add в работе");
    }

    private void put(String name, String n) {
        try {
            int count = Integer.parseInt(n);
            boolean inBasket = checkBasket(name);
            if (inBasket){
                for(Product product: basket){
                    if (product.getProductName().equals(name)) {
                        product.setCount(product.getCount() + count);
                        System.out.println("Количество товара " + name + " увеличено. Теперь в корзине "
                                + product.getCount());
                        break;
                    }
                }
            } else {
                Product newProduct = new Product(clientName, name, count, null);
                basket.add(newProduct);
                System.out.println("В корзину успешно добавлен товар - " + name);
            }
        } catch (NumberFormatException e) {
            System.out.println("Число товаров задано некорректно");
        }
    }

    private void remove(String name, String n) {
        try {
            int count = Integer.parseInt(n);
            boolean inBasket = checkBasket(name);

            if (!inBasket) System.out.println("Такого продукта нет в корзине");
            else {
                Iterator<Product> iterator = basket.iterator();

                while (iterator.hasNext()) {
                    Product nextProduct = iterator.next();
                    if (nextProduct.getProductName().equals(name)) {
                        int currentCount = nextProduct.getCount();
                        if (currentCount == count) {
                            iterator.remove();
                            System.out.println("Из корзины полностью удалён товар - " + name);
                        } else if (currentCount > count) {
                            nextProduct.setCount(currentCount - count);
                            System.out.println("Из корзины убрано " + count + " товара " + name);
                        } else {
                            System.out.println("У вас в корзине товара " + name + " только " + nextProduct.getCount() +
                                    " штук. Для удаления укажите меньшее или равное число");
                        }
                    }
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Число товаров задано некорректно");
        }
    }

    private void removeAll(){
        basket.clear();
        System.out.println("Корзина полностью очищена");
    }

    private void checkout(){
        if (basket.isEmpty()) System.out.println("У вас в корзине ничего нет");
        else {
            System.out.println("У вас в корзине:");
            for (Product product: basket){
                product.checkoutString();
            }
        }
    }

    @SneakyThrows
    private void buy(){
        ObjectMapper mapper = new ObjectMapper();
        Response sum = httpClient.buyProduct(basket);
        if (sum.code() == 200) {
            String json = Objects.requireNonNull(sum.body()).string();
            Product result = mapper.readValue(json, Product.class);
            System.out.println("Покупка на сумму " + result.getPrice() + " успешно совершена!");
        } else {
            System.out.println("При покупке возникли проблемы");
        }
    }

    private void quit(){
        int result = httpClient.disconnect(clientName);
        if (result == 200) {
            System.out.println("Вы покидаете магазин. До свидания!");
            System.exit(-1);
        } else {
            System.out.println("Не удалось разорвать соединение. Повторите попытку");
        }
    }

    private boolean checkBasket(String name){
        boolean flag = false;
        for(Product product: basket){
            if (product.getProductName().equals(name)) {
                flag = true;
                break;
            }
        }
        return flag;
    }
}
