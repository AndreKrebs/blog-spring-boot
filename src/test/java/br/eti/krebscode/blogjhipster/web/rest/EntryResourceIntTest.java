package br.eti.krebscode.blogjhipster.web.rest;

import br.eti.krebscode.blogjhipster.BlogJhipsterApp;

import br.eti.krebscode.blogjhipster.domain.Entry;
import br.eti.krebscode.blogjhipster.domain.Blog;
import br.eti.krebscode.blogjhipster.domain.Tag;
import br.eti.krebscode.blogjhipster.repository.EntryRepository;
import br.eti.krebscode.blogjhipster.service.EntryService;
import br.eti.krebscode.blogjhipster.service.dto.EntryDTO;
import br.eti.krebscode.blogjhipster.service.mapper.EntryMapper;
import br.eti.krebscode.blogjhipster.web.rest.errors.ExceptionTranslator;
import br.eti.krebscode.blogjhipster.service.dto.EntryCriteria;
import br.eti.krebscode.blogjhipster.service.EntryQueryService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;
import org.springframework.validation.Validator;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;


import static br.eti.krebscode.blogjhipster.web.rest.TestUtil.sameInstant;
import static br.eti.krebscode.blogjhipster.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the EntryResource REST controller.
 *
 * @see EntryResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = BlogJhipsterApp.class)
public class EntryResourceIntTest {

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final String DEFAULT_CONTENT = "AAAAAAAAAA";
    private static final String UPDATED_CONTENT = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_DATE = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_DATE = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    @Autowired
    private EntryRepository entryRepository;

    @Mock
    private EntryRepository entryRepositoryMock;

    @Autowired
    private EntryMapper entryMapper;

    @Mock
    private EntryService entryServiceMock;

    @Autowired
    private EntryService entryService;

    @Autowired
    private EntryQueryService entryQueryService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    @Autowired
    private Validator validator;

    private MockMvc restEntryMockMvc;

    private Entry entry;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final EntryResource entryResource = new EntryResource(entryService, entryQueryService);
        this.restEntryMockMvc = MockMvcBuilders.standaloneSetup(entryResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Entry createEntity(EntityManager em) {
        Entry entry = new Entry()
            .title(DEFAULT_TITLE)
            .content(DEFAULT_CONTENT)
            .date(DEFAULT_DATE);
        // Add required entity
        Blog blog = BlogResourceIntTest.createEntity(em);
        em.persist(blog);
        em.flush();
        entry.setBlog(blog);
        return entry;
    }

    @Before
    public void initTest() {
        entry = createEntity(em);
    }

    @Test
    @Transactional
    public void createEntry() throws Exception {
        int databaseSizeBeforeCreate = entryRepository.findAll().size();

        // Create the Entry
        EntryDTO entryDTO = entryMapper.toDto(entry);
        restEntryMockMvc.perform(post("/api/entries")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(entryDTO)))
            .andExpect(status().isCreated());

        // Validate the Entry in the database
        List<Entry> entryList = entryRepository.findAll();
        assertThat(entryList).hasSize(databaseSizeBeforeCreate + 1);
        Entry testEntry = entryList.get(entryList.size() - 1);
        assertThat(testEntry.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testEntry.getContent()).isEqualTo(DEFAULT_CONTENT);
        assertThat(testEntry.getDate()).isEqualTo(DEFAULT_DATE);
    }

    @Test
    @Transactional
    public void createEntryWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = entryRepository.findAll().size();

        // Create the Entry with an existing ID
        entry.setId(1L);
        EntryDTO entryDTO = entryMapper.toDto(entry);

