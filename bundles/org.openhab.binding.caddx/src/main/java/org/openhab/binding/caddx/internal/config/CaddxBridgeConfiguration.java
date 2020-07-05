/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.caddx.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.caddx.internal.CaddxProtocol;

/**
 * Configuration class for the Caddx RS232 Serial interface bridge.
 *
 * @author Georgios Moutsos - Initial contribution
 */

@NonNullByDefault
public class CaddxBridgeConfiguration {

    // Caddx Bridge Thing constants
    public static final String PROTOCOL = "protocol";
    public static final String SERIAL_PORT = "serialPort";
    public static final String BAUD = "baud";

    private CaddxProtocol protocol = CaddxProtocol.Binary;
    private @NonNullByDefault({}) String serialPort;
    private Integer baudrate = 9600;

    public CaddxProtocol getProtocol() {
        return protocol;
    }

    public void setProtocol(CaddxProtocol protocol) {
        this.protocol = protocol;
    }

    public String getSerialPort() {
        return serialPort;
    }

    public void setSerialPort(String serialPort) {
        this.serialPort = serialPort;
    }

    public Integer getBaudrate() {
        return baudrate;
    }

    public void setBaudrate(Integer baudrate) {
        this.baudrate = baudrate;
    }
}
