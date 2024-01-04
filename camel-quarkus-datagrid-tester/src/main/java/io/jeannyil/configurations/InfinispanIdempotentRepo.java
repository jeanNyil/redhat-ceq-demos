package io.jeannyil.configurations;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import io.quarkus.runtime.annotations.RegisterForReflection;

import org.apache.camel.component.infinispan.remote.InfinispanRemoteIdempotentRepository;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.infinispan.client.hotrod.RemoteCacheManager;

@ApplicationScoped
@RegisterForReflection // Lets Quarkus register this class for reflection during the native build
public class InfinispanIdempotentRepo {

    @Inject
    RemoteCacheManager remoteCacheManager;

    @ConfigProperty(name="datagrid.caches.idempotency")
    String idempotencyCache;
    
    @Produces
    @Named("infinispanRepo")
    public InfinispanRemoteIdempotentRepository createInfinispanIdempotentRepo() {
        
        InfinispanRemoteIdempotentRepository repo = new InfinispanRemoteIdempotentRepository(idempotencyCache);
        repo.setCacheContainer(remoteCacheManager);
        return repo;

    }
}
