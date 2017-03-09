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

package com.liferay.adaptive.media.image.internal.test;

import com.liferay.adaptive.media.ImageAdaptiveMediaConfigurationException.InvalidStateImageAdaptiveMediaConfigurationEntryException;
import com.liferay.adaptive.media.image.configuration.ImageAdaptiveMediaConfigurationEntry;
import com.liferay.adaptive.media.image.configuration.ImageAdaptiveMediaConfigurationHelper;
import com.liferay.adaptive.media.image.internal.test.util.DestinationReplacer;
import com.liferay.adaptive.media.image.service.AdaptiveMediaImageLocalServiceUtil;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.document.library.kernel.service.DLAppLocalServiceUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.FileVersion;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.rule.Sync;
import com.liferay.portal.kernel.test.rule.SynchronousDestinationTestRule;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Sergio González
 */
@RunWith(Arquillian.class)
@Sync
public class ImageAdaptiveMediaDeleteConfigurationTest
	extends ImageAdaptiveMediaConfigurationBaseTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			SynchronousDestinationTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		super.setUp();

		_group = GroupTestUtil.addGroup();
	}

	@Test
	public void testDeleteAllConfigurationEntries() throws Exception {
		ImageAdaptiveMediaConfigurationHelper configurationHelper =
			serviceTracker.getService();

		Map<String, String> properties = new HashMap<>();

		properties.put("max-height", "100");
		properties.put("max-width", "100");

		configurationHelper.addImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "one", "1", properties);

		properties = new HashMap<>();

		properties.put("max-height", "200");
		properties.put("max-width", "200");

		configurationHelper.addImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "two", "2", properties);

		Optional<ImageAdaptiveMediaConfigurationEntry>
			firstConfigurationEntryOptional =
				configurationHelper.getImageAdaptiveMediaConfigurationEntry(
					TestPropsValues.getCompanyId(), "1");

		assertEnabled(firstConfigurationEntryOptional);

		Optional<ImageAdaptiveMediaConfigurationEntry>
			secondConfigurationEntryOptional =
				configurationHelper.getImageAdaptiveMediaConfigurationEntry(
					TestPropsValues.getCompanyId(), "2");

		assertEnabled(secondConfigurationEntryOptional);

		configurationHelper.disableImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "1");

		configurationHelper.deleteImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "1");

		configurationHelper.disableImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "2");

		configurationHelper.deleteImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "2");

		firstConfigurationEntryOptional =
			configurationHelper.getImageAdaptiveMediaConfigurationEntry(
				TestPropsValues.getCompanyId(), "1");

		Assert.assertFalse(firstConfigurationEntryOptional.isPresent());

		secondConfigurationEntryOptional =
			configurationHelper.getImageAdaptiveMediaConfigurationEntry(
				TestPropsValues.getCompanyId(), "2");

		Assert.assertFalse(secondConfigurationEntryOptional.isPresent());
	}

	@Test
	public void testDeleteConfigurationEntryWithExistingDisabledConfiguration()
		throws Exception {

		ImageAdaptiveMediaConfigurationHelper configurationHelper =
			serviceTracker.getService();

		Map<String, String> properties = new HashMap<>();

		properties.put("max-height", "100");
		properties.put("max-width", "100");

		configurationHelper.addImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "one", "1", properties);

		configurationHelper.disableImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "1");

		properties = new HashMap<>();

		properties.put("max-height", "200");
		properties.put("max-width", "200");

		configurationHelper.addImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "two", "2", properties);

		configurationHelper.disableImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "2");

		configurationHelper.deleteImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "2");

		Optional<ImageAdaptiveMediaConfigurationEntry>
			configurationEntryOptional =
				configurationHelper.getImageAdaptiveMediaConfigurationEntry(
					TestPropsValues.getCompanyId(), "1");

		assertDisabled(configurationEntryOptional);

		configurationEntryOptional =
			configurationHelper.getImageAdaptiveMediaConfigurationEntry(
				TestPropsValues.getCompanyId(), "2");

		Assert.assertFalse(configurationEntryOptional.isPresent());
	}

	@Test
	public void testDeleteDeletedConfigurationEntry() throws Exception {
		ImageAdaptiveMediaConfigurationHelper configurationHelper =
			serviceTracker.getService();

		Map<String, String> properties = new HashMap<>();

		properties.put("max-height", "100");
		properties.put("max-width", "100");

		configurationHelper.addImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "one", "1", properties);

		Optional<ImageAdaptiveMediaConfigurationEntry>
			configurationEntryOptional =
				configurationHelper.getImageAdaptiveMediaConfigurationEntry(
					TestPropsValues.getCompanyId(), "1");

		assertEnabled(configurationEntryOptional);

		configurationHelper.disableImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "1");

		configurationHelper.deleteImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "1");

		configurationEntryOptional =
			configurationHelper.getImageAdaptiveMediaConfigurationEntry(
				TestPropsValues.getCompanyId(), "1");

		Assert.assertFalse(configurationEntryOptional.isPresent());

		configurationHelper.deleteImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "1");

		configurationEntryOptional =
			configurationHelper.getImageAdaptiveMediaConfigurationEntry(
				TestPropsValues.getCompanyId(), "1");

		Assert.assertFalse(configurationEntryOptional.isPresent());
	}

	@Test(
		expected =
			InvalidStateImageAdaptiveMediaConfigurationEntryException.class
	)
	public void testDeleteEnabledConfigurationEntry() throws Exception {
		ImageAdaptiveMediaConfigurationHelper configurationHelper =
			serviceTracker.getService();

		Map<String, String> properties = new HashMap<>();

		properties.put("max-height", "100");
		properties.put("max-width", "100");

		configurationHelper.addImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "one", "1", properties);

		Optional<ImageAdaptiveMediaConfigurationEntry>
			configurationEntryOptional =
				configurationHelper.getImageAdaptiveMediaConfigurationEntry(
					TestPropsValues.getCompanyId(), "1");

		assertEnabled(configurationEntryOptional);

		configurationHelper.deleteImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "1");
	}

	@Test
	public void testDeleteFirstConfigurationEntry() throws Exception {
		ImageAdaptiveMediaConfigurationHelper configurationHelper =
			serviceTracker.getService();

		Map<String, String> properties = new HashMap<>();

		properties.put("max-height", "100");
		properties.put("max-width", "100");

		configurationHelper.addImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "one", "1", properties);

		properties = new HashMap<>();

		properties.put("max-height", "200");
		properties.put("max-width", "200");

		configurationHelper.addImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "two", "2", properties);

		Optional<ImageAdaptiveMediaConfigurationEntry>
			firstConfigurationEntryOptional =
				configurationHelper.getImageAdaptiveMediaConfigurationEntry(
					TestPropsValues.getCompanyId(), "1");

		assertEnabled(firstConfigurationEntryOptional);

		Optional<ImageAdaptiveMediaConfigurationEntry>
			secondConfigurationEntryOptional =
				configurationHelper.getImageAdaptiveMediaConfigurationEntry(
					TestPropsValues.getCompanyId(), "2");

		assertEnabled(secondConfigurationEntryOptional);

		configurationHelper.disableImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "1");

		configurationHelper.deleteImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "1");

		firstConfigurationEntryOptional =
			configurationHelper.getImageAdaptiveMediaConfigurationEntry(
				TestPropsValues.getCompanyId(), "1");

		Assert.assertFalse(firstConfigurationEntryOptional.isPresent());

		secondConfigurationEntryOptional =
			configurationHelper.getImageAdaptiveMediaConfigurationEntry(
				TestPropsValues.getCompanyId(), "2");

		assertEnabled(secondConfigurationEntryOptional);
	}

	@Test
	public void testDeleteSecondConfigurationEntry() throws Exception {
		ImageAdaptiveMediaConfigurationHelper configurationHelper =
			serviceTracker.getService();

		Map<String, String> properties = new HashMap<>();

		properties.put("max-height", "100");
		properties.put("max-width", "100");

		configurationHelper.addImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "one", "1", properties);

		properties = new HashMap<>();

		properties.put("max-height", "200");
		properties.put("max-width", "200");

		configurationHelper.addImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "two", "2", properties);

		Optional<ImageAdaptiveMediaConfigurationEntry>
			firstConfigurationEntryOptional =
				configurationHelper.getImageAdaptiveMediaConfigurationEntry(
					TestPropsValues.getCompanyId(), "1");

		assertEnabled(firstConfigurationEntryOptional);

		Optional<ImageAdaptiveMediaConfigurationEntry>
			secondConfigurationEntryOptional =
				configurationHelper.getImageAdaptiveMediaConfigurationEntry(
					TestPropsValues.getCompanyId(), "2");

		assertEnabled(secondConfigurationEntryOptional);

		configurationHelper.disableImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "2");

		configurationHelper.deleteImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "2");

		firstConfigurationEntryOptional =
			configurationHelper.getImageAdaptiveMediaConfigurationEntry(
				TestPropsValues.getCompanyId(), "1");

		assertEnabled(firstConfigurationEntryOptional);

		secondConfigurationEntryOptional =
			configurationHelper.getImageAdaptiveMediaConfigurationEntry(
				TestPropsValues.getCompanyId(), "2");

		Assert.assertFalse(secondConfigurationEntryOptional.isPresent());
	}

	@Test
	public void testDeleteUniqueConfigurationEntry() throws Exception {
		ImageAdaptiveMediaConfigurationHelper configurationHelper =
			serviceTracker.getService();

		Map<String, String> properties = new HashMap<>();

		properties.put("max-height", "100");
		properties.put("max-width", "100");

		configurationHelper.addImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "one", "1", properties);

		Optional<ImageAdaptiveMediaConfigurationEntry>
			configurationEntryOptional =
				configurationHelper.getImageAdaptiveMediaConfigurationEntry(
					TestPropsValues.getCompanyId(), "1");

		assertEnabled(configurationEntryOptional);

		configurationHelper.disableImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "1");

		configurationHelper.deleteImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "1");

		configurationEntryOptional =
			configurationHelper.getImageAdaptiveMediaConfigurationEntry(
				TestPropsValues.getCompanyId(), "1");

		Assert.assertFalse(configurationEntryOptional.isPresent());
	}

	@Test
	public void testForceDeleteAllConfigurationEntries() throws Exception {
		ImageAdaptiveMediaConfigurationHelper configurationHelper =
			serviceTracker.getService();

		Map<String, String> properties = new HashMap<>();

		properties.put("max-height", "100");
		properties.put("max-width", "100");

		configurationHelper.addImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "one", "1", properties);

		properties = new HashMap<>();

		properties.put("max-height", "200");
		properties.put("max-width", "200");

		configurationHelper.addImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "two", "2", properties);

		Optional<ImageAdaptiveMediaConfigurationEntry>
			firstConfigurationEntryOptional =
				configurationHelper.getImageAdaptiveMediaConfigurationEntry(
					TestPropsValues.getCompanyId(), "1");

		assertEnabled(firstConfigurationEntryOptional);

		Optional<ImageAdaptiveMediaConfigurationEntry>
			secondConfigurationEntryOptional =
				configurationHelper.getImageAdaptiveMediaConfigurationEntry(
					TestPropsValues.getCompanyId(), "2");

		assertEnabled(secondConfigurationEntryOptional);

		configurationHelper.forceDeleteImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "1");

		configurationHelper.forceDeleteImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "2");

		firstConfigurationEntryOptional =
			configurationHelper.getImageAdaptiveMediaConfigurationEntry(
				TestPropsValues.getCompanyId(), "1");

		Assert.assertFalse(firstConfigurationEntryOptional.isPresent());

		secondConfigurationEntryOptional =
			configurationHelper.getImageAdaptiveMediaConfigurationEntry(
				TestPropsValues.getCompanyId(), "2");

		Assert.assertFalse(secondConfigurationEntryOptional.isPresent());
	}

	@Test
	public void
			testForceDeleteConfigurationEntryWithExistingDisabledConfiguration()
		throws Exception {

		ImageAdaptiveMediaConfigurationHelper configurationHelper =
			serviceTracker.getService();

		Map<String, String> properties = new HashMap<>();

		properties.put("max-height", "100");
		properties.put("max-width", "100");

		configurationHelper.addImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "one", "1", properties);

		configurationHelper.disableImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "1");

		properties = new HashMap<>();

		properties.put("max-height", "200");
		properties.put("max-width", "200");

		configurationHelper.addImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "two", "2", properties);

		configurationHelper.forceDeleteImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "2");

		Optional<ImageAdaptiveMediaConfigurationEntry>
			configurationEntryOptional =
				configurationHelper.getImageAdaptiveMediaConfigurationEntry(
					TestPropsValues.getCompanyId(), "1");

		assertDisabled(configurationEntryOptional);

		configurationEntryOptional =
			configurationHelper.getImageAdaptiveMediaConfigurationEntry(
				TestPropsValues.getCompanyId(), "2");

		Assert.assertFalse(configurationEntryOptional.isPresent());
	}

	@Test
	public void testForceDeleteConfigurationEntryWithImages() throws Exception {
		try (DestinationReplacer destinationReplacer = new DestinationReplacer(
				"liferay/adaptive_media_processor")) {

			ImageAdaptiveMediaConfigurationHelper configurationHelper =
				serviceTracker.getService();

			Map<String, String> properties = new HashMap<>();

			properties.put("max-height", "100");
			properties.put("max-width", "100");

			ImageAdaptiveMediaConfigurationEntry configurationEntry =
				configurationHelper.addImageAdaptiveMediaConfigurationEntry(
					TestPropsValues.getCompanyId(), "one", "1", properties);

			FileEntry fileEntry = _addFileEntry();

			FileVersion fileVersion = fileEntry.getFileVersion();

			Assert.assertNotNull(
				AdaptiveMediaImageLocalServiceUtil.fetchAdaptiveMediaImage(
					configurationEntry.getUUID(),
					fileVersion.getFileVersionId()));

			configurationHelper.forceDeleteImageAdaptiveMediaConfigurationEntry(
				TestPropsValues.getCompanyId(), configurationEntry.getUUID());

			Assert.assertNull(
				AdaptiveMediaImageLocalServiceUtil.fetchAdaptiveMediaImage(
					configurationEntry.getUUID(),
					fileVersion.getFileVersionId()));
		}
	}

	@Test
	public void testForceDeleteDeletedConfigurationEntry() throws Exception {
		ImageAdaptiveMediaConfigurationHelper configurationHelper =
			serviceTracker.getService();

		Map<String, String> properties = new HashMap<>();

		properties.put("max-height", "100");
		properties.put("max-width", "100");

		configurationHelper.addImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "one", "1", properties);

		Optional<ImageAdaptiveMediaConfigurationEntry>
			configurationEntryOptional =
				configurationHelper.getImageAdaptiveMediaConfigurationEntry(
					TestPropsValues.getCompanyId(), "1");

		assertEnabled(configurationEntryOptional);

		configurationHelper.forceDeleteImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "1");

		configurationEntryOptional =
			configurationHelper.getImageAdaptiveMediaConfigurationEntry(
				TestPropsValues.getCompanyId(), "1");

		Assert.assertFalse(configurationEntryOptional.isPresent());

		configurationHelper.forceDeleteImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "1");

		configurationEntryOptional =
			configurationHelper.getImageAdaptiveMediaConfigurationEntry(
				TestPropsValues.getCompanyId(), "1");

		Assert.assertFalse(configurationEntryOptional.isPresent());
	}

	@Test
	public void testForceDeleteEnabledConfigurationEntry() throws Exception {
		ImageAdaptiveMediaConfigurationHelper configurationHelper =
			serviceTracker.getService();

		Map<String, String> properties = new HashMap<>();

		properties.put("max-height", "100");
		properties.put("max-width", "100");

		configurationHelper.addImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "one", "1", properties);

		Optional<ImageAdaptiveMediaConfigurationEntry>
			configurationEntryOptional =
				configurationHelper.getImageAdaptiveMediaConfigurationEntry(
					TestPropsValues.getCompanyId(), "1");

		assertEnabled(configurationEntryOptional);

		configurationHelper.forceDeleteImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "1");

		configurationEntryOptional =
			configurationHelper.getImageAdaptiveMediaConfigurationEntry(
				TestPropsValues.getCompanyId(), "1");

		Assert.assertFalse(configurationEntryOptional.isPresent());
	}

	@Test
	public void testForceDeleteFirstConfigurationEntry() throws Exception {
		ImageAdaptiveMediaConfigurationHelper configurationHelper =
			serviceTracker.getService();

		Map<String, String> properties = new HashMap<>();

		properties.put("max-height", "100");
		properties.put("max-width", "100");

		configurationHelper.addImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "one", "1", properties);

		properties = new HashMap<>();

		properties.put("max-height", "200");
		properties.put("max-width", "200");

		configurationHelper.addImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "two", "2", properties);

		Optional<ImageAdaptiveMediaConfigurationEntry>
			firstConfigurationEntryOptional =
				configurationHelper.getImageAdaptiveMediaConfigurationEntry(
					TestPropsValues.getCompanyId(), "1");

		assertEnabled(firstConfigurationEntryOptional);

		Optional<ImageAdaptiveMediaConfigurationEntry>
			secondConfigurationEntryOptional =
				configurationHelper.getImageAdaptiveMediaConfigurationEntry(
					TestPropsValues.getCompanyId(), "2");

		assertEnabled(secondConfigurationEntryOptional);

		configurationHelper.forceDeleteImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "1");

		firstConfigurationEntryOptional =
			configurationHelper.getImageAdaptiveMediaConfigurationEntry(
				TestPropsValues.getCompanyId(), "1");

		Assert.assertFalse(firstConfigurationEntryOptional.isPresent());

		secondConfigurationEntryOptional =
			configurationHelper.getImageAdaptiveMediaConfigurationEntry(
				TestPropsValues.getCompanyId(), "2");

		assertEnabled(secondConfigurationEntryOptional);
	}

	@Test
	public void testForceDeleteSecondConfigurationEntry() throws Exception {
		ImageAdaptiveMediaConfigurationHelper configurationHelper =
			serviceTracker.getService();

		Map<String, String> properties = new HashMap<>();

		properties.put("max-height", "100");
		properties.put("max-width", "100");

		configurationHelper.addImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "one", "1", properties);

		properties = new HashMap<>();

		properties.put("max-height", "200");
		properties.put("max-width", "200");

		configurationHelper.addImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "two", "2", properties);

		Optional<ImageAdaptiveMediaConfigurationEntry>
			firstConfigurationEntryOptional =
				configurationHelper.getImageAdaptiveMediaConfigurationEntry(
					TestPropsValues.getCompanyId(), "1");

		assertEnabled(firstConfigurationEntryOptional);

		Optional<ImageAdaptiveMediaConfigurationEntry>
			secondConfigurationEntryOptional =
				configurationHelper.getImageAdaptiveMediaConfigurationEntry(
					TestPropsValues.getCompanyId(), "2");

		assertEnabled(secondConfigurationEntryOptional);

		configurationHelper.forceDeleteImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "2");

		firstConfigurationEntryOptional =
			configurationHelper.getImageAdaptiveMediaConfigurationEntry(
				TestPropsValues.getCompanyId(), "1");

		assertEnabled(firstConfigurationEntryOptional);

		secondConfigurationEntryOptional =
			configurationHelper.getImageAdaptiveMediaConfigurationEntry(
				TestPropsValues.getCompanyId(), "2");

		Assert.assertFalse(secondConfigurationEntryOptional.isPresent());
	}

	private FileEntry _addFileEntry() throws Exception {
		return DLAppLocalServiceUtil.addFileEntry(
			TestPropsValues.getUserId(), _group.getGroupId(),
			DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			StringUtil.randomString() + ".jpg", ContentTypes.IMAGE_JPEG,
			FileUtil.getBytes(
				ImageAdaptiveMediaDeleteConfigurationTest.class,
				_PNG_IMAGE_FILE_PATH),
			new ServiceContext());
	}

	private static final String _PNG_IMAGE_FILE_PATH =
		"/com/liferay/adaptive/media/image/internal/test/dependencies/image.jpg";

	@DeleteAfterTestRun
	private Group _group;

}