package com.example.uniqueAproovaResidency.module.flat.dto;

import com.example.uniqueAproovaResidency.module.flat.entity.Flat;
import com.example.uniqueAproovaResidency.module.user.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlatDto {
    private String id;
    private String flatNumber;
    private Integer floor;
    private String status;
    private List<UserDto> members;

    public static FlatDto fromEntity(Flat flat, List<UserDto> members) {
        return FlatDto.builder()
                .id(flat.getId())
                .flatNumber(flat.getFlatNumber())
                .floor(flat.getFloor())
                .status(flat.getStatus())
                .members(members)
                .build();
    }
}
