package demo.jojoaddison.web.rest;

import demo.jojoaddison.domain.Task;
import demo.jojoaddison.repository.TaskRepository;
import demo.jojoaddison.repository.search.TaskSearchRepository;
import demo.jojoaddison.web.rest.errors.BadRequestAlertException;
import demo.jojoaddison.web.rest.errors.ElasticsearchExceptionMapper;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link demo.jojoaddison.domain.Task}.
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskResource {

    private final Logger log = LoggerFactory.getLogger(TaskResource.class);

    private static final String ENTITY_NAME = "patientMsTask";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final TaskRepository taskRepository;

    private final TaskSearchRepository taskSearchRepository;

    public TaskResource(TaskRepository taskRepository, TaskSearchRepository taskSearchRepository) {
        this.taskRepository = taskRepository;
        this.taskSearchRepository = taskSearchRepository;
    }

    /**
     * {@code POST  /tasks} : Create a new task.
     *
     * @param task the task to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new task, or with status {@code 400 (Bad Request)} if the task has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<Task> createTask(@RequestBody Task task) throws URISyntaxException {
        log.debug("REST request to save Task : {}", task);
        if (task.getId() != null) {
            throw new BadRequestAlertException("A new task cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Task result = taskRepository.save(task);
        taskSearchRepository.index(result);
        return ResponseEntity
            .created(new URI("/api/tasks/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId()))
            .body(result);
    }

    /**
     * {@code PUT  /tasks/:id} : Updates an existing task.
     *
     * @param id the id of the task to save.
     * @param task the task to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated task,
     * or with status {@code 400 (Bad Request)} if the task is not valid,
     * or with status {@code 500 (Internal Server Error)} if the task couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable(value = "id", required = false) final String id, @RequestBody Task task)
        throws URISyntaxException {
        log.debug("REST request to update Task : {}, {}", id, task);
        if (task.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, task.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!taskRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Task result = taskRepository.save(task);
        taskSearchRepository.index(result);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, task.getId()))
            .body(result);
    }

    /**
     * {@code PATCH  /tasks/:id} : Partial updates given fields of an existing task, field will ignore if it is null
     *
     * @param id the id of the task to save.
     * @param task the task to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated task,
     * or with status {@code 400 (Bad Request)} if the task is not valid,
     * or with status {@code 404 (Not Found)} if the task is not found,
     * or with status {@code 500 (Internal Server Error)} if the task couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Task> partialUpdateTask(@PathVariable(value = "id", required = false) final String id, @RequestBody Task task)
        throws URISyntaxException {
        log.debug("REST request to partial update Task partially : {}, {}", id, task);
        if (task.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, task.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!taskRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Task> result = taskRepository
            .findById(task.getId())
            .map(existingTask -> {
                if (task.getName() != null) {
                    existingTask.setName(task.getName());
                }
                if (task.getDescription() != null) {
                    existingTask.setDescription(task.getDescription());
                }
                if (task.getSchedule() != null) {
                    existingTask.setSchedule(task.getSchedule());
                }
                if (task.getDuration() != null) {
                    existingTask.setDuration(task.getDuration());
                }
                if (task.getAttendantId() != null) {
                    existingTask.setAttendantId(task.getAttendantId());
                }
                if (task.getTeamId() != null) {
                    existingTask.setTeamId(task.getTeamId());
                }
                if (task.getPatientId() != null) {
                    existingTask.setPatientId(task.getPatientId());
                }
                if (task.getAttendant() != null) {
                    existingTask.setAttendant(task.getAttendant());
                }
                if (task.getCreatedDate() != null) {
                    existingTask.setCreatedDate(task.getCreatedDate());
                }
                if (task.getModifiedDate() != null) {
                    existingTask.setModifiedDate(task.getModifiedDate());
                }
                if (task.getCreatedBy() != null) {
                    existingTask.setCreatedBy(task.getCreatedBy());
                }
                if (task.getModifiedBy() != null) {
                    existingTask.setModifiedBy(task.getModifiedBy());
                }

                return existingTask;
            })
            .map(taskRepository::save)
            .map(savedTask -> {
                taskSearchRepository.index(savedTask);
                return savedTask;
            });

        return ResponseUtil.wrapOrNotFound(result, HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, task.getId()));
    }

    /**
     * {@code GET  /tasks} : get all the tasks.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of tasks in body.
     */
    @GetMapping("")
    public List<Task> getAllTasks() {
        log.debug("REST request to get all Tasks");
        return taskRepository.findAll();
    }

    /**
     * {@code GET  /tasks/:id} : get the "id" task.
     *
     * @param id the id of the task to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the task, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTask(@PathVariable("id") String id) {
        log.debug("REST request to get Task : {}", id);
        Optional<Task> task = taskRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(task);
    }

    /**
     * {@code DELETE  /tasks/:id} : delete the "id" task.
     *
     * @param id the id of the task to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable("id") String id) {
        log.debug("REST request to delete Task : {}", id);
        taskRepository.deleteById(id);
        taskSearchRepository.deleteFromIndexById(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id)).build();
    }

    /**
     * {@code SEARCH  /tasks/_search?query=:query} : search for the task corresponding
     * to the query.
     *
     * @param query the query of the task search.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public List<Task> searchTasks(@RequestParam("query") String query) {
        log.debug("REST request to search Tasks for query {}", query);
        try {
            return StreamSupport.stream(taskSearchRepository.search(query).spliterator(), false).toList();
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }
}
