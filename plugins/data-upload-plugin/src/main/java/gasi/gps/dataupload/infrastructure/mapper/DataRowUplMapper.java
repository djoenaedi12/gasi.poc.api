package gasi.gps.dataupload.infrastructure.mapper;

import org.mapstruct.Mapper;

import gasi.gps.core.starter.infrastructure.mapper.BaseMapper;
import gasi.gps.dataupload.domain.model.DataRowUpl;
import gasi.gps.dataupload.infrastructure.entity.DataRowUplEntity;

/**
 * Maps upload row domain models and JPA entities.
 *
 * @since 1.0.0
 */
@Mapper(uses = DataUplMapper.class)
public interface DataRowUplMapper extends BaseMapper<DataRowUpl, DataRowUplEntity> {
}
