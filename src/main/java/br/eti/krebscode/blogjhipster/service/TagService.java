package br.eti.krebscode.blogjhipster.service;

import br.eti.krebscode.blogjhipster.domain.Tag;
import br.eti.krebscode.blogjhipster.repository.TagRepository;
import br.eti.krebscode.blogjhipster.service.dto.TagDTO;
import br.eti.krebscode.blogjhipster.service.mapper.TagMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service Implementation for managing Tag.
 */
@Service
@Transactional
public class TagService {

    private final Logger log = LoggerFactory.getLogger(TagService.class);

    private final TagRepository tagRepository;

    private final TagMapper tagMapper;

    public TagService(TagRepository tagRepository, TagMapper tagMapper) {
        this.tagRepository = tagRepository;
        this.tagMapper = tagMapper;
    }

    /**
     * Save a tag.
     *
     * @param tagDTO the entity to save
     * @return the persisted entity
     */
    public TagDTO save(TagDTO tagDTO) {
        log.debug("Request to save Tag : {}", tagDTO);
        Tag tag = tagMapper.toEntity(tagDTO);
        tag = tagRepository.save(tag);
        return tagMapper.toDto(tag);
    }

    /**
     * Get all the tags.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<TagDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Tags");
        return tagRepository.findAll(pageable)
            .map(tagMapper::toDto);
    }


    /**
     * Get one tag by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public Optional<TagDTO> findOne(Long id) {
        log.debug("Request to get Tag : {}", id);
        return tagRepository.findById(id)
            .map(tagMapper::toDto);
    }

    /**
     * Delete the tag by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Tag : {}", id);
        tagRepository.deleteById(id);
    }
}
