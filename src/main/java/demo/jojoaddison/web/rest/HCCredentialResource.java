package demo.jojoaddison.web.rest;

import demo.jojoaddison.domain.HCCredential;
import demo.jojoaddison.repository.HCCredentialRepository;
import demo.jojoaddison.repository.search.HCCredentialSearchRepository;
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
 * REST controller for managing {@link demo.jojoaddison.domain.HCCredential}.
 */
@RestController
@RequestMapping("/api/hc-credentials")
public class HCCredentialResource {

    private final Logger log = LoggerFactory.getLogger(HCCredentialResource.class);

    private static final String ENTITY_NAME = "patientMshcCredential";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final HCCredentialRepository hCCredentialRepository;

    private final HCCredentialSearchRepository hCCredentialSearchRepository;

    public HCCredentialResource(HCCredentialRepository hCCredentialRepository, HCCredentialSearchRepository hCCredentialSearchRepository) {
        this.hCCredentialRepository = hCCredentialRepository;
        this.hCCredentialSearchRepository = hCCredentialSearchRepository;
    }

    /**
     * {@code POST  /hc-credentials} : Create a new hCCredential.
     *
     * @param hCCredential the hCCredential to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new hCCredential, or with status {@code 400 (Bad Request)} if the hCCredential has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<HCCredential> createHCCredential(@RequestBody HCCredential hCCredential) throws URISyntaxException {
        log.debug("REST request to save HCCredential : {}", hCCredential);
        if (hCCredential.getId() != null) {
            throw new BadRequestAlertException("A new hCCredential cannot already have an ID", ENTITY_NAME, "idexists");
        }
        HCCredential result = hCCredentialRepository.save(hCCredential);
        hCCredentialSearchRepository.index(result);
        return ResponseEntity
            .created(new URI("/api/hc-credentials/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId()))
            .body(result);
    }

    /**
     * {@code PUT  /hc-credentials/:id} : Updates an existing hCCredential.
     *
     * @param id the id of the hCCredential to save.
     * @param hCCredential the hCCredential to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated hCCredential,
     * or with status {@code 400 (Bad Request)} if the hCCredential is not valid,
     * or with status {@code 500 (Internal Server Error)} if the hCCredential couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<HCCredential> updateHCCredential(
        @PathVariable(value = "id", required = false) final String id,
        @RequestBody HCCredential hCCredential
    ) throws URISyntaxException {
        log.debug("REST request to update HCCredential : {}, {}", id, hCCredential);
        if (hCCredential.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, hCCredential.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!hCCredentialRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        HCCredential result = hCCredentialRepository.save(hCCredential);
        hCCredentialSearchRepository.index(result);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, hCCredential.getId()))
            .body(result);
    }

    /**
     * {@code PATCH  /hc-credentials/:id} : Partial updates given fields of an existing hCCredential, field will ignore if it is null
     *
     * @param id the id of the hCCredential to save.
     * @param hCCredential the hCCredential to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated hCCredential,
     * or with status {@code 400 (Bad Request)} if the hCCredential is not valid,
     * or with status {@code 404 (Not Found)} if the hCCredential is not found,
     * or with status {@code 500 (Internal Server Error)} if the hCCredential couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<HCCredential> partialUpdateHCCredential(
        @PathVariable(value = "id", required = false) final String id,
        @RequestBody HCCredential hCCredential
    ) throws URISyntaxException {
        log.debug("REST request to partial update HCCredential partially : {}, {}", id, hCCredential);
        if (hCCredential.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, hCCredential.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!hCCredentialRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<HCCredential> result = hCCredentialRepository
            .findById(hCCredential.getId())
            .map(existingHCCredential -> {
                if (hCCredential.getEmail() != null) {
                    existingHCCredential.setEmail(hCCredential.getEmail());
                }
                if (hCCredential.getPhoneNumber() != null) {
                    existingHCCredential.setPhoneNumber(hCCredential.getPhoneNumber());
                }
                if (hCCredential.getPassword() != null) {
                    existingHCCredential.setPassword(hCCredential.getPassword());
                }
                if (hCCredential.getRole() != null) {
                    existingHCCredential.setRole(hCCredential.getRole());
                }
                if (hCCredential.getCreatedDate() != null) {
                    existingHCCredential.setCreatedDate(hCCredential.getCreatedDate());
                }
                if (hCCredential.getActive() != null) {
                    existingHCCredential.setActive(hCCredential.getActive());
                }
                if (hCCredential.getModifiedDate() != null) {
                    existingHCCredential.setModifiedDate(hCCredential.getModifiedDate());
                }

                return existingHCCredential;
            })
            .map(hCCredentialRepository::save)
            .map(savedHCCredential -> {
                hCCredentialSearchRepository.index(savedHCCredential);
                return savedHCCredential;
            });

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, hCCredential.getId())
        );
    }

    /**
     * {@code GET  /hc-credentials} : get all the hCCredentials.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of hCCredentials in body.
     */
    @GetMapping("")
    public List<HCCredential> getAllHCCredentials() {
        log.debug("REST request to get all HCCredentials");
        return hCCredentialRepository.findAll();
    }

    /**
     * {@code GET  /hc-credentials/:id} : get the "id" hCCredential.
     *
     * @param id the id of the hCCredential to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the hCCredential, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<HCCredential> getHCCredential(@PathVariable("id") String id) {
        log.debug("REST request to get HCCredential : {}", id);
        Optional<HCCredential> hCCredential = hCCredentialRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(hCCredential);
    }

    /**
     * {@code DELETE  /hc-credentials/:id} : delete the "id" hCCredential.
     *
     * @param id the id of the hCCredential to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHCCredential(@PathVariable("id") String id) {
        log.debug("REST request to delete HCCredential : {}", id);
        hCCredentialRepository.deleteById(id);
        hCCredentialSearchRepository.deleteFromIndexById(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id)).build();
    }

    /**
     * {@code SEARCH  /hc-credentials/_search?query=:query} : search for the hCCredential corresponding
     * to the query.
     *
     * @param query the query of the hCCredential search.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public List<HCCredential> searchHCCredentials(@RequestParam("query") String query) {
        log.debug("REST request to search HCCredentials for query {}", query);
        try {
            return StreamSupport.stream(hCCredentialSearchRepository.search(query).spliterator(), false).toList();
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }
}
