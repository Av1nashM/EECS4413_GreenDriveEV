
package ca.yorku.greendriveev.model;

import jakarta.persistence.*;

@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "cart_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_cart_items_cart")
    )
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "vehicle_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_cart_items_vehicle")
    )
    private Vehicle vehicle;

    @Column(nullable = false)
    private int quantity = 1;

    // PB10: Vehicle customization information
    @Column(name = "customization_json", columnDefinition = "TEXT")
    private String customizationJson;

    @Column(name = "customization_cost", nullable = false)
    private double customizationCost = 0.0;

    @Column(name = "customization_key", nullable = false, length = 64)
    private String customizationKey = "DEFAULT";

    public CartItem() {}

    public Long getId() { 
        return id; 
    }
    
    public void setId(Long id) { 
        this.id = id; 
    }

    public Cart getCart() { 
        return cart; 
    }
    
    public void setCart(Cart cart) { 
        this.cart = cart; 
    }

    public Vehicle getVehicle() { 
        return vehicle; 
    }
    
    public void setVehicle(Vehicle vehicle) { 
        this.vehicle = vehicle; 
    }

    public int getQuantity() { 
        return quantity; 
    }
    
    public void setQuantity(int quantity) { 
        this.quantity = quantity; 
    }

    // PB10 getters and setters
    public String getCustomizationJson() {
        return customizationJson;
    }

    public void setCustomizationJson(String customizationJson) {
        this.customizationJson = customizationJson;
    }

    public double getCustomizationCost() {
        return customizationCost;
    }

    public void setCustomizationCost(double customizationCost) {
        this.customizationCost = customizationCost;
    }

    public String getCustomizationKey() {
        return customizationKey;
    }

    public void setCustomizationKey(String customizationKey) {
        this.customizationKey = customizationKey;
    }
}
