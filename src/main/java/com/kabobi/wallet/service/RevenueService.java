package com.kabobi.wallet.service;

import com.kabobi.wallet.dto.RevenueDTO;
import com.kabobi.wallet.exception.ResourceNotFoundException;
import com.kabobi.wallet.model.Revenue;
import com.kabobi.wallet.repository.RevenueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RevenueService {

    @Autowired
    private RevenueRepository revenueRepository;

    // Conversion Entity -> DTO
    private RevenueDTO convertToDTO(Revenue revenue) {
        RevenueDTO dto = new RevenueDTO();
        dto.setId(revenue.getId());
        dto.setDescription(revenue.getDescription());
        dto.setAmount(revenue.getAmount());
        dto.setRevenueDate(revenue.getRevenueDate());
        dto.setNotes(revenue.getNotes());
        return dto;
    }

    // Conversion DTO -> Entity
    private Revenue convertToEntity(RevenueDTO dto) {
        Revenue revenue = new Revenue();
        revenue.setDescription(dto.getDescription());
        revenue.setAmount(dto.getAmount());
        revenue.setRevenueDate(dto.getRevenueDate());
        revenue.setNotes(dto.getNotes());
        return revenue;
    }

    // CRUD Operations

    public RevenueDTO createRevenue(RevenueDTO revenueDTO) {
        Revenue revenue = convertToEntity(revenueDTO);
        Revenue savedRevenue = revenueRepository.save(revenue);
        return convertToDTO(savedRevenue);
    }

    public List<RevenueDTO> getAllRevenues() {
        return revenueRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public RevenueDTO getRevenueById(Long id) {
        Revenue revenue = revenueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Revenue", "id", id));
        return convertToDTO(revenue);
    }

    public RevenueDTO updateRevenue(Long id, RevenueDTO revenueDTO) {
        Revenue revenue = revenueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Revenue", "id", id));

        revenue.setDescription(revenueDTO.getDescription());
        revenue.setAmount(revenueDTO.getAmount());
        revenue.setRevenueDate(revenueDTO.getRevenueDate());
        revenue.setNotes(revenueDTO.getNotes());

        Revenue updatedRevenue = revenueRepository.save(revenue);
        return convertToDTO(updatedRevenue);
    }

    public void deleteRevenue(Long id) {
        if (!revenueRepository.existsById(id)) {
            throw new ResourceNotFoundException("Revenue", "id", id);
        }
        revenueRepository.deleteById(id);
    }

    // Méthodes additionnelles

    public List<RevenueDTO> searchRevenues(String keyword) {
        return revenueRepository.findByDescriptionContainingIgnoreCase(keyword)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}