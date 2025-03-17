package com.sportivas.sportivas.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sportivas.sportivas.dto.InjuryDto;
import com.sportivas.sportivas.exception.ResourceNotFoundException;
import com.sportivas.sportivas.model.Injury;
import com.sportivas.sportivas.model.User;
import com.sportivas.sportivas.repository.InjuryRepository;
import com.sportivas.sportivas.service.InjuryService;
import com.sportivas.sportivas.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InjuryServiceImpl implements InjuryService {

    private final InjuryRepository injuryRepository;
    private final UserService userService;

    @Override
    public InjuryDto addInjury(InjuryDto injuryDto) {
        User currentUser = userService.getCurrentUser();
        
        Injury injury = Injury.builder()
                .user(currentUser)
                .bodyPart(injuryDto.getBodyPart())
                .painType(injuryDto.getPainType())
                .injuryDate(injuryDto.getInjuryDate())
                .recoveryDate(injuryDto.getRecoveryDate())
                .description(injuryDto.getDescription())
                .build();
        
        Injury savedInjury = injuryRepository.save(injury);
        return mapToDto(savedInjury);
    }

    @Override
    public List<InjuryDto> getUserInjuries() {
        User currentUser = userService.getCurrentUser();
        return injuryRepository.findByUserOrderByInjuryDateDesc(currentUser).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public InjuryDto getInjuryById(Long id) {
        User currentUser = userService.getCurrentUser();
        Injury injury = injuryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Injury", "id", id));
        
        // Security check - ensure the injury belongs to the current user
        if (!injury.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Injury", "id", id);
        }
        
        return mapToDto(injury);
    }

    @Override
    public InjuryDto updateInjury(Long id, InjuryDto injuryDto) {
        User currentUser = userService.getCurrentUser();
        Injury injury = injuryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Injury", "id", id));
        
        // Security check - ensure the injury belongs to the current user
        if (!injury.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Injury", "id", id);
        }
        
        injury.setBodyPart(injuryDto.getBodyPart());
        injury.setPainType(injuryDto.getPainType());
        injury.setInjuryDate(injuryDto.getInjuryDate());
        injury.setRecoveryDate(injuryDto.getRecoveryDate());
        injury.setDescription(injuryDto.getDescription());
        
        Injury updatedInjury = injuryRepository.save(injury);
        return mapToDto(updatedInjury);
    }

    @Override
    public void deleteInjury(Long id) {
        User currentUser = userService.getCurrentUser();
        Injury injury = injuryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Injury", "id", id));
        
        // Security check - ensure the injury belongs to the current user
        if (!injury.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Injury", "id", id);
        }
        
        injuryRepository.delete(injury);
    }
    
    private InjuryDto mapToDto(Injury injury) {
        return InjuryDto.builder()
                .id(injury.getId())
                .bodyPart(injury.getBodyPart())
                .painType(injury.getPainType())
                .injuryDate(injury.getInjuryDate())
                .recoveryDate(injury.getRecoveryDate())
                .description(injury.getDescription())
                .build();
    }
}