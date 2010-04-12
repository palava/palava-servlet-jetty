/**
 * palava - a java-php-bridge
 * Copyright (C) 2007-2010  CosmoCode GmbH
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */

package de.cosmocode.palava.servlet.jetty;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.inject.servlet.GuiceFilter;
import de.cosmocode.palava.core.lifecycle.Disposable;
import de.cosmocode.palava.core.lifecycle.Initializable;
import de.cosmocode.palava.core.lifecycle.LifecycleException;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import java.util.EnumSet;


/**
 * @author Tobias Sarnowski
 */
final class Jetty implements Initializable, Disposable {
    private static final Logger LOG = LoggerFactory.getLogger(Jetty.class);

    private Server jetty;

    private String host;
    private int port;

    @Inject
    public Jetty(
            @Named(JettyConfig.HOST) String host,
            @Named(JettyConfig.PORT) int port
    ) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void initialize() throws LifecycleException {
        jetty = new Server();

        // configure listener
        Connector connector = new SelectChannelConnector();
        connector.setHost(host);
        connector.setPort(port);
        jetty.addConnector(connector);

        // configure guice filter
        ServletContextHandler root = new ServletContextHandler(jetty, "/", ServletContextHandler.SESSIONS);

        root.addFilter(GuiceFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
        root.addServlet(DefaultServlet.class, "/");

        try {
            jetty.start();
        } catch (Exception e) {
            throw new LifecycleException(e);
        }
    }

    @Override
    public void dispose() throws LifecycleException {
        try {
            jetty.stop();
        } catch (Exception e) {
            throw new LifecycleException(e);
        }
    }
}

