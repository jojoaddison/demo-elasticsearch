package demo.jojoaddison.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import demo.jojoaddison.domain.HCPayOption;
import demo.jojoaddison.repository.HCPayOptionRepository;
import java.util.stream.Stream;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link HCPayOption} entity.
 */
public interface HCPayOptionSearchRepository extends ElasticsearchRepository<HCPayOption, String>, HCPayOptionSearchRepositoryInternal {}

interface HCPayOptionSearchRepositoryInternal {
    Stream<HCPayOption> search(String query);

    Stream<HCPayOption> search(Query query);

    @Async
    void index(HCPayOption entity);

    @Async
    void deleteFromIndexById(String id);
}

class HCPayOptionSearchRepositoryInternalImpl implements HCPayOptionSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final HCPayOptionRepository repository;

    HCPayOptionSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, HCPayOptionRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<HCPayOption> search(String query) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery);
    }

    @Override
    public Stream<HCPayOption> search(Query query) {
        return elasticsearchTemplate.search(query, HCPayOption.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(HCPayOption entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(String id) {
        elasticsearchTemplate.delete(String.valueOf(id), HCPayOption.class);
    }
}
