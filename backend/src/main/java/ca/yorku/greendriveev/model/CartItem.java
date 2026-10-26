
package ca.yorku.greendriveev.model;

import jakarta.persistence.*;

@Entity
@Table(
    name = "cart_items",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_cart_items_cart_vehicle",
            columnNames = {"cart_id", "vehicle_id"}
        )
    }
)
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
}
