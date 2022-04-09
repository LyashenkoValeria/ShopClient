import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
                    case "/disconnect" -> disconnect();
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
                    boolean isConnected = auth();
                    if (isConnected) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка при введении имени");
            System.exit(-1);
        }
    }

    private boolean auth(){
        ResponseCode code = httpClient.authorization(clientName);
        boolean isConnected = false;
        switch (code){
            case USER_ADDED -> {
                System.out.println("Вы успешно зарегистрировались в магазине!");
                isConnected = true;
            }
            case USER_NOT_ADDED -> System.out.println("Не удалось зарегистрироваться в магазине. Повторите попытку");
            case USER_AUTHORIZED -> {
                System.out.println("Вы успешно зашли в магазин!");
                isConnected = true;
            }
            default -> System.out.println("Ошибка при подключении к серверу. Повторите попытку");
        }
        return isConnected;
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
        System.out.println("/disconnect - удалить этого пользователя с сервера и завершить работу в магазине");
        System.out.println("/quit - завершить работу без удаления этого пользователя");
    }

    private void list(){
        shopList = httpClient.getList();
        if (shopList.size() != 0) {
            for (Product product : shopList) {
                product.productToString();
            }
        } else System.out.println("В магазине пока что ничего нет");
    }

    private void add(String name, String n, String p){
        try {
            int count = Integer.parseInt(n);
            int price = Integer.parseInt(p);
            Product newProduct = new Product(clientName, name, count, price);
            ResponseCode code = httpClient.addProduct(newProduct);

            switch (code){
                case PRODUCT_ADDED -> System.out.println("Продукт " + name +  " был добавлен в магазин");
                case PRODUCT_NOT_ADDED -> System.out.println("Не удалось добавить продукт "+ name + " в магазин. Повторите попытку");
                default -> System.out.println("Ошибка при отправке на сервер. Повторите попытку");
            }
        } catch (NumberFormatException e) {
            System.out.println("Некорректно заданы аргументы");
        }
    }

    private void put(String name, String n) {
        try {
            int count = Integer.parseInt(n);
            int price = checkShop(name, count);
            if (price != -1) {
                boolean inBasket = checkBasket(name);
                if (inBasket) {
                    for (Product product : basket) {
                        if (product.getProductName().equals(name)) {
                            product.setCount(product.getCount() + count);
                            System.out.println("Количество товара " + name + " увеличено. Теперь в корзине "
                                    + product.getCount());
                            break;
                        }
                    }
                } else {
                    Product newProduct = new Product(clientName, name, count, price);
                    basket.add(newProduct);
                    System.out.println("В корзину успешно добавлен товар - " + name);
                }
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


    private void buy(){
        int sum = 0;
        for (Product product: basket){
            sum += product.getPrice()*product.getCount();
        }
        ResponseCode code = httpClient.buyProduct(basket);
        switch (code){
            case PURCHASE_COMPLETE -> {
                System.out.println("Покупка на сумму " + sum + " прошла успешно");
                basket.clear();
            }
            case PURCHASE_NOT_COMPLETE -> System.out.println("Покупка не удалась");
            default -> System.out.println("Ошибка при покупке на сервере. Повторите попытку");
        }
    }

    private void disconnect(){
        ResponseCode code = httpClient.disconnect(clientName);
        switch (code){
            case USER_DISCONNECTED -> {
                System.out.println("Успешное удаление с сервера");
                System.out.println("Вы покидаете магазин. До свидания!");
                System.exit(-1);
            }
            case USER_NOT_FOUND -> System.out.println("Пользователь не найден на сервере");
            default -> System.out.println("Ошибка при отключении. Повторите попытку");
        }
    }

    private void quit(){
        System.out.println("Вы покидаете магазин. До свидания!");
        System.exit(-1);
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

    private int checkShop(String name, int n){
        boolean flag_p = false;
        boolean flag_n = false;
        int currentCount = -1;
        int price = -1;
        shopList = httpClient.getList();
        for(Product product: shopList){
            if (product.getProductName().equals(name)) {
                flag_p = true;
                currentCount = product.getCount();
                if (product.getCount() >= n) {
                    flag_n = true;
                    price = product.getPrice();
                    break;
                }
            }
        }
        if(!flag_p) System.out.println("Продукт " + name + " не найден");
        else {
            if (!flag_n) System.out.println("Продукт " + name + " имеется в количестве " + currentCount + ". Укажите меньшее число для покупки");
        }
        return price;
    }

}
