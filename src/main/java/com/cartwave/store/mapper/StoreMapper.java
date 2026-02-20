package com.cartwave.store.mapper;

import com.cartwave.store.dto.StoreDTO;
import com.cartwave.store.entity.Store;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface StoreMapper {

    StoreDTO toStoreDTO(Store store);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "bannerUrl", ignore = true)
    @Mapping(target = "businessAddress", ignore = true)
    @Mapping(target = "businessRegistrationNumber", ignore = true)
    @Mapping(target = "businessPhoneNumber", ignore = true)
    @Mapping(target = "businessEmail", ignore = true)
    Store toStore(StoreDTO storeDTO);

}
