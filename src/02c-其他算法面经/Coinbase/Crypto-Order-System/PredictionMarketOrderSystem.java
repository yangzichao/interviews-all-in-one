import java.util.*;

/**
 * Coinbase-style interview practice -- Prediction Market Order System.
 *
 * This is intentionally close to CryptoOrderSystem Part 1-3, but the schema is
 * prediction-market flavored:
 *
 *   - symbol becomes marketId, for example "will-btc-close-above-100k-2026".
 *   - outcome is YES / NO. For the first interview pass, we can assume only YES
 *     orders are used, but keeping the field makes the model ready for follow-ups.
 *   - ordersById is the single source of truth.
 *   - ordersByUserId is only a secondary index from userId to orderId references.
 *
 * Lifecycle used in this simplified version:
 *
 *   New order -> ACTIVE
 *   ACTIVE    -> CANCELLED
 *   CANCELLED -> ACTIVE       (reactivate / replace later if product allows it)
 *   ACTIVE    -> FULFILLED
 *   FULFILLED is terminal.
 *
 * Missing order behavior:
 *   getOrder returns null.
 *   state-transform methods silently return if the orderId does not exist.
 */
public class PredictionMarketOrderSystem {

    public enum Outcome { YES, NO }

    public enum OrderState { ACTIVE, CANCELLED, FULFILLED }

    public static record Order(
            String orderId,
            String userId,
            String marketId,
            Outcome outcome,
            long quantity,
            OrderState state
    ) {}

    // ====================================================================
    // PART 1 -- Basic Place / Get
    // ====================================================================
    // Requirement:
    //   placeOrder(...) inserts a new ACTIVE order.
    //   duplicate orderId throws IllegalArgumentException.
    //   getOrder(missing) returns null.

    public static class MarketPart1 {
        private final Map<String, Order> ordersById;

        public MarketPart1() {
            this.ordersById = new HashMap<>();
        }

        public void placeOrder(String orderId, String userId, String marketId,
                               Outcome outcome, long quantity) {
            if (ordersById.containsKey(orderId)) {
                throw new IllegalArgumentException("duplicate orderId: " + orderId);
            }
            ordersById.put(orderId, new Order(
                    orderId, userId, marketId, outcome, quantity, OrderState.ACTIVE));
        }

        public Order getOrder(String orderId) {
            return ordersById.get(orderId);
        }
    }

    // ====================================================================
    // PART 2 -- State Machine
    // ====================================================================
    // Requirement:
    //   cancelOrder:   ACTIVE/CANCELLED -> CANCELLED
    //   activateOrder: CANCELLED -> ACTIVE
    //   fulfillOrder:  ACTIVE -> FULFILLED
    //
    // Why is cancelOrder allowed from CANCELLED?
    //   It makes cancel idempotent. Calling cancel twice is not a new state
    //   transition, but it also should not break a client retry.

    public static class MarketPart2 {
        private final Map<String, Order> ordersById;

        public MarketPart2() {
            this.ordersById = new HashMap<>();
        }

        public void placeOrder(String orderId, String userId, String marketId,
                               Outcome outcome, long quantity) {
            if (ordersById.containsKey(orderId)) {
                throw new IllegalArgumentException("duplicate orderId: " + orderId);
            }
            ordersById.put(orderId, new Order(
                    orderId, userId, marketId, outcome, quantity, OrderState.ACTIVE));
        }

        public Order getOrder(String orderId) {
            return ordersById.get(orderId);
        }

        public void cancelOrder(String orderId) {
            transformOrder(orderId, OrderState.CANCELLED,
                    OrderState.ACTIVE, OrderState.CANCELLED);
        }

        public void activateOrder(String orderId) {
            transformOrder(orderId, OrderState.ACTIVE, OrderState.CANCELLED);
        }

        public void fulfillOrder(String orderId) {
            transformOrder(orderId, OrderState.FULFILLED, OrderState.ACTIVE);
        }