        // An entity with an existing ID cannot be created, so this API call must fail
        restEntryMockMvc.perform(post("/api/entries")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(entryDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Entry in the database
        List<Entry> entryList = entryRepository.findAll();
        assertThat(entryList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkTitleIsRequired() throws Exception {
        int databaseSizeBeforeTest = entryRepository.findAll().size();
        // set the field null
        entry.setTitle(null);

        // Create the Entry, which fails.
        EntryDTO entryDTO = entryMapper.toDto(entry);

        restEntryMockMvc.perform(post("/api/entries")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(entryDTO)))
            .andExpect(status().isBadRequest());

        List<Entry> entryList = entryRepository.findAll();
        assertThat(entryList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = entryRepository.findAll().size();
        // set the field null
        entry.setDate(null);

        // Create the Entry, which fails.
        EntryDTO entryDTO = entryMapper.toDto(entry);

        restEntryMockMvc.perform(post("/api/entries")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(entryDTO)))
            .andExpect(status().isBadRequest());

        List<Entry> entryList = entryRepository.findAll();
        assertThat(entryList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllEntries() throws Exception {
        // Initialize the database
        entryRepository.saveAndFlush(entry);

        // Get all the entryList
        restEntryMockMvc.perform(get("/api/entries?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(entry.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE.toString())))
            .andExpect(jsonPath("$.[*].content").value(hasItem(DEFAULT_CONTENT.toString())))
            .andExpect(jsonPath("$.[*].date").value(hasItem(sameInstant(DEFAULT_DATE))));
    }
    
    @SuppressWarnings({"unchecked"})
    public void getAllEntriesWithEagerRelationshipsIsEnabled() throws Exception {
        EntryResource entryResource = new EntryResource(entryServiceMock, entryQueryService);
        when(entryServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        MockMvc restEntryMockMvc = MockMvcBuilders.standaloneSetup(entryResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter).build();

        restEntryMockMvc.perform(get("/api/entries?eagerload=true"))
        .andExpect(status().isOk());

        verify(entryServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({"unchecked"})
    public void getAllEntriesWithEagerRelationshipsIsNotEnabled() throws Exception {
        EntryResource entryResource = new EntryResource(entryServiceMock, entryQueryService);
            when(entryServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));
            MockMvc restEntryMockMvc = MockMvcBuilders.standaloneSetup(entryResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter).build();

        restEntryMockMvc.perform(get("/api/entries?eagerload=true"))
        .andExpect(status().isOk());

            verify(entryServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    @Transactional
    public void getEntry() throws Exception {
        // Initialize the database
        entryRepository.saveAndFlush(entry);

        // Get the entry
        restEntryMockMvc.perform(get("/api/entries/{id}", entry.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(entry.getId().intValue()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE.toString()))
            .andExpect(jsonPath("$.content").value(DEFAULT_CONTENT.toString()))
            .andExpect(jsonPath("$.date").value(sameInstant(DEFAULT_DATE)));
    }

    @Test
    @Transactional
    public void getAllEntriesByTitleIsEqualToSomething() throws Exception {
        // Initialize the database
        entryRepository.saveAndFlush(entry);

        // Get all the entryList where title equals to DEFAULT_TITLE
        defaultEntryShouldBeFound("title.equals=" + DEFAULT_TITLE);

        // Get all the entryList where title equals to UPDATED_TITLE
        defaultEntryShouldNotBeFound("title.equals=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    public void getAllEntriesByTitleIsInShouldWork() throws Exception {
        // Initialize the database
        entryRepository.saveAndFlush(entry);

        // Get all the entryList where title in DEFAULT_TITLE or UPDATED_TITLE
        defaultEntryShouldBeFound("title.in=" + DEFAULT_TITLE + "," + UPDATED_TITLE);

        // Get all the entryList where title equals to UPDATED_TITLE
        defaultEntryShouldNotBeFound("title.in=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    public void getAllEntriesByTitleIsNullOrNotNull() throws Exception {
        // Initialize the database
        entryRepository.saveAndFlush(entry);

        // Get all the entryList where title is not null
        defaultEntryShouldBeFound("title.specified=true");

        // Get all the entryList where title is null
        defaultEntryShouldNotBeFound("title.specified=false");
    }

    @Test
    @Transactional
    public void getAllEntriesByDateIsEqualToSomething() throws Exception {
        // Initialize the database
        entryRepository.saveAndFlush(entry);

        // Get all the entryList where date equals to DEFAULT_DATE
        defaultEntryShouldBeFound("date.equals=" + DEFAULT_DATE);

        // Get all the entryList where date equals to UPDATED_DATE
        defaultEntryShouldNotBeFound("date.equals=" + UPDATED_DATE);
    }

    @Test
    @Transactional
    public void getAllEntriesByDateIsInShouldWork() throws Exception {
        // Initialize the database
        entryRepository.saveAndFlush(entry);

        // Get all the entryList where date in DEFAULT_DATE or UPDATED_DATE
        defaultEntryShouldBeFound("date.in=" + DEFAULT_DATE + "," + UPDATED_DATE);

        // Get all the entryList where date equals to UPDATED_DATE
        defaultEntryShouldNotBeFound("date.in=" + UPDATED_DATE);
    }

    @Test
    @Transactional
    public void getAllEntriesByDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        entryRepository.saveAndFlush(entry);

        // Get all the entryList where date is not null
        defaultEntryShouldBeFound("date.specified=true");

        // Get all the entryList where date is null
        defaultEntryShouldNotBeFound("date.specified=false");
    }

    @Test
    @Transactional
    public void getAllEntriesByDateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        entryRepository.saveAndFlush(entry);

        // Get all the entryList where date greater than or equals to DEFAULT_DATE
        defaultEntryShouldBeFound("date.greaterOrEqualThan=" + DEFAULT_DATE);

        // Get all the entryList where date greater than or equals to UPDATED_DATE
        defaultEntryShouldNotBeFound("date.greaterOrEqualThan=" + UPDATED_DATE);
    }

    @Test
    @Transactional
    public void getAllEntriesByDateIsLessThanSomething() throws Exception {
        // Initialize the database
        entryRepository.saveAndFlush(entry);

        // Get all the entryList where date less than or equals to DEFAULT_DATE
        defaultEntryShouldNotBeFound("date.lessThan=" + DEFAULT_DATE);

        // Get all the entryList where date less than or equals to UPDATED_DATE
        defaultEntryShouldBeFound("date.lessThan=" + UPDATED_DATE);
    }


    @Test
    @Transactional
    public void getAllEntriesByBlogIsEqualToSomething() throws Exception {
        // Initialize the database
        Blog blog = BlogResourceIntTest.createEntity(em);
        em.persist(blog);
        em.flush();
        entry.setBlog(blog);
        entryRepository.saveAndFlush(entry);
        Long blogId = blog.getId();

        // Get all the entryList where blog equals to blogId
        defaultEntryShouldBeFound("blogId.equals=" + blogId);

        // Get all the entryList where blog equals to blogId + 1
        defaultEntryShouldNotBeFound("blogId.equals=" + (blogId + 1));
    }


    @Test
    @Transactional
    public void getAllEntriesByTagIsEqualToSomething() throws Exception {
        // Initialize the database
        Tag tag = TagResourceIntTest.createEntity(em);
        em.persist(tag);
        em.flush();
        entry.addTag(tag);
        entryRepository.saveAndFlush(entry);
        Long tagId = tag.getId();

        // Get all the entryList where tag equals to tagId
        defaultEntryShouldBeFound("tagId.equals=" + tagId);

        // Get all the entryList where tag equals to tagId + 1
        defaultEntryShouldNotBeFound("tagId.equals=" + (tagId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned
     */
    private void defaultEntryShouldBeFound(String filter) throws Exception {
        restEntryMockMvc.perform(get("/api/entries?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(entry.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].content").value(hasItem(DEFAULT_CONTENT.toString())))
            .andExpect(jsonPath("$.[*].date").value(hasItem(sameInstant(DEFAULT_DATE))));

        // Check, that the count call also returns 1
        restEntryMockMvc.perform(get("/api/entries/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    private void defaultEntryShouldNotBeFound(String filter) throws Exception {
        restEntryMockMvc.perform(get("/api/entries?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restEntryMockMvc.perform(get("/api/entries/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("0"));
    }


    @Test
    @Transactional
    public void getNonExistingEntry() throws Exception {
        // Get the entry
        restEntryMockMvc.perform(get("/api/entries/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateEntry() throws Exception {
        // Initialize the database
        entryRepository.saveAndFlush(entry);

        int databaseSizeBeforeUpdate = entryRepository.findAll().size();

        // Update the entry
        Entry updatedEntry = entryRepository.findById(entry.getId()).get();
        // Disconnect from session so that the updates on updatedEntry are not directly saved in db
        em.detach(updatedEntry);
        updatedEntry
            .title(UPDATED_TITLE)
            .content(UPDATED_CONTENT)
            .date(UPDATED_DATE);
        EntryDTO entryDTO = entryMapper.toDto(updatedEntry);

        restEntryMockMvc.perform(put("/api/entries")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(entryDTO)))
            .andExpect(status().isOk());

        // Validate the Entry in the database
        List<Entry> entryList = entryRepository.findAll();
        assertThat(entryList).hasSize(databaseSizeBeforeUpdate);
        Entry testEntry = entryList.get(entryList.size() - 1);
        assertThat(testEntry.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testEntry.getContent()).isEqualTo(UPDATED_CONTENT);
        assertThat(testEntry.getDate()).isEqualTo(UPDATED_DATE);
    }

    @Test
    @Transactional
    public void updateNonExistingEntry() throws Exception {
        int databaseSizeBeforeUpdate = entryRepository.findAll().size();

        // Create the Entry
        EntryDTO entryDTO = entryMapper.toDto(entry);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restEntryMockMvc.perform(put("/api/entries")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(entryDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Entry in the database
        List<Entry> entryList = entryRepository.findAll();
        assertThat(entryList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteEntry() throws Exception {
        // Initialize the database
        entryRepository.saveAndFlush(entry);

        int databaseSizeBeforeDelete = entryRepository.findAll().size();

        // Delete the entry
        restEntryMockMvc.perform(delete("/api/entries/{id}", entry.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Entry> entryList = entryRepository.findAll();
        assertThat(entryList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Entry.class);
        Entry entry1 = new Entry();
        entry1.setId(1L);
        Entry entry2 = new Entry();
        entry2.setId(entry1.getId());
        assertThat(entry1).isEqualTo(entry2);
        entry2.setId(2L);
        assertThat(entry1).isNotEqualTo(entry2);
        entry1.setId(null);
        assertThat(entry1).isNotEqualTo(entry2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(EntryDTO.class);
        EntryDTO entryDTO1 = new EntryDTO();
        entryDTO1.setId(1L);
        EntryDTO entryDTO2 = new EntryDTO();
        assertThat(entryDTO1).isNotEqualTo(entryDTO2);
        entryDTO2.setId(entryDTO1.getId());
        assertThat(entryDTO1).isEqualTo(entryDTO2);
        entryDTO2.setId(2L);
        assertThat(entryDTO1).isNotEqualTo(entryDTO2);
        entryDTO1.setId(null);
        assertThat(entryDTO1).isNotEqualTo(entryDTO2);
    }

    @Test
    @Transactional
    public void testEntityFromId() {
        assertThat(entryMapper.fromId(42L).getId()).isEqualTo(42);
        assertThat(entryMapper.fromId(null)).isNull();
    }
}
