
package ca.yorku.greendriveev.controller;

import ca.yorku.greendriveev.model.Cart;
import ca.yorku.greendriveev.model.CartItem;
import ca.yorku.greendriveev.model.Vehicle;
import ca.yorku.greendriveev.repository.CartItemRepository;
import ca.yorku.greendriveev.repository.CartRepository;
import ca.yorku.greendriveev.repository.UserRepository;
import ca.yorku.greendriveev.repository.VehicleRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;

    public CartController(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            UserRepository userRepository,
            VehicleRepository vehicleRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
    }

    private Cart findCart(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new org.springframework.web.server.ResponseStatusException(
                HttpStatus.NOT_FOUND, "User not found");
        }

        return cartRepository.findByUserId(userId)
            .orElseThrow(() ->
                new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Cart not found"));
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<CartItem> getCart(@RequestParam Long userId) {
        Cart cart = findCart(userId);
        return cartItemRepository.findByCartId(cart.getId());
    }

    @PostMapping("/items")
    @Transactional
    public ResponseEntity<?> addItem(
            @RequestParam Long userId,
            @RequestBody Map<String, Integer> body) {

        Integer vehicleId = body.get("vehicleId");
        Integer quantity = body.get("quantity");

        if (vehicleId == null || quantity == null || quantity < 1) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "vehicleId and a positive quantity are required."));
        }

        Cart cart = findCart(userId);

        Vehicle vehicle = vehicleRepository.findById(vehicleId.longValue())
            .orElseThrow(() ->
                new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Vehicle not found"));

        CartItem item = cartItemRepository
            .findByCartIdAndVehicleId(cart.getId(), vehicle.getId())
            .orElseGet(() -> {
                CartItem newItem = new CartItem();
                newItem.setCart(cart);
                newItem.setVehicle(vehicle);
                newItem.setQuantity(0);
                return newItem;
            });

        item.setQuantity(item.getQuantity() + quantity);
        CartItem savedItem = cartItemRepository.save(item);

        return ResponseEntity.status(HttpStatus.CREATED).body(
            Map.of(
                "message", "Vehicle added to cart.",
                "itemId", savedItem.getId(),
                "vehicleId", vehicle.getId(),
                "quantity", savedItem.getQuantity()
            )
        );
    }

    @PutMapping("/items/{itemId}")
    @Transactional
    public ResponseEntity<?> updateItem(
            @RequestParam Long userId,
            @PathVariable Long itemId,
            @RequestBody Map<String, Integer> body) {

        Integer quantity = body.get("quantity");

        if (quantity == null || quantity < 1) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "Quantity must be at least 1."));
        }

        Cart cart = findCart(userId);

        CartItem item = cartItemRepository.findById(itemId)
            .orElseThrow(() ->
                new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Cart item not found"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new org.springframework.web.server.ResponseStatusException(
                HttpStatus.NOT_FOUND, "Cart item not found");
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);

        return ResponseEntity.ok(Map.of(
            "message", "Cart quantity updated.",
            "itemId", item.getId(),
            "quantity", item.getQuantity()
        ));
    }

    @DeleteMapping("/items/{itemId}")
    @Transactional
    public ResponseEntity<?> removeItem(
            @RequestParam Long userId,
            @PathVariable Long itemId) {

        Cart cart = findCart(userId);

        CartItem item = cartItemRepository.findById(itemId)
            .orElseThrow(() ->
                new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Cart item not found"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new org.springframework.web.server.ResponseStatusException(
                HttpStatus.NOT_FOUND, "Cart item not found");
        }

        cartItemRepository.delete(item);

        return ResponseEntity.ok(Map.of(
            "message", "Vehicle removed from cart."
        ));
    }
}
