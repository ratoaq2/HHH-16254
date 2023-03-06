/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hibernate.bugs;

import static org.assertj.core.api.Assertions.assertThat;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.testing.junit4.BaseCoreFunctionalTestCase;
import org.junit.Test;

/**
 * This template demonstrates how to develop a test case for Hibernate ORM,
 * using its built-in unit test framework. Although ORMStandaloneTestCase is
 * perfectly acceptable as a reproducer, usage of this class is much preferred.
 * Since we nearly always include a regression test with bug fixes, providing
 * your reproducer using this method simplifies the process.
 *
 * What's even better? Fork hibernate-orm itself, add your test case directly to
 * a module's unit tests, then submit it as a PR!
 */
public class ORMUnitTestCase extends BaseCoreFunctionalTestCase {

	// Add your entities here.
	@Override
	protected Class<?>[] getAnnotatedClasses() {
		return new Class<?>[] { ParentEntity.class, ChildEntity.class };
	}

	// Add in any settings that are specific to your test. See
	// resources/hibernate.properties for the defaults.
	@Override
	protected void configure(Configuration configuration) {
		super.configure(configuration);

		configuration.setProperty(AvailableSettings.SHOW_SQL, Boolean.TRUE.toString());
		configuration.setProperty(AvailableSettings.FORMAT_SQL, Boolean.TRUE.toString());
		configuration.setProperty(AvailableSettings.DEFAULT_BATCH_FETCH_SIZE, "10");
	}

	// Add your tests, using standard JUnit.
	@Test
	public void hhh16254Test() throws Exception {
		// BaseCoreFunctionalTestCase automatically creates the SessionFactory and
		// provides the Session.
		ChildEntity child1 = new ChildEntity();
		ChildEntity child2 = new ChildEntity();
		try (Session s = openSession()) {
			Transaction tx = s.beginTransaction();

			child1.parent = child2;

			s.persist(child1);
			s.persist(child2);
			tx.commit();

			// To simulate that all data was already persisted before the application starts
			s.getSessionFactory().getCache().evictAllRegions();
		}

		try (Session s = openSession()) {
			Transaction tx = s.beginTransaction();
			ParentEntity entity = s.byId(ParentEntity.class).load(child1.id);
			assertThat(entity.parent).isInstanceOf(ChildEntity.class);
			tx.commit();
		}
	}
}
