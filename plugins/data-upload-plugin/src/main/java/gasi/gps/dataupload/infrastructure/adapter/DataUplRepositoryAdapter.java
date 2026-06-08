package gasi.gps.dataupload.infrastructure.adapter;

import org.springframework.stereotype.Component;

import gasi.gps.core.starter.infrastructure.adapter.BaseRepositoryAdapter;
import gasi.gps.dataupload.domain.model.DataUpl;
import gasi.gps.dataupload.domain.port.outbound.DataUplRepositoryPort;
import gasi.gps.dataupload.infrastructure.entity.DataUplEntity;
import gasi.gps.dataupload.infrastructure.mapper.DataUplMapper;
import gasi.gps.dataupload.infrastructure.persistance.DataUplEntityRepository;

/**
 * Spring Data adapter for upload batch persistence.
 *
 * @since 1.0.0
 */
@Component
public class DataUplRepositoryAdapter
        extends BaseRepositoryAdapter<DataUpl, DataUplEntity>
        implements DataUplRepositoryPort {

    /**
     * Creates an upload batch repository adapter.
     *
     * @param repository Spring Data upload repository
     * @param mapper     upload entity mapper
     */
    public DataUplRepositoryAdapter(DataUplEntityRepository repository,
            DataUplMapper mapper) {
        super(repository, mapper);
    }

    @Override
    protected String resourceType() {
        return "DataUpl";
    }
}
