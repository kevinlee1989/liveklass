package com.example.creator.dto;

import com.example.creator.Creator;

public record CreatorResponse (
    String id,
    String name
){
    public static CreatorResponse from(Creator creator){
        return new CreatorResponse(
            creator.getId(),
            creator.getName()
        );
    }
}
