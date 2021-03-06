package br.eti.krebscode.blogjhipster.service.mapper;

import br.eti.krebscode.blogjhipster.domain.*;
import br.eti.krebscode.blogjhipster.service.dto.EntryDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity Entry and its DTO EntryDTO.
 */
@Mapper(componentModel = "spring", uses = {BlogMapper.class, TagMapper.class})
public interface EntryMapper extends EntityMapper<EntryDTO, Entry> {

    @Mapping(source = "blog.id", target = "blogId")
    @Mapping(source = "blog.name", target = "blogName")
    EntryDTO toDto(Entry entry);

    @Mapping(source = "blogId", target = "blog")
    Entry toEntity(EntryDTO entryDTO);

    default Entry fromId(Long id) {
        if (id == null) {
            return null;
        }
        Entry entry = new Entry();
        entry.setId(id);
        return entry;
    }
}
