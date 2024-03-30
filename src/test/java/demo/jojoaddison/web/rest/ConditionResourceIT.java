package demo.jojoaddison.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import demo.jojoaddison.IntegrationTest;
import demo.jojoaddison.domain.Condition;
import demo.jojoaddison.repository.ConditionRepository;
import demo.jojoaddison.repository.search.ConditionSearchRepository;
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
 * Integration tests for the {@link ConditionResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ConditionResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String DEFAULT_PATIENT_ID = "AAAAAAAAAA";
    private static final String UPDATED_PATIENT_ID = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_CREATED_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_CREATED_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final LocalDate DEFAULT_MODIFIED_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_MODIFIED_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final String DEFAULT_CREATED_BY = "AAAAAAAAAA";
    private static final String UPDATED_CREATED_BY = "BBBBBBBBBB";

    private static final String DEFAULT_MODIFIED_BY = "AAAAAAAAAA";
    private static final String UPDATED_MODIFIED_BY = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/conditions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/conditions/_search";

    @Autowired
    private ConditionRepository conditionRepository;

    @Autowired
    private ConditionSearchRepository conditionSearchRepository;

    @Autowired
    private MockMvc restConditionMockMvc;

    private Condition condition;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Condition createEntity() {
        Condition condition = new Condition()
            .name(DEFAULT_NAME)
            .description(DEFAULT_DESCRIPTION)
            .patientId(DEFAULT_PATIENT_ID)
            .createdDate(DEFAULT_CREATED_DATE)
            .modifiedDate(DEFAULT_MODIFIED_DATE)
            .createdBy(DEFAULT_CREATED_BY)
            .modifiedBy(DEFAULT_MODIFIED_BY);
        return condition;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Condition createUpdatedEntity() {
        Condition condition = new Condition()
            .name(UPDATED_NAME)
            .description(UPDATED_DESCRIPTION)
            .patientId(UPDATED_PATIENT_ID)
            .createdDate(UPDATED_CREATED_DATE)
            .modifiedDate(UPDATED_MODIFIED_DATE)
            .createdBy(UPDATED_CREATED_BY)
            .modifiedBy(UPDATED_MODIFIED_BY);
        return condition;
    }

    @AfterEach
    public void cleanupElasticSearchRepository() {
        conditionSearchRepository.deleteAll();
        assertThat(conditionSearchRepository.count()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        conditionRepository.deleteAll();
        condition = createEntity();
    }

    @Test
    void createCondition() throws Exception {
        int databaseSizeBeforeCreate = conditionRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(conditionSearchRepository.findAll());
        // Create the Condition
        restConditionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(condition)))
            .andExpect(status().isCreated());

        // Validate the Condition in the database
        List<Condition> conditionList = conditionRepository.findAll();
        assertThat(conditionList).hasSize(databaseSizeBeforeCreate + 1);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(conditionSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
        Condition testCondition = conditionList.get(conditionList.size() - 1);
        assertThat(testCondition.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testCondition.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testCondition.getPatientId()).isEqualTo(DEFAULT_PATIENT_ID);
        assertThat(testCondition.getCreatedDate()).isEqualTo(DEFAULT_CREATED_DATE);
        assertThat(testCondition.getModifiedDate()).isEqualTo(DEFAULT_MODIFIED_DATE);
        assertThat(testCondition.getCreatedBy()).isEqualTo(DEFAULT_CREATED_BY);
        assertThat(testCondition.getModifiedBy()).isEqualTo(DEFAULT_MODIFIED_BY);
    }

    @Test
    void createConditionWithExistingId() throws Exception {
        // Create the Condition with an existing ID
        condition.setId("existing_id");

        int databaseSizeBeforeCreate = conditionRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(conditionSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restConditionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(condition)))
            .andExpect(status().isBadRequest());

        // Validate the Condition in the database
        List<Condition> conditionList = conditionRepository.findAll();
        assertThat(conditionList).hasSize(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(conditionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void getAllConditions() throws Exception {
        // Initialize the database
        conditionRepository.save(condition);

        // Get all the conditionList
        restConditionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(condition.getId())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].patientId").value(hasItem(DEFAULT_PATIENT_ID)))
            .andExpect(jsonPath("$.[*].createdDate").value(hasItem(DEFAULT_CREATED_DATE.toString())))
            .andExpect(jsonPath("$.[*].modifiedDate").value(hasItem(DEFAULT_MODIFIED_DATE.toString())))
            .andExpect(jsonPath("$.[*].createdBy").value(hasItem(DEFAULT_CREATED_BY)))
            .andExpect(jsonPath("$.[*].modifiedBy").value(hasItem(DEFAULT_MODIFIED_BY)));
    }

    @Test
    void getCondition() throws Exception {
        // Initialize the database
        conditionRepository.save(condition);

        // Get the condition
        restConditionMockMvc
            .perform(get(ENTITY_API_URL_ID, condition.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(condition.getId()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.patientId").value(DEFAULT_PATIENT_ID))
            .andExpect(jsonPath("$.createdDate").value(DEFAULT_CREATED_DATE.toString()))
            .andExpect(jsonPath("$.modifiedDate").value(DEFAULT_MODIFIED_DATE.toString()))
            .andExpect(jsonPath("$.createdBy").value(DEFAULT_CREATED_BY))
            .andExpect(jsonPath("$.modifiedBy").value(DEFAULT_MODIFIED_BY));
    }

    @Test
    void getNonExistingCondition() throws Exception {
        // Get the condition
        restConditionMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    void putExistingCondition() throws Exception {
        // Initialize the database
        conditionRepository.save(condition);

        int databaseSizeBeforeUpdate = conditionRepository.findAll().size();
        conditionSearchRepository.save(condition);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(conditionSearchRepository.findAll());

        // Update the condition
        Condition updatedCondition = conditionRepository.findById(condition.getId()).orElseThrow();
        updatedCondition
            .name(UPDATED_NAME)
            .description(UPDATED_DESCRIPTION)
            .patientId(UPDATED_PATIENT_ID)
            .createdDate(UPDATED_CREATED_DATE)
            .modifiedDate(UPDATED_MODIFIED_DATE)
            .createdBy(UPDATED_CREATED_BY)
            .modifiedBy(UPDATED_MODIFIED_BY);

        restConditionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedCondition.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedCondition))
            )
            .andExpect(status().isOk());

        // Validate the Condition in the database
        List<Condition> conditionList = conditionRepository.findAll();
        assertThat(conditionList).hasSize(databaseSizeBeforeUpdate);
        Condition testCondition = conditionList.get(conditionList.size() - 1);
        assertThat(testCondition.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testCondition.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testCondition.getPatientId()).isEqualTo(UPDATED_PATIENT_ID);
        assertThat(testCondition.getCreatedDate()).isEqualTo(UPDATED_CREATED_DATE);
        assertThat(testCondition.getModifiedDate()).isEqualTo(UPDATED_MODIFIED_DATE);
        assertThat(testCondition.getCreatedBy()).isEqualTo(UPDATED_CREATED_BY);
        assertThat(testCondition.getModifiedBy()).isEqualTo(UPDATED_MODIFIED_BY);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(conditionSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<Condition> conditionSearchList = IterableUtils.toList(conditionSearchRepository.findAll());
                Condition testConditionSearch = conditionSearchList.get(searchDatabaseSizeAfter - 1);
                assertThat(testConditionSearch.getName()).isEqualTo(UPDATED_NAME);
                assertThat(testConditionSearch.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
                assertThat(testConditionSearch.getPatientId()).isEqualTo(UPDATED_PATIENT_ID);
                assertThat(testConditionSearch.getCreatedDate()).isEqualTo(UPDATED_CREATED_DATE);
                assertThat(testConditionSearch.getModifiedDate()).isEqualTo(UPDATED_MODIFIED_DATE);
                assertThat(testConditionSearch.getCreatedBy()).isEqualTo(UPDATED_CREATED_BY);
                assertThat(testConditionSearch.getModifiedBy()).isEqualTo(UPDATED_MODIFIED_BY);
            });
    }

    @Test
    void putNonExistingCondition() throws Exception {
        int databaseSizeBeforeUpdate = conditionRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(conditionSearchRepository.findAll());
        condition.setId(UUID.randomUUID().toString());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restConditionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, condition.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(condition))
            )
            .andExpect(status().isBadRequest());

        // Validate the Condition in the database
        List<Condition> conditionList = conditionRepository.findAll();
        assertThat(conditionList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(conditionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithIdMismatchCondition() throws Exception {
        int databaseSizeBeforeUpdate = conditionRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(conditionSearchRepository.findAll());
        condition.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restConditionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(condition))
            )
            .andExpect(status().isBadRequest());

        // Validate the Condition in the database
        List<Condition> conditionList = conditionRepository.findAll();
        assertThat(conditionList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(conditionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithMissingIdPathParamCondition() throws Exception {
        int databaseSizeBeforeUpdate = conditionRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(conditionSearchRepository.findAll());
        condition.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restConditionMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(condition)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Condition in the database
        List<Condition> conditionList = conditionRepository.findAll();
        assertThat(conditionList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(conditionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void partialUpdateConditionWithPatch() throws Exception {
        // Initialize the database
        conditionRepository.save(condition);

        int databaseSizeBeforeUpdate = conditionRepository.findAll().size();

        // Update the condition using partial update
        Condition partialUpdatedCondition = new Condition();
        partialUpdatedCondition.setId(condition.getId());

        partialUpdatedCondition.name(UPDATED_NAME);

        restConditionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCondition.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedCondition))
            )
            .andExpect(status().isOk());

        // Validate the Condition in the database
        List<Condition> conditionList = conditionRepository.findAll();
        assertThat(conditionList).hasSize(databaseSizeBeforeUpdate);
        Condition testCondition = conditionList.get(conditionList.size() - 1);
        assertThat(testCondition.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testCondition.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testCondition.getPatientId()).isEqualTo(DEFAULT_PATIENT_ID);
        assertThat(testCondition.getCreatedDate()).isEqualTo(DEFAULT_CREATED_DATE);
        assertThat(testCondition.getModifiedDate()).isEqualTo(DEFAULT_MODIFIED_DATE);
        assertThat(testCondition.getCreatedBy()).isEqualTo(DEFAULT_CREATED_BY);
        assertThat(testCondition.getModifiedBy()).isEqualTo(DEFAULT_MODIFIED_BY);
    }

    @Test
    void fullUpdateConditionWithPatch() throws Exception {
        // Initialize the database
        conditionRepository.save(condition);

        int databaseSizeBeforeUpdate = conditionRepository.findAll().size();

        // Update the condition using partial update
        Condition partialUpdatedCondition = new Condition();
        partialUpdatedCondition.setId(condition.getId());

        partialUpdatedCondition
            .name(UPDATED_NAME)
            .description(UPDATED_DESCRIPTION)
            .patientId(UPDATED_PATIENT_ID)
            .createdDate(UPDATED_CREATED_DATE)
            .modifiedDate(UPDATED_MODIFIED_DATE)
            .createdBy(UPDATED_CREATED_BY)
            .modifiedBy(UPDATED_MODIFIED_BY);

        restConditionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCondition.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedCondition))
            )
            .andExpect(status().isOk());

        // Validate the Condition in the database
        List<Condition> conditionList = conditionRepository.findAll();
        assertThat(conditionList).hasSize(databaseSizeBeforeUpdate);
        Condition testCondition = conditionList.get(conditionList.size() - 1);
        assertThat(testCondition.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testCondition.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testCondition.getPatientId()).isEqualTo(UPDATED_PATIENT_ID);
        assertThat(testCondition.getCreatedDate()).isEqualTo(UPDATED_CREATED_DATE);
        assertThat(testCondition.getModifiedDate()).isEqualTo(UPDATED_MODIFIED_DATE);
        assertThat(testCondition.getCreatedBy()).isEqualTo(UPDATED_CREATED_BY);
        assertThat(testCondition.getModifiedBy()).isEqualTo(UPDATED_MODIFIED_BY);
    }

    @Test
    void patchNonExistingCondition() throws Exception {
        int databaseSizeBeforeUpdate = conditionRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(conditionSearchRepository.findAll());
        condition.setId(UUID.randomUUID().toString());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restConditionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, condition.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(condition))
            )
            .andExpect(status().isBadRequest());

        // Validate the Condition in the database
        List<Condition> conditionList = conditionRepository.findAll();
        assertThat(conditionList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(conditionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithIdMismatchCondition() throws Exception {
        int databaseSizeBeforeUpdate = conditionRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(conditionSearchRepository.findAll());
        condition.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restConditionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(condition))
            )
            .andExpect(status().isBadRequest());

        // Validate the Condition in the database
        List<Condition> conditionList = conditionRepository.findAll();
        assertThat(conditionList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(conditionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithMissingIdPathParamCondition() throws Exception {
        int databaseSizeBeforeUpdate = conditionRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(conditionSearchRepository.findAll());
        condition.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restConditionMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(condition))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Condition in the database
        List<Condition> conditionList = conditionRepository.findAll();
        assertThat(conditionList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(conditionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void deleteCondition() throws Exception {
        // Initialize the database
        conditionRepository.save(condition);
        conditionRepository.save(condition);
        conditionSearchRepository.save(condition);

        int databaseSizeBeforeDelete = conditionRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(conditionSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the condition
        restConditionMockMvc
            .perform(delete(ENTITY_API_URL_ID, condition.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Condition> conditionList = conditionRepository.findAll();
        assertThat(conditionList).hasSize(databaseSizeBeforeDelete - 1);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(conditionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    void searchCondition() throws Exception {
        // Initialize the database
        condition = conditionRepository.save(condition);
        conditionSearchRepository.save(condition);

        // Search the condition
        restConditionMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + condition.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(condition.getId())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].patientId").value(hasItem(DEFAULT_PATIENT_ID)))
            .andExpect(jsonPath("$.[*].createdDate").value(hasItem(DEFAULT_CREATED_DATE.toString())))
            .andExpect(jsonPath("$.[*].modifiedDate").value(hasItem(DEFAULT_MODIFIED_DATE.toString())))
            .andExpect(jsonPath("$.[*].createdBy").value(hasItem(DEFAULT_CREATED_BY)))
            .andExpect(jsonPath("$.[*].modifiedBy").value(hasItem(DEFAULT_MODIFIED_BY)));
    }
}
