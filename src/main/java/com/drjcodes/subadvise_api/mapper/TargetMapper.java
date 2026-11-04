package com.drjcodes.subadvise_api.mapper;

import org.springframework.stereotype.Component;

import com.drjcodes.subadvise_api.dto.TargetRequestDto;
import com.drjcodes.subadvise_api.dto.TargetResponseDto;
import com.drjcodes.subadvise_api.model.Target;

@Component
public class TargetMapper {

    public Target toEntity(TargetRequestDto dto) {
        if (dto == null) {
            return null;
        }

        Target target = new Target();
        target.setTargetExpense(dto.getTargetExpense());
        target.setCurrency(dto.getCurrency());
        target.setStartDate(dto.getStartDate());
        target.setEndDate(dto.getEndDate());
        return target;
    }

    public TargetResponseDto toResponseDto(Target target) {
        if (target == null) {
            return null;
        }

        TargetResponseDto dto = new TargetResponseDto();
        dto.setTargetId(target.getTargetId());
        dto.setTargetExpense(target.getTargetExpense());
        dto.setCurrency(target.getCurrency());
        dto.setStartDate(target.getStartDate());
        dto.setEndDate(target.getEndDate());
        return dto;
    }
}
