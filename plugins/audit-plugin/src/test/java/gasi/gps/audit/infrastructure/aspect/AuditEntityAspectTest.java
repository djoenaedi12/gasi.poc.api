package gasi.gps.audit.infrastructure.aspect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.pf4j.PluginManager;

import gasi.gps.audit.AuditContext;
import gasi.gps.audit.domain.model.AuditLog;
import gasi.gps.audit.domain.port.outbound.AuditLogRepositoryPort;
import gasi.gps.core.api.application.dto.BaseDetailResponse;
import gasi.gps.core.api.audit.AuditLogExtension;
import gasi.gps.core.api.audit.AuditableEntity;
import gasi.gps.core.api.security.SecurityContextProvider;
import gasi.gps.core.starter.infrastructure.util.IdEncoder;

class AuditEntityAspectTest {

    private AuditLogRepositoryPort repository;
    private AuditEntityAspect aspect;

    @BeforeEach
    void setUp() {
        repository = mock(AuditLogRepositoryPort.class);
        SecurityContextProvider securityContextProvider = mock(SecurityContextProvider.class);
        PluginManager pluginManager = mock(PluginManager.class);
        IdEncoder idEncoder = mock(IdEncoder.class);

        when(securityContextProvider.getCurrentUsername()).thenReturn("tester");
        when(securityContextProvider.getCurrentIp()).thenReturn("127.0.0.1");
        when(pluginManager.getExtensions(AuditLogExtension.class)).thenReturn(List.of());
        when(idEncoder.decode("encoded-id")).thenReturn(42L);
        when(repository.save(any(AuditLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        aspect = new AuditEntityAspect(
                repository, securityContextProvider, pluginManager, idEncoder);
    }

    @AfterEach
    void tearDown() {
        AuditContext.clear();
    }

    @Test
    void keepsContextActiveDuringCreateAndWritesSuccessLog() throws Throwable {
        ProceedingJoinPoint joinPoint = joinPoint("create");
        TestDetailResponse response = TestDetailResponse.builder()
                .id("encoded-id")
                .name("created")
                .build();
        when(joinPoint.proceed()).thenAnswer(invocation -> {
            assertTrue(AuditContext.isActive());
            return response;
        });

        Object result = aspect.auditOperation(joinPoint, new TestService());

        assertSame(response, result);
        assertFalse(AuditContext.isActive());

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(repository).save(captor.capture());
        AuditLog log = captor.getValue();
        assertEquals("CREATE", log.getAction());
        assertEquals("test", log.getModule());
        assertEquals("TestResource", log.getResourceType());
        assertEquals("42", log.getResourceId());
        assertEquals("SUCCESS", log.getStatus());
        assertEquals("tester", log.getActorId());
    }

    @Test
    void suppressesNestedAuditByDefault() throws Throwable {
        ProceedingJoinPoint joinPoint = joinPoint("create");
        TestDetailResponse response = TestDetailResponse.builder()
                .id("encoded-id")
                .name("nested")
                .build();
        when(joinPoint.proceed()).thenReturn(response);
        AuditContext.start();

        Object result = aspect.auditOperation(joinPoint, new TestService());

        assertSame(response, result);
        assertTrue(AuditContext.isActive());
        verify(repository, never()).save(any(AuditLog.class));
    }

    @Test
    void writesFailureLogAndRethrowsOriginalException() throws Throwable {
        ProceedingJoinPoint joinPoint = joinPoint("create");
        IllegalStateException failure = new IllegalStateException("boom");
        when(joinPoint.proceed()).thenThrow(failure);

        IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> aspect.auditOperation(joinPoint, new TestService()));

        assertSame(failure, thrown);
        assertFalse(AuditContext.isActive());

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(repository).save(captor.capture());
        assertEquals("FAILED", captor.getValue().getStatus());
        assertEquals("Failed: boom", captor.getValue().getDescription());
    }

    private ProceedingJoinPoint joinPoint(String methodName) {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);
        when(signature.getName()).thenReturn(methodName);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getArgs()).thenReturn(new Object[] { new Object() });
        return joinPoint;
    }

    @AuditableEntity(module = "test", resourceType = "TestResource")
    private static class TestService {
    }

    private static class TestDetailResponse extends BaseDetailResponse {

        private String name;

        TestDetailResponse() {
        }

        String getName() {
            return name;
        }

        static TestDetailResponseBuilder builder() {
            return new TestDetailResponseBuilder();
        }

        private static class TestDetailResponseBuilder {

            private String id;
            private String name;

            TestDetailResponseBuilder id(String value) {
                id = value;
                return this;
            }

            TestDetailResponseBuilder name(String value) {
                name = value;
                return this;
            }

            TestDetailResponse build() {
                TestDetailResponse response = new TestDetailResponse();
                response.setId(id);
                response.name = name;
                return response;
            }
        }
    }
}
