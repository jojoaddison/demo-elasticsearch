package demo.jojoaddison.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import demo.jojoaddison.domain.Membership;
import demo.jojoaddison.repository.MembershipRepository;
import java.util.stream.Stream;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link Membership} entity.
 */
public interface MembershipSearchRepository extends ElasticsearchRepository<Membership, String>, MembershipSearchRepositoryInternal {}

interface MembershipSearchRepositoryInternal {
    Stream<Membership> search(String query);

    Stream<Membership> search(Query query);

    @Async
    void index(Membership entity);

    @Async
    void deleteFromIndexById(String id);
}

class MembershipSearchRepositoryInternalImpl implements MembershipSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final MembershipRepository repository;

    MembershipSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, MembershipRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<Membership> search(String query) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery);
    }

    @Override
    public Stream<Membership> search(Query query) {
        return elasticsearchTemplate.search(query, Membership.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(Membership entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(String id) {
        elasticsearchTemplate.delete(String.valueOf(id), Membership.class);
    }
}
