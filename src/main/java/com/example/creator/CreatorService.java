package com.example.creator;

import com.example.creator.dto.CreatorRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreatorService {

    private final CreatorRepository creatorRepository;

    @Transactional
    public String register(CreatorRequest request) {
        if (creatorRepository.existsById(request.id())) {
            throw new IllegalArgumentException("이미 존재하는 크리에이터 ID입니다: " + request.id());
        }

        Creator creator = Creator.of(request.id(), request.name());
        return creatorRepository.save(creator).getId();
    }
}
