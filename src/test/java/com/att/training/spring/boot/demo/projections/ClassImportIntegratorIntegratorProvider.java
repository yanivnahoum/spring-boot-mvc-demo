package com.att.training.spring.boot.demo.projections;

import io.hypersistence.utils.hibernate.type.util.ClassImportIntegrator;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.jpa.boot.spi.IntegratorProvider;

import java.util.List;

/**
 * This class allows you to provide a List of classes to be imported using their simple name.
 * See the property referencing this class at the top of this test class
 * com.att.training.spring.boot.demo.projections.ProjectionTest
 */
public class ClassImportIntegratorIntegratorProvider implements IntegratorProvider {

    @Override
    public List<Integrator> getIntegrators() {
        return List.of(new ClassImportIntegrator(List.of(PostCommentDto.class)));
    }
}
