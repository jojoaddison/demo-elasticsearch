package demo.jojoaddison.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import demo.jojoaddison.domain.Team;
import demo.jojoaddison.repository.TeamRepository;
import java.util.stream.Stream;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link Team} entity.
 */
public interface TeamSearchRepository extends ElasticsearchRepository<Team, String>, TeamSearchRepositoryInternal {}

interface TeamSearchRepositoryInternal {
    Stream<Team> search(String query);

    Stream<Team> search(Query query);

    @Async
    void index(Team entity);

    @Async
    void deleteFromIndexById(String id);
}

class TeamSearchRepositoryInternalImpl implements TeamSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final TeamRepository repository;

    TeamSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, TeamRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<Team> search(String query) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery);
    }

    @Override
    public Stream<Team> search(Query query) {
        return elasticsearchTemplate.search(query, Team.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(Team entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(String id) {
        elasticsearchTemplate.delete(String.valueOf(id), Team.class);
    }
}
