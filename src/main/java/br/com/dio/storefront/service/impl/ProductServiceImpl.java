package br.com.dio.storefront.service.impl;

import br.com.dio.storefront.dto.ProductDetailDTO;
import br.com.dio.storefront.dto.ProductInfoDTO;
import br.com.dio.storefront.entity.ProductEntity;
import br.com.dio.storefront.mapper.IProductMapper;
import br.com.dio.storefront.repository.ProductRepository;
import br.com.dio.storefront.service.IProductService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

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
    
    private BigDecimal requestCurrentAmount(final UUID id) {
        try {
            var dto = warehouseClient.get()
                    .uri("/products/" + id)
                    .retrieve()
                    .body(ProductDetailDTO.class);
            
            return dto != null ? dto.price() : BigDecimal.ZERO;
            
        } catch (RestClientException e) {
            System.err.println("⚠️ Falha ao conectar com o Warehouse para buscar o preço. Motivo: " + e.getMessage());
            
            return BigDecimal.ZERO; 
        }
    }

    private void purchaseWarehouse(final UUID id){
        try {
            var path = String.format("/products/%s/purchase", id);
            warehouseClient.post()
                    .uri(path)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            System.err.println("⚠️ Não foi possível registrar a compra no Warehouse: " + e.getMessage());

        }
    }
}
