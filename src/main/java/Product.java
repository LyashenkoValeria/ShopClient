import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private String username;
    private String productName;
    private int count;
    private Integer price;

    public void checkoutString(){
        System.out.println(productName + " - " + count);
    }

    public void productToString(){
        System.out.println(productName + " - " + count + " шт. - " + price + " руб.");
    }
}
