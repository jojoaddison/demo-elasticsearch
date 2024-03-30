package demo.jojoaddison.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import demo.jojoaddison.domain.Metadata;
import demo.jojoaddison.repository.MetadataRepository;
import java.util.stream.Stream;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link Metadata} entity.
 */
public interface MetadataSearchRepository extends ElasticsearchRepository<Metadata, String>, MetadataSearchRepositoryInternal {}

interface MetadataSearchRepositoryInternal {
    Stream<Metadata> search(String query);

    Stream<Metadata> search(Query query);

    @Async
    void index(Metadata entity);

    @Async
    void deleteFromIndexById(String id);
}

class MetadataSearchRepositoryInternalImpl implements MetadataSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final MetadataRepository repository;

    MetadataSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, MetadataRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<Metadata> search(String query) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery);
    }

    @Override
    public Stream<Metadata> search(Query query) {
        return elasticsearchTemplate.search(query, Metadata.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(Metadata entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(String id) {
        elasticsearchTemplate.delete(String.valueOf(id), Metadata.class);
    }
}