        private void transformOrder(String orderId, OrderState nextState,
                                    OrderState... allowedCurrentStates) {
            Order currentOrder = ordersById.get(orderId);
            if (currentOrder == null) {
                return;
            }
            if (!isAllowed(currentOrder.state(), allowedCurrentStates)) {
                throw new IllegalStateException(
                        "cannot move " + orderId + " from " + currentOrder.state()
                                + " to " + nextState);
            }
            ordersById.put(orderId, withState(currentOrder, nextState));
        }

        private boolean isAllowed(OrderState currentState, OrderState[] allowedStates) {
            for (OrderState allowedState : allowedStates) {
                if (allowedState == currentState) {
                    return true;
                }
            }
            return false;
        }

        private Order withState(Order order, OrderState state) {
            return new Order(order.orderId(), order.userId(), order.marketId(),
                    order.outcome(), order.quantity(), state);
        }
    }

    // ====================================================================
    // PART 3 -- Cancel All Orders for User
    // ====================================================================
    // Requirement:
    //   userId has high cardinality and cancel-all is user-scoped, so keep a
    //   secondary index: userId -> orderIds.
    //
    // Important invariant:
    //   ordersById is still the source of truth. The user index only stores ids.
    //   Before cancelling, always read the real order from ordersById.

    public static class MarketPart3 {
        private final Map<String, Order> ordersById;
        private final Map<String, Set<String>> orderIdsByUserId;

        public MarketPart3() {
            this.ordersById = new HashMap<>();
            this.orderIdsByUserId = new HashMap<>();
        }

        public void placeOrder(String orderId, String userId, String marketId,
                               Outcome outcome, long quantity) {
            if (ordersById.containsKey(orderId)) {
                throw new IllegalArgumentException("duplicate orderId: " + orderId);
            }

            Order order = new Order(
                    orderId, userId, marketId, outcome, quantity, OrderState.ACTIVE);
            ordersById.put(orderId, order);
            orderIdsByUserId.computeIfAbsent(userId, ignored -> new HashSet<>()).add(orderId);
        }

        public Order getOrder(String orderId) {
            return ordersById.get(orderId);
        }

        public void cancelOrder(String orderId) {
            transformOrder(orderId, OrderState.CANCELLED,
                    OrderState.ACTIVE, OrderState.CANCELLED);
        }

        public void activateOrder(String orderId) {
            transformOrder(orderId, OrderState.ACTIVE, OrderState.CANCELLED);
        }

        public void fulfillOrder(String orderId) {
            transformOrder(orderId, OrderState.FULFILLED, OrderState.ACTIVE);
        }

        public int cancelAllOrdersForUser(String userId) {
            int cancelledCount = 0;
            for (String orderId : orderIdsByUserId.getOrDefault(userId, Collections.emptySet())) {
                Order order = ordersById.get(orderId);
                if (order != null && order.state() == OrderState.ACTIVE) {
                    cancelOrder(orderId);
                    cancelledCount++;
                }
            }
            return cancelledCount;
        }

        private void transformOrder(String orderId, OrderState nextState,
                                    OrderState... allowedCurrentStates) {
            Order currentOrder = ordersById.get(orderId);
            if (currentOrder == null) {
                return;
            }
            if (!isAllowed(currentOrder.state(), allowedCurrentStates)) {
                throw new IllegalStateException(
                        "cannot move " + orderId + " from " + currentOrder.state()
                                + " to " + nextState);
            }
            ordersById.put(orderId, withState(currentOrder, nextState));
        }

        private boolean isAllowed(OrderState currentState, OrderState[] allowedStates) {
            for (OrderState allowedState : allowedStates) {
                if (allowedState == currentState) {
                    return true;
                }
            }
            return false;
        }

        private Order withState(Order order, OrderState state) {
            return new Order(order.orderId(), order.userId(), order.marketId(),
                    order.outcome(), order.quantity(), state);
        }
    }
}
