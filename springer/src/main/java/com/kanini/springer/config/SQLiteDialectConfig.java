package com.kanini.springer.config;

import org.hibernate.boot.model.TypeContributions;
import org.hibernate.community.dialect.SQLiteDialect;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.descriptor.jdbc.VarbinaryJdbcType;

import java.sql.Types;

/**
 *
 * The SQLite JDBC driver does not implement getBlob() / getBinaryStream(),
 * so @Lob byte[] fields (HiringCycle.jd, BatchAllocation.image,
 * DocumentSubmission.uploadedFile) fail at runtime with:
 *   "not implemented by SQLite JDBC driver"
 *
 * Overriding contributeTypes() replaces BlobJdbcType with VarbinaryJdbcType
 * so Hibernate calls getBytes() instead — which SQLite JDBC fully supports.
 * No entity or service code changes required.
 */
public class SQLiteDialectConfig extends SQLiteDialect {

    @Override
    public void contributeTypes(TypeContributions typeContributions, ServiceRegistry serviceRegistry) {
        super.contributeTypes(typeContributions, serviceRegistry);
        typeContributions.getTypeConfiguration()
                .getJdbcTypeRegistry()
                .addDescriptor(Types.BLOB, VarbinaryJdbcType.INSTANCE);
        typeContributions.getTypeConfiguration()
                .getJdbcTypeRegistry()
                .addDescriptor(Types.LONGVARBINARY, VarbinaryJdbcType.INSTANCE);
    }
}
