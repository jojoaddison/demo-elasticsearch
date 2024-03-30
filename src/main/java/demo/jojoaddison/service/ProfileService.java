package demo.jojoaddison.service;

import demo.jojoaddison.domain.Profile;
import demo.jojoaddison.repository.ProfileRepository;
import demo.jojoaddison.repository.search.ProfileSearchRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service Implementation for managing {@link demo.jojoaddison.domain.Profile}.
 */
@Service
public class ProfileService {

    private final Logger log = LoggerFactory.getLogger(ProfileService.class);

    private final ProfileRepository profileRepository;

    private final ProfileSearchRepository profileSearchRepository;

    public ProfileService(ProfileRepository profileRepository, ProfileSearchRepository profileSearchRepository) {
        this.profileRepository = profileRepository;
        this.profileSearchRepository = profileSearchRepository;
    }

    /**
     * Save a profile.
     *
     * @param profile the entity to save.
     * @return the persisted entity.
     */
    public Profile save(Profile profile) {
        log.debug("Request to save Profile : {}", profile);
        Profile result = profileRepository.save(profile);
        profileSearchRepository.index(result);
        return result;
    }

    /**
     * Update a profile.
     *
     * @param profile the entity to save.
     * @return the persisted entity.
     */
    public Profile update(Profile profile) {
        log.debug("Request to update Profile : {}", profile);
        Profile result = profileRepository.save(profile);
        profileSearchRepository.index(result);
        return result;
    }

    /**
     * Partially update a profile.
     *
     * @param profile the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<Profile> partialUpdate(Profile profile) {
        log.debug("Request to partially update Profile : {}", profile);

        return profileRepository
            .findById(profile.getId())
            .map(existingProfile -> {
                if (profile.getFirstName() != null) {
                    existingProfile.setFirstName(profile.getFirstName());
                }
                if (profile.getMiddleNames() != null) {
                    existingProfile.setMiddleNames(profile.getMiddleNames());
                }
                if (profile.getLastName() != null) {
                    existingProfile.setLastName(profile.getLastName());
                }
                if (profile.getMembership() != null) {
                    existingProfile.setMembership(profile.getMembership());
                }
                if (profile.getBirthDate() != null) {
                    existingProfile.setBirthDate(profile.getBirthDate());
                }
                if (profile.getSex() != null) {
                    existingProfile.setSex(profile.getSex());
                }
                if (profile.getMobilePhone() != null) {
                    existingProfile.setMobilePhone(profile.getMobilePhone());
                }
                if (profile.getPhoneNumber() != null) {
                    existingProfile.setPhoneNumber(profile.getPhoneNumber());
                }
                if (profile.getEmail() != null) {
                    existingProfile.setEmail(profile.getEmail());
                }
                if (profile.getCardType() != null) {
                    existingProfile.setCardType(profile.getCardType());
                }
                if (profile.getCardNumber() != null) {
                    existingProfile.setCardNumber(profile.getCardNumber());
                }
                if (profile.getContacts() != null) {
                    existingProfile.setContacts(profile.getContacts());
                }
                if (profile.getAddress() != null) {
                    existingProfile.setAddress(profile.getAddress());
                }
                if (profile.getTeam() != null) {
                    existingProfile.setTeam(profile.getTeam());
                }

                return existingProfile;
            })
            .map(profileRepository::save)
            .map(savedProfile -> {
                profileSearchRepository.index(savedProfile);
                return savedProfile;
            });
    }

    /**
     * Get all the profiles.
     *
     * @return the list of entities.
     */
    public List<Profile> findAll() {
        log.debug("Request to get all Profiles");
        return profileRepository.findAll();
    }

    /**
     * Get one profile by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    public Optional<Profile> findOne(String id) {
        log.debug("Request to get Profile : {}", id);
        return profileRepository.findById(id);
    }

    /**
     * Delete the profile by id.
     *
     * @param id the id of the entity.
     */
    public void delete(String id) {
        log.debug("Request to delete Profile : {}", id);
        profileRepository.deleteById(id);
        profileSearchRepository.deleteFromIndexById(id);
    }

    public boolean existsById(String id) {
        return profileRepository.existsById(id);
    }

    public List<Address> search(final String query) throws RuntimeException {
        return StreamSupport.stream(profileSearchRepository.search(query).spliterator(), false).toList();
    }
}
