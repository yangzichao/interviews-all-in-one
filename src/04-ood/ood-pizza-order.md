
这个题就不要太过于较真，因为真实的情况，同样的 pizza 点十份是很复杂的。
同理也不要增加双拼披萨的选项。


```java
// "static void main" must be defined in a public class.

class PizzaOrderManager {
// CRUD PizzaOrder, trival;
//     private AtomicInteger counter;
//     private Map<Integer, PizzaOrder>
//     public PizzaOrderManager() {
//         counter = new AtomicInteger(0);
//     }
    
//     public createOrder() {
        
//     }
    // Trivial operations, we test in Main directly.
}

class PizzaOrder {
    // state
    // total price
    // add remove Pizza
    // 
    enum OrderState {
        CREATE, PAID, MADE, DELIVERED;
    }
    
    private int orderId;
    Map<Integer, Pizza> items; // key: hashcode of pizza instances
    double totalPrice;
    OrderState state;
    /**
    * Ignore customer (address, phone), payment info for now.
    */
    public PizzaOrder(int orderId) {
        this.orderId = orderId;
        items = new HashMap<>();
        totalPrice = 0;
        state = OrderState.CREATE;
    }
    
    public boolean addPizza(Pizza pizza) {
        if (items.containsKey(pizza.hashCode())) return false;
        items.put(pizza.hashCode(), pizza);
        totalPrice += pizza.getPrice();
        return true;
    }
    
    public Pizza removePizza(Integer hashCode) {
        if (!items.containsKey(hashCode)) return null;
        Pizza pizza = items.remove(hashCode);
        totalPrice -= pizza.getPrice();
        return pizza;
    }
    
    public void printOrder() {
        for (Integer key : items.keySet()) {
            Pizza pizza = items.get(key);
            pizza.printPizza();
        }
    }
}


enum PizzaSize {
    SMALL(6, 5.5), MEDIUM(8, 7.2), LARGE(10, 8.43);
    private double inches;
    private double basePrice;
    PizzaSize(double inches, double basePrice) {
        this.inches = inches;
        this.basePrice = basePrice;
    }

    public double getBasePrice() {
        return basePrice;
    }
}

enum Topping {
    CHEESE("Cheese", 0.5),
    SAUSAGE("Sausage", 0.6);
    
    private String name;
    private double unitPrice;
    
    Topping(String name, double unitPrice) {
        this.name = name;
        this.unitPrice = unitPrice;
    }
    public String getName(){return name;}
    public double getUnitPrice(){return unitPrice;}
}


class Pizza {
    
    private PizzaSize size;
    private Map<Topping, Integer> toppings;
    private double price;
    
    public Pizza(PizzaSize size) {
        this.size = size;
        this.toppings = new TreeMap<>();
        this.price = size.getBasePrice();
    }

    public Pizza addTopping(Topping topping) {
        toppings.put(topping, toppings.getOrDefault(topping, 0) + 1);
        price += topping.getUnitPrice();
        return this;
    }
    
    public Pizza removeTopping(Topping topping) {
        if (toppings.get(topping) == null || toppings.get(topping) == 0) return this;
        toppings.put(topping, toppings.getOrDefault(topping, 0) - 1);
        price -= topping.getUnitPrice();
        if (toppings.get(topping) == 0) {
            toppings.remove(topping);
        }
        return this;
    }
    
    public double getPrice() {
        return price;
    }
    
    public void printPizza() {
        System.out.println(this.hashCode() + " " +size + " " + String.valueOf(getPrice()));
    }
}

// Ignore for now, PizzaStore, Customer, Inventory of each item, Payment.


enum PreMadePizzaOption {
    CHEESE_PIZZA_LARGE, SUPER_PIZZA;
}

class SimplePizzaFactory {
    public static Pizza getPizza(PreMadePizzaOption option) {
        switch (option) {
            case CHEESE_PIZZA_LARGE: 
                return new Pizza(PizzaSize.LARGE).addTopping(Topping.CHEESE);
            case SUPER_PIZZA:
                return new Pizza(PizzaSize.LARGE).addTopping(Topping.CHEESE).addTopping(Topping.CHEESE).addTopping(Topping.CHEESE).addTopping(Topping.CHEESE);
            default:
                return null;
        }
    }
}

public class Main {
    public static void main(String[] args) {
        Pizza pizza1 = new Pizza(PizzaSize.SMALL);
        pizza1.printPizza();
        pizza1.addTopping(Topping.CHEESE);
        pizza1.printPizza();
        pizza1.addTopping(Topping.SAUSAGE).addTopping(Topping.SAUSAGE).removeTopping(Topping.CHEESE);
        pizza1.printPizza();
        
        Pizza pizza2 = new Pizza(PizzaSize.MEDIUM).addTopping(Topping.CHEESE);
        PizzaOrder order1 = new PizzaOrder(4723);
        order1.addPizza(pizza1);
        order1.addPizza(pizza2);
        order1.removePizza(pizza1.hashCode());
        order1.printOrder();
        
        Pizza superPizza = SimplePizzaFactory.getPizza(PreMadePizzaOption.CHEESE_PIZZA_LARGE);
        order1.addPizza(superPizza);
        order1.printOrder();
    }
}
```