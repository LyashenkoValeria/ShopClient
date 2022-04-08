import java.util.ArrayList;
import java.util.List;

public class Products {
    private List products;

    public Products(List pl) {
        this.products = pl;
    }

    public Products() {
        this.products = new ArrayList();
    }

    public List getProducts() {
        return this.products;
    }

    public void setProducts(List products) {
        this.products = products;
    }
}
