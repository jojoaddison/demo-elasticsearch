package demo.jojoaddison.web.rest;

import demo.jojoaddison.domain.Report;
import demo.jojoaddison.repository.ReportRepository;
import demo.jojoaddison.repository.search.ReportSearchRepository;
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
 * REST controller for managing {@link demo.jojoaddison.domain.Report}.
 */
@RestController
@RequestMapping("/api/reports")
public class ReportResource {

    private final Logger log = LoggerFactory.getLogger(ReportResource.class);

    private static final String ENTITY_NAME = "patientMsReport";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ReportRepository reportRepository;

    private final ReportSearchRepository reportSearchRepository;

    public ReportResource(ReportRepository reportRepository, ReportSearchRepository reportSearchRepository) {
        this.reportRepository = reportRepository;
        this.reportSearchRepository = reportSearchRepository;
    }

    /**
     * {@code POST  /reports} : Create a new report.
     *
     * @param report the report to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new report, or with status {@code 400 (Bad Request)} if the report has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<Report> createReport(@RequestBody Report report) throws URISyntaxException {
        log.debug("REST request to save Report : {}", report);
        if (report.getId() != null) {
            throw new BadRequestAlertException("A new report cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Report result = reportRepository.save(report);
        reportSearchRepository.index(result);
        return ResponseEntity
            .created(new URI("/api/reports/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId()))
            .body(result);
    }

    /**
     * {@code PUT  /reports/:id} : Updates an existing report.
     *
     * @param id the id of the report to save.
     * @param report the report to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated report,
     * or with status {@code 400 (Bad Request)} if the report is not valid,
     * or with status {@code 500 (Internal Server Error)} if the report couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Report> updateReport(@PathVariable(value = "id", required = false) final String id, @RequestBody Report report)
        throws URISyntaxException {
        log.debug("REST request to update Report : {}, {}", id, report);
        if (report.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, report.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!reportRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Report result = reportRepository.save(report);
        reportSearchRepository.index(result);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, report.getId()))
            .body(result);
    }

    /**
     * {@code PATCH  /reports/:id} : Partial updates given fields of an existing report, field will ignore if it is null
     *
     * @param id the id of the report to save.
     * @param report the report to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated report,
     * or with status {@code 400 (Bad Request)} if the report is not valid,
     * or with status {@code 404 (Not Found)} if the report is not found,
     * or with status {@code 500 (Internal Server Error)} if the report couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Report> partialUpdateReport(
        @PathVariable(value = "id", required = false) final String id,
        @RequestBody Report report
    ) throws URISyntaxException {
        log.debug("REST request to partial update Report partially : {}, {}", id, report);
        if (report.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, report.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!reportRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Report> result = reportRepository
            .findById(report.getId())
            .map(existingReport -> {
                if (report.getCategory() != null) {
                    existingReport.setCategory(report.getCategory());
                }
                if (report.getDescription() != null) {
                    existingReport.setDescription(report.getDescription());
                }
                if (report.getName() != null) {
                    existingReport.setName(report.getName());
                }
                if (report.getUrl() != null) {
                    existingReport.setUrl(report.getUrl());
                }
                if (report.getPatientId() != null) {
                    existingReport.setPatientId(report.getPatientId());
                }
                if (report.getCreatedDate() != null) {
                    existingReport.setCreatedDate(report.getCreatedDate());
                }
                if (report.getModifiedDate() != null) {
                    existingReport.setModifiedDate(report.getModifiedDate());
                }
                if (report.getCreatedBy() != null) {
                    existingReport.setCreatedBy(report.getCreatedBy());
                }
                if (report.getModifiedBy() != null) {
                    existingReport.setModifiedBy(report.getModifiedBy());
                }

                return existingReport;
            })
            .map(reportRepository::save)
            .map(savedReport -> {
                reportSearchRepository.index(savedReport);
                return savedReport;
            });

        return ResponseUtil.wrapOrNotFound(result, HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, report.getId()));
    }

    /**
     * {@code GET  /reports} : get all the reports.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of reports in body.
     */
    @GetMapping("")
    public List<Report> getAllReports() {
        log.debug("REST request to get all Reports");
        return reportRepository.findAll();
    }

    /**
     * {@code GET  /reports/:id} : get the "id" report.
     *
     * @param id the id of the report to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the report, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Report> getReport(@PathVariable("id") String id) {
        log.debug("REST request to get Report : {}", id);
        Optional<Report> report = reportRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(report);
    }

    /**
     * {@code DELETE  /reports/:id} : delete the "id" report.
     *
     * @param id the id of the report to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable("id") String id) {
        log.debug("REST request to delete Report : {}", id);
        reportRepository.deleteById(id);
        reportSearchRepository.deleteFromIndexById(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id)).build();
    }

    /**
     * {@code SEARCH  /reports/_search?query=:query} : search for the report corresponding
     * to the query.
     *
     * @param query the query of the report search.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public List<Report> searchReports(@RequestParam("query") String query) {
        log.debug("REST request to search Reports for query {}", query);
        try {
            return StreamSupport.stream(reportSearchRepository.search(query).spliterator(), false).toList();
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }
}
