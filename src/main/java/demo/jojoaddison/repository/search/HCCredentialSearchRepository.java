package demo.jojoaddison.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import demo.jojoaddison.domain.HCCredential;
import demo.jojoaddison.repository.HCCredentialRepository;
import java.util.stream.Stream;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link HCCredential} entity.
 */
public interface HCCredentialSearchRepository extends ElasticsearchRepository<HCCredential, String>, HCCredentialSearchRepositoryInternal {}

interface HCCredentialSearchRepositoryInternal {
    Stream<HCCredential> search(String query);

    Stream<HCCredential> search(Query query);

    @Async
    void index(HCCredential entity);

    @Async
    void deleteFromIndexById(String id);
}

class HCCredentialSearchRepositoryInternalImpl implements HCCredentialSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final HCCredentialRepository repository;

    HCCredentialSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, HCCredentialRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<HCCredential> search(String query) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery);
    }

    @Override
    public Stream<HCCredential> search(Query query) {
        return elasticsearchTemplate.search(query, HCCredential.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(HCCredential entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(String id) {
        elasticsearchTemplate.delete(String.valueOf(id), HCCredential.class);
    }
}
