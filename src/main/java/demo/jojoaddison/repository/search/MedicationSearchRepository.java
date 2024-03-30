package demo.jojoaddison.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import demo.jojoaddison.domain.Medication;
import demo.jojoaddison.repository.MedicationRepository;
import java.util.stream.Stream;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link Medication} entity.
 */
public interface MedicationSearchRepository extends ElasticsearchRepository<Medication, String>, MedicationSearchRepositoryInternal {}

interface MedicationSearchRepositoryInternal {
    Stream<Medication> search(String query);

    Stream<Medication> search(Query query);

    @Async
    void index(Medication entity);

    @Async
    void deleteFromIndexById(String id);
}

class MedicationSearchRepositoryInternalImpl implements MedicationSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final MedicationRepository repository;

    MedicationSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, MedicationRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<Medication> search(String query) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery);
    }

    @Override
    public Stream<Medication> search(Query query) {
        return elasticsearchTemplate.search(query, Medication.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(Medication entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(String id) {
        elasticsearchTemplate.delete(String.valueOf(id), Medication.class);
    }
}
