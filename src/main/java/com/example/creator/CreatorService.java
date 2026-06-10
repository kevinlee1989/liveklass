package com.example.creator;

import com.example.creator.dto.CreatorRequest;
import com.example.creator.dto.CreatorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CreatorService {

    private final CreatorMapper creatorMapper;

    @Transactional
    public String register(CreatorRequest request) {
        if (creatorMapper.existsById(request.id())) {
            throw new IllegalArgumentException("이미 존재하는 크리에이터 ID입니다: " + request.id());
        }

        Creator creator = Creator.of(request.id(), request.name());
        creatorMapper.insert(creator);

        return creator.getId();
    }

    @Transactional(readOnly = true)
    public List<CreatorResponse> findAll(){
        return creatorMapper.findAll().stream().map(CreatorResponse::from).toList();
    }

    @Transactional
    public void delete(String creatorId){
        if(!creatorMapper.existsById(creatorId)) {
            throw new IllegalArgumentException("존재하지않는 강사입니다 " + creatorId);
        }

        creatorMapper.deleteById(creatorId);
    }
}
