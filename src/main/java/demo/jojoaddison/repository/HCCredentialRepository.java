package demo.jojoaddison.repository;

import demo.jojoaddison.domain.HCCredential;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data Unknown repository for the HCCredential entity.
 */
@SuppressWarnings("unused")
@Repository
public interface HCCredentialRepository extends MongoRepository<HCCredential, String> {}
