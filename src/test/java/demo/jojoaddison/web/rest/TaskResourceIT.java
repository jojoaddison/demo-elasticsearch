package demo.jojoaddison.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import demo.jojoaddison.IntegrationTest;
import demo.jojoaddison.domain.Task;
import demo.jojoaddison.repository.TaskRepository;
import demo.jojoaddison.repository.search.TaskSearchRepository;
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
 * Integration tests for the {@link TaskResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class TaskResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_SCHEDULE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_SCHEDULE = LocalDate.now(ZoneId.systemDefault());

    private static final Double DEFAULT_DURATION = 1D;
    private static final Double UPDATED_DURATION = 2D;

    private static final String DEFAULT_ATTENDANT_ID = "AAAAAAAAAA";
    private static final String UPDATED_ATTENDANT_ID = "BBBBBBBBBB";

    private static final String DEFAULT_TEAM_ID = "AAAAAAAAAA";
    private static final String UPDATED_TEAM_ID = "BBBBBBBBBB";

    private static final String DEFAULT_PATIENT_ID = "AAAAAAAAAA";
    private static final String UPDATED_PATIENT_ID = "BBBBBBBBBB";

    private static final String DEFAULT_ATTENDANT = "AAAAAAAAAA";
    private static final String UPDATED_ATTENDANT = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_CREATED_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_CREATED_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final LocalDate DEFAULT_MODIFIED_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_MODIFIED_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final String DEFAULT_CREATED_BY = "AAAAAAAAAA";
    private static final String UPDATED_CREATED_BY = "BBBBBBBBBB";

    private static final String DEFAULT_MODIFIED_BY = "AAAAAAAAAA";
    private static final String UPDATED_MODIFIED_BY = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/tasks";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/tasks/_search";

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskSearchRepository taskSearchRepository;

    @Autowired
    private MockMvc restTaskMockMvc;

    private Task task;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Task createEntity() {
        Task task = new Task()
            .name(DEFAULT_NAME)
            .description(DEFAULT_DESCRIPTION)
            .schedule(DEFAULT_SCHEDULE)
            .duration(DEFAULT_DURATION)
            .attendantId(DEFAULT_ATTENDANT_ID)
            .teamId(DEFAULT_TEAM_ID)
            .patientId(DEFAULT_PATIENT_ID)
            .attendant(DEFAULT_ATTENDANT)
            .createdDate(DEFAULT_CREATED_DATE)
            .modifiedDate(DEFAULT_MODIFIED_DATE)
            .createdBy(DEFAULT_CREATED_BY)
            .modifiedBy(DEFAULT_MODIFIED_BY);
        return task;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Task createUpdatedEntity() {
        Task task = new Task()
            .name(UPDATED_NAME)
            .description(UPDATED_DESCRIPTION)
            .schedule(UPDATED_SCHEDULE)
            .duration(UPDATED_DURATION)
            .attendantId(UPDATED_ATTENDANT_ID)
            .teamId(UPDATED_TEAM_ID)
            .patientId(UPDATED_PATIENT_ID)
            .attendant(UPDATED_ATTENDANT)
            .createdDate(UPDATED_CREATED_DATE)
            .modifiedDate(UPDATED_MODIFIED_DATE)
            .createdBy(UPDATED_CREATED_BY)
            .modifiedBy(UPDATED_MODIFIED_BY);
        return task;
    }

    @AfterEach
    public void cleanupElasticSearchRepository() {
        taskSearchRepository.deleteAll();
        assertThat(taskSearchRepository.count()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        taskRepository.deleteAll();
        task = createEntity();
    }

    @Test
    void createTask() throws Exception {
        int databaseSizeBeforeCreate = taskRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(taskSearchRepository.findAll());
        // Create the Task
        restTaskMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(task)))
            .andExpect(status().isCreated());

        // Validate the Task in the database
        List<Task> taskList = taskRepository.findAll();
        assertThat(taskList).hasSize(databaseSizeBeforeCreate + 1);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(taskSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
        Task testTask = taskList.get(taskList.size() - 1);
        assertThat(testTask.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testTask.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testTask.getSchedule()).isEqualTo(DEFAULT_SCHEDULE);
        assertThat(testTask.getDuration()).isEqualTo(DEFAULT_DURATION);
        assertThat(testTask.getAttendantId()).isEqualTo(DEFAULT_ATTENDANT_ID);
        assertThat(testTask.getTeamId()).isEqualTo(DEFAULT_TEAM_ID);
        assertThat(testTask.getPatientId()).isEqualTo(DEFAULT_PATIENT_ID);
        assertThat(testTask.getAttendant()).isEqualTo(DEFAULT_ATTENDANT);
        assertThat(testTask.getCreatedDate()).isEqualTo(DEFAULT_CREATED_DATE);
        assertThat(testTask.getModifiedDate()).isEqualTo(DEFAULT_MODIFIED_DATE);
        assertThat(testTask.getCreatedBy()).isEqualTo(DEFAULT_CREATED_BY);
        assertThat(testTask.getModifiedBy()).isEqualTo(DEFAULT_MODIFIED_BY);
    }

    @Test
    void createTaskWithExistingId() throws Exception {
        // Create the Task with an existing ID
        task.setId("existing_id");

        int databaseSizeBeforeCreate = taskRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(taskSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restTaskMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(task)))
            .andExpect(status().isBadRequest());

        // Validate the Task in the database
        List<Task> taskList = taskRepository.findAll();
        assertThat(taskList).hasSize(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(taskSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void getAllTasks() throws Exception {
        // Initialize the database
        taskRepository.save(task);

        // Get all the taskList
        restTaskMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(task.getId())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].schedule").value(hasItem(DEFAULT_SCHEDULE.toString())))
            .andExpect(jsonPath("$.[*].duration").value(hasItem(DEFAULT_DURATION.doubleValue())))
            .andExpect(jsonPath("$.[*].attendantId").value(hasItem(DEFAULT_ATTENDANT_ID)))
            .andExpect(jsonPath("$.[*].teamId").value(hasItem(DEFAULT_TEAM_ID)))
            .andExpect(jsonPath("$.[*].patientId").value(hasItem(DEFAULT_PATIENT_ID)))
            .andExpect(jsonPath("$.[*].attendant").value(hasItem(DEFAULT_ATTENDANT)))
            .andExpect(jsonPath("$.[*].createdDate").value(hasItem(DEFAULT_CREATED_DATE.toString())))
            .andExpect(jsonPath("$.[*].modifiedDate").value(hasItem(DEFAULT_MODIFIED_DATE.toString())))
            .andExpect(jsonPath("$.[*].createdBy").value(hasItem(DEFAULT_CREATED_BY)))
            .andExpect(jsonPath("$.[*].modifiedBy").value(hasItem(DEFAULT_MODIFIED_BY)));
    }

    @Test
    void getTask() throws Exception {
        // Initialize the database
        taskRepository.save(task);

        // Get the task
        restTaskMockMvc
            .perform(get(ENTITY_API_URL_ID, task.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(task.getId()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.schedule").value(DEFAULT_SCHEDULE.toString()))
            .andExpect(jsonPath("$.duration").value(DEFAULT_DURATION.doubleValue()))
            .andExpect(jsonPath("$.attendantId").value(DEFAULT_ATTENDANT_ID))
            .andExpect(jsonPath("$.teamId").value(DEFAULT_TEAM_ID))
            .andExpect(jsonPath("$.patientId").value(DEFAULT_PATIENT_ID))
            .andExpect(jsonPath("$.attendant").value(DEFAULT_ATTENDANT))
            .andExpect(jsonPath("$.createdDate").value(DEFAULT_CREATED_DATE.toString()))
            .andExpect(jsonPath("$.modifiedDate").value(DEFAULT_MODIFIED_DATE.toString()))
            .andExpect(jsonPath("$.createdBy").value(DEFAULT_CREATED_BY))
            .andExpect(jsonPath("$.modifiedBy").value(DEFAULT_MODIFIED_BY));
    }

    @Test
    void getNonExistingTask() throws Exception {
        // Get the task
        restTaskMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    void putExistingTask() throws Exception {
        // Initialize the database
        taskRepository.save(task);

        int databaseSizeBeforeUpdate = taskRepository.findAll().size();
        taskSearchRepository.save(task);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(taskSearchRepository.findAll());

        // Update the task
        Task updatedTask = taskRepository.findById(task.getId()).orElseThrow();
        updatedTask
            .name(UPDATED_NAME)
            .description(UPDATED_DESCRIPTION)
            .schedule(UPDATED_SCHEDULE)
            .duration(UPDATED_DURATION)
            .attendantId(UPDATED_ATTENDANT_ID)
            .teamId(UPDATED_TEAM_ID)
            .patientId(UPDATED_PATIENT_ID)
            .attendant(UPDATED_ATTENDANT)
            .createdDate(UPDATED_CREATED_DATE)
            .modifiedDate(UPDATED_MODIFIED_DATE)
            .createdBy(UPDATED_CREATED_BY)
            .modifiedBy(UPDATED_MODIFIED_BY);

        restTaskMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedTask.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedTask))
            )
            .andExpect(status().isOk());

        // Validate the Task in the database
        List<Task> taskList = taskRepository.findAll();
        assertThat(taskList).hasSize(databaseSizeBeforeUpdate);
        Task testTask = taskList.get(taskList.size() - 1);
        assertThat(testTask.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testTask.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testTask.getSchedule()).isEqualTo(UPDATED_SCHEDULE);
        assertThat(testTask.getDuration()).isEqualTo(UPDATED_DURATION);
        assertThat(testTask.getAttendantId()).isEqualTo(UPDATED_ATTENDANT_ID);
        assertThat(testTask.getTeamId()).isEqualTo(UPDATED_TEAM_ID);
        assertThat(testTask.getPatientId()).isEqualTo(UPDATED_PATIENT_ID);
        assertThat(testTask.getAttendant()).isEqualTo(UPDATED_ATTENDANT);
        assertThat(testTask.getCreatedDate()).isEqualTo(UPDATED_CREATED_DATE);
        assertThat(testTask.getModifiedDate()).isEqualTo(UPDATED_MODIFIED_DATE);
        assertThat(testTask.getCreatedBy()).isEqualTo(UPDATED_CREATED_BY);
        assertThat(testTask.getModifiedBy()).isEqualTo(UPDATED_MODIFIED_BY);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(taskSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<Task> taskSearchList = IterableUtils.toList(taskSearchRepository.findAll());
                Task testTaskSearch = taskSearchList.get(searchDatabaseSizeAfter - 1);
                assertThat(testTaskSearch.getName()).isEqualTo(UPDATED_NAME);
                assertThat(testTaskSearch.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
                assertThat(testTaskSearch.getSchedule()).isEqualTo(UPDATED_SCHEDULE);
                assertThat(testTaskSearch.getDuration()).isEqualTo(UPDATED_DURATION);
                assertThat(testTaskSearch.getAttendantId()).isEqualTo(UPDATED_ATTENDANT_ID);
                assertThat(testTaskSearch.getTeamId()).isEqualTo(UPDATED_TEAM_ID);
                assertThat(testTaskSearch.getPatientId()).isEqualTo(UPDATED_PATIENT_ID);
                assertThat(testTaskSearch.getAttendant()).isEqualTo(UPDATED_ATTENDANT);
                assertThat(testTaskSearch.getCreatedDate()).isEqualTo(UPDATED_CREATED_DATE);
                assertThat(testTaskSearch.getModifiedDate()).isEqualTo(UPDATED_MODIFIED_DATE);
                assertThat(testTaskSearch.getCreatedBy()).isEqualTo(UPDATED_CREATED_BY);
                assertThat(testTaskSearch.getModifiedBy()).isEqualTo(UPDATED_MODIFIED_BY);
            });
    }

    @Test
    void putNonExistingTask() throws Exception {
        int databaseSizeBeforeUpdate = taskRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(taskSearchRepository.findAll());
        task.setId(UUID.randomUUID().toString());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTaskMockMvc
            .perform(
                put(ENTITY_API_URL_ID, task.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(task))
            )
            .andExpect(status().isBadRequest());

        // Validate the Task in the database
        List<Task> taskList = taskRepository.findAll();
        assertThat(taskList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(taskSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithIdMismatchTask() throws Exception {
        int databaseSizeBeforeUpdate = taskRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(taskSearchRepository.findAll());
        task.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTaskMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(task))
            )
            .andExpect(status().isBadRequest());

        // Validate the Task in the database
        List<Task> taskList = taskRepository.findAll();
        assertThat(taskList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(taskSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithMissingIdPathParamTask() throws Exception {
        int databaseSizeBeforeUpdate = taskRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(taskSearchRepository.findAll());
        task.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTaskMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(task)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Task in the database
        List<Task> taskList = taskRepository.findAll();
        assertThat(taskList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(taskSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void partialUpdateTaskWithPatch() throws Exception {
        // Initialize the database
        taskRepository.save(task);

        int databaseSizeBeforeUpdate = taskRepository.findAll().size();

        // Update the task using partial update
        Task partialUpdatedTask = new Task();
        partialUpdatedTask.setId(task.getId());

        partialUpdatedTask
            .schedule(UPDATED_SCHEDULE)
            .teamId(UPDATED_TEAM_ID)
            .patientId(UPDATED_PATIENT_ID)
            .attendant(UPDATED_ATTENDANT)
            .modifiedDate(UPDATED_MODIFIED_DATE);

        restTaskMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTask.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedTask))
            )
            .andExpect(status().isOk());

        // Validate the Task in the database
        List<Task> taskList = taskRepository.findAll();
        assertThat(taskList).hasSize(databaseSizeBeforeUpdate);
        Task testTask = taskList.get(taskList.size() - 1);
        assertThat(testTask.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testTask.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testTask.getSchedule()).isEqualTo(UPDATED_SCHEDULE);
        assertThat(testTask.getDuration()).isEqualTo(DEFAULT_DURATION);
        assertThat(testTask.getAttendantId()).isEqualTo(DEFAULT_ATTENDANT_ID);
        assertThat(testTask.getTeamId()).isEqualTo(UPDATED_TEAM_ID);
        assertThat(testTask.getPatientId()).isEqualTo(UPDATED_PATIENT_ID);
        assertThat(testTask.getAttendant()).isEqualTo(UPDATED_ATTENDANT);
        assertThat(testTask.getCreatedDate()).isEqualTo(DEFAULT_CREATED_DATE);
        assertThat(testTask.getModifiedDate()).isEqualTo(UPDATED_MODIFIED_DATE);
        assertThat(testTask.getCreatedBy()).isEqualTo(DEFAULT_CREATED_BY);
        assertThat(testTask.getModifiedBy()).isEqualTo(DEFAULT_MODIFIED_BY);
    }

    @Test
    void fullUpdateTaskWithPatch() throws Exception {
        // Initialize the database
        taskRepository.save(task);

        int databaseSizeBeforeUpdate = taskRepository.findAll().size();

        // Update the task using partial update
        Task partialUpdatedTask = new Task();
        partialUpdatedTask.setId(task.getId());

        partialUpdatedTask
            .name(UPDATED_NAME)
            .description(UPDATED_DESCRIPTION)
            .schedule(UPDATED_SCHEDULE)
            .duration(UPDATED_DURATION)
            .attendantId(UPDATED_ATTENDANT_ID)
            .teamId(UPDATED_TEAM_ID)
            .patientId(UPDATED_PATIENT_ID)
            .attendant(UPDATED_ATTENDANT)
            .createdDate(UPDATED_CREATED_DATE)
            .modifiedDate(UPDATED_MODIFIED_DATE)
            .createdBy(UPDATED_CREATED_BY)
            .modifiedBy(UPDATED_MODIFIED_BY);

        restTaskMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTask.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedTask))
            )
            .andExpect(status().isOk());

        // Validate the Task in the database
        List<Task> taskList = taskRepository.findAll();
        assertThat(taskList).hasSize(databaseSizeBeforeUpdate);
        Task testTask = taskList.get(taskList.size() - 1);
        assertThat(testTask.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testTask.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testTask.getSchedule()).isEqualTo(UPDATED_SCHEDULE);
        assertThat(testTask.getDuration()).isEqualTo(UPDATED_DURATION);
        assertThat(testTask.getAttendantId()).isEqualTo(UPDATED_ATTENDANT_ID);
        assertThat(testTask.getTeamId()).isEqualTo(UPDATED_TEAM_ID);
        assertThat(testTask.getPatientId()).isEqualTo(UPDATED_PATIENT_ID);
        assertThat(testTask.getAttendant()).isEqualTo(UPDATED_ATTENDANT);
        assertThat(testTask.getCreatedDate()).isEqualTo(UPDATED_CREATED_DATE);
        assertThat(testTask.getModifiedDate()).isEqualTo(UPDATED_MODIFIED_DATE);
        assertThat(testTask.getCreatedBy()).isEqualTo(UPDATED_CREATED_BY);
        assertThat(testTask.getModifiedBy()).isEqualTo(UPDATED_MODIFIED_BY);
    }

    @Test
    void patchNonExistingTask() throws Exception {
        int databaseSizeBeforeUpdate = taskRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(taskSearchRepository.findAll());
        task.setId(UUID.randomUUID().toString());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTaskMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, task.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(task))
            )
            .andExpect(status().isBadRequest());

        // Validate the Task in the database
        List<Task> taskList = taskRepository.findAll();
        assertThat(taskList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(taskSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithIdMismatchTask() throws Exception {
        int databaseSizeBeforeUpdate = taskRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(taskSearchRepository.findAll());
        task.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTaskMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(task))
            )
            .andExpect(status().isBadRequest());

        // Validate the Task in the database
        List<Task> taskList = taskRepository.findAll();
        assertThat(taskList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(taskSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithMissingIdPathParamTask() throws Exception {
        int databaseSizeBeforeUpdate = taskRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(taskSearchRepository.findAll());
        task.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTaskMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(task)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Task in the database
        List<Task> taskList = taskRepository.findAll();
        assertThat(taskList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(taskSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void deleteTask() throws Exception {
        // Initialize the database
        taskRepository.save(task);
        taskRepository.save(task);
        taskSearchRepository.save(task);

        int databaseSizeBeforeDelete = taskRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(taskSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the task
        restTaskMockMvc
            .perform(delete(ENTITY_API_URL_ID, task.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Task> taskList = taskRepository.findAll();
        assertThat(taskList).hasSize(databaseSizeBeforeDelete - 1);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(taskSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    void searchTask() throws Exception {
        // Initialize the database
        task = taskRepository.save(task);
        taskSearchRepository.save(task);

        // Search the task
        restTaskMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + task.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(task.getId())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].schedule").value(hasItem(DEFAULT_SCHEDULE.toString())))
            .andExpect(jsonPath("$.[*].duration").value(hasItem(DEFAULT_DURATION.doubleValue())))
            .andExpect(jsonPath("$.[*].attendantId").value(hasItem(DEFAULT_ATTENDANT_ID)))
            .andExpect(jsonPath("$.[*].teamId").value(hasItem(DEFAULT_TEAM_ID)))
            .andExpect(jsonPath("$.[*].patientId").value(hasItem(DEFAULT_PATIENT_ID)))
            .andExpect(jsonPath("$.[*].attendant").value(hasItem(DEFAULT_ATTENDANT)))
            .andExpect(jsonPath("$.[*].createdDate").value(hasItem(DEFAULT_CREATED_DATE.toString())))
            .andExpect(jsonPath("$.[*].modifiedDate").value(hasItem(DEFAULT_MODIFIED_DATE.toString())))
            .andExpect(jsonPath("$.[*].createdBy").value(hasItem(DEFAULT_CREATED_BY)))
            .andExpect(jsonPath("$.[*].modifiedBy").value(hasItem(DEFAULT_MODIFIED_BY)));
    }
}
