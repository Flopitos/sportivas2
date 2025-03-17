package com.sportivas.sportivas.service;

import java.util.List;

import com.sportivas.sportivas.dto.InjuryDto;

public interface InjuryService {
    InjuryDto addInjury(InjuryDto injuryDto);
    List<InjuryDto> getUserInjuries();
    InjuryDto getInjuryById(Long id);
    InjuryDto updateInjury(Long id, InjuryDto injuryDto);
    void deleteInjury(Long id);
}