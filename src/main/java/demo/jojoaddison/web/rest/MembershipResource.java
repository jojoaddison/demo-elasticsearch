package demo.jojoaddison.web.rest;

import demo.jojoaddison.domain.Membership;
import demo.jojoaddison.repository.MembershipRepository;
import demo.jojoaddison.repository.search.MembershipSearchRepository;
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
 * REST controller for managing {@link demo.jojoaddison.domain.Membership}.
 */
@RestController
@RequestMapping("/api/memberships")
public class MembershipResource {

    private final Logger log = LoggerFactory.getLogger(MembershipResource.class);

    private static final String ENTITY_NAME = "patientMsMembership";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final MembershipRepository membershipRepository;

    private final MembershipSearchRepository membershipSearchRepository;

    public MembershipResource(MembershipRepository membershipRepository, MembershipSearchRepository membershipSearchRepository) {
        this.membershipRepository = membershipRepository;
        this.membershipSearchRepository = membershipSearchRepository;
    }

    /**
     * {@code POST  /memberships} : Create a new membership.
     *
     * @param membership the membership to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new membership, or with status {@code 400 (Bad Request)} if the membership has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<Membership> createMembership(@RequestBody Membership membership) throws URISyntaxException {
        log.debug("REST request to save Membership : {}", membership);
        if (membership.getId() != null) {
            throw new BadRequestAlertException("A new membership cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Membership result = membershipRepository.save(membership);
        membershipSearchRepository.index(result);
        return ResponseEntity
            .created(new URI("/api/memberships/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId()))
            .body(result);
    }

    /**
     * {@code PUT  /memberships/:id} : Updates an existing membership.
     *
     * @param id the id of the membership to save.
     * @param membership the membership to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated membership,
     * or with status {@code 400 (Bad Request)} if the membership is not valid,
     * or with status {@code 500 (Internal Server Error)} if the membership couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Membership> updateMembership(
        @PathVariable(value = "id", required = false) final String id,
        @RequestBody Membership membership
    ) throws URISyntaxException {
        log.debug("REST request to update Membership : {}, {}", id, membership);
        if (membership.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, membership.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!membershipRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Membership result = membershipRepository.save(membership);
        membershipSearchRepository.index(result);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, membership.getId()))
            .body(result);
    }

    /**
     * {@code PATCH  /memberships/:id} : Partial updates given fields of an existing membership, field will ignore if it is null
     *
     * @param id the id of the membership to save.
     * @param membership the membership to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated membership,
     * or with status {@code 400 (Bad Request)} if the membership is not valid,
     * or with status {@code 404 (Not Found)} if the membership is not found,
     * or with status {@code 500 (Internal Server Error)} if the membership couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Membership> partialUpdateMembership(
        @PathVariable(value = "id", required = false) final String id,
        @RequestBody Membership membership
    ) throws URISyntaxException {
        log.debug("REST request to partial update Membership partially : {}, {}", id, membership);
        if (membership.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, membership.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!membershipRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Membership> result = membershipRepository
            .findById(membership.getId())
            .map(existingMembership -> {
                if (membership.getName() != null) {
                    existingMembership.setName(membership.getName());
                }
                if (membership.getDescription() != null) {
                    existingMembership.setDescription(membership.getDescription());
                }
                if (membership.getStatus() != null) {
                    existingMembership.setStatus(membership.getStatus());
                }
                if (membership.getCreatedDate() != null) {
                    existingMembership.setCreatedDate(membership.getCreatedDate());
                }
                if (membership.getModifiedDate() != null) {
                    existingMembership.setModifiedDate(membership.getModifiedDate());
                }
                if (membership.getCreatedBy() != null) {
                    existingMembership.setCreatedBy(membership.getCreatedBy());
                }
                if (membership.getModifiedBy() != null) {
                    existingMembership.setModifiedBy(membership.getModifiedBy());
                }

                return existingMembership;
            })
            .map(membershipRepository::save)
            .map(savedMembership -> {
                membershipSearchRepository.index(savedMembership);
                return savedMembership;
            });

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, membership.getId())
        );
    }

    /**
     * {@code GET  /memberships} : get all the memberships.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of memberships in body.
     */
    @GetMapping("")
    public List<Membership> getAllMemberships() {
        log.debug("REST request to get all Memberships");
        return membershipRepository.findAll();
    }

    /**
     * {@code GET  /memberships/:id} : get the "id" membership.
     *
     * @param id the id of the membership to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the membership, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Membership> getMembership(@PathVariable("id") String id) {
        log.debug("REST request to get Membership : {}", id);
        Optional<Membership> membership = membershipRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(membership);
    }

    /**
     * {@code DELETE  /memberships/:id} : delete the "id" membership.
     *
     * @param id the id of the membership to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMembership(@PathVariable("id") String id) {
        log.debug("REST request to delete Membership : {}", id);
        membershipRepository.deleteById(id);
        membershipSearchRepository.deleteFromIndexById(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id)).build();
    }

    /**
     * {@code SEARCH  /memberships/_search?query=:query} : search for the membership corresponding
     * to the query.
     *
     * @param query the query of the membership search.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public List<Membership> searchMemberships(@RequestParam("query") String query) {
        log.debug("REST request to search Memberships for query {}", query);
        try {
            return StreamSupport.stream(membershipSearchRepository.search(query).spliterator(), false).toList();
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }
}
