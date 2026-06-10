package com.example.settlement;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface SettlementMapper {
    void insert(Settlement settlement);
    void update(Settlement settlement);
    Optional<Settlement> findByCreatorIdAndMonth(@Param("creatorId") String creatorId, @Param("month") String month);
}
