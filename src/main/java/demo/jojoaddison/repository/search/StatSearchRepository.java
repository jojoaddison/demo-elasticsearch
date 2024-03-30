package demo.jojoaddison.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import demo.jojoaddison.domain.Stat;
import demo.jojoaddison.repository.StatRepository;
import java.util.stream.Stream;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link Stat} entity.
 */
public interface StatSearchRepository extends ElasticsearchRepository<Stat, String>, StatSearchRepositoryInternal {}

interface StatSearchRepositoryInternal {
    Stream<Stat> search(String query);

    Stream<Stat> search(Query query);

    @Async
    void index(Stat entity);

    @Async
    void deleteFromIndexById(String id);
}

class StatSearchRepositoryInternalImpl implements StatSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final StatRepository repository;

    StatSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, StatRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<Stat> search(String query) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery);
    }

    @Override
    public Stream<Stat> search(Query query) {
        return elasticsearchTemplate.search(query, Stat.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(Stat entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(String id) {
        elasticsearchTemplate.delete(String.valueOf(id), Stat.class);
    }
}
