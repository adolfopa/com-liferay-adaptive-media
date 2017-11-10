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

package com.liferay.adaptive.media.image.internal.processor;

import com.liferay.adaptive.media.exception.AMRuntimeException;
import com.liferay.adaptive.media.image.configuration.AMImageConfigurationEntry;
import com.liferay.adaptive.media.image.configuration.AMImageConfigurationHelper;
import com.liferay.adaptive.media.image.mime.type.AMImageMimeTypeProvider;
import com.liferay.adaptive.media.image.model.AMImageEntry;
import com.liferay.adaptive.media.image.processor.AMImageProcessor;
import com.liferay.adaptive.media.image.scaler.AMImageScaled;
import com.liferay.adaptive.media.image.scaler.AMImageScaler;
import com.liferay.adaptive.media.image.scaler.AMImageScalerTracker;
import com.liferay.adaptive.media.image.service.AMImageEntryLocalService;
import com.liferay.adaptive.media.processor.AMProcessor;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.io.unsync.UnsyncByteArrayInputStream;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.FileVersion;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Adolfo Pérez
 */
@Component(
	immediate = true,
	property = "model.class.name=com.liferay.portal.kernel.repository.model.FileVersion",
	service = {AMImageProcessor.class, AMProcessor.class}
)
public final class AMImageProcessorImpl implements AMImageProcessor {

	@Override
	public void cleanUp(FileVersion fileVersion) {
		try {
			if (!_amImageMimeTypeProvider.isMimeTypeSupported(
					fileVersion.getMimeType())) {

				return;
			}

			_amImageEntryLocalService.deleteAMImageEntryFileVersion(
				fileVersion);
		}
		catch (PortalException pe) {
			throw new AMRuntimeException.IOException(pe);
		}
	}

	@Override
	public void process(FileVersion fileVersion) {
		if (!_amImageMimeTypeProvider.isMimeTypeSupported(
				fileVersion.getMimeType())) {

			return;
		}

		Iterable<AMImageConfigurationEntry> amImageConfigurationEntries =
			_amImageConfigurationHelper.getAMImageConfigurationEntries(
				fileVersion.getCompanyId());

		amImageConfigurationEntries.forEach(
			amImageConfigurationEntry -> process(
				fileVersion, amImageConfigurationEntry.getUUID()));
	}

	@Override
	public void process(
		FileVersion fileVersion, String configurationEntryUuid) {

		if (!_amImageMimeTypeProvider.isMimeTypeSupported(
				fileVersion.getMimeType())) {

			return;
		}

		Optional<AMImageConfigurationEntry> amImageConfigurationEntryOptional =
			_amImageConfigurationHelper.getAMImageConfigurationEntry(
				fileVersion.getCompanyId(), configurationEntryUuid);

		if (!amImageConfigurationEntryOptional.isPresent()) {
			return;
		}

		AMImageConfigurationEntry amImageConfigurationEntry =
			amImageConfigurationEntryOptional.get();

		AMImageEntry amImageEntry = _amImageEntryLocalService.fetchAMImageEntry(
			amImageConfigurationEntry.getUUID(),
			fileVersion.getFileVersionId());

		try {
			FileEntry fileEntry = fileVersion.getFileEntry();

			if ((amImageEntry != null) && !fileEntry.isCheckedOut()) {
				return;
			}

			if ((amImageEntry != null) && fileEntry.isCheckedOut()) {
				_amImageEntryLocalService.deleteAMImageEntry(
					amImageEntry.getAmImageEntryId());
			}

			AMImageScaler amImageScaler =
				_amImageScalerTracker.getAMImageScaler(
					fileVersion.getMimeType());

			if (amImageScaler == null) {
				return;
			}

			AMImageScaled amImageScaled = amImageScaler.scaleImage(
				fileVersion, amImageConfigurationEntry);

			byte[] bytes = amImageScaled.getBytes();

			_amImageEntryLocalService.addAMImageEntry(
				amImageConfigurationEntry, fileVersion,
				amImageScaled.getHeight(), amImageScaled.getWidth(),
				new UnsyncByteArrayInputStream(bytes), bytes.length);
		}
		catch (PortalException pe) {
			throw new AMRuntimeException.IOException(pe);
		}
	}

	@Reference(unbind = "-")
	public void setAMImageConfigurationHelper(
		AMImageConfigurationHelper amImageConfigurationHelper) {

		_amImageConfigurationHelper = amImageConfigurationHelper;
	}

	@Reference(unbind = "-")
	public void setAMImageEntryLocalService(
		AMImageEntryLocalService amImageEntryLocalService) {

		_amImageEntryLocalService = amImageEntryLocalService;
	}

	@Reference(unbind = "-")
	public void setAMImageMimeTypeProvider(
		AMImageMimeTypeProvider amImageMimeTypeProvider) {

		_amImageMimeTypeProvider = amImageMimeTypeProvider;
	}

	@Reference(unbind = "-")
	public void setAMImageScalerTracker(
		AMImageScalerTracker amImageScalerTracker) {

		_amImageScalerTracker = amImageScalerTracker;
	}

	private AMImageConfigurationHelper _amImageConfigurationHelper;
	private AMImageEntryLocalService _amImageEntryLocalService;
	private AMImageMimeTypeProvider _amImageMimeTypeProvider;
	private AMImageScalerTracker _amImageScalerTracker;

}