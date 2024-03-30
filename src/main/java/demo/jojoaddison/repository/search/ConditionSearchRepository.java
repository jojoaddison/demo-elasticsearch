package demo.jojoaddison.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import demo.jojoaddison.domain.Condition;
import demo.jojoaddison.repository.ConditionRepository;
import java.util.stream.Stream;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link Condition} entity.
 */
public interface ConditionSearchRepository extends ElasticsearchRepository<Condition, String>, ConditionSearchRepositoryInternal {}

interface ConditionSearchRepositoryInternal {
    Stream<Condition> search(String query);

    Stream<Condition> search(Query query);

    @Async
    void index(Condition entity);

    @Async
    void deleteFromIndexById(String id);
}

class ConditionSearchRepositoryInternalImpl implements ConditionSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final ConditionRepository repository;

    ConditionSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, ConditionRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<Condition> search(String query) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery);
    }

    @Override
    public Stream<Condition> search(Query query) {
        return elasticsearchTemplate.search(query, Condition.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(Condition entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(String id) {
        elasticsearchTemplate.delete(String.valueOf(id), Condition.class);
    }
}
