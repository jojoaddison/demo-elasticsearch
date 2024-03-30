package demo.jojoaddison.web.rest;

import demo.jojoaddison.domain.Address;
import demo.jojoaddison.repository.AddressRepository;
import demo.jojoaddison.repository.search.AddressSearchRepository;
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
 * REST controller for managing {@link demo.jojoaddison.domain.Address}.
 */
@RestController
@RequestMapping("/api/addresses")
public class AddressResource {

    private final Logger log = LoggerFactory.getLogger(AddressResource.class);

    private static final String ENTITY_NAME = "demoSearchAddress";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AddressRepository addressRepository;

    private final AddressSearchRepository addressSearchRepository;

    public AddressResource(AddressRepository addressRepository, AddressSearchRepository addressSearchRepository) {
        this.addressRepository = addressRepository;
        this.addressSearchRepository = addressSearchRepository;
    }

    /**
     * {@code POST  /addresses} : Create a new address.
     *
     * @param address the address to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new address, or with status {@code 400 (Bad Request)} if the address has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<Address> createAddress(@RequestBody Address address) throws URISyntaxException {
        log.debug("REST request to save Address : {}", address);
        if (address.getId() != null) {
            throw new BadRequestAlertException("A new address cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Address result = addressRepository.save(address);
        addressSearchRepository.index(result);
        return ResponseEntity
            .created(new URI("/api/addresses/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId()))
            .body(result);
    }

    /**
     * {@code PUT  /addresses/:id} : Updates an existing address.
     *
     * @param id the id of the address to save.
     * @param address the address to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated address,
     * or with status {@code 400 (Bad Request)} if the address is not valid,
     * or with status {@code 500 (Internal Server Error)} if the address couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Address> updateAddress(
        @PathVariable(value = "id", required = false) final String id,
        @RequestBody Address address
    ) throws URISyntaxException {
        log.debug("REST request to update Address : {}, {}", id, address);
        if (address.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, address.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!addressRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Address result = addressRepository.save(address);
        addressSearchRepository.index(result);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, address.getId()))
            .body(result);
    }

    /**
     * {@code PATCH  /addresses/:id} : Partial updates given fields of an existing address, field will ignore if it is null
     *
     * @param id the id of the address to save.
     * @param address the address to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated address,
     * or with status {@code 400 (Bad Request)} if the address is not valid,
     * or with status {@code 404 (Not Found)} if the address is not found,
     * or with status {@code 500 (Internal Server Error)} if the address couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Address> partialUpdateAddress(
        @PathVariable(value = "id", required = false) final String id,
        @RequestBody Address address
    ) throws URISyntaxException {
        log.debug("REST request to partial update Address partially : {}, {}", id, address);
        if (address.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, address.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!addressRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Address> result = addressRepository
            .findById(address.getId())
            .map(existingAddress -> {
                if (address.getDigitalAddress() != null) {
                    existingAddress.setDigitalAddress(address.getDigitalAddress());
                }
                if (address.getStreetAddress() != null) {
                    existingAddress.setStreetAddress(address.getStreetAddress());
                }
                if (address.getAreaCode() != null) {
                    existingAddress.setAreaCode(address.getAreaCode());
                }
                if (address.getTown() != null) {
                    existingAddress.setTown(address.getTown());
                }
                if (address.getCity() != null) {
                    existingAddress.setCity(address.getCity());
                }
                if (address.getDistrict() != null) {
                    existingAddress.setDistrict(address.getDistrict());
                }
                if (address.getState() != null) {
                    existingAddress.setState(address.getState());
                }
                if (address.getRegion() != null) {
                    existingAddress.setRegion(address.getRegion());
                }
                if (address.getCountry() != null) {
                    existingAddress.setCountry(address.getCountry());
                }
                if (address.getCreatedDate() != null) {
                    existingAddress.setCreatedDate(address.getCreatedDate());
                }
                if (address.getModifiedDate() != null) {
                    existingAddress.setModifiedDate(address.getModifiedDate());
                }
                if (address.getCreatedBy() != null) {
                    existingAddress.setCreatedBy(address.getCreatedBy());
                }
                if (address.getModifiedBy() != null) {
                    existingAddress.setModifiedBy(address.getModifiedBy());
                }

                return existingAddress;
            })
            .map(addressRepository::save)
            .map(savedAddress -> {
                addressSearchRepository.index(savedAddress);
                return savedAddress;
            });

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, address.getId())
        );
    }

    /**
     * {@code GET  /addresses} : get all the addresses.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of addresses in body.
     */
    @GetMapping("")
    public List<Address> getAllAddresses() {
        log.debug("REST request to get all Addresses");
        return addressRepository.findAll();
    }

    /**
     * {@code GET  /addresses/:id} : get the "id" address.
     *
     * @param id the id of the address to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the address, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Address> getAddress(@PathVariable("id") String id) {
        log.debug("REST request to get Address : {}", id);
        Optional<Address> address = addressRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(address);
    }

    /**
     * {@code DELETE  /addresses/:id} : delete the "id" address.
     *
     * @param id the id of the address to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable("id") String id) {
        log.debug("REST request to delete Address : {}", id);
        addressRepository.deleteById(id);
        addressSearchRepository.deleteFromIndexById(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id)).build();
    }

    /**
     * {@code SEARCH  /addresses/_search?query=:query} : search for the address corresponding
     * to the query.
     *
     * @param query the query of the address search.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public List<Address> searchAddresses(@RequestParam("query") String query) {
        log.debug("REST request to search Addresses for query {}", query);
        try {
            return StreamSupport.stream(addressSearchRepository.search(query).spliterator(), false).toList();
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }
}
