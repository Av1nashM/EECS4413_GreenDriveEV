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

/**
 * REST API for the shopping cart.
 *
 * Every endpoint takes the owning user's id as the ?userId= query parameter
 * (there are no tokens yet), and only touches that user's cart.
 *
 *   GET    /api/cart?userId=1              view the cart with totals
 *   POST   /api/cart/items?userId=1        PB-15 add a vehicle
 *   PUT    /api/cart/items/{id}?userId=1   PB-16 change a line's quantity
 *   DELETE /api/cart/items/{id}?userId=1   PB-17 remove a line
 */
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

    /** Loads the user's cart, or responds 404 if the user or cart does not exist. */
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

    /** Flat view of a cart line; avoids serializing lazy JPA entities (and the user's password hash). */
    // One line of the cart as sent to the frontend (vehicle details + quantity + line total).
    public record CartItemResponse(
            Long itemId,
            Long vehicleId,
            String make,
            String model,
            int year,
            String bodyStyle,
            String imageUrl,
            double price,
            int quantity,
            double lineTotal) {}

    // Whole cart: its lines, total number of vehicles (sum of quantities), and the price subtotal.
    public record CartResponse(
            List<CartItemResponse> items,
            int itemCount,
            double subtotal) {}

    /** Converts a CartItem entity into the flat response record. */
    private CartItemResponse toResponse(CartItem item) {
        Vehicle v = item.getVehicle();
        return new CartItemResponse(
            item.getId(), v.getId(), v.getMake(), v.getModel(), v.getYear(),
            v.getBodyStyle(), v.getImageUrl(), v.getPrice(),
            item.getQuantity(), v.getPrice() * item.getQuantity());
    }

    /** Returns the user's cart. Runs in a transaction so the lazy vehicle relation can be read. */
    @GetMapping
    @Transactional(readOnly = true)
    public CartResponse getCart(@RequestParam Long userId) {
        Cart cart = findCart(userId);
        List<CartItemResponse> items = cartItemRepository
            .findByCartId(cart.getId()).stream()
            .map(this::toResponse)
            // Stable order so lines don't jump around after an edit.
            .sorted(java.util.Comparator.comparing(CartItemResponse::itemId))
            .toList();

        int itemCount = items.stream().mapToInt(CartItemResponse::quantity).sum();
        double subtotal = items.stream().mapToDouble(CartItemResponse::lineTotal).sum();
        return new CartResponse(items, itemCount, subtotal);
    }

    // Upper limit for one vehicle's quantity in a cart.
    private static final int MAX_QUANTITY = 99;

    /**
     * PB-15: add a vehicle to the cart. If the vehicle is already in the cart,
     * the requested quantity is added to the existing line instead of creating a duplicate
     * (the cart_items table has a unique (cart, vehicle) constraint).
     */
    @PostMapping("/items")
    @Transactional
    public ResponseEntity<?> addItem(
            @RequestParam Long userId,
            @RequestBody Map<String, Integer> body) {

        Integer vehicleId = body.get("vehicleId");
        Integer quantity = body.get("quantity");

        // Reject missing or out-of-range input before touching the database.
        if (vehicleId == null || quantity == null
                || quantity < 1 || quantity > MAX_QUANTITY) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "vehicleId and a quantity between 1 and "
                    + MAX_QUANTITY + " are required."));
        }

        Cart cart = findCart(userId);

        Vehicle vehicle = vehicleRepository.findById(vehicleId.longValue())
            .orElseThrow(() ->
                new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Vehicle not found"));

        // Reuse the existing line for this vehicle, or start a new one at quantity 0.
        CartItem item = cartItemRepository
            .findByCartIdAndVehicleId(cart.getId(), vehicle.getId())
            .orElseGet(() -> {
                CartItem newItem = new CartItem();
                newItem.setCart(cart);
                newItem.setVehicle(vehicle);
                newItem.setQuantity(0);
                return newItem;
            });

        int newQuantity = item.getQuantity() + quantity;
        if (newQuantity > MAX_QUANTITY) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "A cart can hold at most " + MAX_QUANTITY
                    + " of the same vehicle."));
        }

        item.setQuantity(newQuantity);
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

    /** PB-16: set a cart line to an exact quantity (not a delta). */
    @PutMapping("/items/{itemId}")
    @Transactional
    public ResponseEntity<?> updateItem(
            @RequestParam Long userId,
            @PathVariable Long itemId,
            @RequestBody Map<String, Integer> body) {

        Integer quantity = body.get("quantity");

        if (quantity == null || quantity < 1 || quantity > MAX_QUANTITY) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "Quantity must be between 1 and " + MAX_QUANTITY + "."));
        }

        Cart cart = findCart(userId);

        CartItem item = cartItemRepository.findById(itemId)
            .orElseThrow(() ->
                new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Cart item not found"));

        // The item must belong to this user's cart. Answer 404 (not 403) so we
        // don't reveal that the id exists in someone else's cart.
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

    /** PB-17: remove a line from the cart entirely. */
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

        // Same ownership check as updateItem.
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
