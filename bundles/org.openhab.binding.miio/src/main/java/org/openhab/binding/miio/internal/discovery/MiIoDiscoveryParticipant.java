/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.miio.internal.discovery;

import static org.openhab.binding.miio.internal.MiIoBindingConstants.*;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.miio.internal.MiIoDevices;
import org.openhab.binding.miio.internal.cloud.CloudConnector;
import org.openhab.binding.miio.internal.cloud.CloudDeviceDTO;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovers Mi IO devices announced by mDNS
 *
 * @author Marcel Verpaalen - Initial contribution
 *
 */
@NonNullByDefault
@Component(service = MDNSDiscoveryParticipant.class)
public class MiIoDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private final CloudConnector cloudConnector;
    private Logger logger = LoggerFactory.getLogger(MiIoDiscoveryParticipant.class);

    @Activate
    public MiIoDiscoveryParticipant(@Reference CloudConnector cloudConnector) {
        this.cloudConnector = cloudConnector;
        logger.debug("Start Xiaomi Mi IO mDNS discovery");
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return (NONGENERIC_THING_TYPES_UIDS);
    }

    @Override
    public String getServiceType() {
        return "_miio._udp.local.";
    }

    @Override
    public @Nullable ThingUID getThingUID(@Nullable ServiceInfo service) {
        if (service == null) {
            return null;
        }
        logger.trace("ServiceInfo: {}", service);
        String id[] = service.getName().split("_miio");
        if (id.length != 2) {
            logger.trace("mDNS Could not identify Type / Device Id from '{}'", service.getName());
            return null;
        }
        long did;
        try {
            did = Long.parseUnsignedLong(id[1]);
        } catch (Exception e) {
            logger.trace("mDNS Could not identify Device ID from '{}'", id[1]);
            return null;
        }
        ThingTypeUID thingType = MiIoDevices.getType(id[0].replaceAll("-", ".")).getThingType();
        String uidName = String.format("%08X", did);
        logger.debug("mDNS {} identified as thingtype {} with did {} ({})", id[0], thingType, uidName, did);
        return new ThingUID(thingType, uidName);
    }

    private @Nullable InetAddress getIpAddress(ServiceInfo service) {
        InetAddress address = null;
        for (InetAddress addr : service.getInet4Addresses()) {
            return addr;
        }
        // Fallback for Inet6addresses
        for (InetAddress addr : service.getInet6Addresses()) {
            return addr;
        }
        return address;
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo service) {
        DiscoveryResult result = null;
        ThingUID uid = getThingUID(service);
        if (uid != null) {
            Map<String, Object> properties = new HashMap<>(2);
            // remove the domain from the name
            InetAddress ip = getIpAddress(service);
            if (ip == null) {
                logger.debug("Mi IO mDNS Discovery could not determine ip address from service info: {}", service);
                return null;
            }
            String inetAddress = ip.toString().substring(1); // trim leading slash
            String id = uid.getId();
            String label = "Xiaomi Mi Device " + id + " (" + Long.parseUnsignedLong(id, 16) + ") " + service.getName();
            if (cloudConnector.isConnected()) {
                cloudConnector.getDevicesList();
                CloudDeviceDTO cloudInfo = cloudConnector.getDeviceInfo(id);
                if (cloudInfo != null) {
                    logger.debug("Cloud Info: {}", cloudInfo);
                    properties.put(PROPERTY_TOKEN, cloudInfo.getToken());
                    label = label + " with token";
                    String country = cloudInfo.getServer();
                    if (!country.isEmpty() && cloudInfo.getIsOnline()) {
                        properties.put(PROPERTY_CLOUDSERVER, country);
                    }
                }
            }
            properties.put(PROPERTY_HOST_IP, inetAddress);
            properties.put(PROPERTY_DID, id);
            result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                    .withRepresentationProperty(PROPERTY_DID).withLabel(label).build();
            logger.debug("Mi IO mDNS Discovery found {} with address '{}:{}' name '{}'", uid, inetAddress,
                    service.getPort(), label);
        }
        return result;
    }
}
