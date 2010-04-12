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

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Singleton;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tobias Sarnowski
 */
public class JettyModule implements Module {
    private static final Logger LOG = LoggerFactory.getLogger(JettyModule.class);

    @Override
    public void configure(Binder binder) {
        binder.bind(Jetty.class).asEagerSingleton();
        binder.bind(Server.class).toProvider(Jetty.class).in(Singleton.class);
    }
}
