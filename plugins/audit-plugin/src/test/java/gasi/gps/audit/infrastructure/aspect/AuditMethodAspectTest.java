package gasi.gps.audit.infrastructure.aspect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import gasi.gps.audit.AuditContext;
import gasi.gps.audit.domain.model.AuditLog;
import gasi.gps.audit.domain.port.outbound.AuditLogRepositoryPort;
import gasi.gps.core.api.audit.Auditable;
import gasi.gps.core.api.security.SecurityContextProvider;
import gasi.gps.core.starter.infrastructure.util.IdEncoder;

class AuditMethodAspectTest {

    private AuditLogRepositoryPort repository;
    private AuditMethodAspect aspect;

    @BeforeEach
    void setUp() {
        repository = mock(AuditLogRepositoryPort.class);
        SecurityContextProvider securityContextProvider = mock(SecurityContextProvider.class);
        IdEncoder idEncoder = mock(IdEncoder.class);

        when(securityContextProvider.getCurrentUsername()).thenReturn("tester");
        when(securityContextProvider.getCurrentIp()).thenReturn("127.0.0.1");
        when(repository.save(any(AuditLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        aspect = new AuditMethodAspect(repository, securityContextProvider, idEncoder);
    }

    @AfterEach
    void tearDown() {
        AuditContext.clear();
    }

    @Test
    void resolvesDescriptionAndKeepsContextActive() throws Throwable {
        Method method = TestOperations.class.getDeclaredMethod("approve", Long.class);
        Auditable annotation = method.getAnnotation(Auditable.class);
        ProceedingJoinPoint joinPoint = joinPoint(method, new Object[] { 15L });
        when(joinPoint.proceed()).thenAnswer(invocation -> {
            assertTrue(AuditContext.isActive());
            return "approved";
        });

        aspect.audit(joinPoint, annotation);

        assertFalse(AuditContext.isActive());
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(repository).save(captor.capture());
        AuditLog log = captor.getValue();
        assertEquals("APPROVE", log.getAction());
        assertEquals("workflow", log.getModule());
        assertEquals("Approved request 15: approved", log.getDescription());
        assertEquals("SUCCESS", log.getStatus());
    }

    @Test
    void writesFailureAndRethrowsOriginalException() throws Throwable {
        Method method = TestOperations.class.getDeclaredMethod("approve", Long.class);
        Auditable annotation = method.getAnnotation(Auditable.class);
        ProceedingJoinPoint joinPoint = joinPoint(method, new Object[] { 15L });
        when(joinPoint.proceed()).thenThrow(new IllegalArgumentException("invalid"));

        assertThrows(
                IllegalArgumentException.class,
                () -> aspect.audit(joinPoint, annotation));

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(repository).save(captor.capture());
        assertEquals("FAILED", captor.getValue().getStatus());
        assertEquals("Failed: invalid", captor.getValue().getDescription());
    }

    private ProceedingJoinPoint joinPoint(Method method, Object[] arguments) {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getArgs()).thenReturn(arguments);
        return joinPoint;
    }

    private static class TestOperations {

        @Auditable(
                action = "APPROVE",
                module = "workflow",
                description = "Approved request #{#requestId}: #{#result}")
        String approve(Long requestId) {
            return "approved";
        }
    }
}
