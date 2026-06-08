package gasi.gps.dataupload.infrastructure.adapter;

import org.springframework.stereotype.Component;

import gasi.gps.core.starter.infrastructure.adapter.BaseRepositoryAdapter;
import gasi.gps.dataupload.domain.model.DataRowUpl;
import gasi.gps.dataupload.domain.port.outbound.DataRowUplRepositoryPort;
import gasi.gps.dataupload.infrastructure.entity.DataRowUplEntity;
import gasi.gps.dataupload.infrastructure.mapper.DataRowUplMapper;
import gasi.gps.dataupload.infrastructure.persistance.DataRowUplEntityRepository;

/**
 * Spring Data adapter for upload row persistence.
 *
 * @since 1.0.0
 */
@Component
public class DataRowUplRepositoryAdapter
        extends BaseRepositoryAdapter<DataRowUpl, DataRowUplEntity>
        implements DataRowUplRepositoryPort {

    /**
     * Creates an upload row repository adapter.
     *
     * @param repository Spring Data upload row repository
     * @param mapper     upload row entity mapper
     */
    public DataRowUplRepositoryAdapter(DataRowUplEntityRepository repository,
            DataRowUplMapper mapper) {
        super(repository, mapper);
    }

    @Override
    protected String resourceType() {
        return "DataRowUpl";
    }
}
