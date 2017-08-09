/*
 * This file is part of epSOS OpenNCP implementation
 * Copyright (C) 2012  SPMS (Serviços Partilhados do Ministério da Saúde - Portugal)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact email: epsos@iuz.pt
 */
package eu.epsos.configmanager.database;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import java.io.File;

/**
 * Hibernate Utility class with a convenient method to get Session Factory object.
 *
 * @author Marcelo Fonseca<code> - marcelo.fonseca@iuz.pt</code>
 */
@Deprecated
public final class HibernateUtil {

    private static final SessionFactory sessionFactory;

    static {
        final String hibernateConfigFile;

        if (!StringUtils.contains(HibernateConfigFile.name, "/")) {
            String epsosPropsPath = System.getenv("EPSOS_PROPS_PATH");
            if (epsosPropsPath == null) {
                throw new ExceptionInInitializerError("EPSOS_PROPS_PATH is not defined in the system environment");
            }
            hibernateConfigFile = epsosPropsPath + HibernateConfigFile.name;
        } else {
            hibernateConfigFile = HibernateConfigFile.name;
        }

        final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure(new File(hibernateConfigFile))
                .build();
        try {
            sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
        } catch (RuntimeException e) {
            StandardServiceRegistryBuilder.destroy(registry);
            throw new ExceptionInInitializerError(e);
        }
    }

    private HibernateUtil() {
        throw new IllegalAccessError("Utility class");
    }

    /**
     * Create a {@link SessionFactory} using the properties and mappings in the OpenNCP configuration file.
     *
     * @return The build {@link SessionFactory}.
     */
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
