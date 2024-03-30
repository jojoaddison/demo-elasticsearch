package demo.jojoaddison.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import demo.jojoaddison.domain.Report;
import demo.jojoaddison.repository.ReportRepository;
import java.util.stream.Stream;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link Report} entity.
 */
public interface ReportSearchRepository extends ElasticsearchRepository<Report, String>, ReportSearchRepositoryInternal {}

interface ReportSearchRepositoryInternal {
    Stream<Report> search(String query);

    Stream<Report> search(Query query);

    @Async
    void index(Report entity);

    @Async
    void deleteFromIndexById(String id);
}

class ReportSearchRepositoryInternalImpl implements ReportSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final ReportRepository repository;

    ReportSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, ReportRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<Report> search(String query) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery);
    }

    @Override
    public Stream<Report> search(Query query) {
        return elasticsearchTemplate.search(query, Report.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(Report entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(String id) {
        elasticsearchTemplate.delete(String.valueOf(id), Report.class);
    }
}
