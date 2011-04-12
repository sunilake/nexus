/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.repository;

import java.util.Collection;
import java.util.HashSet;

public class MutableProxyRepositoryKind
    implements RepositoryKind
{
    private final ProxyRepository repository;

    private RepositoryKind hostedKind;

    private RepositoryKind proxyKind;

    private HashSet<Class<?>> sharedFacets;

    public MutableProxyRepositoryKind( ProxyRepository repository )
    {
        this( repository, null );
    }

    public MutableProxyRepositoryKind( ProxyRepository repository, Collection<Class<?>> sharedFacets )
    {
        this( repository, sharedFacets, null, null );
    }

    public MutableProxyRepositoryKind( ProxyRepository repository, Collection<Class<?>> sharedFacets,
        RepositoryKind hostedKind, RepositoryKind proxyKind )
    {
        this.repository = repository;

        this.hostedKind = hostedKind;

        this.proxyKind = proxyKind;

        this.sharedFacets = new HashSet<Class<?>>();

        if ( sharedFacets != null )
        {
            this.sharedFacets.addAll( sharedFacets );
        }
    }

    public RepositoryKind getProxyKind()
    {
        return proxyKind;
    }

    public void setProxyKind( RepositoryKind proxyKind )
    {
        this.proxyKind = proxyKind;
    }

    public RepositoryKind getHostedKind()
    {
        return hostedKind;
    }

    public void setHostedKind( RepositoryKind hostedKind )
    {
        this.hostedKind = hostedKind;
    }

    private boolean isProxy()
    {
        return repository.getRemoteUrl() != null;
    }

    private RepositoryKind getActualRepositoryKind()
    {
        if ( isProxy() )
        {
            return proxyKind;
        }
        else
        {
            return hostedKind;
        }
    }

    public Class<?> getMainFacet()
    {
        return getActualRepositoryKind().getMainFacet();
    }

    public boolean isFacetAvailable( Class<?> f )
    {
        if ( getActualRepositoryKind().isFacetAvailable( f ) )
        {
            return true;
        }

        for ( Class<?> facet : sharedFacets )
        {
            if ( f.isAssignableFrom( facet ) )
            {
                return true;
            }
        }

        return false;
    }
}
