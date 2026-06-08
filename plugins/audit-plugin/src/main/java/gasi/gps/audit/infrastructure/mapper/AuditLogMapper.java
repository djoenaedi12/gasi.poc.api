package gasi.gps.audit.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import gasi.gps.audit.domain.model.AuditLog;
import gasi.gps.audit.infrastructure.entity.AuditLogEntity;
import gasi.gps.core.starter.application.mapper.IgnoreAuditFields;
import gasi.gps.core.starter.infrastructure.mapper.BaseMapper;
import gasi.gps.core.starter.infrastructure.mapper.StringArrayMapper;

/**
 * Maps audit log domain models and JPA entities.
 *
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", uses = { StringArrayMapper.class })
public interface AuditLogMapper extends BaseMapper<AuditLog, AuditLogEntity> {

    @Override
    @Mapping(target = "changedFields", source = "changedFields", qualifiedByName = "stringToArray")
    AuditLog toDomain(AuditLogEntity entity);

    @Override
    @Mapping(target = "changedFields", source = "changedFields", qualifiedByName = "arrayToString")
    AuditLogEntity toEntity(AuditLog domain);

    @IgnoreAuditFields
    @Mapping(target = "changedFields", source = "changedFields", qualifiedByName = "arrayToString")
    void updateEntity(AuditLog source, @MappingTarget AuditLogEntity target);

}
