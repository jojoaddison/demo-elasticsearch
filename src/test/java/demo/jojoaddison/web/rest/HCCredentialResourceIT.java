package demo.jojoaddison.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import demo.jojoaddison.IntegrationTest;
import demo.jojoaddison.domain.HCCredential;
import demo.jojoaddison.repository.HCCredentialRepository;
import demo.jojoaddison.repository.search.HCCredentialSearchRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections4.IterableUtils;
import org.assertj.core.util.IterableUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for the {@link HCCredentialResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class HCCredentialResourceIT {

    private static final String DEFAULT_EMAIL = "AAAAAAAAAA";
    private static final String UPDATED_EMAIL = "BBBBBBBBBB";

    private static final String DEFAULT_PHONE_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_PHONE_NUMBER = "BBBBBBBBBB";

    private static final String DEFAULT_PASSWORD = "AAAAAAAAAA";
    private static final String UPDATED_PASSWORD = "BBBBBBBBBB";

    private static final String DEFAULT_ROLE = "AAAAAAAAAA";
    private static final String UPDATED_ROLE = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_CREATED_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_CREATED_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final LocalDate DEFAULT_MODIFIED_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_MODIFIED_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final String ENTITY_API_URL = "/api/hc-credentials";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/hc-credentials/_search";

    @Autowired
    private HCCredentialRepository hCCredentialRepository;

    @Autowired
    private HCCredentialSearchRepository hCCredentialSearchRepository;

    @Autowired
    private MockMvc restHCCredentialMockMvc;

    private HCCredential hCCredential;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static HCCredential createEntity() {
        HCCredential hCCredential = new HCCredential()
            .email(DEFAULT_EMAIL)
            .phoneNumber(DEFAULT_PHONE_NUMBER)
            .password(DEFAULT_PASSWORD)
            .role(DEFAULT_ROLE)
            .createdDate(DEFAULT_CREATED_DATE)
            .active(DEFAULT_ACTIVE)
            .modifiedDate(DEFAULT_MODIFIED_DATE);
        return hCCredential;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static HCCredential createUpdatedEntity() {
        HCCredential hCCredential = new HCCredential()
            .email(UPDATED_EMAIL)
            .phoneNumber(UPDATED_PHONE_NUMBER)
            .password(UPDATED_PASSWORD)
            .role(UPDATED_ROLE)
            .createdDate(UPDATED_CREATED_DATE)
            .active(UPDATED_ACTIVE)
            .modifiedDate(UPDATED_MODIFIED_DATE);
        return hCCredential;
    }

    @AfterEach
    public void cleanupElasticSearchRepository() {
        hCCredentialSearchRepository.deleteAll();
        assertThat(hCCredentialSearchRepository.count()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        hCCredentialRepository.deleteAll();
        hCCredential = createEntity();
    }

    @Test
    void createHCCredential() throws Exception {
        int databaseSizeBeforeCreate = hCCredentialRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(hCCredentialSearchRepository.findAll());
        // Create the HCCredential
        restHCCredentialMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(hCCredential)))
            .andExpect(status().isCreated());

        // Validate the HCCredential in the database
        List<HCCredential> hCCredentialList = hCCredentialRepository.findAll();
        assertThat(hCCredentialList).hasSize(databaseSizeBeforeCreate + 1);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(hCCredentialSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
        HCCredential testHCCredential = hCCredentialList.get(hCCredentialList.size() - 1);
        assertThat(testHCCredential.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(testHCCredential.getPhoneNumber()).isEqualTo(DEFAULT_PHONE_NUMBER);
        assertThat(testHCCredential.getPassword()).isEqualTo(DEFAULT_PASSWORD);
        assertThat(testHCCredential.getRole()).isEqualTo(DEFAULT_ROLE);
        assertThat(testHCCredential.getCreatedDate()).isEqualTo(DEFAULT_CREATED_DATE);
        assertThat(testHCCredential.getActive()).isEqualTo(DEFAULT_ACTIVE);
        assertThat(testHCCredential.getModifiedDate()).isEqualTo(DEFAULT_MODIFIED_DATE);
    }

    @Test
    void createHCCredentialWithExistingId() throws Exception {
        // Create the HCCredential with an existing ID
        hCCredential.setId("existing_id");

        int databaseSizeBeforeCreate = hCCredentialRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(hCCredentialSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restHCCredentialMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(hCCredential)))
            .andExpect(status().isBadRequest());

        // Validate the HCCredential in the database
        List<HCCredential> hCCredentialList = hCCredentialRepository.findAll();
        assertThat(hCCredentialList).hasSize(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(hCCredentialSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void getAllHCCredentials() throws Exception {
        // Initialize the database
        hCCredentialRepository.save(hCCredential);

        // Get all the hCCredentialList
        restHCCredentialMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(hCCredential.getId())))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
            .andExpect(jsonPath("$.[*].phoneNumber").value(hasItem(DEFAULT_PHONE_NUMBER)))
            .andExpect(jsonPath("$.[*].password").value(hasItem(DEFAULT_PASSWORD)))
            .andExpect(jsonPath("$.[*].role").value(hasItem(DEFAULT_ROLE)))
            .andExpect(jsonPath("$.[*].createdDate").value(hasItem(DEFAULT_CREATED_DATE.toString())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].modifiedDate").value(hasItem(DEFAULT_MODIFIED_DATE.toString())));
    }

    @Test
    void getHCCredential() throws Exception {
        // Initialize the database
        hCCredentialRepository.save(hCCredential);

        // Get the hCCredential
        restHCCredentialMockMvc
            .perform(get(ENTITY_API_URL_ID, hCCredential.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(hCCredential.getId()))
            .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL))
            .andExpect(jsonPath("$.phoneNumber").value(DEFAULT_PHONE_NUMBER))
            .andExpect(jsonPath("$.password").value(DEFAULT_PASSWORD))
            .andExpect(jsonPath("$.role").value(DEFAULT_ROLE))
            .andExpect(jsonPath("$.createdDate").value(DEFAULT_CREATED_DATE.toString()))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()))
            .andExpect(jsonPath("$.modifiedDate").value(DEFAULT_MODIFIED_DATE.toString()));
    }

    @Test
    void getNonExistingHCCredential() throws Exception {
        // Get the hCCredential
        restHCCredentialMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    void putExistingHCCredential() throws Exception {
        // Initialize the database
        hCCredentialRepository.save(hCCredential);

        int databaseSizeBeforeUpdate = hCCredentialRepository.findAll().size();
        hCCredentialSearchRepository.save(hCCredential);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(hCCredentialSearchRepository.findAll());

        // Update the hCCredential
        HCCredential updatedHCCredential = hCCredentialRepository.findById(hCCredential.getId()).orElseThrow();
        updatedHCCredential
            .email(UPDATED_EMAIL)
            .phoneNumber(UPDATED_PHONE_NUMBER)
            .password(UPDATED_PASSWORD)
            .role(UPDATED_ROLE)
            .createdDate(UPDATED_CREATED_DATE)
            .active(UPDATED_ACTIVE)
            .modifiedDate(UPDATED_MODIFIED_DATE);

        restHCCredentialMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedHCCredential.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedHCCredential))
            )
            .andExpect(status().isOk());

        // Validate the HCCredential in the database
        List<HCCredential> hCCredentialList = hCCredentialRepository.findAll();
        assertThat(hCCredentialList).hasSize(databaseSizeBeforeUpdate);
        HCCredential testHCCredential = hCCredentialList.get(hCCredentialList.size() - 1);
        assertThat(testHCCredential.getEmail()).isEqualTo(UPDATED_EMAIL);
        assertThat(testHCCredential.getPhoneNumber()).isEqualTo(UPDATED_PHONE_NUMBER);
        assertThat(testHCCredential.getPassword()).isEqualTo(UPDATED_PASSWORD);
        assertThat(testHCCredential.getRole()).isEqualTo(UPDATED_ROLE);
        assertThat(testHCCredential.getCreatedDate()).isEqualTo(UPDATED_CREATED_DATE);
        assertThat(testHCCredential.getActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testHCCredential.getModifiedDate()).isEqualTo(UPDATED_MODIFIED_DATE);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(hCCredentialSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<HCCredential> hCCredentialSearchList = IterableUtils.toList(hCCredentialSearchRepository.findAll());
                HCCredential testHCCredentialSearch = hCCredentialSearchList.get(searchDatabaseSizeAfter - 1);
                assertThat(testHCCredentialSearch.getEmail()).isEqualTo(UPDATED_EMAIL);
                assertThat(testHCCredentialSearch.getPhoneNumber()).isEqualTo(UPDATED_PHONE_NUMBER);
                assertThat(testHCCredentialSearch.getPassword()).isEqualTo(UPDATED_PASSWORD);
                assertThat(testHCCredentialSearch.getRole()).isEqualTo(UPDATED_ROLE);
                assertThat(testHCCredentialSearch.getCreatedDate()).isEqualTo(UPDATED_CREATED_DATE);
                assertThat(testHCCredentialSearch.getActive()).isEqualTo(UPDATED_ACTIVE);
                assertThat(testHCCredentialSearch.getModifiedDate()).isEqualTo(UPDATED_MODIFIED_DATE);
            });
    }

    @Test
    void putNonExistingHCCredential() throws Exception {
        int databaseSizeBeforeUpdate = hCCredentialRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(hCCredentialSearchRepository.findAll());
        hCCredential.setId(UUID.randomUUID().toString());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restHCCredentialMockMvc
            .perform(
                put(ENTITY_API_URL_ID, hCCredential.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(hCCredential))
            )
            .andExpect(status().isBadRequest());

        // Validate the HCCredential in the database
        List<HCCredential> hCCredentialList = hCCredentialRepository.findAll();
        assertThat(hCCredentialList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(hCCredentialSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithIdMismatchHCCredential() throws Exception {
        int databaseSizeBeforeUpdate = hCCredentialRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(hCCredentialSearchRepository.findAll());
        hCCredential.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restHCCredentialMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(hCCredential))
            )
            .andExpect(status().isBadRequest());

        // Validate the HCCredential in the database
        List<HCCredential> hCCredentialList = hCCredentialRepository.findAll();
        assertThat(hCCredentialList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(hCCredentialSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithMissingIdPathParamHCCredential() throws Exception {
        int databaseSizeBeforeUpdate = hCCredentialRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(hCCredentialSearchRepository.findAll());
        hCCredential.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restHCCredentialMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(hCCredential)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the HCCredential in the database
        List<HCCredential> hCCredentialList = hCCredentialRepository.findAll();
        assertThat(hCCredentialList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(hCCredentialSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void partialUpdateHCCredentialWithPatch() throws Exception {
        // Initialize the database
        hCCredentialRepository.save(hCCredential);

        int databaseSizeBeforeUpdate = hCCredentialRepository.findAll().size();

        // Update the hCCredential using partial update
        HCCredential partialUpdatedHCCredential = new HCCredential();
        partialUpdatedHCCredential.setId(hCCredential.getId());

        partialUpdatedHCCredential
            .email(UPDATED_EMAIL)
            .phoneNumber(UPDATED_PHONE_NUMBER)
            .password(UPDATED_PASSWORD)
            .role(UPDATED_ROLE)
            .createdDate(UPDATED_CREATED_DATE)
            .active(UPDATED_ACTIVE)
            .modifiedDate(UPDATED_MODIFIED_DATE);

        restHCCredentialMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedHCCredential.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedHCCredential))
            )
            .andExpect(status().isOk());

        // Validate the HCCredential in the database
        List<HCCredential> hCCredentialList = hCCredentialRepository.findAll();
        assertThat(hCCredentialList).hasSize(databaseSizeBeforeUpdate);
        HCCredential testHCCredential = hCCredentialList.get(hCCredentialList.size() - 1);
        assertThat(testHCCredential.getEmail()).isEqualTo(UPDATED_EMAIL);
        assertThat(testHCCredential.getPhoneNumber()).isEqualTo(UPDATED_PHONE_NUMBER);
        assertThat(testHCCredential.getPassword()).isEqualTo(UPDATED_PASSWORD);
        assertThat(testHCCredential.getRole()).isEqualTo(UPDATED_ROLE);
        assertThat(testHCCredential.getCreatedDate()).isEqualTo(UPDATED_CREATED_DATE);
        assertThat(testHCCredential.getActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testHCCredential.getModifiedDate()).isEqualTo(UPDATED_MODIFIED_DATE);
    }

    @Test
    void fullUpdateHCCredentialWithPatch() throws Exception {
        // Initialize the database
        hCCredentialRepository.save(hCCredential);

        int databaseSizeBeforeUpdate = hCCredentialRepository.findAll().size();

        // Update the hCCredential using partial update
        HCCredential partialUpdatedHCCredential = new HCCredential();
        partialUpdatedHCCredential.setId(hCCredential.getId());

        partialUpdatedHCCredential
            .email(UPDATED_EMAIL)
            .phoneNumber(UPDATED_PHONE_NUMBER)
            .password(UPDATED_PASSWORD)
            .role(UPDATED_ROLE)
            .createdDate(UPDATED_CREATED_DATE)
            .active(UPDATED_ACTIVE)
            .modifiedDate(UPDATED_MODIFIED_DATE);

        restHCCredentialMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedHCCredential.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedHCCredential))
            )
            .andExpect(status().isOk());

        // Validate the HCCredential in the database
        List<HCCredential> hCCredentialList = hCCredentialRepository.findAll();
        assertThat(hCCredentialList).hasSize(databaseSizeBeforeUpdate);
        HCCredential testHCCredential = hCCredentialList.get(hCCredentialList.size() - 1);
        assertThat(testHCCredential.getEmail()).isEqualTo(UPDATED_EMAIL);
        assertThat(testHCCredential.getPhoneNumber()).isEqualTo(UPDATED_PHONE_NUMBER);
        assertThat(testHCCredential.getPassword()).isEqualTo(UPDATED_PASSWORD);
        assertThat(testHCCredential.getRole()).isEqualTo(UPDATED_ROLE);
        assertThat(testHCCredential.getCreatedDate()).isEqualTo(UPDATED_CREATED_DATE);
        assertThat(testHCCredential.getActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testHCCredential.getModifiedDate()).isEqualTo(UPDATED_MODIFIED_DATE);
    }

    @Test
    void patchNonExistingHCCredential() throws Exception {
        int databaseSizeBeforeUpdate = hCCredentialRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(hCCredentialSearchRepository.findAll());
        hCCredential.setId(UUID.randomUUID().toString());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restHCCredentialMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, hCCredential.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(hCCredential))
            )
            .andExpect(status().isBadRequest());

        // Validate the HCCredential in the database
        List<HCCredential> hCCredentialList = hCCredentialRepository.findAll();
        assertThat(hCCredentialList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(hCCredentialSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithIdMismatchHCCredential() throws Exception {
        int databaseSizeBeforeUpdate = hCCredentialRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(hCCredentialSearchRepository.findAll());
        hCCredential.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restHCCredentialMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(hCCredential))
            )
            .andExpect(status().isBadRequest());

        // Validate the HCCredential in the database
        List<HCCredential> hCCredentialList = hCCredentialRepository.findAll();
        assertThat(hCCredentialList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(hCCredentialSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithMissingIdPathParamHCCredential() throws Exception {
        int databaseSizeBeforeUpdate = hCCredentialRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(hCCredentialSearchRepository.findAll());
        hCCredential.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restHCCredentialMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(hCCredential))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the HCCredential in the database
        List<HCCredential> hCCredentialList = hCCredentialRepository.findAll();
        assertThat(hCCredentialList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(hCCredentialSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void deleteHCCredential() throws Exception {
        // Initialize the database
        hCCredentialRepository.save(hCCredential);
        hCCredentialRepository.save(hCCredential);
        hCCredentialSearchRepository.save(hCCredential);

        int databaseSizeBeforeDelete = hCCredentialRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(hCCredentialSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the hCCredential
        restHCCredentialMockMvc
            .perform(delete(ENTITY_API_URL_ID, hCCredential.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<HCCredential> hCCredentialList = hCCredentialRepository.findAll();
        assertThat(hCCredentialList).hasSize(databaseSizeBeforeDelete - 1);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(hCCredentialSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    void searchHCCredential() throws Exception {
        // Initialize the database
        hCCredential = hCCredentialRepository.save(hCCredential);
        hCCredentialSearchRepository.save(hCCredential);

        // Search the hCCredential
        restHCCredentialMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + hCCredential.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(hCCredential.getId())))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
            .andExpect(jsonPath("$.[*].phoneNumber").value(hasItem(DEFAULT_PHONE_NUMBER)))
            .andExpect(jsonPath("$.[*].password").value(hasItem(DEFAULT_PASSWORD)))
            .andExpect(jsonPath("$.[*].role").value(hasItem(DEFAULT_ROLE)))
            .andExpect(jsonPath("$.[*].createdDate").value(hasItem(DEFAULT_CREATED_DATE.toString())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].modifiedDate").value(hasItem(DEFAULT_MODIFIED_DATE.toString())));
    }
}
