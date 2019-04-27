package br.eti.krebscode.blogjhipster.service;

import br.eti.krebscode.blogjhipster.domain.Entry;
import br.eti.krebscode.blogjhipster.repository.EntryRepository;
import br.eti.krebscode.blogjhipster.service.dto.EntryDTO;
import br.eti.krebscode.blogjhipster.service.mapper.EntryMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service Implementation for managing Entry.
 */
@Service
@Transactional
public class EntryService {

    private final Logger log = LoggerFactory.getLogger(EntryService.class);

    private final EntryRepository entryRepository;

    private final EntryMapper entryMapper;

    public EntryService(EntryRepository entryRepository, EntryMapper entryMapper) {
        this.entryRepository = entryRepository;
        this.entryMapper = entryMapper;
    }

    /**
     * Save a entry.
     *
     * @param entryDTO the entity to save
     * @return the persisted entity
     */
    public EntryDTO save(EntryDTO entryDTO) {
        log.debug("Request to save Entry : {}", entryDTO);
        Entry entry = entryMapper.toEntity(entryDTO);
        entry = entryRepository.save(entry);
        return entryMapper.toDto(entry);
    }

    /**
     * Get all the entries.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<EntryDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Entries");
        return entryRepository.findAll(pageable)
            .map(entryMapper::toDto);
    }

    /**
     * Get all the Entry with eager load of many-to-many relationships.
     *
     * @return the list of entities
     */
    public Page<EntryDTO> findAllWithEagerRelationships(Pageable pageable) {
        return entryRepository.findAllWithEagerRelationships(pageable).map(entryMapper::toDto);
    }
    

    /**
     * Get one entry by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public Optional<EntryDTO> findOne(Long id) {
        log.debug("Request to get Entry : {}", id);
        return entryRepository.findOneWithEagerRelationships(id)
            .map(entryMapper::toDto);
    }

    /**
     * Delete the entry by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Entry : {}", id);
        entryRepository.deleteById(id);
    }
}
