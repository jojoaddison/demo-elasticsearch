package demo.jojoaddison.repository;

import demo.jojoaddison.domain.HCPayOption;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data Unknown repository for the HCPayOption entity.
 */
@SuppressWarnings("unused")
@Repository
public interface HCPayOptionRepository extends MongoRepository<HCPayOption, String> {}
