package com.cartwave.product.mapper;

import com.cartwave.product.dto.ProductDTO;
import com.cartwave.product.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface ProductMapper {

    ProductDTO toProductDTO(Product product);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "storeId", ignore = true)
    @Mapping(target = "lowStockThreshold", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "attributes", ignore = true)
    Product toProduct(ProductDTO productDTO);

}
