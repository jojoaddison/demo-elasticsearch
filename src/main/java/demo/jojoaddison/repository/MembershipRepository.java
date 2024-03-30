package demo.jojoaddison.repository;

import demo.jojoaddison.domain.Membership;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data Unknown repository for the Membership entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MembershipRepository extends MongoRepository<Membership, String> {}
