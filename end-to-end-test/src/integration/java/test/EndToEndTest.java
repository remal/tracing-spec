/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package test;

import static java.util.Arrays.asList;

import apps.documents.DocumentsApplication;
import apps.schemas.ImmutableSchema;
import apps.schemas.ImmutableSchemaReference;
import apps.schemas.SchemasApplication;
import apps.schemas.SchemasClient;
import apps.users.UsersApplication;
import java.util.ArrayList;
import java.util.List;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.ParentContextApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import shared.SharedConfiguration;

class EndToEndTest {

    @Test
    void test() {
        val schemas = sharedContext.getBean(SchemasClient.class);
        schemas.saveSchema(ImmutableSchema.builder()
            .id("task")
            .addReference(ImmutableSchemaReference.builder()
                .dataType("user")
                .idField("userId")
                .putFieldMapping("fullName", "userFullName")
                .putFieldMapping("email", "userEmail")
                .build()
            )
            .build()
        );
    }

    private static final AnnotationConfigApplicationContext sharedContext = new AnnotationConfigApplicationContext();

    private static final List<ConfigurableApplicationContext> applicationContexts = new ArrayList<>();

    @BeforeAll
    static void startApplications() {
        sharedContext.register(SharedConfiguration.class);
        sharedContext.refresh();
        sharedContext.start();

        asList(
            UsersApplication.class,
            DocumentsApplication.class,
            SchemasApplication.class
        ).forEach(applicationClass -> {
            LogManager.getLogger(applicationClass).info("Starting...");

            val application = new SpringApplication(applicationClass);
            application.addInitializers(new ParentContextApplicationContextInitializer(sharedContext));

            val applicationContext = application.run();
            applicationContexts.add(applicationContext);

            LogManager.getLogger(applicationClass).info("Started");
        });
    }

    @AfterAll
    static void stopApplications() {
        applicationContexts.forEach(EndToEndTest::closeContext);
        closeContext(sharedContext);
    }

    private static void closeContext(ConfigurableApplicationContext context) {
        if (context.isRunning()) {
            context.stop();
        }

        if (context.isActive()) {
            context.close();
        }
    }

}
