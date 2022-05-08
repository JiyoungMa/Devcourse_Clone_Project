package com.example.gccoffee.repository;

import com.example.gccoffee.model.Category;
import com.example.gccoffee.model.Product;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class ProductJdbcRepository implements ProductRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ProductJdbcRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Product> findAll() {
        return jdbcTemplate.query("select * from products", productRowMapper);
    }

    @Override
    public Product insert(Product product) {
        var update =  jdbcTemplate.update("INSERT INTO PRODUCTS(product_id,product_name,category,price,description,created_at,updated_at)" +
                " VALUES(UNHEX(REPLACE( :product_id, '-', '')), :product_name, :category,:price,:description,:created_at,:updated_at)",toParamMap(product));
        if (update != 1)
            throw new RuntimeException("Nothing was inserted");
        return product;
    }

    @Override
    public Product update(Product product) {
        var update =  jdbcTemplate.update(
                "Update products set product_name = :product_name, category = :category, price = :price, description = :description, created_at = :created_at, updated_at = :updated_at " +
                        "where product_id = UNHEX(REPLACE( :product_id, '-', ''))",
                toParamMap(product)
        );
        if (update != 1){
            throw new RuntimeException("Nothing to update");
        }
        return product;
    }

    @Override
    public Optional<Product> findById(UUID productId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("Select * from products where product_id = UNHEX(REPLACE(:product_id, '-', ''))",
                    Collections.singletonMap("product_id", productId.toString().getBytes()), productRowMapper));
        }catch(EmptyResultDataAccessException e){
            return Optional.empty();
        }
    }

    @Override
    public Optional<Product> findByName(String productName) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("Select * from products where product_name = :product_name",
                    Collections.singletonMap("product_name", productName), productRowMapper));
        }catch(EmptyResultDataAccessException e){
            return Optional.empty();
        }
    }

    @Override
    public List<Product> findByCategory(Category category) {
        return jdbcTemplate.query("SELECT * FROM products where category = :category",
                Collections.singletonMap("category",category.toString())
                ,productRowMapper);
    }

    @Override
    public void deleteAll() {
        jdbcTemplate.update("Delete from products", Collections.emptyMap());
    }



    private static final RowMapper<Product> productRowMapper = (resultSet, i) -> {
        var productId = JdbcUtils.toUUID(resultSet.getBytes("product_id"));
        var productName = resultSet.getString("product_name");
        var category = Category.valueOf(resultSet.getString("category"));
        var price = resultSet.getLong("price");
        var description = resultSet.getString("description");
        var createdAt = JdbcUtils.toLocalDateTime(resultSet.getTimestamp("created_at"));
        var updatedAt = JdbcUtils.toLocalDateTime(resultSet.getTimestamp("updated_at"));

        return new Product(productId, createdAt,productName,category,price,description,updatedAt);
    };

    private Map<String, Object> toParamMap(Product product){
        var paramMap = new HashMap<String, Object>();
        paramMap.put("product_id", product.getProductId().toString().getBytes());
        paramMap.put("product_name", product.getProductName());
        paramMap.put("category", product.getCategory().toString());
        paramMap.put("price", product.getPrice());
        paramMap.put("description", product.getDescription());
        paramMap.put("created_at", product.getCreatedAt());
        paramMap.put("updated_at", product.getUpdatedAt());
        return paramMap;
    }
}
