package com.example.gccoffee.repository;

import com.example.gccoffee.model.Order;
import com.example.gccoffee.model.OrderItem;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Repository
public class OrderJdbcRepository implements OrderRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public OrderJdbcRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public Order insert(Order order) {
        jdbcTemplate.update("INSERT INTO orders(order_id, email, address, postcode, order_status, created_at, updated_at) " +
                "Values(UNHEX(REPLACE( :order_id, '-', '')), :email, :address, :postcode, :order_status, :created_at, :updated_at)", toOrderParamMap(order));
        order.getOrderItems().forEach(items ->
                jdbcTemplate.update("INSERT INTO order_items(order_id, product_id, category, price, quantity, created_at, updated_at)" +
                " Values(UNHEX(REPLACE( :order_id, '-', '')), UNHEX(REPLACE( :product_id, '-', '')), :category, :price, :quantity, :created_at, :updated_at)",
                toOrderItemParamMap(order.getOrderId(), order.getCreatedAt(), order.getUpdatedAt(), items)));
        return order;
    }

    private Map<String, Object> toOrderParamMap(Order order){
        var paramMap = new HashMap<String, Object>();
        paramMap.put("order_id", order.getOrderId().toString().getBytes());
        paramMap.put("email", order.getEmail().getAddress());
        paramMap.put("address", order.getAddress());
        paramMap.put("postcode", order.getPostcode());
        paramMap.put("order_status", order.getOrderStatus().toString());
        paramMap.put("created_at", order.getCreatedAt());
        paramMap.put("updated_at", order.getUpdatedAt());
        return paramMap;
    }

    private Map<String, Object> toOrderItemParamMap(UUID orderId, LocalDateTime createdAt, LocalDateTime updatedAt, OrderItem item){
        var paramMap = new HashMap<String, Object>();
        paramMap.put("order_id", orderId.toString().getBytes());
        paramMap.put("product_id", item.productId().toString().getBytes());
        paramMap.put("category", item.category().toString());
        paramMap.put("price", item.price());
        paramMap.put("quantity", item.quantity());
        paramMap.put("created_at", createdAt);
        paramMap.put("updated_at", updatedAt);
        return paramMap;
    }
}
