/*
 * Copyright (c) 2003, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.sun.corba.se.spi.monitoring;

import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.monitoring.MonitoredObject;
import java.util.*;

/**
 * <p>
 * Monitoring Manager will have a 1 to 1 association with the ORB. This
 * gives access to the top level Monitored Object, using which more
 * Monitored Objects and Attributes can be added and traversed.
 * </p>
 * <p>
 *
 * @author Hemanth Puttaswamy
 * </p>
 */
public interface MonitoringManager {

  ///////////////////////////////////////
  // operations

/**
 * <p>
 * Gets the Root Monitored Object which contains a Hierarchy Of Monitored
 * Objects exposing various Monitorable Attributes of Various modules.
 * </p>
 * <p>
 *
 * @param MonitoredObject ...
 * </p>
 */
    public MonitoredObject getRootMonitoredObject();
/**
 * <p>
 * Initialize is called whenever there is a start monitoring call to CORBA
 * MBean. This will result in triaging initialize to all the
 * MonitoredObjects and it's Monitored Attributes.
 * </p>
 *
 */
    public void clearState();

} // end MonitoringManager
