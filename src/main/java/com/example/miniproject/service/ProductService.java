package com.example.miniproject.service;

import com.example.miniproject.dto.request.ProductDTO;
import com.example.miniproject.dto.response.ProductResponse;
import com.example.miniproject.entity.Product;
import com.example.miniproject.exception.ResourceNotFoundException;
import com.example.miniproject.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * Lấy danh sách sản phẩm – Cached trên Redis (NFR-02)
     * Latency < 50ms khi cache hit, giảm 95% áp lực MySQL
     */
    @Cacheable(value = "products", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findByDeletedFalse(pageable).map(this::toResponse);
    }

    /**
     * Tìm kiếm sản phẩm theo từ khóa – Cached (NFR-02)
     */
    @Cacheable(value = "products", key = "'search-' + #keyword + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<ProductResponse> searchProducts(String keyword, Pageable pageable) {
        return productRepository.searchByName(keyword, pageable).map(this::toResponse);
    }

    /**
     * Xem chi tiết sản phẩm – Cached theo ID (NFR-02)
     */
    @Cacheable(value = "product", key = "#id")
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", id));
        return toResponse(product);
    }

    /**
     * Thêm sản phẩm mới – Xóa cache để đảm bảo data nhất quán (NFR-02)
     */
    @Transactional
    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public ProductResponse createProduct(ProductDTO dto) {
        Product product = new Product();
        mapDtoToEntity(dto, product);
        return toResponse(productRepository.save(product));
    }

    /**
     * Cập nhật sản phẩm – Xóa cache (NFR-02)
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "product", key = "#id"),
            @CacheEvict(value = "products", allEntries = true)
    })
    public ProductResponse updateProduct(Long id, ProductDTO dto) {
        Product product = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", id));
        mapDtoToEntity(dto, product);
        return toResponse(productRepository.save(product));
    }

    /**
     * Xóa mềm sản phẩm (Soft Delete) – Xóa cache (NFR-02)
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "product", key = "#id"),
            @CacheEvict(value = "products", allEntries = true)
    })
    public void deleteProduct(Long id) {
        Product product = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", id));
        product.setDeleted(true); // Soft delete – không xóa khỏi DB
        productRepository.save(product);
    }

    private void mapDtoToEntity(ProductDTO dto, Product product) {
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStockQuantity(dto.getStockQuantity());
    }

    public ProductResponse toResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setStockQuantity(product.getStockQuantity());
        return response;
    }
}
