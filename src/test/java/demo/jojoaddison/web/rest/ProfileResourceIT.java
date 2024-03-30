package demo.jojoaddison.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import demo.jojoaddison.IntegrationTest;
import demo.jojoaddison.domain.Profile;
import demo.jojoaddison.repository.ProfileRepository;
import demo.jojoaddison.repository.search.ProfileSearchRepository;
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
 * Integration tests for the {@link ProfileResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ProfileResourceIT {

    private static final String DEFAULT_FIRST_NAME = "AAAAAAAAAA";
    private static final String UPDATED_FIRST_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_MIDDLE_NAMES = "AAAAAAAAAA";
    private static final String UPDATED_MIDDLE_NAMES = "BBBBBBBBBB";

    private static final String DEFAULT_LAST_NAME = "AAAAAAAAAA";
    private static final String UPDATED_LAST_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_MEMBERSHIP = "AAAAAAAAAA";
    private static final String UPDATED_MEMBERSHIP = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_BIRTH_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_BIRTH_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final String DEFAULT_SEX = "AAAAAAAAAA";
    private static final String UPDATED_SEX = "BBBBBBBBBB";

    private static final String DEFAULT_MOBILE_PHONE = "AAAAAAAAAA";
    private static final String UPDATED_MOBILE_PHONE = "BBBBBBBBBB";

    private static final String DEFAULT_PHONE_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_PHONE_NUMBER = "BBBBBBBBBB";

    private static final String DEFAULT_EMAIL = "AAAAAAAAAA";
    private static final String UPDATED_EMAIL = "BBBBBBBBBB";

    private static final String DEFAULT_CARD_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_CARD_TYPE = "BBBBBBBBBB";

    private static final String DEFAULT_CARD_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_CARD_NUMBER = "BBBBBBBBBB";

    private static final String DEFAULT_CONTACTS = "AAAAAAAAAA";
    private static final String UPDATED_CONTACTS = "BBBBBBBBBB";

    private static final String DEFAULT_ADDRESS = "AAAAAAAAAA";
    private static final String UPDATED_ADDRESS = "BBBBBBBBBB";

    private static final String DEFAULT_TEAM = "AAAAAAAAAA";
    private static final String UPDATED_TEAM = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/profiles";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/profiles/_search";

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ProfileSearchRepository profileSearchRepository;

    @Autowired
    private MockMvc restProfileMockMvc;

    private Profile profile;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Profile createEntity() {
        Profile profile = new Profile()
            .firstName(DEFAULT_FIRST_NAME)
            .middleNames(DEFAULT_MIDDLE_NAMES)
            .lastName(DEFAULT_LAST_NAME)
            .membership(DEFAULT_MEMBERSHIP)
            .birthDate(DEFAULT_BIRTH_DATE)
            .sex(DEFAULT_SEX)
            .mobilePhone(DEFAULT_MOBILE_PHONE)
            .phoneNumber(DEFAULT_PHONE_NUMBER)
            .email(DEFAULT_EMAIL)
            .cardType(DEFAULT_CARD_TYPE)
            .cardNumber(DEFAULT_CARD_NUMBER)
            .contacts(DEFAULT_CONTACTS)
            .address(DEFAULT_ADDRESS)
            .team(DEFAULT_TEAM);
        return profile;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Profile createUpdatedEntity() {
        Profile profile = new Profile()
            .firstName(UPDATED_FIRST_NAME)
            .middleNames(UPDATED_MIDDLE_NAMES)
            .lastName(UPDATED_LAST_NAME)
            .membership(UPDATED_MEMBERSHIP)
            .birthDate(UPDATED_BIRTH_DATE)
            .sex(UPDATED_SEX)
            .mobilePhone(UPDATED_MOBILE_PHONE)
            .phoneNumber(UPDATED_PHONE_NUMBER)
            .email(UPDATED_EMAIL)
            .cardType(UPDATED_CARD_TYPE)
            .cardNumber(UPDATED_CARD_NUMBER)
            .contacts(UPDATED_CONTACTS)
            .address(UPDATED_ADDRESS)
            .team(UPDATED_TEAM);
        return profile;
    }

    @AfterEach
    public void cleanupElasticSearchRepository() {
        profileSearchRepository.deleteAll();
        assertThat(profileSearchRepository.count()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        profileRepository.deleteAll();
        profile = createEntity();
    }

    @Test
    void createProfile() throws Exception {
        int databaseSizeBeforeCreate = profileRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(profileSearchRepository.findAll());
        // Create the Profile
        restProfileMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(profile)))
            .andExpect(status().isCreated());

        // Validate the Profile in the database
        List<Profile> profileList = profileRepository.findAll();
        assertThat(profileList).hasSize(databaseSizeBeforeCreate + 1);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(profileSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
        Profile testProfile = profileList.get(profileList.size() - 1);
        assertThat(testProfile.getFirstName()).isEqualTo(DEFAULT_FIRST_NAME);
        assertThat(testProfile.getMiddleNames()).isEqualTo(DEFAULT_MIDDLE_NAMES);
        assertThat(testProfile.getLastName()).isEqualTo(DEFAULT_LAST_NAME);
        assertThat(testProfile.getMembership()).isEqualTo(DEFAULT_MEMBERSHIP);
        assertThat(testProfile.getBirthDate()).isEqualTo(DEFAULT_BIRTH_DATE);
        assertThat(testProfile.getSex()).isEqualTo(DEFAULT_SEX);
        assertThat(testProfile.getMobilePhone()).isEqualTo(DEFAULT_MOBILE_PHONE);
        assertThat(testProfile.getPhoneNumber()).isEqualTo(DEFAULT_PHONE_NUMBER);
        assertThat(testProfile.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(testProfile.getCardType()).isEqualTo(DEFAULT_CARD_TYPE);
        assertThat(testProfile.getCardNumber()).isEqualTo(DEFAULT_CARD_NUMBER);
        assertThat(testProfile.getContacts()).isEqualTo(DEFAULT_CONTACTS);
        assertThat(testProfile.getAddress()).isEqualTo(DEFAULT_ADDRESS);
        assertThat(testProfile.getTeam()).isEqualTo(DEFAULT_TEAM);
    }

    @Test
    void createProfileWithExistingId() throws Exception {
        // Create the Profile with an existing ID
        profile.setId("existing_id");

        int databaseSizeBeforeCreate = profileRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(profileSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restProfileMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(profile)))
            .andExpect(status().isBadRequest());

        // Validate the Profile in the database
        List<Profile> profileList = profileRepository.findAll();
        assertThat(profileList).hasSize(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(profileSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void getAllProfiles() throws Exception {
        // Initialize the database
        profileRepository.save(profile);

        // Get all the profileList
        restProfileMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(profile.getId())))
            .andExpect(jsonPath("$.[*].firstName").value(hasItem(DEFAULT_FIRST_NAME)))
            .andExpect(jsonPath("$.[*].middleNames").value(hasItem(DEFAULT_MIDDLE_NAMES)))
            .andExpect(jsonPath("$.[*].lastName").value(hasItem(DEFAULT_LAST_NAME)))
            .andExpect(jsonPath("$.[*].membership").value(hasItem(DEFAULT_MEMBERSHIP)))
            .andExpect(jsonPath("$.[*].birthDate").value(hasItem(DEFAULT_BIRTH_DATE.toString())))
            .andExpect(jsonPath("$.[*].sex").value(hasItem(DEFAULT_SEX)))
            .andExpect(jsonPath("$.[*].mobilePhone").value(hasItem(DEFAULT_MOBILE_PHONE)))
            .andExpect(jsonPath("$.[*].phoneNumber").value(hasItem(DEFAULT_PHONE_NUMBER)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
            .andExpect(jsonPath("$.[*].cardType").value(hasItem(DEFAULT_CARD_TYPE)))
            .andExpect(jsonPath("$.[*].cardNumber").value(hasItem(DEFAULT_CARD_NUMBER)))
            .andExpect(jsonPath("$.[*].contacts").value(hasItem(DEFAULT_CONTACTS)))
            .andExpect(jsonPath("$.[*].address").value(hasItem(DEFAULT_ADDRESS)))
            .andExpect(jsonPath("$.[*].team").value(hasItem(DEFAULT_TEAM)));
    }

    @Test
    void getProfile() throws Exception {
        // Initialize the database
        profileRepository.save(profile);

        // Get the profile
        restProfileMockMvc
            .perform(get(ENTITY_API_URL_ID, profile.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(profile.getId()))
            .andExpect(jsonPath("$.firstName").value(DEFAULT_FIRST_NAME))
            .andExpect(jsonPath("$.middleNames").value(DEFAULT_MIDDLE_NAMES))
            .andExpect(jsonPath("$.lastName").value(DEFAULT_LAST_NAME))
            .andExpect(jsonPath("$.membership").value(DEFAULT_MEMBERSHIP))
            .andExpect(jsonPath("$.birthDate").value(DEFAULT_BIRTH_DATE.toString()))
            .andExpect(jsonPath("$.sex").value(DEFAULT_SEX))
            .andExpect(jsonPath("$.mobilePhone").value(DEFAULT_MOBILE_PHONE))
            .andExpect(jsonPath("$.phoneNumber").value(DEFAULT_PHONE_NUMBER))
            .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL))
            .andExpect(jsonPath("$.cardType").value(DEFAULT_CARD_TYPE))
            .andExpect(jsonPath("$.cardNumber").value(DEFAULT_CARD_NUMBER))
            .andExpect(jsonPath("$.contacts").value(DEFAULT_CONTACTS))
            .andExpect(jsonPath("$.address").value(DEFAULT_ADDRESS))
            .andExpect(jsonPath("$.team").value(DEFAULT_TEAM));
    }

    @Test
    void getNonExistingProfile() throws Exception {
        // Get the profile
        restProfileMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    void putExistingProfile() throws Exception {
        // Initialize the database
        profileRepository.save(profile);

        int databaseSizeBeforeUpdate = profileRepository.findAll().size();
        profileSearchRepository.save(profile);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(profileSearchRepository.findAll());

        // Update the profile
        Profile updatedProfile = profileRepository.findById(profile.getId()).orElseThrow();
        updatedProfile
            .firstName(UPDATED_FIRST_NAME)
            .middleNames(UPDATED_MIDDLE_NAMES)
            .lastName(UPDATED_LAST_NAME)
            .membership(UPDATED_MEMBERSHIP)
            .birthDate(UPDATED_BIRTH_DATE)
            .sex(UPDATED_SEX)
            .mobilePhone(UPDATED_MOBILE_PHONE)
            .phoneNumber(UPDATED_PHONE_NUMBER)
            .email(UPDATED_EMAIL)
            .cardType(UPDATED_CARD_TYPE)
            .cardNumber(UPDATED_CARD_NUMBER)
            .contacts(UPDATED_CONTACTS)
            .address(UPDATED_ADDRESS)
            .team(UPDATED_TEAM);

        restProfileMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedProfile.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedProfile))
            )
            .andExpect(status().isOk());

        // Validate the Profile in the database
        List<Profile> profileList = profileRepository.findAll();
        assertThat(profileList).hasSize(databaseSizeBeforeUpdate);
        Profile testProfile = profileList.get(profileList.size() - 1);
        assertThat(testProfile.getFirstName()).isEqualTo(UPDATED_FIRST_NAME);
        assertThat(testProfile.getMiddleNames()).isEqualTo(UPDATED_MIDDLE_NAMES);
        assertThat(testProfile.getLastName()).isEqualTo(UPDATED_LAST_NAME);
        assertThat(testProfile.getMembership()).isEqualTo(UPDATED_MEMBERSHIP);
        assertThat(testProfile.getBirthDate()).isEqualTo(UPDATED_BIRTH_DATE);
        assertThat(testProfile.getSex()).isEqualTo(UPDATED_SEX);
        assertThat(testProfile.getMobilePhone()).isEqualTo(UPDATED_MOBILE_PHONE);
        assertThat(testProfile.getPhoneNumber()).isEqualTo(UPDATED_PHONE_NUMBER);
        assertThat(testProfile.getEmail()).isEqualTo(UPDATED_EMAIL);
        assertThat(testProfile.getCardType()).isEqualTo(UPDATED_CARD_TYPE);
        assertThat(testProfile.getCardNumber()).isEqualTo(UPDATED_CARD_NUMBER);
        assertThat(testProfile.getContacts()).isEqualTo(UPDATED_CONTACTS);
        assertThat(testProfile.getAddress()).isEqualTo(UPDATED_ADDRESS);
        assertThat(testProfile.getTeam()).isEqualTo(UPDATED_TEAM);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(profileSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<Profile> profileSearchList = IterableUtils.toList(profileSearchRepository.findAll());
                Profile testProfileSearch = profileSearchList.get(searchDatabaseSizeAfter - 1);
                assertThat(testProfileSearch.getFirstName()).isEqualTo(UPDATED_FIRST_NAME);
                assertThat(testProfileSearch.getMiddleNames()).isEqualTo(UPDATED_MIDDLE_NAMES);
                assertThat(testProfileSearch.getLastName()).isEqualTo(UPDATED_LAST_NAME);
                assertThat(testProfileSearch.getMembership()).isEqualTo(UPDATED_MEMBERSHIP);
                assertThat(testProfileSearch.getBirthDate()).isEqualTo(UPDATED_BIRTH_DATE);
                assertThat(testProfileSearch.getSex()).isEqualTo(UPDATED_SEX);
                assertThat(testProfileSearch.getMobilePhone()).isEqualTo(UPDATED_MOBILE_PHONE);
                assertThat(testProfileSearch.getPhoneNumber()).isEqualTo(UPDATED_PHONE_NUMBER);
                assertThat(testProfileSearch.getEmail()).isEqualTo(UPDATED_EMAIL);
                assertThat(testProfileSearch.getCardType()).isEqualTo(UPDATED_CARD_TYPE);
                assertThat(testProfileSearch.getCardNumber()).isEqualTo(UPDATED_CARD_NUMBER);
                assertThat(testProfileSearch.getContacts()).isEqualTo(UPDATED_CONTACTS);
                assertThat(testProfileSearch.getAddress()).isEqualTo(UPDATED_ADDRESS);
                assertThat(testProfileSearch.getTeam()).isEqualTo(UPDATED_TEAM);
            });
    }

    @Test
    void putNonExistingProfile() throws Exception {
        int databaseSizeBeforeUpdate = profileRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(profileSearchRepository.findAll());
        profile.setId(UUID.randomUUID().toString());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProfileMockMvc
            .perform(
                put(ENTITY_API_URL_ID, profile.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(profile))
            )
            .andExpect(status().isBadRequest());

        // Validate the Profile in the database
        List<Profile> profileList = profileRepository.findAll();
        assertThat(profileList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(profileSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithIdMismatchProfile() throws Exception {
        int databaseSizeBeforeUpdate = profileRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(profileSearchRepository.findAll());
        profile.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProfileMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(profile))
            )
            .andExpect(status().isBadRequest());

        // Validate the Profile in the database
        List<Profile> profileList = profileRepository.findAll();
        assertThat(profileList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(profileSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithMissingIdPathParamProfile() throws Exception {
        int databaseSizeBeforeUpdate = profileRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(profileSearchRepository.findAll());
        profile.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProfileMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(profile)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Profile in the database
        List<Profile> profileList = profileRepository.findAll();
        assertThat(profileList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(profileSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void partialUpdateProfileWithPatch() throws Exception {
        // Initialize the database
        profileRepository.save(profile);

        int databaseSizeBeforeUpdate = profileRepository.findAll().size();

        // Update the profile using partial update
        Profile partialUpdatedProfile = new Profile();
        partialUpdatedProfile.setId(profile.getId());

        partialUpdatedProfile
            .sex(UPDATED_SEX)
            .phoneNumber(UPDATED_PHONE_NUMBER)
            .email(UPDATED_EMAIL)
            .cardNumber(UPDATED_CARD_NUMBER)
            .contacts(UPDATED_CONTACTS)
            .address(UPDATED_ADDRESS)
            .team(UPDATED_TEAM);

        restProfileMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedProfile.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedProfile))
            )
            .andExpect(status().isOk());

        // Validate the Profile in the database
        List<Profile> profileList = profileRepository.findAll();
        assertThat(profileList).hasSize(databaseSizeBeforeUpdate);
        Profile testProfile = profileList.get(profileList.size() - 1);
        assertThat(testProfile.getFirstName()).isEqualTo(DEFAULT_FIRST_NAME);
        assertThat(testProfile.getMiddleNames()).isEqualTo(DEFAULT_MIDDLE_NAMES);
        assertThat(testProfile.getLastName()).isEqualTo(DEFAULT_LAST_NAME);
        assertThat(testProfile.getMembership()).isEqualTo(DEFAULT_MEMBERSHIP);
        assertThat(testProfile.getBirthDate()).isEqualTo(DEFAULT_BIRTH_DATE);
        assertThat(testProfile.getSex()).isEqualTo(UPDATED_SEX);
        assertThat(testProfile.getMobilePhone()).isEqualTo(DEFAULT_MOBILE_PHONE);
        assertThat(testProfile.getPhoneNumber()).isEqualTo(UPDATED_PHONE_NUMBER);
        assertThat(testProfile.getEmail()).isEqualTo(UPDATED_EMAIL);
        assertThat(testProfile.getCardType()).isEqualTo(DEFAULT_CARD_TYPE);
        assertThat(testProfile.getCardNumber()).isEqualTo(UPDATED_CARD_NUMBER);
        assertThat(testProfile.getContacts()).isEqualTo(UPDATED_CONTACTS);
        assertThat(testProfile.getAddress()).isEqualTo(UPDATED_ADDRESS);
        assertThat(testProfile.getTeam()).isEqualTo(UPDATED_TEAM);
    }

    @Test
    void fullUpdateProfileWithPatch() throws Exception {
        // Initialize the database
        profileRepository.save(profile);

        int databaseSizeBeforeUpdate = profileRepository.findAll().size();

        // Update the profile using partial update
        Profile partialUpdatedProfile = new Profile();
        partialUpdatedProfile.setId(profile.getId());

        partialUpdatedProfile
            .firstName(UPDATED_FIRST_NAME)
            .middleNames(UPDATED_MIDDLE_NAMES)
            .lastName(UPDATED_LAST_NAME)
            .membership(UPDATED_MEMBERSHIP)
            .birthDate(UPDATED_BIRTH_DATE)
            .sex(UPDATED_SEX)
            .mobilePhone(UPDATED_MOBILE_PHONE)
            .phoneNumber(UPDATED_PHONE_NUMBER)
            .email(UPDATED_EMAIL)
            .cardType(UPDATED_CARD_TYPE)
            .cardNumber(UPDATED_CARD_NUMBER)
            .contacts(UPDATED_CONTACTS)
            .address(UPDATED_ADDRESS)
            .team(UPDATED_TEAM);

        restProfileMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedProfile.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedProfile))
            )
            .andExpect(status().isOk());

        // Validate the Profile in the database
        List<Profile> profileList = profileRepository.findAll();
        assertThat(profileList).hasSize(databaseSizeBeforeUpdate);
        Profile testProfile = profileList.get(profileList.size() - 1);
        assertThat(testProfile.getFirstName()).isEqualTo(UPDATED_FIRST_NAME);
        assertThat(testProfile.getMiddleNames()).isEqualTo(UPDATED_MIDDLE_NAMES);
        assertThat(testProfile.getLastName()).isEqualTo(UPDATED_LAST_NAME);
        assertThat(testProfile.getMembership()).isEqualTo(UPDATED_MEMBERSHIP);
        assertThat(testProfile.getBirthDate()).isEqualTo(UPDATED_BIRTH_DATE);
        assertThat(testProfile.getSex()).isEqualTo(UPDATED_SEX);
        assertThat(testProfile.getMobilePhone()).isEqualTo(UPDATED_MOBILE_PHONE);
        assertThat(testProfile.getPhoneNumber()).isEqualTo(UPDATED_PHONE_NUMBER);
        assertThat(testProfile.getEmail()).isEqualTo(UPDATED_EMAIL);
        assertThat(testProfile.getCardType()).isEqualTo(UPDATED_CARD_TYPE);
        assertThat(testProfile.getCardNumber()).isEqualTo(UPDATED_CARD_NUMBER);
        assertThat(testProfile.getContacts()).isEqualTo(UPDATED_CONTACTS);
        assertThat(testProfile.getAddress()).isEqualTo(UPDATED_ADDRESS);
        assertThat(testProfile.getTeam()).isEqualTo(UPDATED_TEAM);
    }

    @Test
    void patchNonExistingProfile() throws Exception {
        int databaseSizeBeforeUpdate = profileRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(profileSearchRepository.findAll());
        profile.setId(UUID.randomUUID().toString());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProfileMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, profile.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(profile))
            )
            .andExpect(status().isBadRequest());

        // Validate the Profile in the database
        List<Profile> profileList = profileRepository.findAll();
        assertThat(profileList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(profileSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithIdMismatchProfile() throws Exception {
        int databaseSizeBeforeUpdate = profileRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(profileSearchRepository.findAll());
        profile.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProfileMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(profile))
            )
            .andExpect(status().isBadRequest());

        // Validate the Profile in the database
        List<Profile> profileList = profileRepository.findAll();
        assertThat(profileList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(profileSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithMissingIdPathParamProfile() throws Exception {
        int databaseSizeBeforeUpdate = profileRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(profileSearchRepository.findAll());
        profile.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProfileMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(profile)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Profile in the database
        List<Profile> profileList = profileRepository.findAll();
        assertThat(profileList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(profileSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void deleteProfile() throws Exception {
        // Initialize the database
        profileRepository.save(profile);
        profileRepository.save(profile);
        profileSearchRepository.save(profile);

        int databaseSizeBeforeDelete = profileRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(profileSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the profile
        restProfileMockMvc
            .perform(delete(ENTITY_API_URL_ID, profile.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Profile> profileList = profileRepository.findAll();
        assertThat(profileList).hasSize(databaseSizeBeforeDelete - 1);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(profileSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    void searchProfile() throws Exception {
        // Initialize the database
        profile = profileRepository.save(profile);
        profileSearchRepository.save(profile);

        // Search the profile
        restProfileMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + profile.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(profile.getId())))
            .andExpect(jsonPath("$.[*].firstName").value(hasItem(DEFAULT_FIRST_NAME)))
            .andExpect(jsonPath("$.[*].middleNames").value(hasItem(DEFAULT_MIDDLE_NAMES)))
            .andExpect(jsonPath("$.[*].lastName").value(hasItem(DEFAULT_LAST_NAME)))
            .andExpect(jsonPath("$.[*].membership").value(hasItem(DEFAULT_MEMBERSHIP)))
            .andExpect(jsonPath("$.[*].birthDate").value(hasItem(DEFAULT_BIRTH_DATE.toString())))
            .andExpect(jsonPath("$.[*].sex").value(hasItem(DEFAULT_SEX)))
            .andExpect(jsonPath("$.[*].mobilePhone").value(hasItem(DEFAULT_MOBILE_PHONE)))
            .andExpect(jsonPath("$.[*].phoneNumber").value(hasItem(DEFAULT_PHONE_NUMBER)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
            .andExpect(jsonPath("$.[*].cardType").value(hasItem(DEFAULT_CARD_TYPE)))
            .andExpect(jsonPath("$.[*].cardNumber").value(hasItem(DEFAULT_CARD_NUMBER)))
            .andExpect(jsonPath("$.[*].contacts").value(hasItem(DEFAULT_CONTACTS)))
            .andExpect(jsonPath("$.[*].address").value(hasItem(DEFAULT_ADDRESS)))
            .andExpect(jsonPath("$.[*].team").value(hasItem(DEFAULT_TEAM)));
    }
}
