/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.adaptive.media.image.internal.scaler;

import com.liferay.adaptive.media.image.scaler.AMImageScaler;
import com.liferay.adaptive.media.image.scaler.AMImageScalerTracker;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * @author Sergio González
 */
@Component(service = AMImageScalerTracker.class)
public class AMImageScalerTrackerImpl implements AMImageScalerTracker {

	@Activate
	public void activate(BundleContext bundleContext) {
		_serviceTrackerMap = ServiceTrackerMapFactory.openSingleValueMap(
			bundleContext, AMImageScaler.class, "mime.type");
	}

	@Deactivate
	public void deactivate() {
		_serviceTrackerMap.close();
	}

	public AMImageScaler getAMImageScaler(String mimeType) {
		AMImageScaler amImageScaler = _serviceTrackerMap.getService(mimeType);

		if (amImageScaler == null) {
			amImageScaler = _serviceTrackerMap.getService("*");
		}

		if (amImageScaler == null) {
			if (_log.isWarnEnabled()) {
				_log.warn("Unable to find default image scaler");
			}
		}

		return amImageScaler;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		AMImageScalerTrackerImpl.class);

	private ServiceTrackerMap<String, AMImageScaler> _serviceTrackerMap;

}