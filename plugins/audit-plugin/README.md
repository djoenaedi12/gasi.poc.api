# audit-plugin

Plugin audit log untuk GPS v4. Secara otomatis mencatat setiap operasi CUD (Create, Update, Delete) dan operasi non-CRUD (Approve, Reject, Export, Login, dll.) ke tabel `audit_log` menggunakan AOP, tanpa perlu kode boilerplate di setiap service.

---

## Cara Kerja

Plugin ini menyediakan dua mekanisme audit:

### 1. `@AuditableEntity` — Audit otomatis untuk CRUD

Annotasi di **class level** pada service yang extend `BaseService`. Aspect `AuditEntityAspect` akan intercept method `create`, `update`, dan `delete` secara otomatis.

```java
@AuditableEntity(module = "employee", resourceType = "Employee")
public class EmployeeServiceImpl extends BaseServiceImpl<...> implements EmployeeService {
    // Audit CREATE, UPDATE, DELETE otomatis tanpa kode tambahan
}
```

| Atribut        | Wajib | Default                    | Keterangan |
|---------------|-------|----------------------------|------------|
| `module`      | ya    | —                          | Modul pemilik resource, misal: `employee`, `payroll`, `auth` |
| `resourceType`| tidak | `""`                       | Nama entity yang terbaca di log, misal: `Employee` |
| `auditActions`| tidak | `["CREATE","UPDATE","DELETE"]` | Pilih action mana saja yang diaudit |
| `alwaysLog`   | tidak | `false`                    | Jika `true`, tetap dilog meski dipanggil dari dalam service lain yang sudah diaudit |

### 2. `@Auditable` — Audit untuk operasi non-CRUD

Annotasi di **method level** untuk operasi di luar CRUD standar. Mendukung **SpEL expression** di field `description`.

```java
@Auditable(
    action = "APPROVE",
    module = "leave",
    description = "Approve leave request #{#id} for employee #{#result.employeeName}"
)
public LeaveRequest approveLeave(Long id) { ... }
```

| Atribut      | Wajib | Default | Keterangan |
|-------------|-------|---------|------------|
| `action`    | ya    | —       | Nama aksi: `APPROVE`, `REJECT`, `EXPORT`, `LOGIN`, dll. |
| `module`    | tidak | `""`    | Modul pemilik operasi |
| `description`| tidak | `""`   | Deskripsi dengan SpEL: `#{#paramName}`, `#{#result.field}` |
| `alwaysLog` | tidak | `false` | Sama seperti di `@AuditableEntity` |

---

## Nested Call Protection

`AuditContext` (ThreadLocal) memastikan tidak ada **duplicate log** saat satu service memanggil service lain yang juga di-audit.

**Contoh:**
```
EmployeeService.create()        ← dicatat (root)
  └── UserService.create()      ← DILEWATI (nested, alwaysLog = false)
  └── RoleService.assign()      ← DILEWATI (nested, alwaysLog = false)
```

Jika entity sensitif harus selalu dicatat (misal `BankAccount`), gunakan `alwaysLog = true`.

---

## Model AuditLog

| Field          | Tipe       | Keterangan |
|---------------|------------|------------|
| `traceId`     | `String`   | MDC trace ID dari request |
| `actorId`     | `String`   | Username pelaku aksi |
| `actorIp`     | `String`   | IP address pelaku |
| `action`      | `String`   | `CREATE`, `UPDATE`, `DELETE`, `APPROVE`, dll. |
| `module`      | `String`   | Nama plugin/module |
| `resourceType`| `String`   | Nama entity, misal `Employee` |
| `resourceId`  | `String`   | ID entity yang dikenai aksi |
| `changedFields`| `String[]`| (opsional) Field yang berubah |
| `description` | `String`   | Deskripsi human-readable |
| `status`      | `String`   | `SUCCESS` atau `FAILED` |
| `createdAt`   | `Instant`  | Waktu log dibuat |

---

## REST API

| Method | Endpoint                         | Deskripsi                    |
|--------|----------------------------------|------------------------------|
| GET    | `/api/v1/audit-logs/{id}`        | Detail satu audit log        |
| POST   | `/api/v1/audit-logs/search`      | Cari satu audit log          |
| POST   | `/api/v1/audit-logs/search/list` | List audit log               |
| POST   | `/api/v1/audit-logs/search/page` | Pagination audit log         |

