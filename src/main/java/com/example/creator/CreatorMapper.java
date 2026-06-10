package com.example.creator;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CreatorMapper { 
    boolean existsById(String id);
    void insert(Creator creator);
    List<Creator> findAll();
    Optional<Creator> findById(String id);
    void deleteById(String id);
}