package br.eti.krebscode.blogjhipster.service;

import java.util.List;

import javax.persistence.criteria.JoinType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.jhipster.service.QueryService;

import br.eti.krebscode.blogjhipster.domain.Entry;
import br.eti.krebscode.blogjhipster.domain.*; // for static metamodels
import br.eti.krebscode.blogjhipster.repository.EntryRepository;
import br.eti.krebscode.blogjhipster.service.dto.EntryCriteria;
import br.eti.krebscode.blogjhipster.service.dto.EntryDTO;
import br.eti.krebscode.blogjhipster.service.mapper.EntryMapper;

/**
 * Service for executing complex queries for Entry entities in the database.
 * The main input is a {@link EntryCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link EntryDTO} or a {@link Page} of {@link EntryDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class EntryQueryService extends QueryService<Entry> {

    private final Logger log = LoggerFactory.getLogger(EntryQueryService.class);

    private final EntryRepository entryRepository;

    private final EntryMapper entryMapper;

    public EntryQueryService(EntryRepository entryRepository, EntryMapper entryMapper) {
        this.entryRepository = entryRepository;
        this.entryMapper = entryMapper;
    }

    /**
     * Return a {@link List} of {@link EntryDTO} which matches the criteria from the database
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<EntryDTO> findByCriteria(EntryCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<Entry> specification = createSpecification(criteria);
        return entryMapper.toDto(entryRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link EntryDTO} which matches the criteria from the database
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<EntryDTO> findByCriteria(EntryCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Entry> specification = createSpecification(criteria);
        return entryRepository.findAll(specification, page)
            .map(entryMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(EntryCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<Entry> specification = createSpecification(criteria);
        return entryRepository.count(specification);
    }

    /**
     * Function to convert EntryCriteria to a {@link Specification}
     */
    private Specification<Entry> createSpecification(EntryCriteria criteria) {
        Specification<Entry> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildSpecification(criteria.getId(), Entry_.id));
            }
            if (criteria.getTitle() != null) {
                specification = specification.and(buildStringSpecification(criteria.getTitle(), Entry_.title));
            }
            if (criteria.getDate() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getDate(), Entry_.date));
            }
            if (criteria.getBlogId() != null) {
                specification = specification.and(buildSpecification(criteria.getBlogId(),
                    root -> root.join(Entry_.blog, JoinType.LEFT).get(Blog_.id)));
            }
            if (criteria.getTagId() != null) {
                specification = specification.and(buildSpecification(criteria.getTagId(),
                    root -> root.join(Entry_.tags, JoinType.LEFT).get(Tag_.id)));
            }
        }
        return specification;
    }
}