> ID pada endpoint menggunakan **Sqids** (encoded), bukan raw DB ID.

---

## Extension Point: `AuditLogExtension`

Plugin lain dapat memperkaya deskripsi audit log dengan mengimplementasi `AuditLogExtension` dari `core-api`:

```java
@Extension
public class PayrollAuditEnricher implements AuditLogExtension {

    @Override
    public String supportedModule() {
        return "gps-payroll";
    }

    @Override
    public String resolveDescription(String action, String resourceType, String resourceId) {
        if ("PayrollRun".equals(resourceType) && "CREATE".equals(action)) {
            return "Payroll run #" + resourceId + " diproses";
        }
        return null; // null = pakai default description
    }
}
```

Jika enricher mengembalikan `null`, deskripsi default yang dipakai: `"ACTION ResourceType#id"`.

---

## Struktur Package

```
audit-plugin/src/main/java/gasi/gps/audit/
├── AuditPlugin.java                          # PF4J plugin entry point
├── AuditAppExtension.java                    # Metadata plugin
├── AuditFlywayExtension.java                 # Registrasi Flyway migration
├── AuditContext.java                         # ThreadLocal untuk nested call control
├── application/
│   ├── dto/
│   │   ├── AuditLogSummaryResponse.java
│   │   └── AuditLogDetailResponse.java
│   ├── mapper/
│   │   └── AuditLogDtoMapper.java
│   └── service/
│       └── AuditLogServiceImpl.java
├── domain/
│   ├── model/
│   │   └── AuditLog.java
│   └── port/
│       ├── inbound/
│       │   └── AuditLogService.java
│       └── outbound/
│           └── AuditLogRepositoryPort.java
├── infrastructure/
│   ├── adapter/
│   │   └── AuditLogRepositoryAdapter.java
│   ├── aspect/
│   │   ├── AuditEntityAspect.java            # Intercept @AuditableEntity (CUD)
│   │   └── AuditMethodAspect.java            # Intercept @Auditable (non-CRUD)
│   ├── entity/
│   │   └── AuditLogEntity.java
│   ├── mapper/
│   │   └── AuditLogMapper.java
│   └── persistence/
│       └── AuditLogEntityRepository.java
└── presentation/
    └── controller/
        └── AuditLogController.java
```

---

## `AuditLogSpi` — Menulis Log Secara Manual

Untuk kasus di luar jangkauan AOP (misal: proses batch, job scheduler, atau logika kondisional), plugin dapat inject `AuditLogSpi` dan menulis log secara manual.

```java
@Service
public class PayrollBatchService {

    private final AuditLogSpi auditLogSpi;

    public PayrollBatchService(AuditLogSpi auditLogSpi) {
        this.auditLogSpi = auditLogSpi;
    }

    public void runPayroll(Long periodId) {
        // ... proses payroll ...

        auditLogSpi.log(
            "RUN",
            "gps-payroll",
            "PayrollRun",
            periodId.toString(),
            "Payroll run periode #" + periodId + " selesai diproses"
        );
    }
}
```

**Method tersedia:**

| Method | Parameter | Keterangan |
|--------|-----------|------------|
| `log(action, module, entityType, entityId, description)` | 5 param | Menulis event audit dari module pemilik |

> `AuditLogSpi` diimplementasikan oleh `audit-plugin` dan diekspos sebagai Spring Bean. Penulisan log memakai transaksi `REQUIRES_NEW`, sehingga log kegagalan tetap dapat tersimpan ketika transaksi bisnis rollback.

---

## Contracts di `core-api`

Semua kontrak audit didefinisikan di `core-api` (`gasi.gps.core.api.audit`) agar bisa dipakai plugin manapun tanpa depend ke `audit-plugin`:

| Kontrak             | Jenis                        | Keterangan |
|--------------------|------------------------------|------------|
| `@AuditableEntity` | Annotation (class level)     | Audit otomatis CUD via AOP |
| `@Auditable`       | Annotation (method level)    | Audit manual non-CRUD via AOP + SpEL |
| `AuditLogExtension`| Interface (PF4J ExtensionPoint) | Enricher deskripsi log dari plugin lain |
| `AuditLogSpi`      | Interface (Spring Bean)      | Menulis log secara manual dari kode plugin |
