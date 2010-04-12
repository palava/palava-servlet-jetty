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
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.google.inject.servlet.GuiceFilter;
import de.cosmocode.palava.core.lifecycle.Disposable;
import de.cosmocode.palava.core.lifecycle.Initializable;
import de.cosmocode.palava.core.lifecycle.LifecycleException;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.xml.XmlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.servlet.DispatcherType;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumSet;


/**
 * @author Tobias Sarnowski
 */
final class Jetty implements Initializable, Disposable, Provider<Server> {
    private static final Logger LOG = LoggerFactory.getLogger(Jetty.class);

    private Server jetty;

    private URL config;

    @Inject(optional = true)
    public void setConfig(@Named(JettyConfig.CONFIG) URL config) {
        this.config = config;
    }

    @Override
    public void initialize() throws LifecycleException {
        if (config == null) {
            try {
                this.config = new File("conf/jetty.xml").toURI().toURL();
            } catch (MalformedURLException e) {
                throw new LifecycleException(e);
            }
        }

        // initialize jetty
        jetty = new Server();

        // configure with jetty.xml
        final XmlConfiguration configuration;
        try {
            configuration = new XmlConfiguration(config);
            configuration.configure(jetty);
        } catch (Exception e) {
            throw new LifecycleException(e);
        }

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

    @Override
    public Server get() {
        return jetty;
    }
}

