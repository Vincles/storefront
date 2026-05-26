package br.com.dio.storefront.service.impl;

import br.com.dio.storefront.dto.ProductDetailDTO;
import br.com.dio.storefront.dto.ProductInfoDTO;
import br.com.dio.storefront.entity.ProductEntity;
import br.com.dio.storefront.mapper.IProductMapper;
import br.com.dio.storefront.repository.ProductRepository;
import br.com.dio.storefront.service.IProductService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ProductServiceImpl implements IProductService {

    private final ProductRepository repository;
    private final RestClient warehouseClient;
    private final IProductMapper mapper;

    @Override
    public ProductEntity save(final ProductEntity entity) {
        return repository.save(entity);
    }

    @Override
    public void changeActivated(final UUID id, final boolean active) {
        var entity = findById(id);
        entity.setActive(active);
        repository.save(entity);
    }

    @Override
    public List<ProductEntity> findAllActive() {
        return repository.findByActiveTrueOrderByNameAsc();
    }

    @Override
    public ProductInfoDTO findInfo(final UUID id) {
        var entity = findById(id);
        var price = requestCurrentAmount(id);
        return mapper.toDTO(entity, price);
    }

    @Override
    public void purchase(final UUID id) {
        purchaseWarehouse(id);
    }

    private ProductEntity findById(final UUID id){
        return repository.findById(id).orElseThrow();
    }

    @CircuitBreaker(name = "warehouseService", fallbackMethod = "fallbackRequestAmount")
    public BigDecimal requestCurrentAmount(final UUID id) {
        var dto = warehouseClient.get()
                .uri("/products/" + id)
                .retrieve()
                .body(ProductDetailDTO.class);
        return dto.price();
    }
    
    public BigDecimal fallbackRequestAmount(final UUID id, final Throwable throwable) {
        System.err.println("⚠️ Circuit Breaker aberto para o Warehouse! Motivo: " + throwable.getMessage());
        return BigDecimal.ZERO; 
    }

    private void purchaseWarehouse(final UUID id){
        var path = String.format("/products/%s/purchase", id);
        warehouseClient.post()
                .uri(path)
                .retrieve()
                .toBodilessEntity();
    }

}
